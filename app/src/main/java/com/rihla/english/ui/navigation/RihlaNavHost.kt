package com.rihla.english.ui.navigation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.rihla.english.R
import com.rihla.english.core.model.SessionId
import com.rihla.english.ui.gamification.GamificationViewModel
import com.rihla.english.ui.home.SkillTreeHomeScreen
import com.rihla.english.ui.lesson.LessonArenaScreen
import com.rihla.english.ui.lesson.LessonViewModel
import com.rihla.english.ui.onboarding.OnboardingScreen
import com.rihla.english.ui.progress.ProgressScreen
import com.rihla.english.ui.shop.ShopAndAchievementsScreen
import com.rihla.english.ui.sound.SoundLabScreen
import com.rihla.english.ui.story.StoriesScreen
import com.rihla.english.ui.story.StoryScreen
import com.rihla.english.ui.story.StoryViewModel
import com.rihla.english.ui.theme.NightSkyBrush
import com.rihla.english.ui.theme.Oasis
import com.rihla.english.ui.theme.RihlaTheme

object Routes {
    const val HOME = "home"
    const val STORIES = "stories"
    const val SOUNDS = "sounds"
    const val PROGRESS = "progress"
    const val SHOP = "shop"
    const val LESSON = "lesson/{${LessonViewModel.ARG_NODE_ID}}"
    const val STORY = "story/{${StoryViewModel.ARG_STORY_ID}}"
    fun lesson(sessionId: String) = "lesson/$sessionId"
    fun story(storyId: String) = "story/$storyId"
}

private data class TopLevel(val route: String, val emoji: String, val labelRes: Int)

private val topLevel = listOf(
    TopLevel(Routes.HOME, "🐪", R.string.nav_journey),
    TopLevel(Routes.STORIES, "💬", R.string.nav_stories),
    TopLevel(Routes.SOUNDS, "👂", R.string.nav_sounds),
    TopLevel(Routes.PROGRESS, "📈", R.string.nav_progress),
    TopLevel(Routes.SHOP, "💎", R.string.nav_store),
)

/** Shows onboarding on first launch, then the main app. */
@Composable
fun RihlaApp(gamificationViewModel: GamificationViewModel) {
    val profile by gamificationViewModel.profile.collectAsStateWithLifecycle()
    val current = profile
    when {
        current == null -> Box(Modifier.fillMaxSize().background(NightSkyBrush), contentAlignment = Alignment.Center) {
            Text("🐪", fontSize = 96.sp)
        }
        !current.onboarded -> OnboardingScreen(onFinish = gamificationViewModel::completeOnboarding)
        else -> RihlaNavHost(gamificationViewModel)
    }
}

@Composable
fun RihlaNavHost(
    gamificationViewModel: GamificationViewModel,
    navController: NavHostController = rememberNavController(),
) {
    val backStack by navController.currentBackStackEntryAsState()
    val route = backStack?.destination?.route
    val showBottomBar = route == null || route in topLevel.map { it.route }

    fun openTab(target: String) {
        navController.navigate(target) {
            popUpTo(navController.graph.findStartDestination().id) { saveState = true }
            launchSingleTop = true
            restoreState = true
        }
    }

    Scaffold(
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        containerColor = MaterialTheme.colorScheme.background,
        bottomBar = {
            if (showBottomBar) {
                NavigationBar(containerColor = RihlaTheme.colors.cardBackground) {
                    topLevel.forEach { item ->
                        NavigationBarItem(
                            selected = route == item.route,
                            onClick = { openTab(item.route) },
                            icon = { Text(item.emoji, fontSize = 22.sp) },
                            label = { Text(stringResource(item.labelRes), style = MaterialTheme.typography.labelMedium) },
                            colors = NavigationBarItemDefaults.colors(
                                selectedTextColor = Oasis,
                                indicatorColor = Oasis.copy(alpha = 0.16f),
                            ),
                        )
                    }
                }
            }
        },
    ) { padding ->
        NavHost(
            navController = navController,
            startDestination = Routes.HOME,
            modifier = Modifier.fillMaxSize().padding(padding),
        ) {
            composable(Routes.HOME) {
                SkillTreeHomeScreen(
                    viewModel = gamificationViewModel,
                    onStartLesson = { navController.navigate(Routes.lesson(it)) },
                    onPractice = { navController.navigate(Routes.lesson(SessionId.Practice.raw)) },
                )
            }
            composable(Routes.STORIES) {
                StoriesScreen(gamificationViewModel, onOpenStory = { navController.navigate(Routes.story(it)) })
            }
            composable(Routes.SOUNDS) {
                SoundLabScreen(gamificationViewModel, onStartDrill = { navController.navigate(Routes.lesson(SessionId.Sound(it).raw)) })
            }
            composable(Routes.PROGRESS) {
                ProgressScreen(
                    gamificationViewModel,
                    onPracticePattern = { navController.navigate(Routes.lesson(SessionId.Pattern(it).raw)) },
                    onOpenSoundLab = { openTab(Routes.SOUNDS) },
                )
            }
            composable(Routes.SHOP) {
                ShopAndAchievementsScreen(viewModel = gamificationViewModel)
            }
            composable(
                route = Routes.LESSON,
                arguments = listOf(navArgument(LessonViewModel.ARG_NODE_ID) { type = NavType.StringType }),
            ) {
                val lessonViewModel: LessonViewModel = viewModel(factory = LessonViewModel.Factory)
                LessonArenaScreen(
                    viewModel = lessonViewModel,
                    onExit = { navController.popBackStack() },
                    onOpenPractice = {
                        navController.navigate(Routes.lesson(SessionId.Practice.raw)) { popUpTo(Routes.HOME) }
                    },
                )
            }
            composable(
                route = Routes.STORY,
                arguments = listOf(navArgument(StoryViewModel.ARG_STORY_ID) { type = NavType.StringType }),
            ) {
                val storyViewModel: StoryViewModel = viewModel(factory = StoryViewModel.Factory)
                StoryScreen(storyViewModel, onExit = { navController.popBackStack() })
            }
        }
    }
}

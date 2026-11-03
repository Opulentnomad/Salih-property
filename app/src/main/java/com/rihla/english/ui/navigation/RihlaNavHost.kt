package com.rihla.english.ui.navigation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
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
import com.rihla.english.core.journey.Script
import com.rihla.english.ui.components.CineIcon
import com.rihla.english.ui.components.CineIconView
import com.rihla.english.ui.journey.JourneyHomeScreen
import com.rihla.english.ui.journey.JourneyViewModel
import com.rihla.english.ui.notebook.NotebookScreen
import com.rihla.english.ui.notebook.ReviewScreen
import com.rihla.english.ui.notebook.ReviewViewModel
import com.rihla.english.ui.onboarding.OnboardingScreen
import com.rihla.english.ui.passport.ChapterEndScreen
import com.rihla.english.ui.passport.MissionSheetContent
import com.rihla.english.ui.passport.PassportScreen
import com.rihla.english.ui.scene.SceneScreen
import com.rihla.english.ui.scene.SceneViewModel
import com.rihla.english.ui.theme.Cine

object Routes {
    const val JOURNEY = "journey"
    const val NOTEBOOK = "notebook"
    const val PASSPORT = "passport"
    const val REVIEW = "review"
    const val MISSION = "mission"
    const val SCENE = "scene/{${SceneViewModel.ARG_SCENE_ID}}"
    fun scene(id: String) = "scene/$id"
}

private data class Tab(val route: String, val icon: CineIcon, val label: Int)

private val tabs = listOf(
    Tab(Routes.JOURNEY, CineIcon.Map, R.string.nav_journey),
    Tab(Routes.NOTEBOOK, CineIcon.Book, R.string.nav_notebook),
    Tab(Routes.PASSPORT, CineIcon.Passport, R.string.nav_passport),
)

/** Onboarding on first launch, then the journey. */
@Composable
fun RihlaApp(viewModel: JourneyViewModel) {
    val profile by viewModel.profile.collectAsStateWithLifecycle()
    val current = profile
    when {
        current == null -> Box(Modifier.fillMaxSize().background(Cine.Ink), contentAlignment = Alignment.Center) {
            Text(stringResource(R.string.app_name), style = MaterialTheme.typography.displayMedium)
        }
        !current.onboarded -> OnboardingScreen(onFinish = viewModel::completeOnboarding)
        else -> RihlaNavHost(viewModel)
    }
}

@Composable
fun RihlaNavHost(viewModel: JourneyViewModel, navController: NavHostController = rememberNavController()) {
    val backStack by navController.currentBackStackEntryAsState()
    val route = backStack?.destination?.route
    val showTabs = route == null || route in tabs.map { it.route }

    fun openTab(target: String) = navController.navigate(target) {
        popUpTo(navController.graph.findStartDestination().id) { saveState = true }
        launchSingleTop = true
        restoreState = true
    }

    Scaffold(
        containerColor = Cine.Ink,
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        bottomBar = {
            if (showTabs) {
                NavigationBar(containerColor = Cine.Ink, tonalElevation = 0.dp) {
                    tabs.forEach { tab ->
                        val selected = route == tab.route
                        NavigationBarItem(
                            selected = selected,
                            onClick = { openTab(tab.route) },
                            icon = { CineIconView(tab.icon, if (selected) Cine.Amber else Cine.Muted, Modifier.size(24.dp)) },
                            label = { Text(stringResource(tab.label), style = MaterialTheme.typography.labelMedium) },
                            colors = NavigationBarItemDefaults.colors(
                                selectedTextColor = Cine.Amber,
                                unselectedTextColor = Cine.Muted,
                                indicatorColor = Cine.AmberTint,
                            ),
                        )
                    }
                }
            }
        },
    ) { padding ->
        NavHost(navController, startDestination = Routes.JOURNEY, modifier = Modifier.fillMaxSize().padding(padding)) {
            composable(Routes.JOURNEY) {
                JourneyHomeScreen(
                    viewModel,
                    onOpenScene = { navController.navigate(Routes.scene(it)) },
                    onOpenMission = { navController.navigate(Routes.MISSION) },
                    onOpenReview = { navController.navigate(Routes.REVIEW) },
                )
            }
            composable(Routes.NOTEBOOK) { NotebookScreen(viewModel, onStartReview = { navController.navigate(Routes.REVIEW) }) }
            composable(Routes.PASSPORT) { PassportScreen(viewModel) }
            composable(Routes.REVIEW) {
                val vm: ReviewViewModel = viewModel(factory = ReviewViewModel.Factory)
                ReviewScreen(vm, onExit = { navController.popBackStack() })
            }
            composable(Routes.MISSION) {
                val state by viewModel.state.collectAsStateWithLifecycle()
                val mission = state.activeMission
                if (mission == null) {
                    LaunchedEffect(Unit) { navController.popBackStack() }
                } else {
                    Box(Modifier.fillMaxSize().background(Cine.Panel).statusBarsPadding()) {
                        MissionSheetContent(
                            mission = mission,
                            missionState = state.progress.missions[mission.id],
                            onAccept = { viewModel.acceptMission(mission) },
                            onComplete = { note -> viewModel.completeMission(mission, note); navController.popBackStack() },
                        )
                    }
                }
            }
            composable(Routes.SCENE, arguments = listOf(navArgument(SceneViewModel.ARG_SCENE_ID) { type = NavType.StringType })) {
                val vm: SceneViewModel = viewModel(factory = SceneViewModel.Factory)
                SceneScreen(
                    viewModel = vm,
                    onExit = { navController.popBackStack() },
                    onOpenScene = { next -> navController.navigate(Routes.scene(next)) { popUpTo(Routes.JOURNEY) } },
                    chapterEnd = { onDone ->
                        val sceneState by vm.state.collectAsStateWithLifecycle()
                        val journey by viewModel.state.collectAsStateWithLifecycle()
                        val chapter = sceneState?.chapter ?: Script.chapters.first()
                        ChapterEndScreen(
                            chapter = chapter,
                            missionState = journey.progress.missions[chapter.mission.id],
                            onAccept = { viewModel.acceptMission(chapter.mission) },
                            onDone = onDone,
                        )
                    },
                )
            }
        }
    }
}

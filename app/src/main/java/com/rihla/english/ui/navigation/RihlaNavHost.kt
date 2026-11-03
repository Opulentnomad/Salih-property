package com.rihla.english.ui.navigation

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
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.sp
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
import com.rihla.english.ui.gamification.GamificationViewModel
import com.rihla.english.ui.home.SkillTreeHomeScreen
import com.rihla.english.ui.lesson.LessonArenaScreen
import com.rihla.english.ui.lesson.LessonViewModel
import com.rihla.english.ui.shop.ShopAndAchievementsScreen
import com.rihla.english.ui.theme.Blue
import com.rihla.english.ui.theme.RihlaTheme

object Routes {
    const val HOME = "home"
    const val SHOP = "shop"
    const val LESSON = "lesson/{${LessonViewModel.ARG_NODE_ID}}"
    fun lesson(nodeId: String) = "lesson/$nodeId"
}

private data class TopLevel(val route: String, val emoji: String, val labelRes: Int)

private val topLevel = listOf(
    TopLevel(Routes.HOME, "🏠", R.string.nav_learn),
    TopLevel(Routes.SHOP, "🏆", R.string.nav_shop),
)

@Composable
fun RihlaNavHost(
    gamificationViewModel: GamificationViewModel,
    navController: NavHostController = rememberNavController(),
) {
    val backStack by navController.currentBackStackEntryAsState()
    val route = backStack?.destination?.route
    val showBottomBar = route == null || route in topLevel.map { it.route }

    Scaffold(
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        bottomBar = {
            if (showBottomBar) {
                NavigationBar(containerColor = MaterialTheme.colorScheme.background) {
                    topLevel.forEach { item ->
                        NavigationBarItem(
                            selected = route == item.route,
                            onClick = {
                                navController.navigate(item.route) {
                                    popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            },
                            icon = { Text(item.emoji, fontSize = 24.sp) },
                            label = { Text(stringResource(item.labelRes), style = MaterialTheme.typography.labelMedium) },
                            colors = NavigationBarItemDefaults.colors(
                                selectedTextColor = Blue,
                                indicatorColor = RihlaTheme.colors.selectedBackground,
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
                    onPractice = { navController.navigate(Routes.lesson(LessonViewModel.PRACTICE_ID)) },
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
                        navController.navigate(Routes.lesson(LessonViewModel.PRACTICE_ID)) {
                            popUpTo(Routes.HOME)
                        }
                    },
                )
            }
        }
    }
}

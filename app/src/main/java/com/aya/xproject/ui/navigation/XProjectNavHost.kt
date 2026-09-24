package com.aya.xproject.ui.navigation

import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.aya.xproject.ui.common.ComingSoonScreen
import com.aya.xproject.ui.favorite.FavoriteScreen
import com.aya.xproject.ui.maps.MapsScreen
import com.aya.xproject.ui.option.OptionScreen

@Composable
fun XProjectNavHost(
    navController: NavHostController,
    modifier: Modifier = Modifier
) {
    NavHost(
        navController = navController,
        startDestination = BottomNavItem.Home.route,
        modifier = modifier,
        // Tanpa animasi transisi antar tab — perpindahan instan
        enterTransition = { EnterTransition.None },
        exitTransition = { ExitTransition.None },
        popEnterTransition = { EnterTransition.None },
        popExitTransition = { ExitTransition.None }
    ) {
        composable(BottomNavItem.Home.route) {
            MapsScreen()
        }
        composable(BottomNavItem.Favorite.route) {
            FavoriteScreen(
                onNavigateToHome = {
                    navController.navigate(BottomNavItem.Home.route) {
                        popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                        launchSingleTop = true
                        restoreState = true
                    }
                }
            )
        }
        composable(BottomNavItem.Jitter.route) {
            ComingSoonScreen(title = "JIT")
        }
        composable(BottomNavItem.Option.route) {
            OptionScreen()
        }
        composable(BottomNavItem.Setting.route) {
            ComingSoonScreen(title = "SET")
        }
    }
}

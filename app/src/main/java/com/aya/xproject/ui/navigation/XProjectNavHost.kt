package com.aya.xproject.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.aya.xproject.ui.common.ComingSoonScreen
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
        modifier = modifier
    ) {
        composable(BottomNavItem.Home.route) {
            MapsScreen()
        }
        composable(BottomNavItem.Favorite.route) {
            ComingSoonScreen(title = "FAV")
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

package com.aya.xproject.ui.main

import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.dp
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.aya.xproject.ui.navigation.BottomNavItem
import com.aya.xproject.ui.navigation.XProjectBottomBar
import com.aya.xproject.ui.navigation.XProjectNavHost

@Composable
fun MainScreen(modifier: Modifier = Modifier) {
    val navController = rememberNavController()
    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = backStackEntry?.destination?.route

    Scaffold(
        modifier = modifier,
        // Insets 0 agar peta tetap full-bleed di atas (bar bawah mengatur inset sendiri)
        contentWindowInsets = WindowInsets(0.dp, 0.dp, 0.dp, 0.dp),
        bottomBar = {
            XProjectBottomBar(
                items = BottomNavItem.items,
                currentRoute = currentRoute,
                onItemClick = { item ->
                    navController.navigate(item.route) {
                        // Satu back stack per tab + state tersimpan saat pindah tab
                        popUpTo(navController.graph.findStartDestination().id) {
                            saveState = true
                        }
                        launchSingleTop = true
                        restoreState = true
                    }
                }
            )
        }
    ) { innerPadding ->
        XProjectNavHost(
            navController = navController,
            modifier = Modifier.padding(innerPadding)
        )
    }
}

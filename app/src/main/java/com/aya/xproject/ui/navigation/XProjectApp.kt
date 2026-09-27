package com.example.xproject.ui.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController

@Composable
fun XProjectApp() {
    val navController = rememberNavController()

    Scaffold(
        bottomBar = { XProjectBottomBar(navController = navController) }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = "home", // Langsung masuk ke rute utama
            modifier = Modifier.padding(innerPadding)
        ) {
            composable("home") {
                // HomeScreen()
            }
            composable("fav") {
                // FavoriteScreen()
            }
            composable("jit") {
                // JitterScreen()
            }
            composable("set") {
                // SettingsScreen()
            }
            composable("opt") {
                // OptionScreen()
            }
        }
    }
}

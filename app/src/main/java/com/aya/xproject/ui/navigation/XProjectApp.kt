package com.aya.xproject.ui.navigation

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
            startDestination = "home",
            modifier = Modifier.padding(innerPadding)
        ) {
            composable("home") { }
            composable("fav") { }
            composable("jit") { }
            composable("set") { }
            composable("opt") { }
        }
    }
}

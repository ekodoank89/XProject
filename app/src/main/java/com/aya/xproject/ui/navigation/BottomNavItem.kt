package com.aya.xproject.ui.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Settings
import androidx.compose.ui.graphics.vector.ImageVector

sealed class BottomNavItem(
    val route: String,
    val label: String,
    val icon: ImageVector
) {
    data object Favorite : BottomNavItem("favorite", "FAV", Icons.Filled.Favorite)
    data object Jitter : BottomNavItem("jitter", "JIT", Icons.Filled.Refresh)
    data object Home : BottomNavItem("home", "HOME", Icons.Filled.Home)
    data object Option : BottomNavItem("option", "OPT", Icons.Filled.MoreVert)
    data object Setting : BottomNavItem("setting", "SET", Icons.Filled.Settings)

    companion object {
        val items = listOf(Favorite, Jitter, Home, Setting, Option)
    }
}

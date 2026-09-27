package com.aya.xproject.ui.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Settings
import androidx.compose.ui.graphics.vector.ImageVector

sealed class BottomNavItem(
    val route: String,
    val label: String,
    val icon: ImageVector
) {
    object Fav : BottomNavItem("fav", "FAV", Icons.Default.Favorite)
    object Jit : BottomNavItem("jit", "JIT", Icons.Default.LocationOn)
    object Home : BottomNavItem("home", "HOME", Icons.Default.Home)
    object Set : BottomNavItem("set", "SET", Icons.Default.Settings)
    object Opt : BottomNavItem("opt", "OPT", Icons.Default.List)
}

package com.aya.xproject.ui.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.vector.ImageVector

data class BottomNavItem(
    val route: String,
    val label: String,
    val icon: ImageVector
)

val defaultBottomNavItems = listOf(
    BottomNavItem("fav", "FAV", Icons.Default.Favorite),
    BottomNavItem("jit", "JIT", Icons.Default.LocationOn),
    BottomNavItem("home", "HOME", Icons.Default.Home),
    BottomNavItem("set", "SET", Icons.Default.Settings),
    BottomNavItem("opt", "OPT", Icons.Default.List)
)

@Composable
fun XProjectBottomBar(
    items: List<BottomNavItem> = defaultBottomNavItems,
    currentRoute: String?,
    onItemClick: (String) -> Unit
) {
    NavigationBar {
        items.forEach { item ->
            NavigationBarItem(
                selected = currentRoute == item.route,
                onClick = { onItemClick(item.route) },
                icon = { Icon(item.icon, contentDescription = item.label) },
                label = { Text(item.label) }
            )
        }
    }
}

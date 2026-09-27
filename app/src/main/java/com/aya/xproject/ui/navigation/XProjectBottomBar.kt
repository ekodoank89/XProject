package com.aya.xproject.ui.navigation

import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable

val defaultBottomNavItems = listOf(
    BottomNavItem.Fav,
    BottomNavItem.Jit,
    BottomNavItem.Home,
    BottomNavItem.Set,
    BottomNavItem.Opt
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

package com.safex.app.presentation.components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.vector.ImageVector
import com.safex.app.presentation.navigation.Screen

@Composable
fun BottomNavBar(
    currentRoute: String?,
    onNavigate: (String) -> Unit
) {
    NavigationBar {
        BottomNavItem.entries.forEach { item ->
            NavigationBarItem(
                icon = {
                    Icon(
                        imageVector = item.icon,
                        contentDescription = item.label
                    )
                },
                label = { Text(item.label) },
                selected = currentRoute == item.route,
                onClick = { onNavigate(item.route) }
            )
        }
    }
}

enum class BottomNavItem(
    val route: String,
    val icon: ImageVector,
    val label: String
) {
    HOME(Screen.Home.route, Icons.Default.Home, "Home"),
    CALL_GUARD(Screen.CallGuard.route, Icons.Default.Security, "Call Guard"),
    SCANNER(Screen.UrlScanner.route, Icons.Default.QrCode, "Scanner"),
    HISTORY(Screen.History.route, Icons.Default.History, "History"),
    SETTINGS(Screen.Settings.route, Icons.Default.Settings, "Settings")
}

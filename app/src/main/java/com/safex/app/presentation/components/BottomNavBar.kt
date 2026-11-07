package com.safex.app.presentation.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import com.safex.app.presentation.navigation.Screen
import com.safex.app.presentation.theme.*

@Composable
fun BottomNavBar(
    currentRoute: String?,
    onNavigate: (String) -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = SurfaceWhite,
        border = androidx.compose.foundation.BorderStroke(1.dp, BorderSubtle)
    ) {
        NavigationBar(
            containerColor = SurfaceWhite,
            tonalElevation = 0.dp,
            modifier = Modifier.height(68.dp)
        ) {
            BottomNavItem.entries.forEach { item ->
                val selected = currentRoute == item.route
                val iconTint by animateColorAsState(
                    targetValue = if (selected) ShieldBlue else MutedSlate,
                    label = "nav_tint"
                )

                NavigationBarItem(
                    icon = {
                        Icon(
                            imageVector = item.icon,
                            contentDescription = item.label,
                            tint = iconTint,
                            modifier = Modifier.size(24.dp)
                        )
                    },
                    label = {
                        Text(
                            text = item.label,
                            style = MaterialTheme.typography.labelSmall,
                            color = if (selected) ShieldBlue else MutedSlate
                        )
                    },
                    selected = selected,
                    onClick = { onNavigate(item.route) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = ShieldBlue,
                        unselectedIconColor = MutedSlate,
                        selectedTextColor = ShieldBlue,
                        unselectedTextColor = MutedSlate,
                        indicatorColor = ShieldBlueContainer
                    )
                )
            }
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

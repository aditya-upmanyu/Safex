package com.safex.app.presentation.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.safex.app.presentation.screens.home.HomeScreen
import com.safex.app.presentation.screens.callguard.CallGuardScreen
import com.safex.app.presentation.screens.urlscanner.UrlScannerScreen
import com.safex.app.presentation.screens.devicesecurity.DeviceSecurityScreen
import com.safex.app.presentation.screens.history.HistoryScreen
import com.safex.app.presentation.screens.reportfraud.ReportFraudScreen
import com.safex.app.presentation.screens.onboarding.OnboardingScreen
import com.safex.app.presentation.screens.settings.SettingsScreen

@Composable
fun SAFExNavGraph(
    navController: NavHostController,
    startDestination: String = Screen.Onboarding.route
) {
    NavHost(
        navController = navController,
        startDestination = startDestination
    ) {
        composable(Screen.Onboarding.route) {
            OnboardingScreen(
                onNavigateNext = {
                    navController.navigate(Screen.Home.route)
                }
            )
        }

        composable(Screen.Home.route) {
            HomeScreen(navController = navController)
        }

        composable(Screen.CallGuard.route) {
            CallGuardScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable(Screen.UrlScanner.route) {
            UrlScannerScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable(Screen.DeviceSecurity.route) {
            DeviceSecurityScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable(Screen.History.route) {
            HistoryScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable(Screen.ReportFraud.route) {
            ReportFraudScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable(Screen.Settings.route) {
            SettingsScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }
    }
}

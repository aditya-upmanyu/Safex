package com.safex.app.presentation.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import androidx.navigation.NavType
import com.safex.app.presentation.home.EnhancedHomeScreen
import com.safex.app.presentation.screens.callguard.CallGuardScreen
import com.safex.app.presentation.screens.urlscanner.UrlScannerScreen
import com.safex.app.presentation.screens.devicesecurity.DeviceSecurityScreen
import com.safex.app.presentation.screens.history.HistoryScreen
import com.safex.app.presentation.screens.reportfraud.ReportFraudScreen
import com.safex.app.presentation.screens.onboarding.OnboardingScreen
import com.safex.app.presentation.screens.settings.SettingsScreen
import com.safex.app.presentation.dashboard.DashboardScreen
import com.safex.app.presentation.emergency.EmergencySOSScreen
import com.safex.app.presentation.contactverification.ContactVerificationScreen
import com.safex.app.presentation.voiceanalysis.VoiceAnalysisScreen

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
                    navController.navigate(Screen.Home.route) {
                        popUpTo(Screen.Onboarding.route) { inclusive = true }
                    }
                }
            )
        }

        composable(Screen.Home.route) {
            EnhancedHomeScreen(
                onNavigateToDashboard = { navController.navigate(Screen.Dashboard.route) },
                onNavigateToCallGuard = { navController.navigate(Screen.CallGuard.route) },
                onNavigateToScanner = { navController.navigate(Screen.UrlScanner.route) },
                onNavigateToHistory = { navController.navigate(Screen.History.route) },
                onNavigateToEmergency = { navController.navigate(Screen.EmergencySOS.route) },
                onNavigateToVerification = { navController.navigate(Screen.ContactVerification.route) },
                onNavigateToSettings = { navController.navigate(Screen.Settings.route) },
                onNavigateToDeviceSecurity = { navController.navigate(Screen.DeviceSecurity.route) },
                onNavigateToReportFraud = { navController.navigate(Screen.ReportFraud.route) }
            )
        }

        composable(Screen.Dashboard.route) {
            DashboardScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable(Screen.EmergencySOS.route) {
            EmergencySOSScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable(Screen.ContactVerification.route) {
            ContactVerificationScreen(
                onNavigateBack = { navController.popBackStack() },
                onNavigateToReport = { navController.navigate(Screen.ReportFraud.route) }
            )
        }

        composable(
            route = Screen.VoiceAnalysis.route,
            arguments = listOf(navArgument("callId") { type = NavType.LongType })
        ) { backStackEntry ->
            val callId = backStackEntry.arguments?.getLong("callId") ?: 0L
            VoiceAnalysisScreen(
                callId = callId,
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable(Screen.CallGuard.route) {
            CallGuardScreen(
                onNavigateBack = { navController.popBackStack() },
                onNavigateToVoiceAnalysis = { callId ->
                    navController.navigate(Screen.VoiceAnalysis.createRoute(callId))
                }
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
                onNavigateBack = { navController.popBackStack() },
                onNavigateToVoiceAnalysis = { callId ->
                    navController.navigate(Screen.VoiceAnalysis.createRoute(callId))
                }
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

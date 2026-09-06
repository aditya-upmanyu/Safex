package com.safex.app.presentation.navigation

sealed class Screen(val route: String) {
    data object Onboarding : Screen("onboarding")
    data object Home : Screen("home")
    data object CallGuard : Screen("call_guard")
    data object UrlScanner : Screen("url_scanner")
    data object DeviceSecurity : Screen("device_security")
    data object History : Screen("history")
    data object ReportFraud : Screen("report_fraud")
    data object Settings : Screen("settings")
}

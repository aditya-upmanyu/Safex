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
    data object Dashboard : Screen("dashboard")
    data object EmergencySOS : Screen("emergency_sos")
    data object ContactVerification : Screen("contact_verification")
    data object VoiceAnalysis : Screen("voice_analysis/{callId}") {
        fun createRoute(callId: Long) = "voice_analysis/$callId"
    }
    data object Chatbot : Screen("chatbot")
    data object ChatbotVoice : Screen("chatbot_voice")
    data object AppSentry : Screen("app_sentry")
    data object PrivacyPolicy : Screen("privacy_policy")
    data object SecurityAdvisor : Screen("security_advisor")
    data object CoreDefense : Screen("core_defense")
    data object UpiProtection : Screen("upi_protection")
    data object RemoteAccess : Screen("remote_access")
    data object DigitalArrest : Screen("digital_arrest")
    data object FamilyGuardian : Screen("family_guardian")
}

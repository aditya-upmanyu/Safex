package com.safex.app.domain.security.usecase.checks

import android.content.Context
import android.os.Build
import android.provider.Settings
import com.safex.app.domain.security.model.*
import com.safex.app.domain.security.usecase.BaseSecurityCheck

class WirelessDebuggingCheck : BaseSecurityCheck() {
    override val id = "wireless_debugging"
    override val title = "Wireless Debugging"
    override val category = SecurityCategory.SYSTEM_SECURITY
    override val minApi = 30 // Android 11+
    override val isScoreable = true
    override val weight = 0.7f
    override val expectedSecureValue = false
    
    override suspend fun performCheck(context: Context): SecurityCheckResult {
        val startTime = System.currentTimeMillis()
        
        if (Build.VERSION.SDK_INT < minApi) {
            return buildResult(
                status = SecurityStatus.UNSUPPORTED,
                riskLevel = RiskLevel.NONE,
                detectedValue = null,
                userExplanation = "Wireless Debugging not available on Android ${Build.VERSION.SDK_INT}",
                technicalDetails = "Feature requires Android 11 (API 30) or higher",
                whyItMatters = "This feature doesn't exist on your Android version",
                recommendation = "No action needed",
                remediation = null,
                confidence = Confidence.HIGH,
                detectionMethod = "API level check",
                evidence = listOf(Evidence("API Level", Build.VERSION.SDK_INT.toString())),
                startTime = startTime
            )
        }
        
        val wirelessAdbEnabled = try {
            Settings.Global.getInt(context.contentResolver, "adb_wifi_enabled", 0)
        } catch (e: Exception) {
            -1
        }
        
        return when {
            wirelessAdbEnabled == 1 -> buildResult(
                status = SecurityStatus.WARNING,
                riskLevel = RiskLevel.HIGH,
                detectedValue = "Enabled",
                userExplanation = "Wireless Debugging is currently active",
                technicalDetails = "ADB over WiFi is enabled",
                whyItMatters = "Your device can be accessed wirelessly by computers on the same network",
                recommendation = "Disable immediately unless actively debugging",
                remediation = RemediationAction("Open Developer Options", RemediationAction.ActionType.OPEN_SPECIFIC_SETTINGS,
                    android.provider.Settings.ACTION_APPLICATION_DEVELOPMENT_SETTINGS),
                confidence = Confidence.MEDIUM,
                detectionMethod = "Settings.Global.adb_wifi_enabled",
                evidence = listOf(Evidence("Wireless ADB", "1")),
                startTime = startTime
            )
            
            wirelessAdbEnabled == 0 -> buildResult(
                status = SecurityStatus.SECURE,
                riskLevel = RiskLevel.NONE,
                detectedValue = "Disabled",
                userExplanation = "Wireless Debugging is disabled",
                technicalDetails = "ADB over WiFi is not active",
                whyItMatters = "Protects against wireless unauthorized access",
                recommendation = "Keep disabled",
                remediation = null,
                confidence = Confidence.MEDIUM,
                detectionMethod = "Settings.Global.adb_wifi_enabled",
                evidence = listOf(Evidence("Wireless ADB", "0")),
                startTime = startTime
            )
            
            else -> buildResult(
                status = SecurityStatus.UNKNOWN,
                riskLevel = RiskLevel.NONE,
                detectedValue = null,
                userExplanation = "Cannot read Wireless Debugging setting",
                technicalDetails = "Setting access restricted",
                whyItMatters = "Wireless debugging allows network-based device control",
                recommendation = "Check Developer Options manually",
                remediation = RemediationAction("Open Developer Options", RemediationAction.ActionType.OPEN_SPECIFIC_SETTINGS,
                    android.provider.Settings.ACTION_APPLICATION_DEVELOPMENT_SETTINGS),
                limitation = "Setting access restricted",
                confidence = Confidence.MEDIUM,
                detectionMethod = "Settings.Global query (restricted)",
                evidence = listOf(Evidence("Read Result", "restricted")),
                startTime = startTime
            )
        }
    }
}

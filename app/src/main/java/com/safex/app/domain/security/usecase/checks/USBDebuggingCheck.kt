package com.safex.app.domain.security.usecase.checks

import android.content.Context
import android.provider.Settings
import com.safex.app.domain.security.model.*
import com.safex.app.domain.security.usecase.BaseSecurityCheck

class USBDebuggingCheck : BaseSecurityCheck() {
    override val id = "usb_debugging"
    override val title = "USB Debugging"
    override val category = SecurityCategory.SYSTEM_SECURITY
    override val minApi = 16
    override val isScoreable = true
    override val weight = 0.8f
    override val expectedSecureValue = false
    
    override suspend fun performCheck(context: Context): SecurityCheckResult {
        val startTime = System.currentTimeMillis()
        
        val adbEnabled = try {
            Settings.Global.getInt(context.contentResolver, Settings.Global.ADB_ENABLED, 0)
        } catch (e: Exception) {
            -1
        }
        
        return when {
            adbEnabled == 1 -> buildResult(
                status = SecurityStatus.WARNING,
                riskLevel = RiskLevel.MEDIUM,
                detectedValue = "Enabled",
                userExplanation = "USB Debugging is currently enabled",
                technicalDetails = "ADB (Android Debug Bridge) is active",
                whyItMatters = "USB Debugging allows computers to access and control your device",
                recommendation = "Disable USB Debugging unless actively developing apps",
                remediation = RemediationAction("Open Developer Options", RemediationAction.ActionType.OPEN_SPECIFIC_SETTINGS,
                    android.provider.Settings.ACTION_APPLICATION_DEVELOPMENT_SETTINGS),
                confidence = Confidence.HIGH,
                detectionMethod = "Settings.Global.ADB_ENABLED",
                evidence = listOf(Evidence("ADB Enabled", "1")),
                startTime = startTime
            )
            
            adbEnabled == 0 -> buildResult(
                status = SecurityStatus.SECURE,
                riskLevel = RiskLevel.NONE,
                detectedValue = "Disabled",
                userExplanation = "USB Debugging is disabled",
                technicalDetails = "ADB access is not enabled",
                whyItMatters = "Keeps your device safe from unauthorized computer access",
                recommendation = "Keep disabled unless developing apps",
                remediation = null,
                confidence = Confidence.HIGH,
                detectionMethod = "Settings.Global.ADB_ENABLED",
                evidence = listOf(Evidence("ADB Enabled", "0")),
                startTime = startTime
            )
            
            else -> buildResult(
                status = SecurityStatus.UNKNOWN,
                riskLevel = RiskLevel.NONE,
                detectedValue = null,
                userExplanation = "Cannot read USB Debugging setting",
                technicalDetails = "Android restricts access to this setting",
                whyItMatters = "USB Debugging can allow computer access to your device",
                recommendation = "Check Developer Options manually",
                remediation = RemediationAction("Open Developer Options", RemediationAction.ActionType.OPEN_SPECIFIC_SETTINGS,
                    android.provider.Settings.ACTION_APPLICATION_DEVELOPMENT_SETTINGS),
                limitation = "Setting access restricted by Android",
                confidence = Confidence.HIGH,
                detectionMethod = "Settings.Global query (restricted)",
                evidence = listOf(Evidence("Read Result", "restricted")),
                startTime = startTime
            )
        }
    }
}

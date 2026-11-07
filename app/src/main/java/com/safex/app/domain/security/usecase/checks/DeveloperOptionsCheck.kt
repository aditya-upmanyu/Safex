package com.safex.app.domain.security.usecase.checks

import android.content.Context
import android.provider.Settings
import com.safex.app.domain.security.model.*
import com.safex.app.domain.security.usecase.BaseSecurityCheck

class DeveloperOptionsCheck : BaseSecurityCheck() {
    override val id = "developer_options"
    override val title = "Developer Options"
    override val category = SecurityCategory.SYSTEM_SECURITY
    override val minApi = 16
    override val isScoreable = true
    override val weight = 0.6f
    override val expectedSecureValue = false
    
    override suspend fun performCheck(context: Context): SecurityCheckResult {
        val startTime = System.currentTimeMillis()
        
        val devOptionsEnabled = try {
            Settings.Global.getInt(context.contentResolver, Settings.Global.DEVELOPMENT_SETTINGS_ENABLED, 0)
        } catch (e: Exception) {
            -1
        }
        
        return when {
            devOptionsEnabled == 1 -> buildResult(
                status = SecurityStatus.INFO,
                riskLevel = RiskLevel.LOW,
                detectedValue = "Enabled",
                userExplanation = "Developer Options are enabled",
                technicalDetails = "Development settings menu is accessible",
                whyItMatters = "Developer options provide advanced settings that can affect security",
                recommendation = "Disable if not developing apps",
                remediation = RemediationAction("Open Developer Options", RemediationAction.ActionType.OPEN_SPECIFIC_SETTINGS,
                    android.provider.Settings.ACTION_APPLICATION_DEVELOPMENT_SETTINGS),
                confidence = Confidence.HIGH,
                detectionMethod = "Settings.Global.DEVELOPMENT_SETTINGS_ENABLED",
                evidence = listOf(Evidence("Dev Settings", "1")),
                startTime = startTime
            )
            
            devOptionsEnabled == 0 -> buildResult(
                status = SecurityStatus.SECURE,
                riskLevel = RiskLevel.NONE,
                detectedValue = "Disabled",
                userExplanation = "Developer Options are disabled",
                technicalDetails = "Development settings are not active",
                whyItMatters = "Keeps advanced potentially risky settings hidden",
                recommendation = "Keep disabled unless developing",
                remediation = null,
                confidence = Confidence.HIGH,
                detectionMethod = "Settings.Global.DEVELOPMENT_SETTINGS_ENABLED",
                evidence = listOf(Evidence("Dev Settings", "0")),
                startTime = startTime
            )
            
            else -> buildResult(
                status = SecurityStatus.UNKNOWN,
                riskLevel = RiskLevel.NONE,
                detectedValue = null,
                userExplanation = "Cannot read Developer Options status",
                technicalDetails = "Setting access restricted",
                whyItMatters = "Developer options contain security-affecting settings",
                recommendation = "Check Settings manually",
                remediation = RemediationAction("Open Settings", RemediationAction.ActionType.OPEN_SETTINGS),
                limitation = "Access restricted",
                confidence = Confidence.MEDIUM,
                detectionMethod = "Settings.Global query (restricted)",
                evidence = listOf(Evidence("Read Result", "restricted")),
                startTime = startTime
            )
        }
    }
}

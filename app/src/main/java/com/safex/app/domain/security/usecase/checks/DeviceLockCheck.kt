package com.safex.app.domain.security.usecase.checks

import android.app.KeyguardManager
import android.content.Context
import android.os.Build
import com.safex.app.domain.security.model.*
import com.safex.app.domain.security.usecase.BaseSecurityCheck

/**
 * Checks if device has a secure lock screen (PIN, Pattern, Password, Biometric)
 */
class DeviceLockCheck : BaseSecurityCheck() {
    
    override val id = "device_lock"
    override val title = "Screen Lock"
    override val category = SecurityCategory.DEVICE_PROTECTION
    override val minApi = 16 // KeyguardManager.isKeyguardSecure available
    override val isScoreable = true
    override val weight = 1.0f
    override val expectedSecureValue = true
    
    override suspend fun performCheck(context: Context): SecurityCheckResult {
        val startTime = System.currentTimeMillis()
        val keyguardManager = context.getSystemService(Context.KEYGUARD_SERVICE) as? KeyguardManager
        
        if (keyguardManager == null) {
            return buildResult(
                status = SecurityStatus.ERROR,
                riskLevel = RiskLevel.NONE,
                detectedValue = null,
                userExplanation = "Unable to access lock screen information",
                technicalDetails = "KeyguardManager not available",
                whyItMatters = "Cannot verify if your device is protected",
                recommendation = "Check your lock screen settings manually",
                remediation = RemediationAction(
                    label = "Open Security Settings",
                    actionType = RemediationAction.ActionType.OPEN_SPECIFIC_SETTINGS,
                    actionData = android.provider.Settings.ACTION_SECURITY_SETTINGS
                ),
                confidence = Confidence.LOW,
                detectionMethod = "System service query",
                evidence = listOf(Evidence("KeyguardManager", "null")),
                startTime = startTime
            )
        }
        
        val isDeviceSecure = keyguardManager.isDeviceSecure
        val isKeyguardSecure = keyguardManager.isKeyguardSecure
        
        val evidence = mutableListOf(
            Evidence("isDeviceSecure()", isDeviceSecure.toString()),
            Evidence("isKeyguardSecure()", isKeyguardSecure.toString())
        )
        
        // Both methods should agree for high confidence
        val confidence = if (isDeviceSecure == isKeyguardSecure) Confidence.HIGH else Confidence.MEDIUM
        
        return when {
            isDeviceSecure -> {
                buildResult(
                    status = SecurityStatus.SECURE,
                    riskLevel = RiskLevel.NONE,
                    detectedValue = "Enabled",
                    userExplanation = "Your device has a secure lock screen enabled",
                    technicalDetails = "Device is protected with PIN, Pattern, Password, or Biometric authentication",
                    whyItMatters = "A strong lock screen is the first line of defense against unauthorized access",
                    recommendation = "Keep your lock screen enabled and use a strong credential",
                    remediation = null,
                    confidence = confidence,
                    detectionMethod = "KeyguardManager.isDeviceSecure()",
                    evidence = evidence,
                    startTime = startTime
                )
            }
            
            else -> {
                buildResult(
                    status = SecurityStatus.DANGER,
                    riskLevel = RiskLevel.CRITICAL,
                    detectedValue = "None or Swipe Only",
                    userExplanation = "Your device has no secure lock screen",
                    technicalDetails = "No PIN, Pattern, Password, or Biometric lock detected",
                    whyItMatters = "Anyone who picks up your phone can access all your data, apps, and accounts",
                    recommendation = "Set up a secure lock screen immediately",
                    remediation = RemediationAction(
                        label = "Set Up Lock Screen",
                        actionType = RemediationAction.ActionType.OPEN_SPECIFIC_SETTINGS,
                        actionData = android.provider.Settings.ACTION_SECURITY_SETTINGS
                    ),
                    confidence = confidence,
                    detectionMethod = "KeyguardManager.isDeviceSecure()",
                    evidence = evidence,
                    startTime = startTime
                )
            }
        }
    }
}

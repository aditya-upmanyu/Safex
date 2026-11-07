package com.safex.app.domain.security.usecase.checks

import android.content.Context
import com.safex.app.domain.security.model.*
import com.safex.app.domain.security.usecase.BaseSecurityCheck
import com.safex.app.util.RootCheckUtil

/**
 * Enhanced root detection check using existing RootCheckUtil
 */
class RootDetectionCheck : BaseSecurityCheck() {
    
    override val id = "root_detection"
    override val title = "Root Access"
    override val category = SecurityCategory.SYSTEM_SECURITY
    override val minApi = 14
    override val isScoreable = true
    override val weight = 1.0f
    override val expectedSecureValue = false // No root is secure
    
    override suspend fun performCheck(context: Context): SecurityCheckResult {
        val startTime = System.currentTimeMillis()
        
        val isRooted = RootCheckUtil.isDeviceRooted()
        val status = RootCheckUtil.getRootStatus()
        
        val evidence = listOf(
            Evidence("Root Status", status),
            Evidence("Detection Method", "Multi-signal heuristic")
        )
        
        return when {
            isRooted -> {
                buildResult(
                    status = SecurityStatus.DANGER,
                    riskLevel = RiskLevel.CRITICAL,
                    detectedValue = "Root Detected",
                    userExplanation = "Your device appears to be rooted",
                    technicalDetails = "Root indicators detected: SU binaries, root management packages, or test-keys build",
                    whyItMatters = "Root access bypasses Android security and can be exploited by malicious apps",
                    recommendation = "If you didn't root your device, it may be compromised. Banking and payment apps may not work.",
                    remediation = RemediationAction(
                        label = "Learn About Root",
                        actionType = RemediationAction.ActionType.SHOW_DIALOG
                    ),
                    limitation = "Heuristic detection - can be bypassed by advanced root hiding",
                    confidence = Confidence.MEDIUM,
                    detectionMethod = "SU binary scan + package detection + build tag analysis",
                    evidence = evidence,
                    startTime = startTime
                )
            }
            
            else -> {
                buildResult(
                    status = SecurityStatus.SECURE,
                    riskLevel = RiskLevel.NONE,
                    detectedValue = "No Root Detected",
                    userExplanation = "No root indicators found",
                    technicalDetails = "No SU binaries, root packages, or suspicious build tags detected",
                    whyItMatters = "Your device maintains Android's security boundaries",
                    recommendation = "Keep your device unrooted for maximum security",
                    remediation = null,
                    limitation = "Heuristic detection - hidden root access may not be detected",
                    confidence = Confidence.MEDIUM,
                    detectionMethod = "SU binary scan + package detection + build tag analysis",
                    evidence = evidence,
                    startTime = startTime
                )
            }
        }
    }
}

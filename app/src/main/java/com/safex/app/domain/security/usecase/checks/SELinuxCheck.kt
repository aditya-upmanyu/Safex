package com.safex.app.domain.security.usecase.checks

import android.content.Context
import com.safex.app.domain.security.model.*
import com.safex.app.domain.security.usecase.BaseSecurityCheck
import java.io.File

class SELinuxCheck : BaseSecurityCheck() {
    override val id = "selinux"
    override val title = "SELinux Status"
    override val category = SecurityCategory.SYSTEM_SECURITY
    override val minApi = 18
    override val isScoreable = true
    override val weight = 0.8f
    override val expectedSecureValue = true
    
    override suspend fun performCheck(context: Context): SecurityCheckResult {
        val startTime = System.currentTimeMillis()
        
        val seLinuxStatus = try {
            File("/sys/fs/selinux/enforce").readText().trim()
        } catch (e: Exception) {
            null
        }
        
        return when (seLinuxStatus) {
            "1" -> buildResult(
                status = SecurityStatus.SECURE,
                riskLevel = RiskLevel.NONE,
                detectedValue = "Enforcing",
                userExplanation = "SELinux is actively enforcing security policies",
                technicalDetails = "/sys/fs/selinux/enforce = 1",
                whyItMatters = "SELinux provides mandatory access control for Android system security",
                recommendation = "Keep enforcing mode enabled",
                remediation = null,
                confidence = Confidence.HIGH,
                detectionMethod = "SELinux enforce file read",
                evidence = listOf(Evidence("enforce", "1")),
                startTime = startTime
            )
            
            else -> buildResult(
                status = SecurityStatus.UNKNOWN,
                riskLevel = RiskLevel.NONE,
                detectedValue = null,
                userExplanation = "Cannot read SELinux status",
                technicalDetails = "Access to /sys/fs/selinux/enforce denied",
                whyItMatters = "SELinux is a core Android security feature",
                recommendation = "Modern Android devices have SELinux enforcing by default",
                remediation = null,
                limitation = "File system access restricted",
                confidence = Confidence.HIGH,
                detectionMethod = "SELinux file read (denied)",
                evidence = listOf(Evidence("Access", "denied")),
                startTime = startTime
            )
        }
    }
}

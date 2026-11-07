package com.safex.app.domain.security.usecase

import android.content.Context
import android.os.Build
import com.safex.app.domain.security.model.*

/**
 * Base implementation providing common functionality and error handling
 */
abstract class BaseSecurityCheck : SecurityCheck {
    
    protected abstract suspend fun performCheck(context: Context): SecurityCheckResult
    
    override suspend fun execute(context: Context): SecurityCheckResult {
        val startTime = System.currentTimeMillis()
        
        return try {
            // Check if supported on this Android version
            if (Build.VERSION.SDK_INT < minApi) {
                return createUnsupportedResult(startTime)
            }
            
            performCheck(context)
        } catch (e: SecurityException) {
            createRestrictedResult(e, startTime)
        } catch (e: Exception) {
            createErrorResult(e, startTime)
        }
    }
    
    private fun createUnsupportedResult(startTime: Long): SecurityCheckResult {
        return SecurityCheckResult(
            id = id,
            title = title,
            category = category,
            status = SecurityStatus.UNSUPPORTED,
            riskLevel = RiskLevel.NONE,
            detectedValue = null,
            userExplanation = "This check is not available on Android ${Build.VERSION.SDK_INT} (requires API $minApi+)",
            technicalDetails = "Device API level: ${Build.VERSION.SDK_INT}, Required: $minApi",
            whyItMatters = "This security feature is not available on your Android version",
            recommendation = "No action needed",
            remediation = null,
            limitation = "Not supported on this Android version",
            confidence = Confidence.HIGH,
            detectionMethod = "API level check",
            evidence = listOf(
                Evidence("Device API Level", Build.VERSION.SDK_INT.toString())
            ),
            isSupported = false,
            minApi = minApi,
            lastCheckedAt = System.currentTimeMillis(),
            durationMs = System.currentTimeMillis() - startTime
        )
    }
    
    private fun createRestrictedResult(e: SecurityException, startTime: Long): SecurityCheckResult {
        return SecurityCheckResult(
            id = id,
            title = title,
            category = category,
            status = SecurityStatus.UNKNOWN,
            riskLevel = RiskLevel.NONE,
            detectedValue = null,
            userExplanation = "Android doesn't allow SAFEx to check this setting",
            technicalDetails = "SecurityException: ${e.message}",
            whyItMatters = "This check requires permissions your device restricts",
            recommendation = "Check this setting manually in Android Settings",
            remediation = RemediationAction(
                label = "Open Settings",
                actionType = RemediationAction.ActionType.OPEN_SETTINGS
            ),
            limitation = "Access restricted by Android security policy",
            confidence = Confidence.HIGH,
            detectionMethod = "Permission check (denied)",
            evidence = listOf(
                Evidence("Security Exception", e.message ?: "Access denied")
            ),
            isSupported = true,
            minApi = minApi,
            lastCheckedAt = System.currentTimeMillis(),
            durationMs = System.currentTimeMillis() - startTime
        )
    }
    
    private fun createErrorResult(e: Exception, startTime: Long): SecurityCheckResult {
        return SecurityCheckResult(
            id = id,
            title = title,
            category = category,
            status = SecurityStatus.ERROR,
            riskLevel = RiskLevel.NONE,
            detectedValue = null,
            userExplanation = "An error occurred while running this check",
            technicalDetails = "Exception: ${e.javaClass.simpleName}: ${e.message}",
            whyItMatters = "Unable to determine the security state",
            recommendation = "Try refreshing the scan",
            remediation = null,
            limitation = "Check failed due to unexpected error",
            confidence = Confidence.LOW,
            detectionMethod = "Error handler",
            evidence = listOf(
                Evidence("Error Type", e.javaClass.simpleName),
                Evidence("Error Message", e.message ?: "Unknown")
            ),
            isSupported = true,
            minApi = minApi,
            lastCheckedAt = System.currentTimeMillis(),
            durationMs = System.currentTimeMillis() - startTime
        )
    }
    
    /**
     * Helper to build result more easily
     */
    protected fun buildResult(
        status: SecurityStatus,
        riskLevel: RiskLevel,
        detectedValue: String?,
        userExplanation: String,
        technicalDetails: String,
        whyItMatters: String,
        recommendation: String,
        remediation: RemediationAction?,
        limitation: String? = null,
        confidence: Confidence,
        detectionMethod: String,
        evidence: List<Evidence>,
        startTime: Long
    ): SecurityCheckResult {
        return SecurityCheckResult(
            id = id,
            title = title,
            category = category,
            status = status,
            riskLevel = riskLevel,
            detectedValue = detectedValue,
            userExplanation = userExplanation,
            technicalDetails = technicalDetails,
            whyItMatters = whyItMatters,
            recommendation = recommendation,
            remediation = remediation,
            limitation = limitation,
            confidence = confidence,
            detectionMethod = detectionMethod,
            evidence = evidence,
            isSupported = true,
            minApi = minApi,
            lastCheckedAt = System.currentTimeMillis(),
            durationMs = System.currentTimeMillis() - startTime
        )
    }
}

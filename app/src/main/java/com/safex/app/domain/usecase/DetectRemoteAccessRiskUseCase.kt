package com.safex.app.domain.usecase

import com.safex.app.core.remote.RemoteAccessAppRegistry
import com.safex.app.core.remote.ScreenShareDetector
import com.safex.app.core.security.ThreatStateManager
import com.safex.app.core.security.model.DetectionStatus
import com.safex.app.core.security.model.ThreatEvent
import com.safex.app.core.security.model.ThreatLevel
import com.safex.app.core.security.model.ThreatModule
import com.safex.app.core.telephony.TelephonyCallStateManager
import com.safex.app.core.upi.UpiAppRegistry
import javax.inject.Inject

class DetectRemoteAccessRiskUseCase @Inject constructor(
    private val screenShareDetector: ScreenShareDetector,
    private val callStateManager: TelephonyCallStateManager,
    private val threatStateManager: ThreatStateManager
) {
    data class RemoteAccessRiskResult(
        val level: ThreatLevel,
        val detectionStatus: DetectionStatus,
        val foregroundAppName: String,
        val installedCount: Int,
        val isCallActive: Boolean,
        val isFinancialAppCorrelated: Boolean,
        val screenShareStatus: DetectionStatus,
        val warningMessage: String,
        val warningMessageHindi: String
    )

    fun evaluate(foregroundPackage: String): RemoteAccessRiskResult {
        val status = screenShareDetector.getStatus()
        val isRemoteForegrounded = RemoteAccessAppRegistry.isRemoteAccessApp(foregroundPackage)
        val isCallActive = callStateManager.isCallActive
        val isUpiApp = UpiAppRegistry.isUpiApp(foregroundPackage)

        if (!isRemoteForegrounded && status.installedCount() == 0) {
            return RemoteAccessRiskResult(
                level = ThreatLevel.SAFE,
                detectionStatus = DetectionStatus.NOT_DETECTED,
                foregroundAppName = "",
                installedCount = 0,
                isCallActive = false,
                isFinancialAppCorrelated = false,
                screenShareStatus = DetectionStatus.NOT_VERIFIED,
                warningMessage = "",
                warningMessageHindi = ""
            )
        }

        val appName = if (isRemoteForegrounded)
            RemoteAccessAppRegistry.getDisplayName(foregroundPackage) else ""

        // Risk scoring
        var score = 0
        if (isRemoteForegrounded) score += 40
        if (isCallActive) score += 30
        if (isUpiApp) score += 20 // financial app correlation (limited to UPI for now)

        val level = when {
            score >= 70 -> ThreatLevel.CRITICAL
            score >= 50 -> ThreatLevel.HIGH
            score >= 30 -> ThreatLevel.MODERATE
            else -> ThreatLevel.LOW
        }

        val result = RemoteAccessRiskResult(
            level = level,
            detectionStatus = if (isRemoteForegrounded) DetectionStatus.DETECTED else DetectionStatus.NOT_DETECTED,
            foregroundAppName = appName,
            installedCount = status.installedApps.size,
            isCallActive = isCallActive,
            isFinancialAppCorrelated = isUpiApp,
            screenShareStatus = status.screenShareStatus,
            warningMessage = WARNING_EN.format(appName),
            warningMessageHindi = WARNING_HI.format(appName)
        )

        if (isRemoteForegrounded) {
            threatStateManager.reportEvent(ThreatEvent(
                module = ThreatModule.REMOTE_ACCESS_SENTRY,
                level = level,
                detectionStatus = DetectionStatus.DETECTED,
                title = "Remote access app in foreground",
                description = "$appName is active${if (isCallActive) " during a phone call" else ""}",
                packageName = foregroundPackage,
                requiresImmediateWarning = level >= ThreatLevel.HIGH
            ))
        }

        return result
    }

    companion object {
        const val WARNING_EN = """CRITICAL RISK: A remote-access application (%s) may be exposing your screen.

The person controlling your device could potentially see passwords, OTPs, banking details and private information.

Stop the remote session immediately if you did not authorize it."""

        const val WARNING_HI = """गंभीर खतरा: रिमोट एक्सेस ऐप (%s) आपकी स्क्रीन की जानकारी किसी अन्य व्यक्ति तक पहुँचा सकता है।

आपके OTP, पासवर्ड और बैंकिंग जानकारी उजागर हो सकती है।

यदि आपने इस सेशन की अनुमति नहीं दी है, तो इसे तुरंत बंद करें।"""
    }

    private fun ScreenShareDetector.ScreenShareStatus.installedCount() = installedApps.size
}

private operator fun ThreatLevel.compareTo(other: ThreatLevel): Int =
    this.ordinal.compareTo(other.ordinal)

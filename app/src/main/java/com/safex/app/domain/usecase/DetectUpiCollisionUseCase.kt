package com.safex.app.domain.usecase

import com.safex.app.core.security.ThreatStateManager
import com.safex.app.core.security.model.DetectionStatus
import com.safex.app.core.security.model.ThreatEvent
import com.safex.app.core.security.model.ThreatLevel
import com.safex.app.core.security.model.ThreatModule
import com.safex.app.core.telephony.TelephonyCallStateManager
import com.safex.app.core.upi.UpiAppRegistry
import javax.inject.Inject

/**
 * Evaluates the risk when a UPI app is opened.
 *
 * Severity:
 * LOW      — UPI app opened, no active call
 * ELEVATED — UPI app opened during an active cellular call
 * HIGH     — UPI + call + additional correlated signals
 * CRITICAL — Multiple independently supported high-risk indicators
 *
 * IMPORTANT: An active call while opening UPI is NOT automatically labelled fraud.
 * We warn — the user decides.
 */
class DetectUpiCollisionUseCase @Inject constructor(
    private val callStateManager: TelephonyCallStateManager,
    private val threatStateManager: ThreatStateManager
) {
    data class UpiCollisionResult(
        val level: ThreatLevel,
        val detectionStatus: DetectionStatus,
        val upiAppName: String,
        val isCallActive: Boolean,
        val warningMessage: String,
        val warningMessageHindi: String
    )

    fun evaluate(foregroundPackage: String): UpiCollisionResult {
        if (!UpiAppRegistry.isUpiApp(foregroundPackage)) {
            return UpiCollisionResult(
                level = ThreatLevel.SAFE,
                detectionStatus = DetectionStatus.NOT_DETECTED,
                upiAppName = "",
                isCallActive = false,
                warningMessage = "",
                warningMessageHindi = ""
            )
        }

        val appName = UpiAppRegistry.getDisplayName(foregroundPackage)
        val callState = callStateManager.callState.value
        val isCallActive = callState == TelephonyCallStateManager.CallState.OFF_HOOK ||
                callState == TelephonyCallStateManager.CallState.RINGING

        val level = when {
            isCallActive -> ThreatLevel.HIGH
            else -> ThreatLevel.LOW
        }

        val result = UpiCollisionResult(
            level = level,
            detectionStatus = DetectionStatus.DETECTED,
            upiAppName = appName,
            isCallActive = isCallActive,
            warningMessage = if (isCallActive) WARNING_EN else INFO_EN,
            warningMessageHindi = if (isCallActive) WARNING_HI else INFO_HI
        )

        if (isCallActive) {
            threatStateManager.reportEvent(
                ThreatEvent(
                    module = ThreatModule.UPI_SHIELD,
                    level = level,
                    detectionStatus = DetectionStatus.DETECTED,
                    title = "UPI app opened during active call",
                    description = "$appName opened while a phone call is active",
                    packageName = foregroundPackage,
                    requiresImmediateWarning = true
                )
            )
        }

        return result
    }

    companion object {
        const val WARNING_EN = """STOP! You may be under financial fraud pressure.

Entering your UPI PIN sends money from your account. You NEVER need a UPI PIN to receive money.

Stop the call and independently verify the request."""

        const val WARNING_HI = """रुकिए! आप वित्तीय धोखाधड़ी के शिकार हो सकते हैं।

UPI PIN डालने से आपके खाते से पैसे कटते हैं। पैसे प्राप्त करने के लिए कभी भी UPI PIN की आवश्यकता नहीं होती।

कॉल रोकें और अनुरोध की स्वतंत्र रूप से पुष्टि करें।"""

        const val INFO_EN = "You opened a UPI payment app. Stay alert for unsolicited payment requests."
        const val INFO_HI = "आपने UPI पेमेंट ऐप खोला है। अनचाहे भुगतान अनुरोधों के प्रति सतर्क रहें।"
    }
}

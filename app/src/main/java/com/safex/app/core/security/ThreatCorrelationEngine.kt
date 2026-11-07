package com.safex.app.core.security

import com.safex.app.core.security.model.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Correlates signals across all four modules to detect compound threats.
 *
 * Example scenario:
 *   Active call DETECTED + UPI app DETECTED + Remote access DETECTED
 *   → Combined CRITICAL threat even if each alone is MODERATE
 *
 * IMPORTANT: Only uses supported, real detection signals.
 * Never upgrades NOT_VERIFIED to DETECTED.
 */
@Singleton
class ThreatCorrelationEngine @Inject constructor(
    private val threatStateManager: ThreatStateManager
) {
    private val scope = CoroutineScope(Dispatchers.Default + SupervisorJob())

    // Current call state (updated by TelephonyCallStateManager)
    var isActiveCallDetected: Boolean = false
        private set

    // Current foreground app package (updated by SafeXAccessibilityService)
    var foregroundPackage: String = ""
        private set

    // UPI app in foreground
    var isUpiAppForeground: Boolean = false
        private set

    // Remote access app in foreground
    var isRemoteAccessAppForeground: Boolean = false
        private set

    // Financial app in foreground
    var isFinancialAppForeground: Boolean = false
        private set

    // Digital arrest score (updated by CoercionRiskScorer)
    var digitalArrestScore: Int = 0
        private set

    private val _correlatedThreat = MutableStateFlow(CorrelationResult())
    val correlatedThreat: StateFlow<CorrelationResult> = _correlatedThreat.asStateFlow()

    fun updateCallState(active: Boolean) {
        isActiveCallDetected = active
        correlate()
    }

    fun updateForegroundPackage(pkg: String) {
        foregroundPackage = pkg
        correlate()
    }

    fun updateUpiForeground(active: Boolean) {
        isUpiAppForeground = active
        correlate()
    }

    fun updateRemoteAccessForeground(active: Boolean) {
        isRemoteAccessAppForeground = active
        correlate()
    }

    fun updateFinancialAppForeground(active: Boolean) {
        isFinancialAppForeground = active
        correlate()
    }

    fun updateDigitalArrestScore(score: Int) {
        digitalArrestScore = score
        correlate()
    }

    private fun correlate() {
        scope.launch {
            val result = evaluateCorrelation()
            _correlatedThreat.value = result

            if (result.level != ThreatLevel.SAFE && result.level != ThreatLevel.UNKNOWN) {
                threatStateManager.reportEvent(
                    ThreatEvent(
                        module = ThreatModule.SYSTEM,
                        level = result.level,
                        detectionStatus = DetectionStatus.DETECTED,
                        title = result.title,
                        description = result.description,
                        evidenceSummary = result.evidenceSummary,
                        requiresImmediateWarning = result.level >= ThreatLevel.HIGH
                    )
                )
            }
        }
    }

    private fun evaluateCorrelation(): CorrelationResult {
        val indicators = mutableListOf<String>()
        var riskScore = 0

        // Active call during UPI transaction — core UPI fraud pattern
        if (isActiveCallDetected && isUpiAppForeground) {
            indicators.add("Active call while UPI app is open")
            riskScore += 40
        }

        // Remote access + financial app — screen exposure risk
        if (isRemoteAccessAppForeground && isFinancialAppForeground) {
            indicators.add("Remote access app active with financial app")
            riskScore += 50
        }

        // Remote access + active call
        if (isRemoteAccessAppForeground && isActiveCallDetected) {
            indicators.add("Remote access app active during call")
            riskScore += 30
        }

        // Digital arrest heuristics detected
        if (digitalArrestScore >= 75) {
            indicators.add("Critical digital arrest indicators matched")
            riskScore += 50
        } else if (digitalArrestScore >= 50) {
            indicators.add("High digital arrest risk indicators")
            riskScore += 30
        } else if (digitalArrestScore >= 25) {
            indicators.add("Moderate coercion language detected")
            riskScore += 15
        }

        // Triple correlation — most dangerous pattern
        if (isActiveCallDetected && isUpiAppForeground && isRemoteAccessAppForeground) {
            indicators.add("TRIPLE: Call + UPI + Remote access simultaneously")
            riskScore += 30 // Additional bonus for compound threat
        }

        val level = when {
            riskScore >= 80 -> ThreatLevel.CRITICAL
            riskScore >= 55 -> ThreatLevel.HIGH
            riskScore >= 30 -> ThreatLevel.MODERATE
            riskScore >= 10 -> ThreatLevel.LOW
            else -> ThreatLevel.SAFE
        }

        val title = when (level) {
            ThreatLevel.CRITICAL -> "CRITICAL: Multiple threat indicators active"
            ThreatLevel.HIGH -> "HIGH RISK: Compound threat detected"
            ThreatLevel.MODERATE -> "WARNING: Suspicious activity pattern"
            ThreatLevel.LOW -> "Low-level risk indicators present"
            else -> "No correlated threats"
        }

        return CorrelationResult(
            level = level,
            score = riskScore,
            indicators = indicators,
            title = title,
            description = indicators.joinToString("; "),
            evidenceSummary = "Risk score: $riskScore/100. Indicators: ${indicators.size}"
        )
    }
}

data class CorrelationResult(
    val level: ThreatLevel = ThreatLevel.SAFE,
    val score: Int = 0,
    val indicators: List<String> = emptyList(),
    val title: String = "",
    val description: String = "",
    val evidenceSummary: String = ""
)

private operator fun ThreatLevel.compareTo(other: ThreatLevel): Int =
    this.ordinal.compareTo(other.ordinal)

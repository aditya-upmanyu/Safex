package com.safex.app.domain.usecase

import com.safex.app.core.digitalarrest.CoercionRiskScorer
import com.safex.app.core.digitalarrest.DigitalArrestHeuristicMatcher
import com.safex.app.core.security.ThreatCorrelationEngine
import com.safex.app.core.security.ThreatStateManager
import com.safex.app.core.security.model.DetectionStatus
import com.safex.app.core.security.model.ThreatEvent
import com.safex.app.core.security.model.ThreatLevel
import com.safex.app.core.security.model.ThreatModule
import javax.inject.Inject

class EvaluateDigitalArrestUseCase @Inject constructor(
    private val threatStateManager: ThreatStateManager,
    private val threatCorrelationEngine: ThreatCorrelationEngine
) {
    fun evaluate(text: String, source: String = "manual_input"): CoercionRiskScorer.CoercionRiskResult {
        val matchResult = DigitalArrestHeuristicMatcher.analyze(text)
        val riskResult = CoercionRiskScorer.score(matchResult, source)

        // Update correlation engine with latest score
        threatCorrelationEngine.updateDigitalArrestScore(riskResult.score)

        // Report to threat state manager if score is notable
        if (riskResult.score >= 25) {
            threatStateManager.reportEvent(
                ThreatEvent(
                    module = ThreatModule.DIGITAL_ARREST_SHIELD,
                    level = riskResult.threatLevel,
                    detectionStatus = DetectionStatus.DETECTED,
                    title = "Digital arrest indicators detected",
                    description = riskResult.evidenceSummary,
                    requiresImmediateWarning = riskResult.score >= 75
                )
            )
        }

        return riskResult
    }
}

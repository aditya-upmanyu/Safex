package com.safex.app.core.digitalarrest

import com.safex.app.core.security.model.ThreatLevel

/**
 * Converts heuristic match results into a structured coercion risk assessment.
 *
 * Categories:
 *  0–24  → LOW
 * 25–49  → MODERATE
 * 50–74  → HIGH
 * 75–100 → CRITICAL
 *
 * Scoring is deterministic — same input always yields same output.
 * No random values. Fully unit-testable.
 *
 * Stronger indicator combinations:
 * - Impersonation + Arrest threat         → significant lift
 * - Financial coercion + Secrecy demand   → significant lift
 * - Procedure fraud (video interrogation) → high base weight
 */
object CoercionRiskScorer {

    enum class RiskCategory { LOW, MODERATE, HIGH, CRITICAL }

    data class CoercionRiskResult(
        val score: Int,
        val riskCategory: RiskCategory,
        val threatLevel: ThreatLevel,
        val matchedIndicators: List<DigitalArrestHeuristicMatcher.Indicator>,
        val evidenceSummary: String,
        val detectionSource: String,
        val confidence: String,        // HIGH / MEDIUM / LOW
        val negationDetected: Boolean
    )

    fun score(
        matchResult: DigitalArrestHeuristicMatcher.MatchResult,
        detectionSource: String = "text_analysis"
    ): CoercionRiskResult {
        var score = matchResult.score

        val positiveIndicators = matchResult.matchedIndicators.filter { it.weight > 0 }
        val categories = positiveIndicators.map { it.category }.toSet()

        // Compound bonus: impersonation + arrest threat together → +10
        if (categories.contains(DigitalArrestHeuristicMatcher.IndicatorCategory.IMPERSONATION) &&
            categories.contains(DigitalArrestHeuristicMatcher.IndicatorCategory.ARREST_THREAT)) {
            score += 10
        }

        // Compound bonus: financial coercion + secrecy demand → +10
        if (categories.contains(DigitalArrestHeuristicMatcher.IndicatorCategory.FINANCIAL_COERCION) &&
            categories.contains(DigitalArrestHeuristicMatcher.IndicatorCategory.SECRECY_DEMAND)) {
            score += 10
        }

        // Procedure fraud alone is a strong indicator → +5 bonus
        if (categories.contains(DigitalArrestHeuristicMatcher.IndicatorCategory.PROCEDURE_FRAUD)) {
            score += 5
        }

        score = score.coerceIn(0, 100)

        val riskCategory = when {
            score >= 75 -> RiskCategory.CRITICAL
            score >= 50 -> RiskCategory.HIGH
            score >= 25 -> RiskCategory.MODERATE
            else        -> RiskCategory.LOW
        }

        val threatLevel = when (riskCategory) {
            RiskCategory.CRITICAL -> ThreatLevel.CRITICAL
            RiskCategory.HIGH     -> ThreatLevel.HIGH
            RiskCategory.MODERATE -> ThreatLevel.MODERATE
            RiskCategory.LOW      -> ThreatLevel.LOW
        }

        val confidence = when {
            positiveIndicators.size >= 4 -> "HIGH"
            positiveIndicators.size >= 2 -> "MEDIUM"
            else -> "LOW"
        }

        val summary = buildString {
            append("Score: $score/100. ")
            append("Category: $riskCategory. ")
            append("Indicators (${positiveIndicators.size}): ")
            append(positiveIndicators.take(5).joinToString(", ") { it.phrase })
            if (matchResult.negationDetected) append(". [Negation detected — weights reduced]")
        }

        return CoercionRiskResult(
            score = score,
            riskCategory = riskCategory,
            threatLevel = threatLevel,
            matchedIndicators = positiveIndicators,
            evidenceSummary = summary,
            detectionSource = detectionSource,
            confidence = confidence,
            negationDetected = matchResult.negationDetected
        )
    }
}

package com.safex.app.domain.security

import com.safex.app.domain.security.model.*

/**
 * Deterministic, transparent security scoring algorithm.
 * Pure function with no Android dependencies - fully unit testable.
 * 
 * Algorithm:
 * 1. Group checks by category
 * 2. Score each category based on verified checks only
 * 3. Apply critical risk cap (prevents high score with critical issues)
 * 4. Calculate coverage (verified/applicable checks)
 * 5. Apply coverage penalty and grade cap
 * 6. Generate transparent message
 */
class SecurityRiskScorer {
    
    companion object {
        // Critical risk cap: if any verified critical risk, max grade is D
        private const val CRITICAL_CAP_GRADE = SecurityScore.Grade.D
        
        // Coverage thresholds for grade capping
        private const val COVERAGE_GRADE_C_MAX = 0.5f
        private const val COVERAGE_GRADE_B_MAX = 0.7f
        
        // Uncertainty penalty (applied when many checks are UNKNOWN)
        private const val MAX_UNCERTAINTY_PENALTY = 10 // max 10 points deduction
    }
    
    fun calculateScore(results: List<SecurityCheckResult>): SecurityScore {
        val timestamp = System.currentTimeMillis()
        
        // Separate checks by status
        val scoreableResults = results.filter { it.category != SecurityCategory.PRIVACY_EXPOSURE || it.isSupported }
        val verifiedResults = scoreableResults.filter { 
            it.status in listOf(SecurityStatus.SECURE, SecurityStatus.WARNING, SecurityStatus.DANGER)
        }
        val unknownResults = scoreableResults.filter { it.status == SecurityStatus.UNKNOWN }
        val unsupportedResults = scoreableResults.filter { it.status == SecurityStatus.UNSUPPORTED }
        
        // Check for critical risks
        val hasCriticalRisk = verifiedResults.any { 
            it.riskLevel == RiskLevel.CRITICAL && it.status == SecurityStatus.DANGER 
        }
        
        // Calculate category scores
        val categoryScores = SecurityCategory.entries.associateWith { category ->
            calculateCategoryScore(category, verifiedResults, unknownResults)
        }
        
        // Weighted total score
        var totalScore = categoryScores.values.sumOf { 
            (it.score * it.category.weight * 100).toDouble()
        }.toInt()
        
        // Apply uncertainty penalty
        val uncertaintyPenalty = calculateUncertaintyPenalty(
            unknownCount = unknownResults.size,
            totalCount = scoreableResults.size
        )
        totalScore = (totalScore - uncertaintyPenalty).coerceIn(0, 100)
        
        // Calculate coverage
        val coverage = if (scoreableResults.isNotEmpty()) {
            verifiedResults.size.toFloat() / scoreableResults.size
        } else {
            1.0f
        }
        
        // Determine grade with critical and coverage caps
        var grade = SecurityScore.Grade.fromScore(totalScore, coverage)
        if (hasCriticalRisk && grade.ordinal < CRITICAL_CAP_GRADE.ordinal) {
            grade = CRITICAL_CAP_GRADE
        }
        
        // Generate message
        val message = generateScoreMessage(
            score = totalScore,
            grade = grade,
            coverage = coverage,
            verifiedCount = verifiedResults.size,
            totalCount = scoreableResults.size,
            unknownCount = unknownResults.size,
            hasCriticalRisk = hasCriticalRisk,
            criticalCount = verifiedResults.count { it.riskLevel == RiskLevel.CRITICAL },
            warningCount = verifiedResults.count { it.status == SecurityStatus.WARNING }
        )
        
        return SecurityScore(
            totalScore = totalScore,
            grade = grade,
            coverage = coverage,
            categoryScores = categoryScores,
            message = message,
            timestamp = timestamp
        )
    }
    
    private fun calculateCategoryScore(
        category: SecurityCategory,
        verifiedResults: List<SecurityCheckResult>,
        unknownResults: List<SecurityCheckResult>
    ): SecurityScore.CategoryScore {
        val categoryResults = verifiedResults.filter { it.category == category }
        val categoryUnknown = unknownResults.filter { it.category == category }
        
        if (categoryResults.isEmpty()) {
            // No verified checks in this category
            return SecurityScore.CategoryScore(
                category = category,
                score = 0f,
                maxPossible = 1f,
                verified = 0,
                total = categoryResults.size + categoryUnknown.size
            )
        }
        
        // Calculate score for this category (0.0 - 1.0)
        val categoryScore = categoryResults.sumOf { result ->
            when (result.status) {
                SecurityStatus.SECURE -> 1.0 // Full points
                SecurityStatus.WARNING -> 0.5 // Half points
                SecurityStatus.DANGER -> 0.0 // No points
                else -> 0.0
            }
        } / categoryResults.size
        
        return SecurityScore.CategoryScore(
            category = category,
            score = categoryScore.toFloat(),
            maxPossible = 1f,
            verified = categoryResults.size,
            total = categoryResults.size + categoryUnknown.size
        )
    }
    
    private fun calculateUncertaintyPenalty(unknownCount: Int, totalCount: Int): Int {
        if (totalCount == 0) return 0
        
        val unknownRatio = unknownCount.toFloat() / totalCount
        
        // Progressive penalty: more unknowns = larger penalty
        return when {
            unknownRatio < 0.2f -> 0
            unknownRatio < 0.4f -> 3
            unknownRatio < 0.6f -> 6
            else -> MAX_UNCERTAINTY_PENALTY
        }
    }
    
    private fun generateScoreMessage(
        score: Int,
        grade: SecurityScore.Grade,
        coverage: Float,
        verifiedCount: Int,
        totalCount: Int,
        unknownCount: Int,
        hasCriticalRisk: Boolean,
        criticalCount: Int,
        warningCount: Int
    ): String {
        return when {
            hasCriticalRisk -> "Critical security risks detected. Immediate action required."
            
            warningCount > 3 -> "Multiple security settings need your attention"
            
            coverage < COVERAGE_GRADE_C_MAX -> 
                "Partial scan — $unknownCount of $totalCount checks couldn't be verified"
            
            score >= 90 -> "Your device security looks strong"
            
            score >= 80 -> "Good security with minor improvements needed"
            
            score >= 65 -> "Several security settings need attention"
            
            score >= 45 -> "Your device has weak security protections"
            
            else -> "Critical security vulnerabilities detected"
        }
    }
}

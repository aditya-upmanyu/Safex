package com.safex.app.domain.security

import com.safex.app.domain.security.model.*

/**
 * Generates prioritized, actionable recommendations from security check results.
 * Pure function - fully testable.
 */
object SecurityRecommendationEngine {
    
    data class Recommendation(
        val id: String,
        val title: String,
        val description: String,
        val priority: Priority,
        val remediation: RemediationAction?,
        val relatedCheckIds: List<String>
    ) {
        enum class Priority {
            CRITICAL, HIGH, MEDIUM, LOW
        }
    }
    
    /**
     * Generate recommendations from check results.
     * Only creates recommendations for actionable findings.
     */
    fun generateRecommendations(results: List<SecurityCheckResult>): List<Recommendation> {
        val recommendations = mutableListOf<Recommendation>()
        
        // Group by risk level for prioritization
        val criticalIssues = results.filter { it.riskLevel == RiskLevel.CRITICAL && it.status == SecurityStatus.DANGER }
        val highRiskIssues = results.filter { it.riskLevel == RiskLevel.HIGH && it.status == SecurityStatus.DANGER }
        val mediumRiskIssues = results.filter { it.riskLevel == RiskLevel.MEDIUM && it.status == SecurityStatus.WARNING }
        val lowRiskIssues = results.filter { it.riskLevel == RiskLevel.LOW && it.status in listOf(SecurityStatus.WARNING, SecurityStatus.INFO) }
        
        // Critical recommendations (Root, No device lock, etc.)
        criticalIssues.forEach { result ->
            result.remediation?.let { action ->
                recommendations.add(
                    Recommendation(
                        id = "rec_${result.id}",
                        title = result.title,
                        description = result.recommendation,
                        priority = Recommendation.Priority.CRITICAL,
                        remediation = action,
                        relatedCheckIds = listOf(result.id)
                    )
                )
            }
        }
        
        // High priority recommendations
        highRiskIssues.forEach { result ->
            result.remediation?.let { action ->
                recommendations.add(
                    Recommendation(
                        id = "rec_${result.id}",
                        title = result.title,
                        description = result.recommendation,
                        priority = Recommendation.Priority.HIGH,
                        remediation = action,
                        relatedCheckIds = listOf(result.id)
                    )
                )
            }
        }
        
        // Medium priority recommendations
        mediumRiskIssues.forEach { result ->
            result.remediation?.let { action ->
                recommendations.add(
                    Recommendation(
                        id = "rec_${result.id}",
                        title = result.title,
                        description = result.recommendation,
                        priority = Recommendation.Priority.MEDIUM,
                        remediation = action,
                        relatedCheckIds = listOf(result.id)
                    )
                )
            }
        }
        
        // Low priority recommendations (only if actionable)
        lowRiskIssues.take(3).forEach { result ->
            result.remediation?.let { action ->
                recommendations.add(
                    Recommendation(
                        id = "rec_${result.id}",
                        title = result.title,
                        description = result.recommendation,
                        priority = Recommendation.Priority.LOW,
                        remediation = action,
                        relatedCheckIds = listOf(result.id)
                    )
                )
            }
        }
        
        return recommendations.sortedByDescending { it.priority }
    }
}

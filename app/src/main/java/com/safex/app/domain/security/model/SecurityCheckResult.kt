package com.safex.app.domain.security.model

/**
 * Complete result of a security check execution.
 * Every field is meaningful and required for honest reporting.
 */
data class SecurityCheckResult(
    val id: String,
    val title: String,
    val category: SecurityCategory,
    
    // Status & Risk
    val status: SecurityStatus,
    val riskLevel: RiskLevel,
    
    // What was detected
    val detectedValue: String?,
    
    // User-facing explanations
    val userExplanation: String,
    val technicalDetails: String,
    val whyItMatters: String,
    val recommendation: String,
    
    // Remediation
    val remediation: RemediationAction?,
    
    // Honesty & transparency
    val limitation: String?,
    val confidence: Confidence,
    val detectionMethod: String,
    val evidence: List<Evidence>,
    
    // Metadata
    val isSupported: Boolean,
    val minApi: Int,
    val lastCheckedAt: Long,
    val durationMs: Long
)

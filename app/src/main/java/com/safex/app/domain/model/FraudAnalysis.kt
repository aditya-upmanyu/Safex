package com.safex.app.domain.model

data class FraudAnalysis(
    val riskScore: Int,
    val verdict: RiskLevel,
    val confidence: Float,
    val detectedPatterns: List<String>,
    val keywords: List<String>,
    val reason: String,
    val reasonHindi: String,
    val recommendedAction: String
)

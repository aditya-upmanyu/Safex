package com.safex.app.domain.model

data class CallRecord(
    val id: Long = 0,
    val phoneNumber: String,
    val timestamp: Long,
    val duration: Long,
    val transcript: String,
    val riskScore: Int,
    val verdict: RiskLevel,
    val confidence: Float,
    val detectedPatterns: List<String>,
    val keywords: List<String>,
    val reason: String,
    val reasonHindi: String,
    val recommendedAction: String,
    val status: String
)

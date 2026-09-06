package com.safex.app.domain.model

data class ScanResult(
    val id: Long = 0,
    val targetUrl: String,
    val scanTimestamp: Long,
    val riskScore: Int,
    val verdict: RiskLevel,
    val threatInfo: String,
    val source: String
)

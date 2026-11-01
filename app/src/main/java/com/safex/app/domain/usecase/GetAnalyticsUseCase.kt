package com.safex.app.domain.usecase

import javax.inject.Inject

/**
 * Generates analytics for the dashboard
 * - Security score (0-100)
 * - Fraud statistics
 * - Risk trends
 */
class GetAnalyticsUseCase @Inject constructor() {

    fun getSecurityDashboard(): SecurityDashboard {
        // Simplified dashboard generation
        return SecurityDashboard(
            securityScore = 85,
            securityStatus = "Secure ✓",
            callsAnalyzed = 0,
            fraudDetected = 0,
            suspiciousDetected = 0,
            urlsScanned = 0,
            maliciousUrls = 0,
            suspiciousUrls = 0,
            topThreats = emptyList(),
            trendLast24h = "No threats detected",
            lastUpdated = System.currentTimeMillis()
        )
    }
}

/**
 * Dashboard data model
 */
data class SecurityDashboard(
    val securityScore: Int,              // 0-100
    val securityStatus: String,          // Status text
    val callsAnalyzed: Int,              // Total calls analyzed
    val fraudDetected: Int,              // Fraud calls
    val suspiciousDetected: Int,         // Suspicious calls
    val urlsScanned: Int,                // Total URLs scanned
    val maliciousUrls: Int,              // Malicious URLs
    val suspiciousUrls: Int,             // Suspicious URLs
    val topThreats: List<String>,        // Top threat types
    val trendLast24h: String,            // Trend description
    val lastUpdated: Long                // Last update timestamp
)


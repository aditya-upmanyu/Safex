// domain/appsentry/model/AppRiskLevel.kt
package com.safex.app.domain.appsentry.model

/**
 * Overall risk classification for a scanned app or APK.
 * Ordered from safest to most dangerous for easy comparison.
 */
enum class AppRiskLevel(val score: Int, val displayName: String) {
    SAFE(0, "Safe"),
    LOW(25, "Low Risk"),
    MEDIUM(50, "Medium Risk"),
    HIGH(75, "High Risk"),
    CRITICAL(100, "Critical Risk");

    companion object {
        /** Derives risk level from a 0-100 numeric score. */
        fun fromScore(score: Int): AppRiskLevel = when {
            score >= 85 -> CRITICAL
            score >= 65 -> HIGH
            score >= 40 -> MEDIUM
            score >= 15 -> LOW
            else -> SAFE
        }
    }
}

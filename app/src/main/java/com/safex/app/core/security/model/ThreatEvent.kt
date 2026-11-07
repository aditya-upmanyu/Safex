package com.safex.app.core.security.model

import java.util.UUID

/**
 * Represents a single threat signal from any module.
 * All four modules produce ThreatEvents consumed by ThreatCorrelationEngine.
 */
data class ThreatEvent(
    val id: String = UUID.randomUUID().toString(),
    val module: ThreatModule,
    val level: ThreatLevel,
    val detectionStatus: DetectionStatus,
    val title: String,
    val description: String,
    val packageName: String? = null,
    val callerNumber: String? = null,
    val evidenceSummary: String = "",
    val timestamp: Long = System.currentTimeMillis(),
    val requiresImmediateWarning: Boolean = false
)

enum class ThreatModule {
    UPI_SHIELD,
    REMOTE_ACCESS_SENTRY,
    DIGITAL_ARREST_SHIELD,
    FAMILY_GUARDIAN,
    SYSTEM
}

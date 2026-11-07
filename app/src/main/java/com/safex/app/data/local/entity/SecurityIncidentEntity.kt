package com.safex.app.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "security_incidents")
data class SecurityIncidentEntity(
    @PrimaryKey
    val incidentId: String,                 // UUID
    val incidentType: String,               // UPI_COLLISION, REMOTE_ACCESS, DIGITAL_ARREST, SUSPICIOUS_APK
    val severity: String,                   // ThreatLevel name
    val threatScore: Int,                   // 0–100
    val detectionSource: String,            // Module name
    val callerNumber: String? = null,       // Only when legitimately available
    val evidenceSummary: String,
    val transcriptSnippet: String? = null,  // Only when user consented
    val timestamp: Long = System.currentTimeMillis(),
    val guardianNotificationStatus: String = "NOT_SENT", // NOT_SENT, SENT, FAILED, SKIPPED
    val userAcknowledged: Boolean = false,
    val incidentStatus: String = "OBSERVED" // IncidentStatus name
)

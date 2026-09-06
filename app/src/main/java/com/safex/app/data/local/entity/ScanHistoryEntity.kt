package com.safex.app.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "scan_history")
data class ScanHistoryEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val targetUrl: String,
    val scanTimestamp: Long,
    val riskScore: Int,
    val verdict: String, // SAFE, SUSPICIOUS, MALICIOUS
    val threatInfo: String, // JSON object as string
    val source: String // MANUAL, QR_CODE, GALLERY
)

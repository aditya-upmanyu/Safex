package com.safex.app.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "call_records")
data class CallRecordEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val phoneNumber: String,
    val timestamp: Long,
    val duration: Long,
    val transcript: String,
    val riskScore: Int,
    val verdict: String, // SAFE, SUSPICIOUS, FRAUD
    val confidence: Float,
    val detectedPatterns: String, // JSON array as string
    val keywords: String, // JSON array as string
    val reason: String,
    val reasonHindi: String,
    val recommendedAction: String,
    val status: String // ANALYZED, PENDING, ERROR
)

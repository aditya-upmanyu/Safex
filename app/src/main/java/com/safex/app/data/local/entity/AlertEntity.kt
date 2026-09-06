package com.safex.app.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "alerts")
data class AlertEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val timestamp: Long,
    val severity: String, // LOW, MEDIUM, HIGH, CRITICAL
    val title: String,
    val message: String,
    val acknowledged: Boolean = false
)

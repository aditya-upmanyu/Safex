package com.safex.app.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "guardians")
data class GuardianEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val guardianName: String,
    val phoneNumber: String,        // Stored as-is; never logged
    val relationship: String,       // e.g. "Son", "Daughter", "Friend"
    val isVerified: Boolean = false,
    val isPrimary: Boolean = false,
    val isEnabled: Boolean = true,
    val notifyOnDigitalArrest: Boolean = true,
    val notifyOnSuspiciousApk: Boolean = true,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)

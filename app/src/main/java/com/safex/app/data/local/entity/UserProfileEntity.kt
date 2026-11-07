package com.safex.app.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "user_profile")
data class UserProfileEntity(
    @PrimaryKey
    val userId: String,
    val phoneNumber: String,
    val email: String?,
    val fullName: String,
    val age: Int,
    val userType: String, // "PARENT", "CHILD", "INDEPENDENT"
    val parentPhoneNumber: String?,
    val parentName: String?,
    val childPhoneNumber: String?,
    val childName: String?,
    val nearestPoliceStation: String?,
    val policeStationPhone: String?,
    val emergencyContact1Name: String?,
    val emergencyContact1Phone: String?,
    val emergencyContact2Name: String?,
    val emergencyContact2Phone: String?,
    val profileCompletedAt: Long,
    val createdAt: Long = System.currentTimeMillis()
)

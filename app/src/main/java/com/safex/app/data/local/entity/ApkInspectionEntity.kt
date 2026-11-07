// data/local/entity/ApkInspectionEntity.kt
package com.safex.app.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "apk_inspections")
data class ApkInspectionEntity(
    @PrimaryKey val filePath: String,
    val fileName: String,
    val fileSize: Long,
    val sha256: String,
    val packageName: String?,
    val appName: String?,
    val versionName: String?,
    val versionCode: Long?,
    val requestedPermissionsJson: String,
    val riskScore: Int,
    val riskLevel: String,
    val threatIndicatorsJson: String,
    val harmlessCount: Int,
    val maliciousCount: Int,
    val suspiciousCount: Int,
    val undetectedCount: Int,
    val totalEngines: Int,
    val permalink: String?,
    val inspectedAt: Long,
    val isLegitimateCertificate: Boolean,
    val errorMessage: String?
)

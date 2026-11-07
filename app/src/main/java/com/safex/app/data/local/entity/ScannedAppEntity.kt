// data/local/entity/ScannedAppEntity.kt
package com.safex.app.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "scanned_apps")
data class ScannedAppEntity(
    @PrimaryKey val packageName: String,
    val appName: String,
    val installedAt: Long,
    val updatedAt: Long,
    val versionName: String,
    val versionCode: Long,
    val permissionsJson: String,
    val isSideLoaded: Boolean,
    val installerPackage: String?,
    val riskScore: Int,
    val riskLevel: String,
    val threatIndicatorsJson: String,
    val scannedAt: Long,
    val isTrusted: Boolean = false
)

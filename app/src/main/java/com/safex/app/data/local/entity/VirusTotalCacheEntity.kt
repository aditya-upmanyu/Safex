// data/local/entity/VirusTotalCacheEntity.kt
package com.safex.app.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "virustotal_cache")
data class VirusTotalCacheEntity(
    @PrimaryKey val sha256: String,
    val harmless: Int,
    val malicious: Int,
    val suspicious: Int,
    val undetected: Int,
    val totalEngines: Int,
    val permalink: String?,
    val cachedAt: Long
)

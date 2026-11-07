package com.safex.app.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.safex.app.data.local.dao.AlertDao
import com.safex.app.data.local.dao.AppSentryDao
import com.safex.app.data.local.dao.CallRecordDao
import com.safex.app.data.local.dao.ChatDao
import com.safex.app.data.local.dao.GuardianDao
import com.safex.app.data.local.dao.ScanHistoryDao
import com.safex.app.data.local.dao.SecurityIncidentDao
import com.safex.app.data.local.entity.AlertEntity
import com.safex.app.data.local.entity.ApkInspectionEntity
import com.safex.app.data.local.entity.CallRecordEntity
import com.safex.app.data.local.entity.ChatMessageEntity
import com.safex.app.data.local.entity.ChatSessionEntity
import com.safex.app.data.local.entity.GuardianEntity
import com.safex.app.data.local.entity.ScanHistoryEntity
import com.safex.app.data.local.entity.ScannedAppEntity
import com.safex.app.data.local.entity.SecurityIncidentEntity
import com.safex.app.data.local.entity.TrustedAppEntity
import com.safex.app.data.local.entity.VirusTotalCacheEntity

@Database(
    entities = [
        CallRecordEntity::class,
        ScanHistoryEntity::class,
        AlertEntity::class,
        ChatSessionEntity::class,
        ChatMessageEntity::class,
        ScannedAppEntity::class,
        ApkInspectionEntity::class,
        VirusTotalCacheEntity::class,
        TrustedAppEntity::class,
        GuardianEntity::class,
        SecurityIncidentEntity::class
    ],
    version = 4,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun callRecordDao(): CallRecordDao
    abstract fun scanHistoryDao(): ScanHistoryDao
    abstract fun alertDao(): AlertDao
    abstract fun chatDao(): ChatDao
    abstract fun appSentryDao(): AppSentryDao
    abstract fun guardianDao(): GuardianDao
    abstract fun securityIncidentDao(): SecurityIncidentDao
}

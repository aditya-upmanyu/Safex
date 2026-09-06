package com.safex.app.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.safex.app.data.local.dao.AlertDao
import com.safex.app.data.local.dao.CallRecordDao
import com.safex.app.data.local.dao.ScanHistoryDao
import com.safex.app.data.local.entity.AlertEntity
import com.safex.app.data.local.entity.CallRecordEntity
import com.safex.app.data.local.entity.ScanHistoryEntity

@Database(
    entities = [
        CallRecordEntity::class,
        ScanHistoryEntity::class,
        AlertEntity::class
    ],
    version = 1,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun callRecordDao(): CallRecordDao
    abstract fun scanHistoryDao(): ScanHistoryDao
    abstract fun alertDao(): AlertDao
}

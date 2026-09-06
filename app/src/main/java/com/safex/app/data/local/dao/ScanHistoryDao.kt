package com.safex.app.data.local.dao

import androidx.room.*
import com.safex.app.data.local.entity.ScanHistoryEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ScanHistoryDao {
    
    @Query("SELECT * FROM scan_history ORDER BY scanTimestamp DESC")
    fun observeAll(): Flow<List<ScanHistoryEntity>>

    @Query("SELECT * FROM scan_history ORDER BY scanTimestamp DESC LIMIT :limit")
    fun observeRecent(limit: Int = 20): Flow<List<ScanHistoryEntity>>

    @Query("SELECT * FROM scan_history WHERE id = :id")
    suspend fun getById(id: Long): ScanHistoryEntity?

    @Query("SELECT * FROM scan_history WHERE targetUrl = :url ORDER BY scanTimestamp DESC LIMIT 1")
    suspend fun getByUrl(url: String): ScanHistoryEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(scan: ScanHistoryEntity): Long

    @Update
    suspend fun update(scan: ScanHistoryEntity)

    @Delete
    suspend fun delete(scan: ScanHistoryEntity)

    @Query("DELETE FROM scan_history")
    suspend fun deleteAll()

    @Query("DELETE FROM scan_history WHERE scanTimestamp < :beforeTimestamp")
    suspend fun deleteOlderThan(beforeTimestamp: Long)
}

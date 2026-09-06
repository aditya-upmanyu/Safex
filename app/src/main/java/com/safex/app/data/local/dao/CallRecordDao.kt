package com.safex.app.data.local.dao

import androidx.room.*
import com.safex.app.data.local.entity.CallRecordEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface CallRecordDao {
    
    @Query("SELECT * FROM call_records ORDER BY timestamp DESC")
    fun observeAll(): Flow<List<CallRecordEntity>>

    @Query("SELECT * FROM call_records ORDER BY timestamp DESC LIMIT :limit")
    fun observeRecent(limit: Int = 20): Flow<List<CallRecordEntity>>

    @Query("SELECT * FROM call_records WHERE id = :id")
    suspend fun getById(id: Long): CallRecordEntity?

    @Query("SELECT * FROM call_records WHERE verdict = 'FRAUD' ORDER BY timestamp DESC")
    fun observeFraudCalls(): Flow<List<CallRecordEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(record: CallRecordEntity): Long

    @Update
    suspend fun update(record: CallRecordEntity)

    @Delete
    suspend fun delete(record: CallRecordEntity)

    @Query("DELETE FROM call_records")
    suspend fun deleteAll()

    @Query("DELETE FROM call_records WHERE timestamp < :beforeTimestamp")
    suspend fun deleteOlderThan(beforeTimestamp: Long)
}

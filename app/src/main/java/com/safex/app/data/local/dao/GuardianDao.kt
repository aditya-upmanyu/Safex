package com.safex.app.data.local.dao

import androidx.room.*
import com.safex.app.data.local.entity.GuardianEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface GuardianDao {

    @Query("SELECT * FROM guardians ORDER BY isPrimary DESC, createdAt ASC")
    fun getAllGuardians(): Flow<List<GuardianEntity>>

    @Query("SELECT * FROM guardians WHERE isEnabled = 1 ORDER BY isPrimary DESC")
    fun getEnabledGuardians(): Flow<List<GuardianEntity>>

    @Query("SELECT * FROM guardians WHERE id = :id")
    suspend fun getGuardianById(id: Long): GuardianEntity?

    @Query("SELECT * FROM guardians WHERE isPrimary = 1 LIMIT 1")
    suspend fun getPrimaryGuardian(): GuardianEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertGuardian(guardian: GuardianEntity): Long

    @Update
    suspend fun updateGuardian(guardian: GuardianEntity)

    @Delete
    suspend fun deleteGuardian(guardian: GuardianEntity)

    @Query("DELETE FROM guardians WHERE id = :id")
    suspend fun deleteGuardianById(id: Long)

    @Query("SELECT COUNT(*) FROM guardians WHERE isEnabled = 1")
    suspend fun getEnabledGuardianCount(): Int

    @Query("UPDATE guardians SET isPrimary = 0")
    suspend fun clearPrimaryGuardian()

    @Query("UPDATE guardians SET isPrimary = 1, updatedAt = :now WHERE id = :id")
    suspend fun setPrimaryGuardian(id: Long, now: Long = System.currentTimeMillis())
}

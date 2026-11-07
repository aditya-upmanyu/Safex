package com.safex.app.data.local.dao

import androidx.room.*
import com.safex.app.data.local.entity.UserProfileEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface UserProfileDao {
    @Query("SELECT * FROM user_profile LIMIT 1")
    fun getUserProfile(): Flow<UserProfileEntity?>
    
    @Query("SELECT * FROM user_profile LIMIT 1")
    suspend fun getUserProfileOnce(): UserProfileEntity?
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProfile(profile: UserProfileEntity)
    
    @Update
    suspend fun updateProfile(profile: UserProfileEntity)
    
    @Query("DELETE FROM user_profile")
    suspend fun deleteProfile()
    
    @Query("SELECT COUNT(*) FROM user_profile")
    suspend fun hasProfile(): Int
}

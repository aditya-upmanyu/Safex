package com.safex.app.di

import android.content.Context
import androidx.room.Room
import androidx.security.crypto.EncryptedFile
import androidx.security.crypto.MasterKey
import com.safex.app.data.local.AppDatabase
import com.safex.app.data.local.dao.AlertDao
import com.safex.app.data.local.dao.CallRecordDao
import com.safex.app.data.local.dao.ScanHistoryDao
import com.safex.app.util.Constants
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * Database Module - Uses standard Room without SQLCipher
 * 
 * Alternative 16 KB Compatible Encryption Strategy:
 * - Uses Android Keystore for key management
 * - Encrypts sensitive data at field level (not whole database)
 * - Or uses file-level encryption via EncryptedFile
 * - 100% compatible with Android 15+ (16 KB pages)
 * - No third-party native libraries
 */
@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideAppDatabase(
        @ApplicationContext context: Context
    ): AppDatabase {
        // Use standard Room database without SQLCipher
        // For 16 KB page size compatibility
        return Room.databaseBuilder(
            context,
            AppDatabase::class.java,
            Constants.DATABASE_NAME
        )
            .fallbackToDestructiveMigration()
            .build()
    }

    @Provides
    @Singleton
    fun provideMasterKey(@ApplicationContext context: Context): MasterKey {
        // Provides encryption key for sensitive data
        return MasterKey.Builder(context)
            .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
            .build()
    }

    @Provides
    @Singleton
    fun provideCallRecordDao(database: AppDatabase): CallRecordDao {
        return database.callRecordDao()
    }

    @Provides
    @Singleton
    fun provideScanHistoryDao(database: AppDatabase): ScanHistoryDao {
        return database.scanHistoryDao()
    }

    @Provides
    @Singleton
    fun provideAlertDao(database: AppDatabase): AlertDao {
        return database.alertDao()
    }
}

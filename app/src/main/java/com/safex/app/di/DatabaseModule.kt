package com.safex.app.di

import android.content.Context
import androidx.room.Room
import com.safex.app.data.local.AppDatabase
import com.safex.app.data.local.dao.AlertDao
import com.safex.app.data.local.dao.CallRecordDao
import com.safex.app.data.local.dao.ScanHistoryDao
import com.safex.app.util.Constants
import com.safex.app.util.DbPassphraseProvider
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import net.sqlcipher.database.SupportFactory
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideAppDatabase(
        @ApplicationContext context: Context
    ): AppDatabase {
        val passphrase = DbPassphraseProvider.getOrCreatePassphrase(context)
        val factory = SupportFactory(passphrase)
        
        return Room.databaseBuilder(
            context,
            AppDatabase::class.java,
            Constants.DATABASE_NAME
        )
            .openHelperFactory(factory)
            .fallbackToDestructiveMigration()
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

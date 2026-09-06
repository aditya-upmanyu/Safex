package com.safex.app.di

import com.google.gson.Gson
import com.safex.app.data.local.dao.CallRecordDao
import com.safex.app.data.local.dao.ScanHistoryDao
import com.safex.app.data.remote.api.VirusTotalApi
import com.safex.app.data.repository.CallRepositoryImpl
import com.safex.app.data.repository.ScanRepositoryImpl
import com.safex.app.domain.repository.CallRepository
import com.safex.app.domain.repository.ScanRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object RepositoryModule {

    @Provides
    @Singleton
    fun provideCallRepository(
        callRecordDao: CallRecordDao
    ): CallRepository {
        return CallRepositoryImpl(callRecordDao)
    }

    @Provides
    @Singleton
    fun provideScanRepository(
        scanHistoryDao: ScanHistoryDao,
        virusTotalApi: VirusTotalApi,
        gson: Gson
    ): ScanRepository {
        return ScanRepositoryImpl(scanHistoryDao, virusTotalApi, gson)
    }
}

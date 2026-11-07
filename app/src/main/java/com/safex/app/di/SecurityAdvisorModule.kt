package com.safex.app.di

import android.content.Context
import com.google.gson.Gson
import com.safex.app.data.security.SecurityAdvisorRepositoryImpl
import com.safex.app.domain.security.SecurityCheckManager
import com.safex.app.domain.security.SecurityRiskScorer
import com.safex.app.domain.security.SecuritySettingsNavigator
import com.safex.app.domain.security.repository.SecurityAdvisorRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object SecurityAdvisorModule {
    
    @Provides
    @Singleton
    fun provideSecurityCheckManager(): SecurityCheckManager {
        return SecurityCheckManager()
    }
    
    @Provides
    @Singleton
    fun provideSecurityRiskScorer(): SecurityRiskScorer {
        return SecurityRiskScorer()
    }
    
    @Provides
    @Singleton
    fun provideSecuritySettingsNavigator(
        @ApplicationContext context: Context
    ): SecuritySettingsNavigator {
        return SecuritySettingsNavigator(context)
    }
    
    @Provides
    @Singleton
    fun provideSecurityAdvisorRepository(
        @ApplicationContext context: Context,
        checkManager: SecurityCheckManager,
        gson: Gson
    ): SecurityAdvisorRepository {
        return SecurityAdvisorRepositoryImpl(context, checkManager, gson)
    }
}

// di/AppSentryModule.kt
package com.safex.app.di

import com.safex.app.data.appsentry.AppSentryRepositoryImpl
import com.safex.app.data.appsentry.FakeAppRegistryDataSource
import com.safex.app.domain.appsentry.repository.AppRegistryProvider
import com.safex.app.domain.appsentry.repository.AppSentryRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class AppSentryModule {

    @Binds
    @Singleton
    abstract fun bindAppSentryRepository(
        impl: AppSentryRepositoryImpl
    ): AppSentryRepository

    @Binds
    @Singleton
    abstract fun bindAppRegistryProvider(
        impl: FakeAppRegistryDataSource
    ): AppRegistryProvider
}

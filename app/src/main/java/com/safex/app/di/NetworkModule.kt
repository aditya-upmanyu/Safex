package com.safex.app.di

import com.google.gson.Gson
import com.safex.app.data.remote.api.GoogleSttApi
import com.safex.app.data.remote.api.OpenAIApi
import com.safex.app.data.remote.api.VirusTotalApi
import com.safex.app.util.Constants
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit
import javax.inject.Qualifier
import javax.inject.Singleton

@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class OpenAIRetrofit

@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class GoogleSttRetrofit

@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class VirusTotalRetrofit

@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {

    @Provides
    @Singleton
    fun provideOkHttpClient(): OkHttpClient {
        val loggingInterceptor = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        }

        return OkHttpClient.Builder()
            .addInterceptor(loggingInterceptor)
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)
            .build()
    }

    @Provides
    @Singleton
    @OpenAIRetrofit
    fun provideOpenAIRetrofit(
        okHttpClient: OkHttpClient,
        gson: Gson
    ): Retrofit {
        return Retrofit.Builder()
            .baseUrl(Constants.OPENAI_BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create(gson))
            .build()
    }

    @Provides
    @Singleton
    @GoogleSttRetrofit
    fun provideGoogleSttRetrofit(
        okHttpClient: OkHttpClient,
        gson: Gson
    ): Retrofit {
        return Retrofit.Builder()
            .baseUrl(Constants.GOOGLE_STT_BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create(gson))
            .build()
    }

    @Provides
    @Singleton
    @VirusTotalRetrofit
    fun provideVirusTotalRetrofit(
        okHttpClient: OkHttpClient,
        gson: Gson
    ): Retrofit {
        return Retrofit.Builder()
            .baseUrl(Constants.VIRUSTOTAL_BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create(gson))
            .build()
    }

    @Provides
    @Singleton
    fun provideOpenAIApi(@OpenAIRetrofit retrofit: Retrofit): OpenAIApi {
        return retrofit.create(OpenAIApi::class.java)
    }

    @Provides
    @Singleton
    fun provideGoogleSttApi(@GoogleSttRetrofit retrofit: Retrofit): GoogleSttApi {
        return retrofit.create(GoogleSttApi::class.java)
    }

    @Provides
    @Singleton
    fun provideVirusTotalApi(@VirusTotalRetrofit retrofit: Retrofit): VirusTotalApi {
        return retrofit.create(VirusTotalApi::class.java)
    }
}

package com.safex.app.data.chatbot

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import javax.inject.Inject
import javax.inject.Singleton

private val Context.rateLimitDataStore by preferencesDataStore(name = "safex_ai_rate_limits")

sealed interface RateLimitStatus {
    data object Allowed : RateLimitStatus
    data class Throttled(val waitSeconds: Int) : RateLimitStatus
    data class HourlyExceeded(val minutesLeft: Int) : RateLimitStatus
    data class DailyExceeded(val minutesLeft: Int) : RateLimitStatus
}

@Singleton
class AiRateLimiter @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val mutex = Mutex()

    private val timestampsKey = stringPreferencesKey("ai_call_timestamps")
    private val lastCallKey = stringPreferencesKey("ai_last_call_timestamp")

    var hourlyLimit: Int = 20
    var dailyLimit: Int = 60
    var minIntervalMs: Long = 1500L // 1.5s between calls

    suspend fun checkLimit(): RateLimitStatus = mutex.withLock {
        val now = System.currentTimeMillis()
        val prefs = context.rateLimitDataStore.data.first()

        val lastCall = prefs[lastCallKey]?.toLongOrNull() ?: 0L
        val diff = now - lastCall
        if (diff < minIntervalMs) {
            val waitSec = (((minIntervalMs - diff) / 1000) + 1).toInt()
            return RateLimitStatus.Throttled(waitSec)
        }

        val rawTimestamps = prefs[timestampsKey] ?: ""
        val timestamps = if (rawTimestamps.isBlank()) {
            emptyList()
        } else {
            rawTimestamps.split(",").mapNotNull { it.toLongOrNull() }
        }

        // Filter last 24h
        val oneDayAgo = now - (24 * 60 * 60 * 1000L)
        val oneHourAgo = now - (60 * 60 * 1000L)

        val pastDayCalls = timestamps.filter { it >= oneDayAgo }
        val pastHourCalls = pastDayCalls.filter { it >= oneHourAgo }

        if (pastHourCalls.size >= hourlyLimit) {
            val oldestInHour = pastHourCalls.minOrNull() ?: now
            val minutesUntilReset = (((oldestInHour + (60 * 60 * 1000L) - now) / 60000) + 1).toInt().coerceAtLeast(1)
            return RateLimitStatus.HourlyExceeded(minutesUntilReset)
        }

        if (pastDayCalls.size >= dailyLimit) {
            val oldestInDay = pastDayCalls.minOrNull() ?: now
            val minutesUntilReset = (((oldestInDay + (24 * 60 * 60 * 1000L) - now) / 60000) + 1).toInt().coerceAtLeast(1)
            return RateLimitStatus.DailyExceeded(minutesUntilReset)
        }

        return RateLimitStatus.Allowed
    }

    suspend fun recordCall(): Unit = mutex.withLock {
        val now = System.currentTimeMillis()
        val oneDayAgo = now - (24 * 60 * 60 * 1000L)

        context.rateLimitDataStore.edit { prefs ->
            val raw = prefs[timestampsKey] ?: ""
            val current = raw.split(",").mapNotNull { it.toLongOrNull() }.filter { it >= oneDayAgo }
            val updated = current + now
            prefs[timestampsKey] = updated.joinToString(",")
            prefs[lastCallKey] = now.toString()
        }
    }

    suspend fun getRemainingHourly(): Int = mutex.withLock {
        val now = System.currentTimeMillis()
        val oneHourAgo = now - (60 * 60 * 1000L)
        val prefs = context.rateLimitDataStore.data.first()
        val raw = prefs[timestampsKey] ?: ""
        val count = raw.split(",").mapNotNull { it.toLongOrNull() }.count { it >= oneHourAgo }
        return (hourlyLimit - count).coerceAtLeast(0)
    }
}

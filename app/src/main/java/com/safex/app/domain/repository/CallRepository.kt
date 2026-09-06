package com.safex.app.domain.repository

import com.safex.app.domain.model.CallRecord
import kotlinx.coroutines.flow.Flow

interface CallRepository {
    fun getRecentCalls(limit: Int = 20): Flow<List<CallRecord>>
    fun getAllCalls(): Flow<List<CallRecord>>
    fun getFraudCalls(): Flow<List<CallRecord>>
    suspend fun getCallById(id: Long): CallRecord?
    suspend fun insertCall(record: CallRecord): Long
    suspend fun updateCall(record: CallRecord)
    suspend fun deleteCall(record: CallRecord)
    suspend fun deleteAllCalls()
    suspend fun deleteOlderThan(timestampMillis: Long)
}

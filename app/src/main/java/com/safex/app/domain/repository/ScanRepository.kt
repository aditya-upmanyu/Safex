package com.safex.app.domain.repository

import com.safex.app.domain.model.ScanResult
import kotlinx.coroutines.flow.Flow

interface ScanRepository {
    suspend fun scanUrl(url: String, source: String): Result<ScanResult>
    fun getScanHistory(limit: Int = 20): Flow<List<ScanResult>>
    fun getAllScans(): Flow<List<ScanResult>>
    suspend fun getScanById(id: Long): ScanResult?
    suspend fun deleteScan(scan: ScanResult)
    suspend fun deleteAllScans()
}

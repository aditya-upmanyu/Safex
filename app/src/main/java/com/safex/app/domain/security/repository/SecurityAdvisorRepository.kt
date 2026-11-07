package com.safex.app.domain.security.repository

import com.safex.app.domain.security.model.SecurityCheckResult
import com.safex.app.domain.security.model.SecurityScore
import kotlinx.coroutines.flow.Flow

/**
 * Repository interface for security advisor operations
 */
interface SecurityAdvisorRepository {
    
    /**
     * Execute security scan and return results
     */
    suspend fun executeScan(): List<SecurityCheckResult>
    
    /**
     * Get cached scan results
     */
    fun getCachedResults(): Flow<List<SecurityCheckResult>>
    
    /**
     * Get last scan timestamp
     */
    suspend fun getLastScanTimestamp(): Long?
    
    /**
     * Clear cached scan data
     */
    suspend fun clearCache()
    
    /**
     * Get scan history (last 10 scans with scores only)
     */
    suspend fun getScanHistory(): List<SecurityScore>
}

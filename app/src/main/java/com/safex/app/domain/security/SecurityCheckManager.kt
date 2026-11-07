package com.safex.app.domain.security

import android.content.Context
import com.safex.app.domain.security.model.SecurityCheckResult
import com.safex.app.domain.security.model.SecurityStatus
import com.safex.app.domain.security.usecase.SecurityCheck
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Manages concurrent execution of security checks with timeouts and error isolation.
 * Each check runs independently and cannot crash the scan.
 */
@Singleton
class SecurityCheckManager @Inject constructor() {
    
    companion object {
        private const val CHECK_TIMEOUT_MS = 3000L // 3 seconds per check
        private const val TOTAL_SCAN_TIMEOUT_MS = 30000L // 30 seconds total
    }
    
    /**
     * Execute all checks concurrently and emit results as they complete.
     * Returns a Flow that emits individual results as they arrive.
     */
    fun executeChecks(
        context: Context,
        checks: List<SecurityCheck>
    ): Flow<SecurityCheckResult> = flow {
        
        withTimeout(TOTAL_SCAN_TIMEOUT_MS) {
            coroutineScope {
                // Launch all checks concurrently
                val deferredResults = checks.map { check ->
                    async(Dispatchers.IO) {
                        executeCheckWithTimeout(context, check)
                    }
                }
                
                // Emit results as they complete
                for (deferred in deferredResults) {
                    try {
                        val result = deferred.await()
                        emit(result)
                    } catch (e: CancellationException) {
                        // Parent scope cancelled, stop processing
                        throw e
                    } catch (e: Exception) {
                        // Individual check failed, skip it
                        // Error already handled in executeCheckWithTimeout
                    }
                }
            }
        }
    }
    
    /**
     * Execute all checks and wait for completion.
     * Returns complete list of results.
     */
    suspend fun executeAllChecks(
        context: Context,
        checks: List<SecurityCheck>
    ): List<SecurityCheckResult> {
        return withContext(Dispatchers.IO) {
            withTimeout(TOTAL_SCAN_TIMEOUT_MS) {
                coroutineScope {
                    checks.map { check ->
                        async {
                            executeCheckWithTimeout(context, check)
                        }
                    }.awaitAll()
                }
            }
        }
    }
    
    /**
     * Execute a single check with timeout and error handling
     */
    private suspend fun executeCheckWithTimeout(
        context: Context,
        check: SecurityCheck
    ): SecurityCheckResult {
        return try {
            withTimeout(CHECK_TIMEOUT_MS) {
                check.execute(context)
            }
        } catch (e: TimeoutCancellationException) {
            // Check timed out
            createTimeoutResult(check)
        } catch (e: CancellationException) {
            // Parent job cancelled
            throw e
        } catch (e: Exception) {
            // Unexpected error
            createErrorResult(check, e)
        }
    }
    
    private fun createTimeoutResult(check: SecurityCheck): SecurityCheckResult {
        return SecurityCheckResult(
            id = check.id,
            title = check.title,
            category = check.category,
            status = SecurityStatus.ERROR,
            riskLevel = com.safex.app.domain.security.model.RiskLevel.NONE,
            detectedValue = null,
            userExplanation = "Check timed out",
            technicalDetails = "Execution exceeded ${CHECK_TIMEOUT_MS}ms timeout",
            whyItMatters = "This check took too long to complete",
            recommendation = "Try rescanning",
            remediation = null,
            limitation = "Check timeout",
            confidence = com.safex.app.domain.security.model.Confidence.LOW,
            detectionMethod = "Timeout",
            evidence = listOf(),
            isSupported = true,
            minApi = check.minApi,
            lastCheckedAt = System.currentTimeMillis(),
            durationMs = CHECK_TIMEOUT_MS
        )
    }
    
    private fun createErrorResult(check: SecurityCheck, error: Exception): SecurityCheckResult {
        return SecurityCheckResult(
            id = check.id,
            title = check.title,
            category = check.category,
            status = SecurityStatus.ERROR,
            riskLevel = com.safex.app.domain.security.model.RiskLevel.NONE,
            detectedValue = null,
            userExplanation = "Check failed unexpectedly",
            technicalDetails = "Error: ${error.message}",
            whyItMatters = "Unable to complete this security check",
            recommendation = "Try rescanning",
            remediation = null,
            limitation = "Unexpected error",
            confidence = com.safex.app.domain.security.model.Confidence.LOW,
            detectionMethod = "Error handler",
            evidence = listOf(),
            isSupported = true,
            minApi = check.minApi,
            lastCheckedAt = System.currentTimeMillis(),
            durationMs = 0
        )
    }
}

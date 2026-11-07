package com.safex.app.domain.security.usecase

import android.content.Context
import com.safex.app.domain.security.model.SecurityCategory
import com.safex.app.domain.security.model.SecurityCheckResult

/**
 * Base interface for all security checks.
 * Each check is independent, testable, and error-isolated.
 */
interface SecurityCheck {
    /** Unique identifier */
    val id: String
    
    /** Display name */
    val title: String
    
    /** Category for organization and scoring */
    val category: SecurityCategory
    
    /** Minimum Android API level required */
    val minApi: Int
    
    /** Whether this check contributes to the score */
    val isScoreable: Boolean
    
    /** Weight within its category (0.0-1.0) */
    val weight: Float
    
    /** Expected secure state for scoring */
    val expectedSecureValue: Boolean
    
    /**
     * Execute the check.
     * Must handle all exceptions internally and return appropriate result.
     * Must complete within timeout (managed by caller).
     */
    suspend fun execute(context: Context): SecurityCheckResult
}

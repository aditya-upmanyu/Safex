package com.safex.app.presentation.security

import com.safex.app.domain.security.SecurityRecommendationEngine
import com.safex.app.domain.security.model.SecurityCategory
import com.safex.app.domain.security.model.SecurityCheckResult
import com.safex.app.domain.security.model.SecurityScore
import com.safex.app.domain.security.model.SecurityStatus

/**
 * Complete UI state for Security Advisor screen
 */
sealed class SecurityAdvisorUiState {
    
    data object Loading : SecurityAdvisorUiState()
    
    data class Success(
        val score: SecurityScore,
        val checks: List<SecurityCheckResult>,
        val recommendations: List<SecurityRecommendationEngine.Recommendation>,
        val lastScanTime: Long,
        val statusFilter: SecurityStatus? = null
    ) : SecurityAdvisorUiState() {
        
        val filteredChecks: List<SecurityCheckResult>
            get() = if (statusFilter != null) {
                checks.filter { it.status == statusFilter }
            } else {
                checks
            }
        
        val checksByCategory: Map<SecurityCategory, List<SecurityCheckResult>>
            get() = filteredChecks.groupBy { it.category }
        
        val criticalCount: Int get() = checks.count { it.status == SecurityStatus.DANGER }
        val warningCount: Int get() = checks.count { it.status == SecurityStatus.WARNING }
        val secureCount: Int get() = checks.count { it.status == SecurityStatus.SECURE }
        val unknownCount: Int get() = checks.count { it.status == SecurityStatus.UNKNOWN }
        
        fun withFilter(status: SecurityStatus?): Success {
            return copy(statusFilter = if (statusFilter == status) null else status)
        }
    }
    
    data class Error(val message: String) : SecurityAdvisorUiState()
}

/**
 * One-time UI events
 */
sealed class SecurityAdvisorEvent {
    data class ShowSnackbar(val message: String) : SecurityAdvisorEvent()
    data class NavigateToSettings(val action: com.safex.app.domain.security.model.RemediationAction) : SecurityAdvisorEvent()
    data class ShowCheckDetail(val check: SecurityCheckResult) : SecurityAdvisorEvent()
}

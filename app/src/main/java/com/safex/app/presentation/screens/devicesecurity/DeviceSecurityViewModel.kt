package com.safex.app.presentation.screens.devicesecurity

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.safex.app.domain.security.SecurityRecommendationEngine
import com.safex.app.domain.security.SecurityRiskScorer
import com.safex.app.domain.security.SecuritySettingsNavigator
import com.safex.app.domain.security.model.RemediationAction
import com.safex.app.domain.security.model.SecurityCheckResult
import com.safex.app.domain.security.model.SecurityStatus
import com.safex.app.domain.security.repository.SecurityAdvisorRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class DeviceSecurityViewModel @Inject constructor(
    private val repository: SecurityAdvisorRepository,
    private val scorer: SecurityRiskScorer,
    private val navigator: SecuritySettingsNavigator
) : ViewModel() {

    private val _uiState = MutableStateFlow<DeviceSecurityUiState>(DeviceSecurityUiState.Loading)
    val uiState: StateFlow<DeviceSecurityUiState> = _uiState.asStateFlow()
    
    private val _events = Channel<DeviceSecurityEvent>(Channel.BUFFERED)
    val events: Flow<DeviceSecurityEvent> = _events.receiveAsFlow()

    init {
        loadSecurityStatus()
    }

    fun loadSecurityStatus() {
        viewModelScope.launch {
            try {
                _uiState.value = DeviceSecurityUiState.Loading
                
                // Try cached results first
                val lastScanTime = repository.getLastScanTimestamp()
                
                if (lastScanTime != null && System.currentTimeMillis() - lastScanTime < 300_000) { // 5 min cache
                    repository.getCachedResults().first().let { cachedResults ->
                        if (cachedResults.isNotEmpty()) {
                            updateUiWithResults(cachedResults, lastScanTime)
                            return@launch
                        }
                    }
                }
                
                // Run fresh scan
                executeScan()
            } catch (e: Exception) {
                _uiState.value = DeviceSecurityUiState.Error("Failed to load security data")
            }
        }
    }
    
    private suspend fun executeScan() {
        try {
            val results = repository.executeScan()
            val timestamp = System.currentTimeMillis()
            updateUiWithResults(results, timestamp)
            _events.send(DeviceSecurityEvent.ScanCompleted)
        } catch (e: Exception) {
            _uiState.value = DeviceSecurityUiState.Error("Scan failed: ${e.message}")
        }
    }
    
    private fun updateUiWithResults(results: List<SecurityCheckResult>, timestamp: Long) {
        val score = scorer.calculateScore(results)
        val recommendations = SecurityRecommendationEngine.generateRecommendations(results)
        
        _uiState.value = DeviceSecurityUiState.Success(
            score = score,
            checks = results,
            recommendations = recommendations,
            lastScanTime = timestamp
        )
    }
    
    fun filterByStatus(status: SecurityStatus?) {
        val currentState = _uiState.value
        if (currentState is DeviceSecurityUiState.Success) {
            _uiState.value = currentState.copy(statusFilter = if (currentState.statusFilter == status) null else status)
        }
    }
    
    fun navigateToSettings(action: RemediationAction) {
        viewModelScope.launch {
            val success = navigator.navigate(action)
            if (!success) {
                _events.send(DeviceSecurityEvent.ShowMessage("Could not open settings"))
            }
        }
    }
    
    fun showCheckDetail(check: SecurityCheckResult) {
        viewModelScope.launch {
            _events.send(DeviceSecurityEvent.ShowCheckDetail(check))
        }
    }
}

sealed class DeviceSecurityUiState {
    data object Loading : DeviceSecurityUiState()
    
    data class Success(
        val score: com.safex.app.domain.security.model.SecurityScore,
        val checks: List<SecurityCheckResult>,
        val recommendations: List<SecurityRecommendationEngine.Recommendation>,
        val lastScanTime: Long,
        val statusFilter: SecurityStatus? = null
    ) : DeviceSecurityUiState() {
        
        val filteredChecks: List<SecurityCheckResult>
            get() = if (statusFilter != null) {
                checks.filter { it.status == statusFilter }
            } else {
                checks
            }
        
        val checksByCategory: Map<com.safex.app.domain.security.model.SecurityCategory, List<SecurityCheckResult>>
            get() = filteredChecks.groupBy { it.category }
        
        val criticalCount: Int get() = checks.count { it.status == SecurityStatus.DANGER }
        val warningCount: Int get() = checks.count { it.status == SecurityStatus.WARNING }
        val secureCount: Int get() = checks.count { it.status == SecurityStatus.SECURE }
        val unknownCount: Int get() = checks.count { it.status == SecurityStatus.UNKNOWN }
    }
    
    data class Error(val message: String) : DeviceSecurityUiState()
}

sealed class DeviceSecurityEvent {
    data object ScanCompleted : DeviceSecurityEvent()
    data class ShowMessage(val message: String) : DeviceSecurityEvent()
    data class ShowCheckDetail(val check: SecurityCheckResult) : DeviceSecurityEvent()
}

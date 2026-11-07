package com.safex.app.presentation.security

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
class SecurityAdvisorViewModel @Inject constructor(
    private val repository: SecurityAdvisorRepository,
    private val scorer: SecurityRiskScorer,
    private val navigator: SecuritySettingsNavigator
) : ViewModel() {
    
    private val _uiState = MutableStateFlow<SecurityAdvisorUiState>(SecurityAdvisorUiState.Loading)
    val uiState: StateFlow<SecurityAdvisorUiState> = _uiState.asStateFlow()
    
    private val _events = Channel<SecurityAdvisorEvent>(Channel.BUFFERED)
    val events: Flow<SecurityAdvisorEvent> = _events.receiveAsFlow()
    
    init {
        loadCachedData()
    }
    
    private fun loadCachedData() {
        viewModelScope.launch {
            try {
                // Try to load cached results first
                val lastScanTime = repository.getLastScanTimestamp()
                
                if (lastScanTime != null) {
                    repository.getCachedResults().first().let { cachedResults ->
                        if (cachedResults.isNotEmpty()) {
                            updateUiWithResults(cachedResults, lastScanTime)
                        } else {
                            // No cache, run fresh scan
                            executeScan()
                        }
                    }
                } else {
                    // No previous scan, run fresh scan
                    executeScan()
                }
            } catch (e: Exception) {
                _uiState.value = SecurityAdvisorUiState.Error(
                    message = "Failed to load security data: ${e.message}"
                )
            }
        }
    }
    
    fun executeScan() {
        viewModelScope.launch {
            try {
                _uiState.value = SecurityAdvisorUiState.Loading
                
                val results = repository.executeScan()
                val timestamp = System.currentTimeMillis()
                
                updateUiWithResults(results, timestamp)
                
                _events.send(SecurityAdvisorEvent.ShowSnackbar("Scan completed"))
            } catch (e: Exception) {
                _uiState.value = SecurityAdvisorUiState.Error(
                    message = "Scan failed: ${e.message}"
                )
                _events.send(SecurityAdvisorEvent.ShowSnackbar("Scan failed"))
            }
        }
    }
    
    private fun updateUiWithResults(results: List<SecurityCheckResult>, timestamp: Long) {
        val score = scorer.calculateScore(results)
        val recommendations = SecurityRecommendationEngine.generateRecommendations(results)
        
        _uiState.value = SecurityAdvisorUiState.Success(
            score = score,
            checks = results,
            recommendations = recommendations,
            lastScanTime = timestamp
        )
    }
    
    fun filterByStatus(status: SecurityStatus?) {
        val currentState = _uiState.value
        if (currentState is SecurityAdvisorUiState.Success) {
            _uiState.value = currentState.withFilter(status)
        }
    }
    
    fun navigateToSettings(action: RemediationAction) {
        viewModelScope.launch {
            val success = navigator.navigate(action)
            if (!success) {
                _events.send(SecurityAdvisorEvent.ShowSnackbar("Could not open settings"))
            }
        }
    }
    
    fun showCheckDetail(check: SecurityCheckResult) {
        viewModelScope.launch {
            _events.send(SecurityAdvisorEvent.ShowCheckDetail(check))
        }
    }
    
    fun rescanCheck(checkId: String) {
        // Future enhancement: rescan individual check
        executeScan()
    }
}

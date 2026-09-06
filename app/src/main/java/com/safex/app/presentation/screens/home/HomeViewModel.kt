package com.safex.app.presentation.screens.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.safex.app.domain.model.CallRecord
import com.safex.app.domain.repository.CallRepository
import com.safex.app.domain.usecase.GetSecurityStatusUseCase
import com.safex.app.util.UiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class HomeUiData(
    val securityScore: Int = 0,
    val recentAlerts: List<CallRecord> = emptyList(),
    val protectionActive: Boolean = false
)

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val getSecurityStatusUseCase: GetSecurityStatusUseCase,
    private val callRepository: CallRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<UiState<HomeUiData>>(UiState.Loading)
    val uiState: StateFlow<UiState<HomeUiData>> = _uiState.asStateFlow()

    init {
        loadHomeData()
    }

    fun loadHomeData() {
        viewModelScope.launch {
            _uiState.value = UiState.Loading

            try {
                val securityStatus = getSecurityStatusUseCase()
                
                callRepository.getFraudCalls()
                    .map { fraudCalls ->
                        HomeUiData(
                            securityScore = securityStatus.overallScore,
                            recentAlerts = fraudCalls.take(5),
                            protectionActive = true
                        )
                    }
                    .collect { data ->
                        _uiState.value = UiState.Success(data)
                    }
            } catch (e: Exception) {
                _uiState.value = UiState.Error(e.message ?: "Failed to load home data")
            }
        }
    }
}

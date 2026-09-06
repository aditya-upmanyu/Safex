package com.safex.app.presentation.screens.devicesecurity

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.safex.app.domain.model.SecurityStatus
import com.safex.app.domain.usecase.GetSecurityStatusUseCase
import com.safex.app.util.UiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class DeviceSecurityViewModel @Inject constructor(
    private val getSecurityStatusUseCase: GetSecurityStatusUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow<UiState<SecurityStatus>>(UiState.Loading)
    val uiState: StateFlow<UiState<SecurityStatus>> = _uiState.asStateFlow()

    init {
        loadSecurityStatus()
    }

    fun loadSecurityStatus() {
        viewModelScope.launch {
            _uiState.value = UiState.Loading
            try {
                val status = getSecurityStatusUseCase()
                _uiState.value = UiState.Success(status)
            } catch (e: Exception) {
                _uiState.value = UiState.Error(e.message ?: "Failed to check security")
            }
        }
    }
}

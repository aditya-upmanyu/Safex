package com.safex.app.presentation.screens.urlscanner

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.safex.app.domain.model.ScanResult
import com.safex.app.domain.usecase.ScanUrlUseCase
import com.safex.app.util.UiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class UrlScannerViewModel @Inject constructor(
    private val scanUrlUseCase: ScanUrlUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow<UiState<ScanResult>>(UiState.Empty)
    val uiState: StateFlow<UiState<ScanResult>> = _uiState.asStateFlow()

    fun scanUrl(url: String) {
        viewModelScope.launch {
            _uiState.value = UiState.Loading

            val result = scanUrlUseCase(url)
            
            _uiState.value = if (result.isSuccess) {
                UiState.Success(result.getOrNull()!!)
            } else {
                UiState.Error(result.exceptionOrNull()?.message ?: "Scan failed")
            }
        }
    }

    fun resetState() {
        _uiState.value = UiState.Empty
    }
}

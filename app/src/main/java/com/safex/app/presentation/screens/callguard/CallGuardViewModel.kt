package com.safex.app.presentation.screens.callguard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.safex.app.domain.model.CallRecord
import com.safex.app.domain.usecase.GetCallHistoryUseCase
import com.safex.app.util.UiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class CallGuardViewModel @Inject constructor(
    private val getCallHistoryUseCase: GetCallHistoryUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow<UiState<List<CallRecord>>>(UiState.Loading)
    val uiState: StateFlow<UiState<List<CallRecord>>> = _uiState.asStateFlow()

    init {
        loadCalls()
    }

    fun loadCalls() {
        viewModelScope.launch {
            _uiState.value = UiState.Loading
            try {
                getCallHistoryUseCase(20).collect { calls ->
                    _uiState.value = if (calls.isEmpty()) {
                        UiState.Empty
                    } else {
                        UiState.Success(calls)
                    }
                }
            } catch (e: Exception) {
                _uiState.value = UiState.Error(e.message ?: "Failed to load calls")
            }
        }
    }
}

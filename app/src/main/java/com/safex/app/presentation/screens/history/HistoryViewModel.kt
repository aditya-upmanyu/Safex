package com.safex.app.presentation.screens.history

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
class HistoryViewModel @Inject constructor(
    private val getCallHistoryUseCase: GetCallHistoryUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow<UiState<List<CallRecord>>>(UiState.Loading)
    val uiState: StateFlow<UiState<List<CallRecord>>> = _uiState.asStateFlow()

    init {
        loadHistory()
    }

    fun loadHistory() {
        viewModelScope.launch {
            _uiState.value = UiState.Loading
            try {
                getCallHistoryUseCase(50).collect { history ->
                    _uiState.value = if (history.isEmpty()) {
                        UiState.Empty
                    } else {
                        UiState.Success(history)
                    }
                }
            } catch (e: Exception) {
                _uiState.value = UiState.Error(e.message ?: "Failed to load history")
            }
        }
    }
}

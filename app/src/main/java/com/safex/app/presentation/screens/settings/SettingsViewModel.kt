package com.safex.app.presentation.screens.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.safex.app.data.datastore.SettingsDataStore
import com.safex.app.presentation.theme.ThemeMode
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val settingsDataStore: SettingsDataStore
) : ViewModel() {

    val themeMode: StateFlow<ThemeMode> = settingsDataStore.themeMode
        .stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5000),
            ThemeMode.SYSTEM
        )

    val emergencyContact: StateFlow<String> = settingsDataStore.emergencyContact
        .stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5000),
            ""
        )

    val riskThreshold: StateFlow<Int> = settingsDataStore.riskThreshold
        .stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5000),
            70
        )

    fun setThemeMode(mode: ThemeMode) {
        viewModelScope.launch {
            settingsDataStore.setThemeMode(mode)
        }
    }

    fun setEmergencyContact(contact: String) {
        viewModelScope.launch {
            settingsDataStore.setEmergencyContact(contact)
        }
    }

    fun setRiskThreshold(threshold: Int) {
        viewModelScope.launch {
            settingsDataStore.setRiskThreshold(threshold)
        }
    }
}

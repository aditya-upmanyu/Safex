package com.safex.app.presentation.screens.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.safex.app.data.datastore.SettingsDataStore
import com.safex.app.domain.chatbot.usecase.DeleteChatsUseCase
import com.safex.app.presentation.theme.ThemeMode
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val settingsDataStore: SettingsDataStore,
    private val deleteChatsUseCase: DeleteChatsUseCase
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

    val chatLanguage: StateFlow<String> = settingsDataStore.chatLanguage
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), "en")

    val chatTtsEnabled: StateFlow<Boolean> = settingsDataStore.chatTtsEnabled
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)

    val chatSpeechRate: StateFlow<Float> = settingsDataStore.chatSpeechRate
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0.9f)

    val chatAiEnabled: StateFlow<Boolean> = settingsDataStore.chatAiEnabled
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), true)

    val chatElderMode: StateFlow<Boolean> = settingsDataStore.chatElderMode
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)

    val chatAutoDeleteDays: StateFlow<Int> = settingsDataStore.chatAutoDeleteDays
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

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

    fun setChatLanguage(lang: String) {
        viewModelScope.launch {
            settingsDataStore.setChatLanguage(lang)
        }
    }

    fun setChatTtsEnabled(enabled: Boolean) {
        viewModelScope.launch {
            settingsDataStore.setChatTtsEnabled(enabled)
        }
    }

    fun setChatSpeechRate(rate: Float) {
        viewModelScope.launch {
            settingsDataStore.setChatSpeechRate(rate)
        }
    }

    fun setChatAiEnabled(enabled: Boolean) {
        viewModelScope.launch {
            settingsDataStore.setChatAiEnabled(enabled)
        }
    }

    fun setChatElderMode(enabled: Boolean) {
        viewModelScope.launch {
            settingsDataStore.setChatElderMode(enabled)
        }
    }

    fun setChatAutoDeleteDays(days: Int) {
        viewModelScope.launch {
            settingsDataStore.setChatAutoDeleteDays(days)
        }
    }

    fun deleteAllChats() {
        viewModelScope.launch {
            deleteChatsUseCase.deleteAll()
        }
    }
}

package com.safex.app.presentation.screens.defense

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.safex.app.data.local.dao.GuardianDao
import com.safex.app.data.local.entity.GuardianEntity
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class FamilyGuardianViewModel @Inject constructor(
    private val guardianDao: GuardianDao
) : ViewModel() {

    val guardians: StateFlow<List<GuardianEntity>> = guardianDao.getAllGuardians()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _uiMessage = MutableStateFlow<String?>(null)
    val uiMessage: StateFlow<String?> = _uiMessage.asStateFlow()

    fun addGuardian(name: String, phone: String, relationship: String) {
        if (name.isBlank() || phone.isBlank()) {
            _uiMessage.value = "Name and phone number are required."
            return
        }
        viewModelScope.launch {
            guardianDao.insertGuardian(
                GuardianEntity(
                    guardianName = name.trim(),
                    phoneNumber = phone.trim(),
                    relationship = relationship.trim().ifBlank { "Family" }
                )
            )
            _uiMessage.value = "Guardian added."
        }
    }

    fun deleteGuardian(guardian: GuardianEntity) {
        viewModelScope.launch {
            guardianDao.deleteGuardian(guardian)
            _uiMessage.value = "Guardian removed."
        }
    }

    fun toggleGuardian(guardian: GuardianEntity, enabled: Boolean) {
        viewModelScope.launch {
            guardianDao.updateGuardian(guardian.copy(isEnabled = enabled, updatedAt = System.currentTimeMillis()))
        }
    }

    fun setPrimary(guardian: GuardianEntity) {
        viewModelScope.launch {
            guardianDao.clearPrimaryGuardian()
            guardianDao.setPrimaryGuardian(guardian.id)
            _uiMessage.value = "${guardian.guardianName} set as primary contact."
        }
    }

    fun clearMessage() { _uiMessage.value = null }
}

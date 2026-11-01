package com.safex.app.presentation.emergency

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class EmergencySOSViewModel @Inject constructor(
    @ApplicationContext private val context: Context
) : ViewModel() {

    private val _uiState = MutableStateFlow(EmergencySOSUiState())
    val uiState: StateFlow<EmergencySOSUiState> = _uiState.asStateFlow()

    init {
        loadEmergencyContacts()
    }

    private fun loadEmergencyContacts() {
        // Load from preferences or database
        _uiState.value = _uiState.value.copy(
            emergencyContacts = listOf(
                EmergencyContact("Police (Cyber Crime)", "1930", "Authority"),
                EmergencyContact("National Helpline", "155260", "Authority")
            )
        )
    }

    fun triggerEmergencySOS() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isSending = true)

            // Simulate sending emergency alert
            delay(2000)

            // Send SMS to emergency contacts
            sendEmergencySMS()

            // Call emergency numbers (if configured)
            callEmergencyNumbers()

            // Log incident
            logEmergencyIncident()

            _uiState.value = _uiState.value.copy(
                isSending = false,
                alertSent = true
            )
        }
    }

    private fun sendEmergencySMS() {
        val message = "EMERGENCY ALERT: I am experiencing a fraud call. Please call me immediately. - SAFEx App"
        
        _uiState.value.emergencyContacts.forEach { contact ->
            try {
                val smsIntent = Intent(Intent.ACTION_VIEW).apply {
                    data = Uri.parse("sms:${contact.phoneNumber}")
                    putExtra("sms_body", message)
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                }
                context.startActivity(smsIntent)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    private fun callEmergencyNumbers() {
        // Option to auto-call emergency number
        val primaryContact = _uiState.value.emergencyContacts.firstOrNull()
        primaryContact?.let {
            try {
                val callIntent = Intent(Intent.ACTION_DIAL).apply {
                    data = Uri.parse("tel:${it.phoneNumber}")
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                }
                context.startActivity(callIntent)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    private fun logEmergencyIncident() {
        // Log to database for future reference
        viewModelScope.launch {
            // TODO: Save emergency incident to database
        }
    }

    fun addEmergencyContact() {
        // TODO: Show contact picker dialog
    }

    fun removeEmergencyContact(contact: EmergencyContact) {
        val updatedContacts = _uiState.value.emergencyContacts.filter { it != contact }
        _uiState.value = _uiState.value.copy(emergencyContacts = updatedContacts)
    }

    fun quickReport() {
        // Open cybercrime reporting website
        try {
            val intent = Intent(Intent.ACTION_VIEW).apply {
                data = Uri.parse("https://cybercrime.gov.in")
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(intent)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun dismissAlert() {
        _uiState.value = _uiState.value.copy(alertSent = false)
    }
}

data class EmergencySOSUiState(
    val isSending: Boolean = false,
    val alertSent: Boolean = false,
    val emergencyContacts: List<EmergencyContact> = emptyList()
)

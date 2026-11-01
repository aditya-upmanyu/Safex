package com.safex.app.presentation.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.safex.app.data.datastore.SettingsDataStore
import com.safex.app.domain.repository.CallRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject

@HiltViewModel
class EnhancedHomeViewModel @Inject constructor(
    private val callRepository: CallRepository,
    private val settingsDataStore: SettingsDataStore
) : ViewModel() {

    private val _uiState = MutableStateFlow(EnhancedHomeUiState())
    val uiState: StateFlow<EnhancedHomeUiState> = _uiState.asStateFlow()

    init {
        loadHomeData()
        loadFraudTips()
    }

    private fun loadHomeData() {
        viewModelScope.launch {
            callRepository.getAllCalls().collect { calls ->
                val today = Calendar.getInstance()
                today.set(Calendar.HOUR_OF_DAY, 0)
                today.set(Calendar.MINUTE, 0)
                today.set(Calendar.SECOND, 0)

                val todayCalls = calls.filter { it.timestamp >= today.timeInMillis }
                val fraudBlocked = todayCalls.count { it.riskScore >= 70 }
                val safeCalls = todayCalls.count { it.riskScore < 40 }

                // Calculate protection score
                val totalCalls = calls.size
                val totalFraud = calls.count { it.riskScore >= 70 }
                val protectionScore = if (totalCalls > 0) {
                    ((totalCalls - totalFraud).toFloat() / totalCalls * 100).toInt()
                } else 100

                // Get recent alerts
                val recentAlerts = calls
                    .filter { it.riskScore >= 70 }
                    .sortedByDescending { it.timestamp }
                    .take(5)
                    .map { call ->
                        HomeAlert(
                            title = call.detectedPatterns.firstOrNull() ?: "Fraud Detected",
                            description = "Call from ${maskPhoneNumber(call.phoneNumber)}",
                            timestamp = formatTimestamp(call.timestamp),
                            severity = when {
                                call.riskScore >= 85 -> "HIGH"
                                call.riskScore >= 70 -> "MEDIUM"
                                else -> "LOW"
                            }
                        )
                    }

                // Check notification count
                val notificationCount = calls.count { 
                    it.riskScore >= 70 && 
                    it.timestamp >= (System.currentTimeMillis() - 24 * 60 * 60 * 1000) 
                }

                _uiState.value = _uiState.value.copy(
                    isProtectionActive = true,
                    protectionScore = protectionScore,
                    todayCalls = todayCalls.size,
                    fraudBlocked = fraudBlocked,
                    safeCalls = safeCalls,
                    recentAlerts = recentAlerts,
                    notificationCount = notificationCount
                )
            }
        }
    }

    private fun loadFraudTips() {
        val tips = listOf(
            "Never share your OTP or PIN with anyone, not even bank officials",
            "Government agencies never ask for money over phone calls",
            "Be cautious of calls claiming urgent legal action or digital arrest",
            "Verify caller identity before sharing any personal information",
            "Don't click on suspicious links sent via SMS or WhatsApp",
            "Banks never ask for your password or CVV over phone",
            "Be wary of lottery or prize winning calls you didn't enter",
            "Report suspicious calls to cybercrime.gov.in immediately"
        )

        _uiState.value = _uiState.value.copy(fraudTips = tips)
    }

    fun toggleProtection() {
        viewModelScope.launch {
            val newState = !_uiState.value.isProtectionActive
            _uiState.value = _uiState.value.copy(isProtectionActive = newState)
            
            // Save to settings
            settingsDataStore.setCallGuardEnabled(newState)
        }
    }

    private fun maskPhoneNumber(phoneNumber: String): String {
        return if (phoneNumber.length > 4) {
            "****" + phoneNumber.takeLast(4)
        } else {
            phoneNumber
        }
    }

    private fun formatTimestamp(timestamp: Long): String {
        val now = System.currentTimeMillis()
        val diff = now - timestamp

        return when {
            diff < 60 * 1000 -> "Just now"
            diff < 60 * 60 * 1000 -> "${diff / (60 * 1000)} min ago"
            diff < 24 * 60 * 60 * 1000 -> "${diff / (60 * 60 * 1000)} hours ago"
            else -> {
                val dateFormat = SimpleDateFormat("MMM dd", Locale.getDefault())
                dateFormat.format(Date(timestamp))
            }
        }
    }
}

data class EnhancedHomeUiState(
    val isProtectionActive: Boolean = true,
    val protectionScore: Int = 100,
    val todayCalls: Int = 0,
    val fraudBlocked: Int = 0,
    val safeCalls: Int = 0,
    val recentAlerts: List<HomeAlert> = emptyList(),
    val fraudTips: List<String> = emptyList(),
    val notificationCount: Int = 0
)

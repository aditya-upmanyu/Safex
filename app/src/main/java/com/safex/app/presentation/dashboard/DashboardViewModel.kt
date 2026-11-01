package com.safex.app.presentation.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
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
class DashboardViewModel @Inject constructor(
    private val callRepository: CallRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(DashboardUiState())
    val uiState: StateFlow<DashboardUiState> = _uiState.asStateFlow()

    init {
        loadDashboardData()
    }

    private fun loadDashboardData() {
        viewModelScope.launch {
            // Load call records from database
            callRepository.getAllCalls().collect { calls ->
                val today = Calendar.getInstance()
                today.set(Calendar.HOUR_OF_DAY, 0)
                today.set(Calendar.MINUTE, 0)
                today.set(Calendar.SECOND, 0)
                
                val todayCalls = calls.filter { it.timestamp >= today.timeInMillis }
                val fraudCalls = todayCalls.filter { it.riskScore >= 70 }
                val safeCalls = todayCalls.filter { it.riskScore < 40 }

                // Calculate protection rate
                val protectionRate = if (todayCalls.isNotEmpty()) {
                    ((fraudCalls.size.toFloat() / todayCalls.size.toFloat()) * 100).toInt()
                } else 100

                // Get recent alerts
                val recentAlerts = calls
                    .sortedByDescending { it.timestamp }
                    .take(10)
                    .map { call ->
                        FraudAlert(
                            type = call.detectedPatterns.firstOrNull() ?: "Unknown",
                            phoneNumber = maskPhoneNumber(call.phoneNumber),
                            timestamp = formatTimestamp(call.timestamp),
                            riskScore = call.riskScore,
                            severity = when {
                                call.riskScore >= 70 -> AlertSeverity.HIGH
                                call.riskScore >= 40 -> AlertSeverity.MEDIUM
                                else -> AlertSeverity.LOW
                            }
                        )
                    }

                // Calculate weekly trends
                val weeklyTrends = calculateWeeklyTrends(calls)

                _uiState.value = _uiState.value.copy(
                    isProtectionActive = true,
                    statistics = DashboardStats(
                        callsAnalyzed = todayCalls.size,
                        fraudDetected = fraudCalls.size,
                        safeCalls = safeCalls.size,
                        protectionRate = protectionRate
                    ),
                    recentAlerts = recentAlerts,
                    weeklyTrends = weeklyTrends
                )
            }
        }
    }

    private fun calculateWeeklyTrends(calls: List<com.safex.app.domain.model.CallRecord>): List<TrendData> {
        val calendar = Calendar.getInstance()
        val dayFormat = SimpleDateFormat("EEE", Locale.getDefault())
        val trends = mutableListOf<TrendData>()

        for (i in 6 downTo 0) {
            calendar.time = Date()
            calendar.add(Calendar.DAY_OF_YEAR, -i)
            calendar.set(Calendar.HOUR_OF_DAY, 0)
            calendar.set(Calendar.MINUTE, 0)
            calendar.set(Calendar.SECOND, 0)
            
            val dayStart = calendar.timeInMillis
            calendar.add(Calendar.DAY_OF_YEAR, 1)
            val dayEnd = calendar.timeInMillis

            val dayFraudCount = calls.count { 
                it.timestamp >= dayStart && it.timestamp < dayEnd && it.riskScore >= 70 
            }

            calendar.time = Date()
            calendar.add(Calendar.DAY_OF_YEAR, -i)
            
            trends.add(
                TrendData(
                    day = dayFormat.format(calendar.time),
                    value = dayFraudCount
                )
            )
        }

        return trends
    }

    private fun maskPhoneNumber(phoneNumber: String): String {
        return if (phoneNumber.length > 4) {
            "****" + phoneNumber.takeLast(4)
        } else {
            phoneNumber
        }
    }

    private fun formatTimestamp(timestamp: Long): String {
        val dateFormat = SimpleDateFormat("MMM dd, HH:mm", Locale.getDefault())
        return dateFormat.format(Date(timestamp))
    }

    fun refreshData() {
        loadDashboardData()
    }

    fun triggerEmergency() {
        // TODO: Implement emergency SOS
        viewModelScope.launch {
            // Send emergency alert to authorities
        }
    }

    fun reportFraud() {
        // TODO: Navigate to report screen
    }

    fun shareAlert() {
        // TODO: Implement share functionality
    }

    fun learnMore() {
        // TODO: Navigate to fraud education screen
    }
}

data class DashboardUiState(
    val isProtectionActive: Boolean = true,
    val statistics: DashboardStats = DashboardStats(),
    val recentAlerts: List<FraudAlert> = emptyList(),
    val weeklyTrends: List<TrendData> = emptyList(),
    val isLoading: Boolean = false
)

package com.safex.app.presentation.contactverification

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.safex.app.domain.repository.CallRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject
import kotlin.random.Random

@HiltViewModel
class ContactVerificationViewModel @Inject constructor(
    private val callRepository: CallRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(ContactVerificationUiState())
    val uiState: StateFlow<ContactVerificationUiState> = _uiState.asStateFlow()

    init {
        loadRecentVerifications()
    }

    fun verifyContact(phoneNumber: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isVerifying = true)

            // Simulate API call delay
            delay(1500)

            // Check against local database first
            val localResult = checkLocalDatabase(phoneNumber)

            // Simulate online verification
            val result = if (localResult != null) {
                localResult
            } else {
                simulateOnlineVerification(phoneNumber)
            }

            // Save to recent verifications
            val recentVerification = RecentVerification(
                phoneNumber = phoneNumber,
                trustLevel = result.trustLevel,
                timestamp = "Just now"
            )

            val updatedRecent = listOf(recentVerification) + _uiState.value.recentVerifications

            _uiState.value = _uiState.value.copy(
                isVerifying = false,
                verificationResult = result,
                recentVerifications = updatedRecent
            )
        }
    }

    private suspend fun checkLocalDatabase(phoneNumber: String): VerificationResult? {
        // Check if number exists in our fraud database
        val allCalls = callRepository.getAllCalls().first()
        val fraudCalls = allCalls.filter { it.phoneNumber == phoneNumber && it.riskScore >= 70 }

        if (fraudCalls.isNotEmpty()) {
            val avgScore = fraudCalls.map { it.riskScore }.average().toInt()
            val lastCall = fraudCalls.maxByOrNull { it.timestamp }

            return VerificationResult(
                phoneNumber = phoneNumber,
                trustLevel = when {
                    avgScore >= 85 -> TrustLevel.DANGEROUS
                    avgScore >= 60 -> TrustLevel.SUSPICIOUS
                    else -> TrustLevel.UNKNOWN
                },
                trustScore = 100 - avgScore,
                reportCount = fraudCalls.size,
                category = fraudCalls.firstOrNull()?.detectedPatterns?.firstOrNull() ?: "Unknown",
                lastReported = lastCall?.timestamp?.let { formatTimestamp(it) },
                recommendation = "This number has been flagged for fraud in your call history. Exercise extreme caution."
            )
        }

        return null
    }

    private fun simulateOnlineVerification(phoneNumber: String): VerificationResult {
        // Simulate online verification with crowdsourced data
        val random = Random.Default
        val reportCount = random.nextInt(0, 50)
        val trustScore = when {
            reportCount > 20 -> random.nextInt(10, 30)
            reportCount > 10 -> random.nextInt(30, 60)
            reportCount > 5 -> random.nextInt(60, 80)
            else -> random.nextInt(80, 100)
        }

        val trustLevel = when {
            trustScore >= 80 -> TrustLevel.SAFE
            trustScore >= 60 -> TrustLevel.UNKNOWN
            trustScore >= 40 -> TrustLevel.SUSPICIOUS
            else -> TrustLevel.DANGEROUS
        }

        val categories = listOf(
            "Telemarketing",
            "Digital Arrest Scam",
            "Lottery Scam",
            "Banking Fraud",
            "Tech Support Scam",
            "Investment Fraud",
            "Unknown"
        )

        val category = if (reportCount > 0) {
            categories.random()
        } else {
            ""
        }

        val recommendation = when (trustLevel) {
            TrustLevel.SAFE -> "This number appears to be legitimate based on community reports."
            TrustLevel.UNKNOWN -> "Limited information available. Answer with caution."
            TrustLevel.SUSPICIOUS -> "This number has some negative reports. Be cautious when answering."
            TrustLevel.DANGEROUS -> "WARNING: Multiple fraud reports associated with this number. DO NOT answer or engage."
        }

        return VerificationResult(
            phoneNumber = phoneNumber,
            trustLevel = trustLevel,
            trustScore = trustScore,
            reportCount = reportCount,
            category = category,
            lastReported = if (reportCount > 0) {
                val daysAgo = random.nextInt(1, 30)
                "$daysAgo days ago"
            } else null,
            recommendation = recommendation
        )
    }

    private fun loadRecentVerifications() {
        // Load from local storage/database
        // For now, empty list
    }

    private fun formatTimestamp(timestamp: Long): String {
        val dateFormat = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
        return dateFormat.format(Date(timestamp))
    }
}

data class ContactVerificationUiState(
    val isVerifying: Boolean = false,
    val verificationResult: VerificationResult? = null,
    val recentVerifications: List<RecentVerification> = emptyList()
)

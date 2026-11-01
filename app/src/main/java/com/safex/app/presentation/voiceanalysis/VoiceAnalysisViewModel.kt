package com.safex.app.presentation.voiceanalysis

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
import kotlin.math.sin
import kotlin.random.Random

@HiltViewModel
class VoiceAnalysisViewModel @Inject constructor(
    private val callRepository: CallRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(VoiceAnalysisUiState())
    val uiState: StateFlow<VoiceAnalysisUiState> = _uiState.asStateFlow()

    fun loadCallAnalysis(callId: Long) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)

            callRepository.getCallById(callId)?.let { call ->
                // Generate waveform data (simulated)
                val waveformData = generateWaveformData()

                _uiState.value = VoiceAnalysisUiState(
                    isLoading = false,
                    phoneNumber = maskPhoneNumber(call.phoneNumber),
                    duration = formatDuration(call.duration),
                    timestamp = formatTimestamp(call.timestamp),
                    riskScore = call.riskScore,
                    verdict = call.verdict.name,
                    confidence = call.confidence,
                    detectedPatterns = call.detectedPatterns,
                    keywords = call.keywords,
                    transcript = call.transcript,
                    waveformData = waveformData,
                    isAnalyzing = false
                )
            }
        }
    }

    private fun generateWaveformData(): List<Float> {
        // Generate realistic waveform data
        val data = mutableListOf<Float>()
        var amplitude = 0.3f
        
        for (i in 0 until 100) {
            // Simulate varying amplitude
            amplitude += Random.nextFloat() * 0.1f - 0.05f
            amplitude = amplitude.coerceIn(0.2f, 0.9f)
            
            // Generate wave value
            val value = amplitude * sin(i * 0.2)
            data.add(value.toFloat())
        }
        
        return data
    }

    private fun maskPhoneNumber(phoneNumber: String): String {
        return if (phoneNumber.length > 4) {
            "****" + phoneNumber.takeLast(4)
        } else {
            phoneNumber
        }
    }

    private fun formatDuration(durationMs: Long): String {
        val seconds = (durationMs / 1000).toInt()
        val minutes = seconds / 60
        val remainingSeconds = seconds % 60
        return String.format("%02d:%02d", minutes, remainingSeconds)
    }

    private fun formatTimestamp(timestamp: Long): String {
        val dateFormat = SimpleDateFormat("MMM dd, yyyy HH:mm", Locale.getDefault())
        return dateFormat.format(Date(timestamp))
    }
}

data class VoiceAnalysisUiState(
    val isLoading: Boolean = true,
    val phoneNumber: String = "",
    val duration: String = "",
    val timestamp: String = "",
    val riskScore: Int = 0,
    val verdict: String = "",
    val confidence: Float = 0f,
    val detectedPatterns: List<String> = emptyList(),
    val keywords: List<String> = emptyList(),
    val transcript: String = "",
    val waveformData: List<Float> = emptyList(),
    val isAnalyzing: Boolean = false
)

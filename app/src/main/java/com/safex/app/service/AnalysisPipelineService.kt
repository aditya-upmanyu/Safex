package com.safex.app.service

import android.app.Service
import android.content.Intent
import android.os.IBinder
import android.util.Log
import com.safex.app.data.datastore.SettingsDataStore
import com.safex.app.domain.model.CallRecord
import com.safex.app.domain.usecase.AnalyzeCallUseCase
import com.safex.app.domain.usecase.OfflineFraudAnalysisUseCase
import com.safex.app.domain.usecase.TranscribeAudioUseCase
import com.safex.app.domain.repository.CallRepository
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.*
import javax.inject.Inject
import java.io.File

@AndroidEntryPoint
class AnalysisPipelineService : Service() {

    @Inject
    lateinit var analyzeCallUseCase: AnalyzeCallUseCase

    @Inject
    lateinit var offlineFraudAnalysisUseCase: OfflineFraudAnalysisUseCase

    @Inject
    lateinit var transcribeAudioUseCase: TranscribeAudioUseCase

    @Inject
    lateinit var callRepository: CallRepository

    @Inject
    lateinit var settingsDataStore: SettingsDataStore

    private val TAG = "AnalysisPipelineService"
    private val serviceScope = CoroutineScope(Dispatchers.IO + SupervisorJob())

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        serviceScope.launch {
            try {
                val recordingFile = intent?.getStringExtra("RECORDING_FILE")
                val callDuration = intent?.getLongExtra("CALL_DURATION", 0L) ?: 0L
                val phoneNumber = intent?.getStringExtra("PHONE_NUMBER") ?: "Unknown"
                
                delay(1000) // Wait for recording to finalize
                performAnalysis(recordingFile, callDuration, phoneNumber)
            } catch (e: Exception) {
                Log.e(TAG, "Analysis failed", e)
            } finally {
                stopSelf()
            }
        }
        return START_NOT_STICKY
    }

    private suspend fun performAnalysis(recordingFile: String?, callDuration: Long, phoneNumber: String) {
        Log.d(TAG, "Starting analysis pipeline for recording: $recordingFile")

        try {
            var transcript = "Test call analysis"
            
            // Try to transcribe the audio file
            if (recordingFile != null && File(recordingFile).exists()) {
                val transcriptionResult = transcribeAudioUseCase(recordingFile)
                transcript = transcriptionResult.getOrNull() ?: transcript
                Log.d(TAG, "Transcription result: $transcript")
            }

            // OFFLINE MODE: Perform offline fraud analysis first (faster, no API)
            Log.d(TAG, "🔍 [OFFLINE MODE] Starting keyword-based fraud detection...")
            val offlineResult = offlineFraudAnalysisUseCase(transcript)
            var finalAnalysis: com.safex.app.domain.model.FraudAnalysis?
            var analysisMode = "OFFLINE"

            if (offlineResult.isSuccess) {
                finalAnalysis = offlineResult.getOrNull()
                Log.d(TAG, "✅ [OFFLINE] Analysis complete - Score: ${finalAnalysis?.riskScore}, Verdict: ${finalAnalysis?.verdict}")
                
                // ONLINE MODE: If offline score < 70 AND API key configured, try OpenAI for better accuracy
                if (finalAnalysis != null && finalAnalysis.riskScore < 70) {
                    val openAIKey = com.safex.app.BuildConfig.OPENAI_API_KEY
                    if (openAIKey.isNotBlank()) {
                        Log.d(TAG, "🌐 [ONLINE MODE] Attempting OpenAI enhancement for better accuracy...")
                        try {
                            val onlineResult = withTimeoutOrNull(30000) {
                                analyzeCallUseCase(transcript)
                            }
                            
                            if (onlineResult != null && onlineResult.isSuccess) {
                                finalAnalysis = onlineResult.getOrNull()!!
                                analysisMode = "ONLINE"
                                Log.d(TAG, "✅ [ONLINE] Enhanced analysis complete - Score: ${finalAnalysis.riskScore}, Verdict: ${finalAnalysis.verdict}")
                            } else {
                                Log.w(TAG, "⚠️ [ONLINE] API analysis failed, using offline results")
                            }
                        } catch (e: TimeoutCancellationException) {
                            Log.w(TAG, "⏱️ [ONLINE] API call timeout, using offline results")
                        } catch (e: Exception) {
                            Log.w(TAG, "⚠️ [ONLINE] API call failed, using offline results: ${e.message}")
                        }
                    } else {
                        Log.d(TAG, "ℹ️ [OFFLINE] No OpenAI API key configured, using offline analysis only")
                    }
                }
            } else {
                Log.e(TAG, "❌ Offline analysis failed: ${offlineResult.exceptionOrNull()?.message}")
                return
            }

            // Save analysis result
            if (finalAnalysis != null) {
                val callRecord = CallRecord(
                    phoneNumber = phoneNumber,
                    timestamp = System.currentTimeMillis(),
                    duration = callDuration,
                    transcript = transcript,
                    riskScore = finalAnalysis.riskScore,
                    verdict = finalAnalysis.verdict,
                    confidence = finalAnalysis.confidence,
                    detectedPatterns = finalAnalysis.detectedPatterns,
                    keywords = finalAnalysis.keywords,
                    reason = finalAnalysis.reason,
                    reasonHindi = finalAnalysis.reasonHindi,
                    recommendedAction = finalAnalysis.recommendedAction,
                    status = "ANALYZED"
                )

                // Save to database
                callRepository.insertCall(callRecord)
                Log.d(TAG, "📊 Call record saved [$analysisMode]: ${finalAnalysis.verdict}, Score: ${finalAnalysis.riskScore}, Phone: $phoneNumber")

                // Trigger alert if high risk
                val threshold = settingsDataStore.getRiskThresholdSync()
                if (finalAnalysis.riskScore >= threshold) {
                    val alertIntent = Intent(this, FraudAlertService::class.java)
                    alertIntent.putExtra("RISK_SCORE", finalAnalysis.riskScore)
                    alertIntent.putExtra("VERDICT", finalAnalysis.verdict.name)
                    alertIntent.putExtra("REASON", finalAnalysis.reason)
                    alertIntent.putExtra("REASON_HINDI", finalAnalysis.reasonHindi)
                    alertIntent.putExtra("ACTION", finalAnalysis.recommendedAction)
                    alertIntent.putExtra("PHONE_NUMBER", phoneNumber)
                    startService(alertIntent)
                    
                    Log.d(TAG, "🚨 FRAUD ALERT TRIGGERED [$analysisMode] - Score: ${finalAnalysis.riskScore} - $phoneNumber")
                }

                Log.d(TAG, "✅ Analysis pipeline complete [$analysisMode]: ${finalAnalysis.verdict}, Confidence: ${finalAnalysis.confidence}")
            }

            // Clean up recording file
            if (recordingFile != null) {
                try {
                    File(recordingFile).delete()
                    Log.d(TAG, "Recording file deleted: $recordingFile")
                } catch (e: Exception) {
                    Log.w(TAG, "Failed to delete recording file", e)
                }
            }

        } catch (e: Exception) {
            Log.e(TAG, "Error in analysis pipeline", e)
        }
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onDestroy() {
        super.onDestroy()
        serviceScope.cancel()
    }
}


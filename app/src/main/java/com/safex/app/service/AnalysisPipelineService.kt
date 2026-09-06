package com.safex.app.service

import android.app.Service
import android.content.Intent
import android.os.IBinder
import android.util.Log
import com.safex.app.data.datastore.SettingsDataStore
import com.safex.app.domain.model.CallRecord
import com.safex.app.domain.model.RiskLevel
import com.safex.app.domain.repository.CallRepository
import com.safex.app.domain.usecase.AnalyzeCallUseCase
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.*
import javax.inject.Inject

@AndroidEntryPoint
class AnalysisPipelineService : Service() {

    @Inject
    lateinit var analyzeCallUseCase: AnalyzeCallUseCase

    @Inject
    lateinit var callRepository: CallRepository

    @Inject
    lateinit var settingsDataStore: SettingsDataStore

    private val TAG = "AnalysisPipelineService"
    private val serviceScope = CoroutineScope(Dispatchers.IO + SupervisorJob())

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        serviceScope.launch {
            try {
                delay(3000) // Wait for call to fully end
                performAnalysis()
            } catch (e: Exception) {
                Log.e(TAG, "Analysis failed", e)
            } finally {
                stopSelf()
            }
        }
        return START_NOT_STICKY
    }

    private suspend fun performAnalysis() {
        // Mock transcript for skeleton - real implementation would use actual recorded audio
        val mockTranscript = "This is a test call transcript"
        
        Log.d(TAG, "Starting analysis pipeline")

        val analysisResult = analyzeCallUseCase(mockTranscript)

        if (analysisResult.isSuccess) {
            val analysis = analysisResult.getOrNull()!!
            
            val callRecord = CallRecord(
                phoneNumber = "Unknown",
                timestamp = System.currentTimeMillis(),
                duration = 0L,
                transcript = mockTranscript,
                riskScore = analysis.riskScore,
                verdict = analysis.verdict,
                confidence = analysis.confidence,
                detectedPatterns = analysis.detectedPatterns,
                keywords = analysis.keywords,
                reason = analysis.reason,
                reasonHindi = analysis.reasonHindi,
                recommendedAction = analysis.recommendedAction,
                status = "ANALYZED"
            )

            callRepository.insertCall(callRecord)

            // Trigger alert if high risk
            val threshold = settingsDataStore.getRiskThresholdSync()
            if (analysis.riskScore >= threshold) {
                val alertIntent = Intent(this, FraudAlertService::class.java)
                alertIntent.putExtra("RISK_SCORE", analysis.riskScore)
                alertIntent.putExtra("VERDICT", analysis.verdict.name)
                startService(alertIntent)
            }

            Log.d(TAG, "Analysis complete: ${analysis.verdict}, Score: ${analysis.riskScore}")
        } else {
            Log.e(TAG, "Analysis failed: ${analysisResult.exceptionOrNull()?.message}")
        }
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onDestroy() {
        super.onDestroy()
        serviceScope.cancel()
    }
}

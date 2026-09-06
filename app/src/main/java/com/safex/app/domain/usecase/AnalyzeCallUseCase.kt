package com.safex.app.domain.usecase

import com.google.gson.Gson
import com.safex.app.BuildConfig
import com.safex.app.data.remote.api.OpenAIApi
import com.safex.app.data.remote.dto.FraudAnalysisResult
import com.safex.app.data.remote.dto.OpenAIRequest
import com.safex.app.domain.model.FraudAnalysis
import com.safex.app.domain.model.RiskLevel
import com.safex.app.util.Constants
import javax.inject.Inject

class AnalyzeCallUseCase @Inject constructor(
    private val openAIApi: OpenAIApi,
    private val gson: Gson
) {
    suspend operator fun invoke(transcript: String): Result<FraudAnalysis> {
        return try {
            val apiKey = BuildConfig.OPENAI_API_KEY
            if (apiKey.isBlank()) {
                return Result.failure(Exception("OpenAI API key not configured"))
            }

            val systemPrompt = buildSystemPrompt()
            val userPrompt = "Analyze this call transcript for fraud:\n\n$transcript"

            val request = OpenAIRequest(
                model = "gpt-4o",
                messages = listOf(
                    OpenAIRequest.Message(role = "system", content = systemPrompt),
                    OpenAIRequest.Message(role = "user", content = userPrompt)
                ),
                temperature = 0.3,
                responseFormat = OpenAIRequest.ResponseFormat("json_object")
            )

            val response = openAIApi.createChatCompletion(
                authorization = "Bearer $apiKey",
                request = request
            )

            if (!response.isSuccessful) {
                return Result.failure(Exception("API call failed: ${response.message()}"))
            }

            val content = response.body()?.choices?.firstOrNull()?.message?.content
                ?: return Result.failure(Exception("Empty response from API"))

            val result = gson.fromJson(content, FraudAnalysisResult::class.java)

            val analysis = FraudAnalysis(
                riskScore = result.riskScore,
                verdict = RiskLevel.valueOf(result.verdict),
                confidence = result.confidence,
                detectedPatterns = result.detectedPatterns,
                keywords = result.keywordsFound,
                reason = result.reason,
                reasonHindi = result.reasonHindi,
                recommendedAction = result.recommendedAction
            )

            Result.success(analysis)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private fun buildSystemPrompt(): String {
        val keywords = buildString {
            appendLine("Digital Arrest: ${Constants.DIGITAL_ARREST_KEYWORDS.joinToString(", ")}")
            appendLine("KYC Fraud: ${Constants.KYC_FRAUD_KEYWORDS.joinToString(", ")}")
            appendLine("OTP Fraud: ${Constants.OTP_FRAUD_KEYWORDS.joinToString(", ")}")
            appendLine("Lottery Scam: ${Constants.LOTTERY_SCAM_KEYWORDS.joinToString(", ")}")
            appendLine("Courier Scam: ${Constants.COURIER_SCAM_KEYWORDS.joinToString(", ")}")
            appendLine("Investment Scam: ${Constants.INVESTMENT_SCAM_KEYWORDS.joinToString(", ")}")
            appendLine("Job Scam: ${Constants.JOB_SCAM_KEYWORDS.joinToString(", ")}")
        }

        return """
You are an AI fraud detection system specifically trained on Indian scam patterns. Analyze call transcripts and detect:

$keywords

Return ONLY a valid JSON object with this exact structure:
{
  "risk_score": <0-100>,
  "verdict": "<SAFE|SUSPICIOUS|FRAUD>",
  "confidence": <0.0-1.0>,
  "detected_patterns": ["pattern1", "pattern2"],
  "keywords_found": ["keyword1", "keyword2"],
  "reason": "Explanation in English",
  "reason_hindi": "Explanation in Hindi",
  "recommended_action": "What user should do"
}

Rules:
- FRAUD: risk_score > 70, clear scam indicators
- SUSPICIOUS: risk_score 40-70, some red flags
- SAFE: risk_score < 40, normal conversation
        """.trimIndent()
    }
}

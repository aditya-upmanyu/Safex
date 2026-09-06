package com.safex.app.data.remote.dto

import com.google.gson.annotations.SerializedName

data class OpenAIRequest(
    @SerializedName("model") val model: String = "gpt-4o",
    @SerializedName("messages") val messages: List<Message>,
    @SerializedName("temperature") val temperature: Double = 0.3,
    @SerializedName("response_format") val responseFormat: ResponseFormat? = ResponseFormat("json_object")
) {
    data class Message(
        @SerializedName("role") val role: String,
        @SerializedName("content") val content: String
    )

    data class ResponseFormat(
        @SerializedName("type") val type: String
    )
}

data class OpenAIResponse(
    @SerializedName("id") val id: String?,
    @SerializedName("object") val objectType: String?,
    @SerializedName("created") val created: Long?,
    @SerializedName("model") val model: String?,
    @SerializedName("choices") val choices: List<Choice>?,
    @SerializedName("usage") val usage: Usage?,
    @SerializedName("error") val error: ErrorDetail?
) {
    data class Choice(
        @SerializedName("index") val index: Int?,
        @SerializedName("message") val message: Message?,
        @SerializedName("finish_reason") val finishReason: String?
    )

    data class Message(
        @SerializedName("role") val role: String?,
        @SerializedName("content") val content: String?
    )

    data class Usage(
        @SerializedName("prompt_tokens") val promptTokens: Int?,
        @SerializedName("completion_tokens") val completionTokens: Int?,
        @SerializedName("total_tokens") val totalTokens: Int?
    )

    data class ErrorDetail(
        @SerializedName("message") val message: String?,
        @SerializedName("type") val type: String?,
        @SerializedName("code") val code: String?
    )
}

data class FraudAnalysisResult(
    @SerializedName("risk_score") val riskScore: Int,
    @SerializedName("verdict") val verdict: String,
    @SerializedName("confidence") val confidence: Float,
    @SerializedName("detected_patterns") val detectedPatterns: List<String>,
    @SerializedName("keywords_found") val keywordsFound: List<String>,
    @SerializedName("reason") val reason: String,
    @SerializedName("reason_hindi") val reasonHindi: String,
    @SerializedName("recommended_action") val recommendedAction: String
)

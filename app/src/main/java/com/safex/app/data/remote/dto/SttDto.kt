package com.safex.app.data.remote.dto

import com.google.gson.annotations.SerializedName

data class SttRequest(
    @SerializedName("config") val config: RecognitionConfig,
    @SerializedName("audio") val audio: RecognitionAudio
) {
    data class RecognitionConfig(
        @SerializedName("encoding") val encoding: String = "LINEAR16",
        @SerializedName("sampleRateHertz") val sampleRateHertz: Int = 16000,
        @SerializedName("languageCode") val languageCode: String = "hi-IN",
        @SerializedName("alternativeLanguageCodes") val alternativeLanguageCodes: List<String> = listOf("en-IN"),
        @SerializedName("enableAutomaticPunctuation") val enableAutomaticPunctuation: Boolean = true
    )

    data class RecognitionAudio(
        @SerializedName("content") val content: String // Base64 encoded audio
    )
}

data class SttResponse(
    @SerializedName("results") val results: List<Result>?,
    @SerializedName("error") val error: ErrorDetail?
) {
    data class Result(
        @SerializedName("alternatives") val alternatives: List<Alternative>?
    )

    data class Alternative(
        @SerializedName("transcript") val transcript: String?,
        @SerializedName("confidence") val confidence: Float?
    )

    data class ErrorDetail(
        @SerializedName("code") val code: Int?,
        @SerializedName("message") val message: String?,
        @SerializedName("status") val status: String?
    )
}

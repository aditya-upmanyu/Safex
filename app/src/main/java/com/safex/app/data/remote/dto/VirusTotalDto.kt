package com.safex.app.data.remote.dto

import com.google.gson.annotations.SerializedName

data class VirusTotalSubmitRequest(
    @SerializedName("url") val url: String
)

data class VirusTotalSubmitResponse(
    @SerializedName("data") val data: SubmitData?
) {
    data class SubmitData(
        @SerializedName("type") val type: String?,
        @SerializedName("id") val id: String?
    )
}

data class VirusTotalAnalysisResponse(
    @SerializedName("data") val data: AnalysisData?,
    @SerializedName("error") val error: ErrorDetail?
) {
    data class AnalysisData(
        @SerializedName("id") val id: String?,
        @SerializedName("type") val type: String?,
        @SerializedName("attributes") val attributes: Attributes?
    )

    data class Attributes(
        @SerializedName("status") val status: String?,
        @SerializedName("stats") val stats: Stats?,
        @SerializedName("results") val results: Map<String, EngineResult>?
    )

    data class Stats(
        @SerializedName("harmless") val harmless: Int?,
        @SerializedName("malicious") val malicious: Int?,
        @SerializedName("suspicious") val suspicious: Int?,
        @SerializedName("undetected") val undetected: Int?,
        @SerializedName("timeout") val timeout: Int?
    )

    data class EngineResult(
        @SerializedName("category") val category: String?,
        @SerializedName("result") val result: String?,
        @SerializedName("method") val method: String?,
        @SerializedName("engine_name") val engineName: String?
    )

    data class ErrorDetail(
        @SerializedName("code") val code: String?,
        @SerializedName("message") val message: String?
    )
}

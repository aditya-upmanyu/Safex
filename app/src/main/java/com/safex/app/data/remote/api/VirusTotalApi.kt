package com.safex.app.data.remote.api

import com.safex.app.data.remote.dto.VirusTotalAnalysisResponse
import com.safex.app.data.remote.dto.VirusTotalSubmitResponse
import retrofit2.Response
import retrofit2.http.*

interface VirusTotalApi {
    
    @FormUrlEncoded
    @POST("urls")
    suspend fun submitUrl(
        @Header("x-apikey") apiKey: String,
        @Field("url") url: String
    ): Response<VirusTotalSubmitResponse>

    @GET("analyses/{id}")
    suspend fun getAnalysis(
        @Header("x-apikey") apiKey: String,
        @Path("id") analysisId: String
    ): Response<VirusTotalAnalysisResponse>
}

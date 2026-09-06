package com.safex.app.data.remote.api

import com.safex.app.data.remote.dto.SttRequest
import com.safex.app.data.remote.dto.SttResponse
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST
import retrofit2.http.Query

interface GoogleSttApi {
    
    @POST("v1/speech:recognize")
    suspend fun recognizeSpeech(
        @Query("key") apiKey: String,
        @Body request: SttRequest
    ): Response<SttResponse>
}

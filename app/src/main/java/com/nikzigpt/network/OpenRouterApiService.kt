package com.nikzigpt.network

import com.nikzigpt.data.AIModel
import com.nikzigpt.data.ChatCompletionChunk
import com.nikzigpt.data.ChatCompletionRequest
import com.nikzigpt.data.ChatCompletionResponse
import com.nikzigpt.data.ModelsResponse
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST
import retrofit2.http.Streaming
import retrofit2.http.Url
import kotlinx.coroutines.flow.Flow

interface OpenRouterApiService {
    
    @GET("models")
    suspend fun getModels(
        @Header("Authorization") authHeader: String
    ): Response<ModelsResponse>
    
    @POST("chat/completions")
    suspend fun chatCompletion(
        @Header("Authorization") authHeader: String,
        @Header("HTTP-Referer") referer: String,
        @Header("X-Title") title: String,
        @Body request: ChatCompletionRequest
    ): Response<ChatCompletionResponse>
    
    @Streaming
    @POST("chat/completions")
    fun chatCompletionStream(
        @Header("Authorization") authHeader: String,
        @Header("HTTP-Referer") referer: String,
        @Header("X-Title") title: String,
        @Body request: ChatCompletionRequest
    ): Flow<Response<ChatCompletionChunk>>
}
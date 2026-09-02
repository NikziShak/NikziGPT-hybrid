package com.nikzigpt.network

import com.nikzigpt.data.AIModel
import com.nikzigpt.data.ChatCompletionChunk
import com.nikzigpt.data.ChatCompletionRequest
import com.nikzigpt.data.ChatCompletionResponse
import com.nikzigpt.data.ModelsResponse
import retrofit2.Retrofit
import retrofit2.converter.kotlinx.serialization.asConverterFactory
import kotlinx.serialization.json.Json
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import java.util.concurrent.TimeUnit

object ApiClient {
    private const val BASE_URL = "https://openrouter.ai/api/v1/"
    private const val APP_NAME = "NikziGPT"
    private const val APP_URL = "https://github.com/nikzigpt"
    
    private val loggingInterceptor = HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.BODY
    }
    
    private val okHttpClient = OkHttpClient.Builder()
        .addInterceptor(loggingInterceptor)
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .writeTimeout(60, TimeUnit.SECONDS)
        .build()
    
    private val json = Json {
        ignoreUnknownKeys = true
        isLenient = true
    }
    
    private val retrofit = Retrofit.Builder()
        .baseUrl(BASE_URL)
        .client(okHttpClient)
        .addConverterFactory(json.asConverterFactory("application/json".toMediaType()))
        .build()
    
    val service: OpenRouterApiService = retrofit.create(OpenRouterApiService::class.java)
    
    fun getAuthHeader(apiKey: String): String = "Bearer $apiKey"
    
    fun getRefererHeader(): String = APP_URL
    
    fun getTitleHeader(): String = APP_NAME
}
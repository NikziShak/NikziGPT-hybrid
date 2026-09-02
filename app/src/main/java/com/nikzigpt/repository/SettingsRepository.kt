package com.nikzigpt.repository

import android.content.Context
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.PreferencesKeys
import androidx.datastore.preferences.core.preferencesDataStore
import androidx.datastore.preferences.core.stringPreferencesKey
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.tasks.await

class SettingsRepository(private val context: Context) {
    
    private val Context.dataStore by preferencesDataStore("nikzigpt_settings")
    
    private val API_KEY_KEY = stringPreferencesKey("openrouter_api_key")
    private val DEFAULT_MODEL_KEY = stringPreferencesKey("default_model")
    private val TEMPERATURE_KEY = stringPreferencesKey("temperature")
    private val MAX_TOKENS_KEY = stringPreferencesKey("max_tokens")
    private val SYSTEM_PROMPT_KEY = stringPreferencesKey("system_prompt")
    private val THEME_KEY = stringPreferencesKey("theme")
    
    // Default values
    private val DEFAULT_TEMPERATURE = "0.7"
    private val DEFAULT_MAX_TOKENS = "4096"
    private val DEFAULT_THEME = "dark"
    
    suspend fun getApiKey(): String? = context.dataStore.data
        .map { it[API_KEY_KEY] }
        .first()
    
    suspend fun saveApiKey(apiKey: String) {
        context.dataStore.edit { it[API_KEY_KEY] = apiKey }
    }
    
    suspend fun getDefaultModel(): String? = context.dataStore.data
        .map { it[DEFAULT_MODEL_KEY] }
        .first()
    
    suspend fun saveDefaultModel(modelId: String) {
        context.dataStore.edit { it[DEFAULT_MODEL_KEY] = modelId }
    }
    
    suspend fun getTemperature(): Float = context.dataStore.data
        .map { it[TEMPERATURE_KEY]?.toFloatOrNull() ?: DEFAULT_TEMPERATURE.toFloat() }
        .first()
    
    suspend fun saveTemperature(temperature: Float) {
        context.dataStore.edit { it[TEMPERATURE_KEY] = temperature.toString() }
    }
    
    suspend fun getMaxTokens(): Int = context.dataStore.data
        .map { it[MAX_TOKENS_KEY]?.toIntOrNull() ?: DEFAULT_MAX_TOKENS.toInt() }
        .first()
    
    suspend fun saveMaxTokens(maxTokens: Int) {
        context.dataStore.edit { it[MAX_TOKENS_KEY] = maxTokens.toString() }
    }
    
    suspend fun getSystemPrompt(): String? = context.dataStore.data
        .map { it[SYSTEM_PROMPT_KEY] }
        .first()
    
    suspend fun saveSystemPrompt(prompt: String) {
        context.dataStore.edit { it[SYSTEM_PROMPT_KEY] = prompt }
    }
    
    suspend fun getTheme(): String = context.dataStore.data
        .map { it[THEME_KEY] ?: DEFAULT_THEME }
        .first()
    
    suspend fun saveTheme(theme: String) {
        context.dataStore.edit { it[THEME_KEY] = theme }
    }
    
    suspend fun clearAll() {
        context.dataStore.edit { it.clear() }
    }
}
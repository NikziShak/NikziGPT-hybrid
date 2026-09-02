package com.nikzigpt.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.nikzigpt.repository.SettingsRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class SettingsViewModel(application: Application) : AndroidViewModel(application) {
    
    private val settingsRepository = SettingsRepository(application)
    
    // State
    private val _apiKey = MutableStateFlow<String>("")
    val apiKey = _apiKey.asStateFlow()
    
    private val _temperature = MutableStateFlow<Float>(0.7f)
    val temperature = _temperature.asStateFlow()
    
    private val _maxTokens = MutableStateFlow<Int>(4096)
    val maxTokens = _maxTokens.asStateFlow()
    
    private val _systemPrompt = MutableStateFlow<String>("")
    val systemPrompt = _systemPrompt.asStateFlow()
    
    private val _theme = MutableStateFlow<String>("dark")
    val theme = _theme.asStateFlow()
    
    private val _isSaving = MutableStateFlow(false)
    val isSaving = _isSaving.asStateFlow()
    
    private val _saveMessage = MutableStateFlow<String?>(null)
    val saveMessage = _saveMessage.asStateFlow()
    
    init {
        loadSettings()
    }
    
    private fun loadSettings() {
        viewModelScope.launch {
            _apiKey.value = settingsRepository.getApiKey() ?: ""
            _temperature.value = settingsRepository.getTemperature()
            _maxTokens.value = settingsRepository.getMaxTokens()
            _systemPrompt.value = settingsRepository.getSystemPrompt() ?: ""
            _theme.value = settingsRepository.getTheme()
        }
    }
    
    fun updateApiKey(key: String) {
        _apiKey.value = key
    }
    
    fun updateTemperature(temp: Float) {
        _temperature.value = temp.coerceIn(0.0f, 2.0f)
    }
    
    fun updateMaxTokens(tokens: Int) {
        _maxTokens.value = tokens.coerceIn(1, 32768)
    }
    
    fun updateSystemPrompt(prompt: String) {
        _systemPrompt.value = prompt
    }
    
    fun updateTheme(theme: String) {
        _theme.value = theme
    }
    
    fun saveSettings() {
        viewModelScope.launch {
            _isSaving.value = true
            _saveMessage.value = null
            
            try {
                settingsRepository.saveApiKey(_apiKey.value)
                settingsRepository.saveTemperature(_temperature.value)
                settingsRepository.saveMaxTokens(_maxTokens.value)
                settingsRepository.saveSystemPrompt(_systemPrompt.value)
                settingsRepository.saveTheme(_theme.value)
                
                _saveMessage.value = "Settings saved successfully"
            } catch (e: Exception) {
                _saveMessage.value = "Error saving settings: ${e.message}"
            } finally {
                _isSaving.value = false
            }
        }
    }
    
    fun clearSaveMessage() {
        _saveMessage.value = null
    }
}
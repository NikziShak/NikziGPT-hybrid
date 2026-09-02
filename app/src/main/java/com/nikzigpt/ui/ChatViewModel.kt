package com.nikzigpt.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.nikzigpt.data.AIModel
import com.nikzigpt.data.ChatMessage
import com.nikzigpt.network.ApiClient
import com.nikzigpt.repository.ChatRepository
import com.nikzigpt.repository.SettingsRepository
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.launch
import kotlinx.coroutines.receive
import kotlinx.coroutines.send
import java.util.concurrent.atomic.AtomicBoolean

class ChatViewModel(application: Application) : AndroidViewModel(application) {
    
    private val chatRepository = ChatRepository(application)
    private val settingsRepository = SettingsRepository(application)
    
    // State
    private val _messages = MutableStateFlow<List<ChatMessage>>(emptyList())
    val messages = _messages.asStateFlow()
    
    private val _isLoading = MutableStateFlow(false)
    val isLoading = _isLoading.asStateFlow()
    
    private val _error = MutableStateFlow<String?>(null)
    val error = _error.asStateFlow()
    
    private val _availableModels = MutableStateFlow<List<AIModel>>(emptyList())
    val availableModels = _availableModels.asStateFlow()
    
    private val _selectedModel = MutableStateFlow<AIModel?>(null)
    val selectedModel = _selectedModel.asStateFlow()
    
    private val _isStreaming = MutableStateFlow(false)
    val isStreaming = _isStreaming.asStateFlow()
    
    private val _currentStreamingMessage = MutableStateFlow<String>("")
    val currentStreamingMessage = _currentStreamingMessage.asStateFlow()
    
    // Channel for streaming
    private val streamingChannel = Channel<String>(Channel.UNLIMITED)
    private val isCancelling = AtomicBoolean(false)
    
    init {
        loadSettings()
        loadModels()
        observeMessages()
    }
    
    private fun observeMessages() {
        viewModelScope.launch {
            chatRepository.getMessages().collect { msgs ->
                _messages.value = msgs
            }
        }
    }
    
    private fun loadSettings() {
        viewModelScope.launch {
            val modelId = settingsRepository.getDefaultModel()
            val apiKey = settingsRepository.getApiKey()
            
            if (modelId != null) {
                // Will be set after models are loaded
            }
            
            if (apiKey == null || apiKey.isEmpty()) {
                _error.value = "Please set your OpenRouter API key in Settings"
            }
        }
    }
    
    fun loadModels() {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            
            val apiKey = settingsRepository.getApiKey()
            if (apiKey == null || apiKey.isEmpty()) {
                _error.value = "Please set your OpenRouter API key in Settings"
                _isLoading.value = false
                return@launch
            }
            
            try {
                val response = ApiClient.service.getModels(ApiClient.getAuthHeader(apiKey))
                if (response.isSuccessful && response.body() != null) {
                    val allModels = response.body()!!.data
                    val freeModels = allModels.filter { it.isFree }
                    
                    _availableModels.value = freeModels
                    chatRepository.cacheModels(freeModels)
                    
                    // Set default model if not set
                    val savedModelId = settingsRepository.getDefaultModel()
                    if (savedModelId != null) {
                        val model = freeModels.find { it.id == savedModelId }
                        if (model != null) {
                            _selectedModel.value = model
                        } else if (freeModels.isNotEmpty()) {
                            _selectedModel.value = freeModels.first()
                            settingsRepository.saveDefaultModel(freeModels.first().id)
                        }
                    } else if (freeModels.isNotEmpty()) {
                        _selectedModel.value = freeModels.first()
                        settingsRepository.saveDefaultModel(freeModels.first().id)
                    }
                } else {
                    // Try to load from cache
                    loadCachedModels()
                    _error.value = "Failed to load models: ${response.message()}"
                }
            } catch (e: Exception) {
                loadCachedModels()
                _error.value = "Error loading models: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }
    
    private fun loadCachedModels() {
        viewModelScope.launch {
            chatRepository.getFreeModels().collect { models ->
                _availableModels.value = models
                if (models.isNotEmpty() && _selectedModel.value == null) {
                    _selectedModel.value = models.first()
                }
            }.cancel()
        }
    }
    
    fun selectModel(model: AIModel) {
        _selectedModel.value = model
        viewModelScope.launch {
            settingsRepository.saveDefaultModel(model.id)
        }
    }
    
    fun sendMessage(content: String) {
        if (content.trim().isEmpty()) return
        if (_selectedModel.value == null) {
            _error.value = "Please select a model first"
            return
        }
        
        val apiKey = settingsRepository.getApiKey()
        if (apiKey == null || apiKey.isEmpty()) {
            _error.value = "Please set your OpenRouter API key in Settings"
            return
        }
        
        // Add user message
        val userMessage = ChatMessage.user(content, _selectedModel.value?.id)
        viewModelScope.launch {
            chatRepository.addMessage(userMessage)
        }
        
        // Start streaming response
        startStreaming(content)
    }
    
    private fun startStreaming(userContent: String) {
        _isStreaming.value = true
        _currentStreamingMessage.value = ""
        isCancelling.set(false)
        
        viewModelScope.launch {
            val model = _selectedModel.value!!
            val temperature = settingsRepository.getTemperature()
            val maxTokens = settingsRepository.getMaxTokens()
            val systemPrompt = settingsRepository.getSystemPrompt()
            
            // Build messages list
            val messagesList = mutableListOf<ChatMessage>()
            
            if (systemPrompt != null && systemPrompt.isNotEmpty()) {
                messagesList.add(ChatMessage.system(systemPrompt))
            }
            
            // Add recent messages (last 10 for context)
            val recentMessages = _messages.value.takeLast(10)
            messagesList.addAll(recentMessages)
            
            // Add current user message
            messagesList.add(ChatMessage.user(userContent, model.id))
            
            val request = com.nikzigpt.data.ChatCompletionRequest(
                model = model.id,
                messages = messagesList,
                temperature = temperature,
                max_tokens = maxTokens,
                stream = true
            )
            
            // Create assistant message placeholder
            val assistantMessageId = java.util.UUID.randomUUID().toString()
            val assistantMessage = ChatMessage(
                role = "assistant",
                content = "",
                id = assistantMessageId,
                isStreaming = true,
                modelId = model.id
            )
            
            chatRepository.addMessage(assistantMessage)
            
            try {
                val flow = ApiClient.service.chatCompletionStream(
                    authHeader = ApiClient.getAuthHeader(settingsRepository.getApiKey()!!),
                    referer = ApiClient.getRefererHeader(),
                    title = ApiClient.getTitleHeader(),
                    request = request
                )
                
                var fullContent = ""
                
                flow.collect { response ->
                    if (isCancelling.get()) return@collect
                    
                    if (response.isSuccessful && response.body() != null) {
                        val chunk = response.body()!!
                        val delta = chunk.choices.firstOrNull()?.delta?.content
                        if (delta != null && delta.isNotEmpty()) {
                            fullContent += delta
                            _currentStreamingMessage.value = fullContent
                            
                            // Update the streaming message
                            val updatedMessage = assistantMessage.copy(
                                content = fullContent,
                                isStreaming = true
                            )
                            chatRepository.updateMessage(updatedMessage)
                        }
                        
                        val finishReason = chunk.choices.firstOrNull()?.finish_reason
                        if (finishReason != null) {
                            // Streaming complete
                            val finalMessage = assistantMessage.copy(
                                content = fullContent,
                                isStreaming = false
                            )
                            chatRepository.updateMessage(finalMessage)
                            _isStreaming.value = false
                            _currentStreamingMessage.value = ""
                            break
                        }
                    } else {
                        val errorMsg = response.errorBody()?.string() ?: "Unknown error"
                        _error.value = "API Error: $errorMsg"
                        _isStreaming.value = false
                        _currentStreamingMessage.value = ""
                        
                        // Update message with error
                        val errorMessage = assistantMessage.copy(
                            content = "Error: $errorMsg",
                            isStreaming = false
                        )
                        chatRepository.updateMessage(errorMessage)
                        break
                    }
                }
            } catch (e: Exception) {
                _error.value = "Streaming error: ${e.message}"
                _isStreaming.value = false
                _currentStreamingMessage.value = ""
                
                val errorMessage = assistantMessage.copy(
                    content = "Error: ${e.message}",
                    isStreaming = false
                )
                chatRepository.updateMessage(errorMessage)
            }
        }
    }
    
    fun cancelStreaming() {
        isCancelling.set(true)
        _isStreaming.value = false
        _currentStreamingMessage.value = ""
    }
    
    fun regenerateLastResponse() {
        val messages = _messages.value
        val lastUserMessage = messages.lastOrNull { it.role == "user" }
        if (lastUserMessage != null) {
            // Remove the last assistant message if exists
            val lastAssistantIndex = messages.lastIndexOf { it.role == "assistant" }
            if (lastAssistantIndex >= 0) {
                // We'd need to implement message deletion
            }
            sendMessage(lastUserMessage.content)
        }
    }
    
    fun newChat() {
        chatRepository.newSession()
        cancelStreaming()
    }
    
    fun clearError() {
        _error.value = null
    }
    
    fun retryLoadModels() {
        loadModels()
    }
    
    override fun onCleared() {
        streamingChannel.close()
        super.onCleared()
    }
}
package com.nikzigpt.data

import kotlinx.serialization.Serializable

@Serializable
data class ChatMessage(
    val role: String, // "user", "assistant", "system"
    val content: String,
    val timestamp: Long = System.currentTimeMillis(),
    val id: String = java.util.UUID.randomUUID().toString(),
    val isStreaming: Boolean = false,
    val modelId: String? = null
) {
    companion object {
        fun user(content: String, modelId: String? = null): ChatMessage {
            return ChatMessage(role = "user", content = content, modelId = modelId)
        }
        
        fun assistant(content: String, modelId: String? = null): ChatMessage {
            return ChatMessage(role = "assistant", content = content, modelId = modelId)
        }
        
        fun system(content: String): ChatMessage {
            return ChatMessage(role = "system", content = content)
        }
    }
}
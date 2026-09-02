package com.nikzigpt.data

import kotlinx.serialization.Serializable

@Serializable
data class AIModel(
    val id: String,
    val name: String,
    val description: String,
    val provider: String,
    val contextLength: Int,
    val pricing: ModelPricing,
    val isFree: Boolean,
    val capabilities: List<String> = emptyList(),
    val architecture: ModelArchitecture? = null
)

@Serializable
data class ModelPricing(
    val prompt: String, // Price per 1M tokens
    val completion: String, // Price per 1M tokens
    val currency: String = "USD"
) {
    val isFree: Boolean
        get() = prompt == "0" && completion == "0"
}

@Serializable
data class ModelArchitecture(
    val modality: String,
    val tokenizer: String,
    val instructType: String?
)

@Serializable
data class ModelsResponse(
    val data: List<AIModel>
)
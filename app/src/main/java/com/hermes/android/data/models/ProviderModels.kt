package com.hermes.android.data.models

import kotlinx.serialization.Serializable

@Serializable
data class ProviderModel(
    val id: String,
    val name: String,
    val providerId: String,
    val contextLength: Int = 128000,
    val isCustom: Boolean = false
)

@Serializable
enum class ProviderType {
    OPENAI_COMPATIBLE,
    ANTHROPIC,
    GOOGLE,
    OPENROUTER,
    OLLAMA,
    HERMES_GATEWAY,
    CUSTOM
}

@Serializable
data class ProviderConfig(
    val id: String,
    val name: String,
    val baseUrl: String,
    val apiKey: String = "",
    val type: ProviderType = ProviderType.OPENAI_COMPATIBLE,
    val isEnabled: Boolean = true,
    val isTermuxLocal: Boolean = false,
    val models: List<ProviderModel> = emptyList()
)

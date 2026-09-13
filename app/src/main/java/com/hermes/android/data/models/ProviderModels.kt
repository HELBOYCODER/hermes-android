package com.hermes.android.data.models

import kotlinx.serialization.Serializable

@Serializable
data class ProviderModel(
    val entryId: String = "",
    val modelId: String,
    val displayName: String,
    val providerId: String,
    val providerLabel: String = "",
    val contextWindow: Int = 128000,
    val isCustom: Boolean = false,
    val isHidden: Boolean = false,
    val supportsVision: Boolean = false,
    val supportsTools: Boolean = true
) {
    val id: String get() = modelId
    val name: String get() = displayName
}

@Serializable
enum class ProviderType {
    OPENAI,
    ANTHROPIC,
    GEMINI,
    OPENROUTER,
    OLLAMA,
    HERMES_GATEWAY,
    CUSTOM
}

@Serializable
data class ProviderConfig(
    val id: String,
    val label: String,
    val customBaseURL: String,
    val apiKey: String = "",
    val providerType: ProviderType = ProviderType.OPENAI,
    val credentialType: String = "apiKey",
    val isEnabled: Boolean = true,
    val appendV1Suffix: Boolean = false,
    val isTermuxLocal: Boolean = false,
    val latencyMs: Long? = null,
    val models: List<ProviderModel> = emptyList()
) {
    val name: String get() = label
    val baseUrl: String get() = customBaseURL
    val type: ProviderType get() = providerType
}

package com.hermes.android.data.models

import android.content.Context
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.withContext
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.File
import java.util.UUID
import java.util.concurrent.TimeUnit

/**
 * Minis-equivalent Provider & Model Repository:
 * Manages provider instances, custom endpoints, model catalogs, and live probing.
 */
class ProviderRepository(private val context: Context) {

    private val json = Json { ignoreUnknownKeys = true; prettyPrint = true }
    private val client = OkHttpClient.Builder()
        .connectTimeout(6, TimeUnit.SECONDS)
        .readTimeout(12, TimeUnit.SECONDS)
        .build()

    private val storageFile: File
        get() = File(context.filesDir, "hermes_providers_v2.json")

    private val prefs = context.getSharedPreferences("hermes_models_prefs", Context.MODE_PRIVATE)

    private val _providers = MutableStateFlow<List<ProviderConfig>>(emptyList())
    val providers: StateFlow<List<ProviderConfig>> = _providers

    private val _activeModelId = MutableStateFlow(
        prefs.getString("active_model_id", "hermes-agent") ?: "hermes-agent"
    )
    val activeModelId: StateFlow<String> = _activeModelId

    private val _activeProviderId = MutableStateFlow(
        prefs.getString("active_provider_id", "local-9") ?: "local-9"
    )
    val activeProviderId: StateFlow<String> = _activeProviderId

    init {
        loadProviders()
    }

    private fun defaultProviders(): List<ProviderConfig> = listOf(
        ProviderConfig(
            id = "local-9",
            label = "local 9",
            customBaseURL = "http://127.0.0.1:20128/v1",
            apiKey = "sk-test",
            providerType = ProviderType.OPENAI,
            isTermuxLocal = true,
            models = listOf(
                ProviderModel("local-9/qwen3.8-27b", "qwen3.8-27b", "Qwen 3.8 27B", "local-9", "local 9", 128000),
                ProviderModel("local-9/deepseek-v4-flash-0731", "deepseek-v4-flash-0731", "DeepSeek V4 Flash", "local-9", "local 9", 64000),
                ProviderModel("local-9/gpt-5.6-luna", "gpt-5.6-luna", "GPT-5.6 Luna", "local-9", "local 9", 128000)
            )
        ),
        ProviderConfig(
            id = "termux-hermes",
            label = "Termux Hermes",
            customBaseURL = "http://127.0.0.1:8765",
            providerType = ProviderType.HERMES_GATEWAY,
            isTermuxLocal = true,
            models = listOf(
                ProviderModel("termux-hermes/hermes-agent", "hermes-agent", "Hermes Agent (Termux)", "termux-hermes", "Termux Hermes", 128000),
                ProviderModel("termux-hermes/hermes-3-llama-3.1-8b", "hermes-3-llama-3.1-8b", "Hermes 3 8B", "termux-hermes", "Termux Hermes", 128000)
            )
        ),
        ProviderConfig(
            id = "openai",
            label = "OpenAI",
            customBaseURL = "https://api.openai.com/v1",
            providerType = ProviderType.OPENAI,
            models = listOf(
                ProviderModel("openai/gpt-4o", "gpt-4o", "GPT-4o", "openai", "OpenAI", 128000),
                ProviderModel("openai/gpt-4o-mini", "gpt-4o-mini", "GPT-4o Mini", "openai", "OpenAI", 128000),
                ProviderModel("openai/o3-mini", "o3-mini", "o3 Mini", "openai", "OpenAI", 200000)
            )
        ),
        ProviderConfig(
            id = "anthropic",
            label = "Anthropic",
            customBaseURL = "https://api.anthropic.com/v1",
            providerType = ProviderType.ANTHROPIC,
            models = listOf(
                ProviderModel("anthropic/claude-3-7-sonnet-20250219", "claude-3-7-sonnet-20250219", "Claude 3.7 Sonnet", "anthropic", "Anthropic", 200000),
                ProviderModel("anthropic/claude-3-5-haiku-20241022", "claude-3-5-haiku-20241022", "Claude 3.5 Haiku", "anthropic", "Anthropic", 200000)
            )
        ),
        ProviderConfig(
            id = "openrouter",
            label = "OpenRouter",
            customBaseURL = "https://openrouter.ai/api/v1",
            providerType = ProviderType.OPENROUTER,
            models = listOf(
                ProviderModel("openrouter/anthropic/claude-3.7-sonnet", "anthropic/claude-3.7-sonnet", "Claude 3.7 Sonnet", "openrouter", "OpenRouter", 200000),
                ProviderModel("openrouter/deepseek/deepseek-chat", "deepseek/deepseek-chat", "DeepSeek V3", "openrouter", "OpenRouter", 64000),
                ProviderModel("openrouter/meta-llama/llama-3.3-70b-instruct", "meta-llama/llama-3.3-70b-instruct", "Llama 3.3 70B", "openrouter", "OpenRouter", 128000)
            )
        ),
        ProviderConfig(
            id = "ollama",
            label = "Ollama Local",
            customBaseURL = "http://127.0.0.1:11434/v1",
            providerType = ProviderType.OLLAMA,
            isTermuxLocal = true,
            models = listOf(
                ProviderModel("ollama/qwen2.5-coder:latest", "qwen2.5-coder:latest", "Qwen 2.5 Coder", "ollama", "Ollama", 32000),
                ProviderModel("ollama/llama3.2:latest", "llama3.2:latest", "Llama 3.2", "ollama", "Ollama", 128000)
            )
        )
    )

    private fun loadProviders() {
        if (storageFile.exists()) {
            try {
                val text = storageFile.readText()
                val list = json.decodeFromString<List<ProviderConfig>>(text)
                if (list.isNotEmpty()) {
                    _providers.value = list
                    return
                }
            } catch (_: Exception) { }
        }
        val defs = defaultProviders()
        _providers.value = defs
        saveProviders(defs)
    }

    private fun saveProviders(list: List<ProviderConfig>) {
        try {
            val text = json.encodeToString(list)
            storageFile.writeText(text)
        } catch (_: Exception) { }
    }

    fun setActiveModel(providerId: String, modelId: String) {
        _activeProviderId.value = providerId
        _activeModelId.value = modelId
        prefs.edit()
            .putString("active_provider_id", providerId)
            .putString("active_model_id", modelId)
            .apply()
    }

    fun addCustomModel(
        providerId: String,
        modelId: String,
        displayName: String = modelId,
        contextWindow: Int = 128000
    ) {
        if (modelId.isBlank()) return
        val current = _providers.value.toMutableList()
        val index = current.indexOfFirst { it.id == providerId }
        if (index != -1) {
            val p = current[index]
            if (p.models.none { it.modelId == modelId }) {
                val updatedModels = p.models + ProviderModel(
                    entryId = "${providerId}/$modelId",
                    modelId = modelId.trim(),
                    displayName = displayName.ifBlank { modelId.trim() },
                    providerId = providerId,
                    providerLabel = p.label,
                    contextWindow = contextWindow,
                    isCustom = true
                )
                current[index] = p.copy(models = updatedModels)
                _providers.value = current
                saveProviders(current)
            }
        }
    }

    fun addProvider(
        label: String,
        customBaseURL: String,
        apiKey: String = "",
        providerType: ProviderType = ProviderType.OPENAI,
        appendV1Suffix: Boolean = false
    ): ProviderConfig {
        val newP = ProviderConfig(
            id = "custom-" + UUID.randomUUID().toString().take(8),
            label = label.trim(),
            customBaseURL = customBaseURL.trim().removeSuffix("/"),
            apiKey = apiKey.trim(),
            providerType = providerType,
            appendV1Suffix = appendV1Suffix,
            models = emptyList()
        )
        val updated = _providers.value + newP
        _providers.value = updated
        saveProviders(updated)
        return newP
    }

    fun updateProvider(provider: ProviderConfig) {
        val updated = _providers.value.map { if (it.id == provider.id) provider else it }
        _providers.value = updated
        saveProviders(updated)
    }

    fun toggleProvider(providerId: String) {
        val current = _providers.value.toMutableList()
        val idx = current.indexOfFirst { it.id == providerId }
        if (idx != -1) {
            current[idx] = current[idx].copy(isEnabled = !current[idx].isEnabled)
            _providers.value = current
            saveProviders(current)
        }
    }

    fun deleteProvider(providerId: String) {
        val updated = _providers.value.filter { it.id != providerId }
        _providers.value = updated
        saveProviders(updated)
    }

    suspend fun pingProvider(providerId: String): Long? = withContext(Dispatchers.IO) {
        val provider = _providers.value.find { it.id == providerId } ?: return@withContext null
        val targetUrl = when {
            provider.customBaseURL.endsWith("/v1") -> "${provider.customBaseURL}/models"
            provider.customBaseURL.endsWith("/v1beta") -> "${provider.customBaseURL}/models"
            else -> "${provider.customBaseURL}/models"
        }
        val t0 = System.currentTimeMillis()
        try {
            val reqBuilder = Request.Builder().url(targetUrl).get()
            if (provider.apiKey.isNotBlank()) {
                reqBuilder.addHeader("Authorization", "Bearer ${provider.apiKey}")
            }
            val res = client.newCall(reqBuilder.build()).execute()
            val latency = System.currentTimeMillis() - t0
            res.close()
            val updated = _providers.value.map {
                if (it.id == providerId) it.copy(latencyMs = latency) else it
            }
            _providers.value = updated
            latency
        } catch (_: Exception) {
            val updated = _providers.value.map {
                if (it.id == providerId) it.copy(latencyMs = -1L) else it
            }
            _providers.value = updated
            null
        }
    }

    suspend fun fetchModelsFromEndpoint(providerId: String): Result<List<String>> = withContext(Dispatchers.IO) {
        val provider = _providers.value.find { it.id == providerId }
            ?: return@withContext Result.failure(IllegalArgumentException("Provider not found"))

        val endpoint = when {
            provider.customBaseURL.endsWith("/v1") -> "${provider.customBaseURL}/models"
            provider.customBaseURL.endsWith("/v1beta") -> "${provider.customBaseURL}/models"
            else -> "${provider.customBaseURL}/models"
        }

        try {
            val reqBuilder = Request.Builder().url(endpoint).get()
            if (provider.apiKey.isNotBlank()) {
                reqBuilder.addHeader("Authorization", "Bearer ${provider.apiKey}")
            }
            val res = client.newCall(reqBuilder.build()).execute()
            val body = res.body?.string().orEmpty()
            if (!res.isSuccessful) {
                return@withContext Result.failure(Exception("HTTP ${res.code}: ${body.take(150)}"))
            }

            val parsed = json.parseToJsonElement(body).jsonObject
            val dataArray = parsed["data"]?.jsonArray ?: parsed["models"]?.jsonArray
            val modelIds = dataArray?.mapNotNull {
                it.jsonObject["id"]?.jsonPrimitive?.content ?: it.jsonObject["name"]?.jsonPrimitive?.content
            } ?: emptyList()

            if (modelIds.isNotEmpty()) {
                val current = _providers.value.toMutableList()
                val idx = current.indexOfFirst { it.id == providerId }
                if (idx != -1) {
                    val existing = current[idx].models.map { it.modelId }.toSet()
                    val newModels = current[idx].models + modelIds.filter { it !in existing }.map { id ->
                        ProviderModel(
                            entryId = "$providerId/$id",
                            modelId = id,
                            displayName = id,
                            providerId = providerId,
                            providerLabel = current[idx].label
                        )
                    }
                    current[idx] = current[idx].copy(models = newModels)
                    _providers.value = current
                    saveProviders(current)
                }
            }
            Result.success(modelIds)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}

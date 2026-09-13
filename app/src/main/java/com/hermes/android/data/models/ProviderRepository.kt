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
 * Repository for managing all LLM providers and models without restrictions (like Minis).
 * Supports OpenAI-compatible, Anthropic, Google, OpenRouter, Ollama, 9Router, Termux Hermes,
 * and arbitrary custom endpoints.
 */
class ProviderRepository(private val context: Context) {

    private val json = Json { ignoreUnknownKeys = true; prettyPrint = true }
    private val client = OkHttpClient.Builder()
        .connectTimeout(8, TimeUnit.SECONDS)
        .readTimeout(15, TimeUnit.SECONDS)
        .build()

    private val storageFile: File
        get() = File(context.filesDir, "hermes_providers.json")

    private val prefs = context.getSharedPreferences("hermes_models_prefs", Context.MODE_PRIVATE)

    private val _providers = MutableStateFlow<List<ProviderConfig>>(emptyList())
    val providers: StateFlow<List<ProviderConfig>> = _providers

    private val _activeModelId = MutableStateFlow(prefs.getString("active_model_id", "hermes-agent") ?: "hermes-agent")
    val activeModelId: StateFlow<String> = _activeModelId

    private val _activeProviderId = MutableStateFlow(prefs.getString("active_provider_id", "termux-hermes") ?: "termux-hermes")
    val activeProviderId: StateFlow<String> = _activeProviderId

    init {
        loadProviders()
    }

    private fun defaultProviders(): List<ProviderConfig> = listOf(
        ProviderConfig(
            id = "termux-hermes",
            name = "Termux Hermes Local",
            baseUrl = "http://127.0.0.1:8765",
            type = ProviderType.HERMES_GATEWAY,
            isTermuxLocal = true,
            models = listOf(
                ProviderModel("hermes-agent", "Hermes Agent (Termux)", "termux-hermes"),
                ProviderModel("hermes-3-llama-3.1-8b", "Hermes 3 8B", "termux-hermes"),
                ProviderModel("qwen2.5-coder-7b", "Qwen 2.5 Coder 7B", "termux-hermes")
            )
        ),
        ProviderConfig(
            id = "termux-9router",
            name = "Termux 9Router Local",
            baseUrl = "http://127.0.0.1:20128/v1",
            apiKey = "sk-test",
            type = ProviderType.OPENAI_COMPATIBLE,
            isTermuxLocal = true,
            models = listOf(
                ProviderModel("codecraft/gpt-5.6-luna", "GPT-5.6 Luna", "termux-9router"),
                ProviderModel("codecraft/deepseek-v4-flash-0731", "DeepSeek V4 Flash", "termux-9router"),
                ProviderModel("codecraft/qwen3.8-27b", "Qwen 3.8 27B", "termux-9router")
            )
        ),
        ProviderConfig(
            id = "openrouter",
            name = "OpenRouter",
            baseUrl = "https://openrouter.ai/api/v1",
            type = ProviderType.OPENROUTER,
            models = listOf(
                ProviderModel("anthropic/claude-3.7-sonnet", "Claude 3.7 Sonnet", "openrouter"),
                ProviderModel("deepseek/deepseek-chat", "DeepSeek V3", "openrouter"),
                ProviderModel("meta-llama/llama-3.3-70b-instruct", "Llama 3.3 70B", "openrouter")
            )
        ),
        ProviderConfig(
            id = "openai",
            name = "OpenAI",
            baseUrl = "https://api.openai.com/v1",
            type = ProviderType.OPENAI_COMPATIBLE,
            models = listOf(
                ProviderModel("gpt-4o", "GPT-4o", "openai"),
                ProviderModel("gpt-4o-mini", "GPT-4o Mini", "openai"),
                ProviderModel("o3-mini", "o3 Mini", "openai")
            )
        ),
        ProviderConfig(
            id = "anthropic",
            name = "Anthropic",
            baseUrl = "https://api.anthropic.com/v1",
            type = ProviderType.ANTHROPIC,
            models = listOf(
                ProviderModel("claude-3-7-sonnet-20250219", "Claude 3.7 Sonnet", "anthropic"),
                ProviderModel("claude-3-5-haiku-20241022", "Claude 3.5 Haiku", "anthropic")
            )
        ),
        ProviderConfig(
            id = "ollama-local",
            name = "Ollama Local",
            baseUrl = "http://127.0.0.1:11434/v1",
            type = ProviderType.OLLAMA,
            isTermuxLocal = true,
            models = listOf(
                ProviderModel("qwen2.5-coder:latest", "Qwen 2.5 Coder", "ollama-local"),
                ProviderModel("llama3.2:latest", "Llama 3.2", "ollama-local")
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

    fun addCustomModel(providerId: String, modelId: String, displayName: String = modelId) {
        if (modelId.isBlank()) return
        val current = _providers.value.toMutableList()
        val index = current.indexOfFirst { it.id == providerId }
        if (index != -1) {
            val p = current[index]
            if (p.models.none { it.id == modelId }) {
                val updatedModels = p.models + ProviderModel(
                    id = modelId.trim(),
                    name = displayName.ifBlank { modelId.trim() },
                    providerId = providerId,
                    isCustom = true
                )
                current[index] = p.copy(models = updatedModels)
                _providers.value = current
                saveProviders(current)
            }
        }
    }

    fun addProvider(
        name: String,
        baseUrl: String,
        apiKey: String = "",
        type: ProviderType = ProviderType.OPENAI_COMPATIBLE
    ): ProviderConfig {
        val newP = ProviderConfig(
            id = "custom-" + UUID.randomUUID().toString().take(8),
            name = name.trim(),
            baseUrl = baseUrl.trim().removeSuffix("/"),
            apiKey = apiKey.trim(),
            type = type,
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

    fun deleteProvider(providerId: String) {
        val updated = _providers.value.filter { it.id != providerId }
        _providers.value = updated
        saveProviders(updated)
    }

    suspend fun fetchModelsFromEndpoint(providerId: String): Result<List<String>> = withContext(Dispatchers.IO) {
        val provider = _providers.value.find { it.id == providerId }
            ?: return@withContext Result.failure(IllegalArgumentException("Provider not found"))

        val endpoint = when {
            provider.baseUrl.endsWith("/v1") -> "${provider.baseUrl}/models"
            provider.baseUrl.endsWith("/v1beta") -> "${provider.baseUrl}/models"
            else -> "${provider.baseUrl}/v1/models"
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
                    val existing = current[idx].models.map { it.id }.toSet()
                    val newModels = current[idx].models + modelIds.filter { it !in existing }.map {
                        ProviderModel(it, it, providerId)
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

package com.hermes.android.ipc

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.buildJsonArray
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.serialization.json.put
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.IOException
import java.util.concurrent.TimeUnit

/**
 * Universal Bridge connecting to:
 * 1. Termux Hermes Gateway (`/chat/stream`)
 * 2. Any OpenAI-compatible Provider / OpenRouter / 9Router / Ollama (`/chat/completions`)
 */
class HermesBridge(
    private val defaultGatewayUrl: String = "http://127.0.0.1:8765",
    private val pairingCode: String? = null,
) {
    private val http = OkHttpClient.Builder()
        .connectTimeout(15, TimeUnit.SECONDS)
        .readTimeout(0, TimeUnit.SECONDS) // stream
        .build()
    private val json = Json { ignoreUnknownKeys = true }

    data class Health(val ok: Boolean, val version: String?, val latencyMs: Long?, val error: String? = null)

    suspend fun health(targetUrl: String = defaultGatewayUrl): Health {
        val t0 = System.currentTimeMillis()
        return try {
            val req = Request.Builder().url("$targetUrl/health").get().build()
            http.newCall(req).execute().use { res ->
                val body = res.body?.string().orEmpty()
                val version = runCatching {
                    json.parseToJsonElement(body).jsonObject["version"]?.jsonPrimitive?.content
                }.getOrNull()
                Health(
                    ok = res.isSuccessful,
                    version = version,
                    latencyMs = System.currentTimeMillis() - t0,
                    error = if (res.isSuccessful) null else "HTTP ${res.code}: ${body.take(240)}",
                )
            }
        } catch (e: Exception) {
            Health(false, null, null, e.message)
        }
    }

    /**
     * Streams one turn from either Hermes Gateway or any OpenAI-compatible provider.
     */
    fun send(
        sessionId: String,
        text: String,
        baseUrl: String = defaultGatewayUrl,
        apiKey: String? = null,
        model: String = "hermes-agent",
        isOpenAiCompatible: Boolean = false
    ): Flow<StreamEvent> = flow {
        val request = if (isOpenAiCompatible) {
            val endpoint = when {
                baseUrl.endsWith("/chat/completions") -> baseUrl
                baseUrl.endsWith("/v1") -> "$baseUrl/chat/completions"
                baseUrl.endsWith("/v1beta") -> "$baseUrl/chat/completions"
                else -> "$baseUrl/v1/chat/completions"
            }
            val payload = json.encodeToString(
                kotlinx.serialization.json.JsonObject.serializer(),
                buildJsonObject {
                    put("model", model)
                    put("stream", true)
                    put("messages", buildJsonArray {
                        add(buildJsonObject {
                            put("role", "user")
                            put("content", text)
                        })
                    })
                }
            )
            val builder = Request.Builder()
                .url(endpoint)
                .post(payload.toRequestBody(JSON_MEDIA_TYPE))
            if (!apiKey.isNullOrBlank()) {
                builder.addHeader("Authorization", "Bearer $apiKey")
            }
            builder.build()
        } else {
            val payload = json.encodeToString(
                kotlinx.serialization.json.JsonObject.serializer(),
                buildJsonObject {
                    put("session_id", sessionId)
                    put("message", text)
                    put("model", model)
                }
            )
            Request.Builder()
                .url("$baseUrl/chat/stream")
                .post(payload.toRequestBody(JSON_MEDIA_TYPE))
                .apply { pairingCode?.let { header("X-Pairing-Code", it) } }
                .build()
        }

        http.newCall(request).execute().use { response ->
            if (!response.isSuccessful) {
                val detail = response.body?.string().orEmpty().take(500)
                throw IOException("Backend HTTP ${response.code}: $detail")
            }
            val source = response.body?.source() ?: throw IOException("Empty stream received")
            while (!source.exhausted()) {
                val line = source.readUtf8Line()?.trim().orEmpty()
                if (line.isEmpty()) continue
                val clean = line.removePrefix("data:").trim()
                if (clean == "[DONE]") {
                    emit(StreamEvent.Done)
                    break
                }
                emit(StreamParser.parseLine(clean))
            }
        }
    }.flowOn(Dispatchers.IO)

    fun interrupt(sessionId: String, targetUrl: String = defaultGatewayUrl) {
        runCatching {
            val req = Request.Builder().url("$targetUrl/sessions/$sessionId/interrupt")
                .post("{}".toRequestBody(JSON_MEDIA_TYPE)).build()
            http.newCall(req).execute().close()
        }
    }

    private companion object {
        val JSON_MEDIA_TYPE = "application/json".toMediaType()
    }
}

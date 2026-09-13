package com.hermes.android.ipc

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.serialization.json.Json
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
 * Bridge to the on-device Hermes backend. Transport priority:
 * ACP when available, then the loopback gateway, then the PTY bridge.
 */
class HermesBridge(
    private val gatewayUrl: String = "http://127.0.0.1:8765",
    private val pairingCode: String? = null,
) {
    private val http = OkHttpClient.Builder()
        .connectTimeout(10, TimeUnit.SECONDS)
        .readTimeout(0, TimeUnit.SECONDS)
        .build()
    private val json = Json { ignoreUnknownKeys = true }

    data class Health(val ok: Boolean, val version: String?, val latencyMs: Long?, val error: String? = null)

    suspend fun health(): Health {
        val t0 = System.currentTimeMillis()
        return try {
            val req = Request.Builder().url("$gatewayUrl/health").get().build()
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

    /** Streams one agent turn as text deltas and tool lifecycle events. */
    fun send(sessionId: String, text: String): Flow<StreamEvent> = flow {
        val payload = json.encodeToString(
            kotlinx.serialization.json.JsonObject.serializer(),
            buildJsonObject {
                put("session_id", sessionId)
                put("message", text)
            },
        )
        val req = Request.Builder()
            .url("$gatewayUrl/chat/stream")
            .post(payload.toRequestBody(JSON_MEDIA_TYPE))
            .apply { pairingCode?.let { header("X-Pairing-Code", it) } }
            .build()
        http.newCall(req).execute().use { response ->
            if (!response.isSuccessful) {
                val detail = response.body?.string().orEmpty().take(500)
                throw IOException("Hermes gateway HTTP ${response.code}: $detail")
            }
            val source = response.body?.source() ?: throw IOException("Hermes gateway returned an empty stream")
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

    fun interrupt(sessionId: String) {
        runCatching {
            val req = Request.Builder().url("$gatewayUrl/sessions/$sessionId/interrupt")
                .post("{}".toRequestBody(JSON_MEDIA_TYPE)).build()
            http.newCall(req).execute().use { response ->
                if (!response.isSuccessful) throw IOException("Interrupt failed: HTTP ${response.code}")
            }
        }
    }

    private companion object {
        val JSON_MEDIA_TYPE = "application/json".toMediaType()
    }
}

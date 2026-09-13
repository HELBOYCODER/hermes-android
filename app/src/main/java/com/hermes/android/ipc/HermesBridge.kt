package com.hermes.android.ipc

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import java.util.concurrent.TimeUnit

/**
 * Bridge to the on-device Hermes backend. Transport priority:
 * 1. ACP (agent-client protocol) if available
 * 2. Gateway local HTTP/WebSocket (127.0.0.1)
 * 3. PTY bridge with [StreamParser] (structured streaming parser)
 */
class HermesBridge(
    private val gatewayUrl: String = "http://127.0.0.1:8765",
    private val pairingCode: String? = null,
) {
    private val http = OkHttpClient.Builder()
        .connectTimeout(10, TimeUnit.SECONDS)
        .readTimeout(0, TimeUnit.SECONDS) // streaming
        .build()
    private val json = Json { ignoreUnknownKeys = true }

    data class Health(val ok: Boolean, val version: String?, val latencyMs: Long?, val error: String? = null)

    suspend fun health(): Health {
        val t0 = System.currentTimeMillis()
        return try {
            val req = Request.Builder().url("$gatewayUrl/health").get().build()
            http.newCall(req).execute().use { res ->
                val body = res.body?.string().orEmpty()
                val v = runCatching {
                    json.parseToJsonElement(body).jsonObject["version"]?.jsonPrimitive?.content
                }.getOrNull()
                Health(res.isSuccessful, v, System.currentTimeMillis() - t0)
            }
        } catch (e: Exception) {
            Health(false, null, null, e.message)
        }
    }

    /** Streams one agent turn: text deltas + tool events, parsed by [StreamParser]. */
    fun send(sessionId: String, text: String): Flow<StreamEvent> = flow {
        val payload = """{"session_id":"$sessionId","message":"${text.replace("\"", "\\\"")}"}"""
        val req = Request.Builder()
            .url("$gatewayUrl/chat/stream")
            .post(payload.toRequestBody("application/json".toMediaType()))
            .apply { pairingCode?.let { header("X-Pairing-Code", it) } }
            .build()
        http.newCall(req).execute().use { res ->
            val src = res.body?.source() ?: return@use
            // ponytail: line-delimited JSON stream; SSE 'data:' prefix tolerated.
            while (!src.exhausted()) {
                val line = src.readUtf8Line()?.trim().orEmpty()
                if (line.isEmpty()) continue
                val clean = line.removePrefix("data:").trim()
                if (clean == "[DONE]") break
                emit(StreamParser.parseLine(clean))
            }
        }
    }.flowOn(Dispatchers.IO)

    fun interrupt(sessionId: String) {
        runCatching {
            val req = Request.Builder().url("$gatewayUrl/sessions/$sessionId/interrupt")
                .post("{}".toRequestBody("application/json".toMediaType())).build()
            http.newCall(req).execute().close()
        }
    }
}

package com.hermes.android.ipc

import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive

/** Stream events surfaced to Chat UI: token deltas + tool lifecycle. */
sealed interface StreamEvent {
    data class Token(val text: String) : StreamEvent
    data class ToolStart(val id: String, val name: String, val input: String) : StreamEvent
    data class ToolOutput(val id: String, val chunk: String) : StreamEvent
    data class ToolEnd(val id: String, val ok: Boolean, val summary: String) : StreamEvent
    data class Session(val id: String) : StreamEvent
    data class Error(val message: String) : StreamEvent
    data object Done : StreamEvent
}

/**
 * Parses one line of the backend stream (JSON object or SSE data: line).
 * Pure + unit-tested (see StreamParserTest).
 */
object StreamParser {
    private val json = Json { ignoreUnknownKeys = true }

    fun parseLine(line: String): StreamEvent {
        val clean = line.removePrefix("data:").trim()
        if (clean.isEmpty()) return StreamEvent.Token("")
        if (clean == "[DONE]") return StreamEvent.Done
        val el = runCatching { json.parseToJsonElement(clean).jsonObject }.getOrNull()
            ?: return StreamEvent.Token(clean) // plain-text fallback
        val type = el["type"]?.jsonPrimitive?.content.orEmpty()
        return when (type) {
            "token" -> StreamEvent.Token(el["text"]?.jsonPrimitive?.content.orEmpty())
            "tool_start" -> StreamEvent.ToolStart(
                el["id"]?.jsonPrimitive?.content.orEmpty(),
                el["name"]?.jsonPrimitive?.content.orEmpty(),
                el["input"]?.toString().orEmpty()
            )
            "tool_output" -> StreamEvent.ToolOutput(
                el["id"]?.jsonPrimitive?.content.orEmpty(),
                el["chunk"]?.jsonPrimitive?.content.orEmpty()
            )
            "tool_end" -> StreamEvent.ToolEnd(
                el["id"]?.jsonPrimitive?.content.orEmpty(),
                el["ok"]?.jsonPrimitive?.content == "true",
                el["summary"]?.jsonPrimitive?.content.orEmpty()
            )
            "session" -> StreamEvent.Session(el["id"]?.jsonPrimitive?.content.orEmpty())
            "error" -> StreamEvent.Error(el["message"]?.jsonPrimitive?.content.orEmpty())
            else -> StreamEvent.Token(
                el["text"]?.jsonPrimitive?.content ?: clean
            )
        }
    }
}

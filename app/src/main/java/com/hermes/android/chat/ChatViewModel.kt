package com.hermes.android.chat

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hermes.android.ipc.HermesBridge
import com.hermes.android.ipc.StreamEvent
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.util.UUID

data class ToolCall(
    val id: String,
    val name: String,
    val input: String,
    val output: StringBuilder = StringBuilder(),
    var done: Boolean = false,
    var ok: Boolean = true,
    var summary: String = ""
)

data class ChatMsg(
    val id: String = UUID.randomUUID().toString(),
    val role: String, // user | assistant
    val text: StringBuilder = StringBuilder(),
    val tools: MutableList<ToolCall> = mutableListOf(),
    val nested: MutableList<ChatMsg> = mutableListOf(), // subagents / execute_code threads
)

class ChatViewModel(
    private val bridge: HermesBridge = HermesBridge(),
) : ViewModel() {
    private val _msgs = MutableStateFlow<List<ChatMsg>>(emptyList())
    val msgs: StateFlow<List<ChatMsg>> = _msgs
    private val _busy = MutableStateFlow(false)
    val busy: StateFlow<Boolean> = _busy
    private val _session = MutableStateFlow("s-" + UUID.randomUUID().toString().take(8))
    val session: StateFlow<String> = _session

    private var job: Job? = null

    fun send(text: String) {
        if (text.isBlank() || _busy.value) return
        val user = ChatMsg(role = "user").also { it.text.append(text) }
        val ai = ChatMsg(role = "assistant")
        _msgs.value = _msgs.value + user + ai
        _busy.value = true
        job = viewModelScope.launch {
            try {
                bridge.send(_session.value, text).collect { ev ->
                    when (ev) {
                        is StreamEvent.Token -> ai.text.append(ev.text)
                        is StreamEvent.ToolStart -> ai.tools += ToolCall(ev.id, ev.name, ev.input)
                        is StreamEvent.ToolOutput -> ai.tools.find { it.id == ev.id }?.output?.append(ev.chunk)
                        is StreamEvent.ToolEnd -> ai.tools.find { it.id == ev.id }?.apply {
                            done = true; ok = ev.ok; summary = ev.summary
                        }
                        is StreamEvent.Session -> _session.value = ev.id
                        is StreamEvent.Error -> ai.text.append("\n⚠ ${ev.message}")
                        StreamEvent.Done -> {}
                    }
                    _msgs.value = _msgs.value.toList() // re-emit (stable keys in UI)
                }
            } catch (e: Exception) {
                ai.text.append("\n⚠ connection failed: ${e.message}")
            } finally {
                _busy.value = false
                _msgs.value = _msgs.value.toList()
            }
        }
    }

    /** Interrupt-and-redirect: stop current turn, send replacement. */
    fun interruptAndRedirect(replacement: String) {
        job?.cancel()
        bridge.interrupt(_session.value)
        _busy.value = false
        if (replacement.isNotBlank()) send(replacement)
    }

    fun interrupt() {
        job?.cancel()
        bridge.interrupt(_session.value)
        _busy.value = false
    }
}

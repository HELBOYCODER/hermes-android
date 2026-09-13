package com.hermes.android.chat

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.hermes.android.data.models.ProviderConfig
import com.hermes.android.data.models.ProviderRepository
import com.hermes.android.data.models.ProviderType
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
    val modelName: String = "",
    val tools: MutableList<ToolCall> = mutableListOf(),
    val nested: MutableList<ChatMsg> = mutableListOf(),
)

class ChatViewModel(application: Application) : AndroidViewModel(application) {

    val repository = ProviderRepository(application)
    private val bridge = HermesBridge()

    private val _msgs = MutableStateFlow<List<ChatMsg>>(emptyList())
    val msgs: StateFlow<List<ChatMsg>> = _msgs

    private val _busy = MutableStateFlow(false)
    val busy: StateFlow<Boolean> = _busy

    private val _session = MutableStateFlow("s-" + UUID.randomUUID().toString().take(8))
    val session: StateFlow<String> = _session

    val activeModelId: StateFlow<String> = repository.activeModelId
    val activeProviderId: StateFlow<String> = repository.activeProviderId
    val providers: StateFlow<List<ProviderConfig>> = repository.providers

    private var job: Job? = null

    fun selectModel(providerId: String, modelId: String) {
        repository.setActiveModel(providerId, modelId)
    }

    fun clearChat() {
        _msgs.value = emptyList()
        _session.value = "s-" + UUID.randomUUID().toString().take(8)
    }

    fun send(text: String) {
        if (text.isBlank() || _busy.value) return

        val provider = repository.providers.value.find { it.id == repository.activeProviderId.value }
            ?: repository.providers.value.firstOrNull()
        val model = repository.activeModelId.value

        val user = ChatMsg(role = "user").also { it.text.append(text) }
        val ai = ChatMsg(role = "assistant", modelName = model)
        _msgs.value = _msgs.value + user + ai
        _busy.value = true

        val isOpenAi = provider?.type != ProviderType.HERMES_GATEWAY
        val baseUrl = provider?.baseUrl ?: "http://127.0.0.1:8765"
        val apiKey = provider?.apiKey

        job = viewModelScope.launch {
            try {
                bridge.send(
                    sessionId = _session.value,
                    text = text,
                    baseUrl = baseUrl,
                    apiKey = apiKey,
                    model = model,
                    isOpenAiCompatible = isOpenAi
                ).collect { ev ->
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
                    _msgs.value = _msgs.value.toList()
                }
            } catch (e: Exception) {
                ai.text.append("\n⚠ Connection failed: ${e.message}\n(Make sure Termux Hermes or target provider is reachable at $baseUrl)")
            } finally {
                _busy.value = false
                _msgs.value = _msgs.value.toList()
            }
        }
    }

    fun interrupt() {
        job?.cancel()
        val provider = repository.providers.value.find { it.id == repository.activeProviderId.value }
        bridge.interrupt(_session.value, provider?.baseUrl ?: "http://127.0.0.1:8765")
        _busy.value = false
    }
}

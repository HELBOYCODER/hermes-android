package com.hermes.android.ui.models

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.hermes.android.llm.LlamaServerManager
import kotlinx.coroutines.launch

/** LOCAL tab: llama.cpp server manager — library, RAM warnings, benchmark, start/stop. */
@Composable
fun LocalModelsScreen() {
    val ctx = LocalContext.current
    val mgr = remember { LlamaServerManager(ctx) }
    val status by mgr.status.collectAsState()
    val scope = rememberCoroutineScope()
    var progress by remember { mutableStateOf<Float?>(null) }
    var note by remember { mutableStateOf("") }

    Column(Modifier.fillMaxSize().padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text("Local models (llama.cpp)", style = MaterialTheme.typography.headlineSmall)
        Text("Device RAM: ${"%.1f".format(mgr.deviceRamGb())} GB · endpoint ${LlamaServerManager.ENDPOINT} (auto-registered as Hermes custom provider)",
            style = MaterialTheme.typography.bodySmall)
        Card(Modifier.fillMaxWidth()) {
            Column(Modifier.padding(12.dp)) {
                Text("Server: ${if (status.running) "● running (${status.modelId})" else "○ stopped"}",
                    style = MaterialTheme.typography.labelLarge)
                if (status.tokPerSec != null) Text("${status.tokPerSec} tok/s")
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedButton(onClick = { scope.launch { mgr.stop() } }) { Text("Stop") }
                }
            }
        }
        if (progress != null) LinearProgressIndicator(progress = { progress!! }, Modifier.fillMaxWidth())
        if (note.isNotEmpty()) Text(note, style = MaterialTheme.typography.bodySmall)
        LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            items(mgr.library, key = { it.id }) { m ->
                val ok = m.minRamGb <= mgr.deviceRamGb()
                Card(Modifier.fillMaxWidth()) {
                    Column(Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text(m.id, style = MaterialTheme.typography.labelLarge)
                        Text("${m.sizeGb} GB · needs ${m.minRamGb} GB RAM" + if (!ok) " ⚠ too big for this device" else "",
                            style = MaterialTheme.typography.bodySmall,
                            color = if (!ok) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurface)
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            OutlinedButton(onClick = {
                                scope.launch {
                                    runCatching { mgr.download(m) { progress = it } }
                                        .onSuccess { note = "${m.id} downloaded ✓"; progress = null }
                                        .onFailure { note = "download failed: ${it.message}"; progress = null }
                                }
                            }) { Text("Download") }
                            Button(onClick = {
                                scope.launch {
                                    runCatching { mgr.start(m) }
                                        .onSuccess { note = "${m.id} serving on ${LlamaServerManager.ENDPOINT}" }
                                        .onFailure { note = "start failed: ${it.message}" }
                                }
                            }, enabled = ok) { Text("Run") }
                        }
                    }
                }
            }
        }
    }
}

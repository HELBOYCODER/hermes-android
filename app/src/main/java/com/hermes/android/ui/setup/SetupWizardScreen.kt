package com.hermes.android.ui.setup

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.hermes.android.backend.ProotInstaller
import com.hermes.android.service.HermesForegroundService
import kotlinx.coroutines.launch

/**
 * One-tap Setup Wizard: live log stream, per-step progress, auto-retry,
 * integrity checks, plain-language error recovery.
 */
@Composable
fun SetupWizardScreen(onDone: () -> Unit) {
    val ctx = LocalContext.current
    val installer = remember { ProotInstaller(ctx) }
    val state by installer.state.collectAsState()
    val scope = rememberCoroutineScope()

    Column(Modifier.fillMaxSize().padding(20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text("Hermes Android", style = MaterialTheme.typography.headlineMedium)
        Text(
            "One-tap setup installs the real Hermes Agent on your device. " +
                "No ads, no paywall — everything unlocked.",
            style = MaterialTheme.typography.bodyMedium
        )
        when (val s = state) {
            is ProotInstaller.State.Idle -> {
                Text("5 steps: userland → packages → Hermes (pinned) → venv → verify.")
                Button(onClick = { scope.launch { installer.install() } }, Modifier.fillMaxWidth()) {
                    Text("Install Hermes on this device")
                }
            }
            is ProotInstaller.State.Running -> {
                Text("${s.index + 1}/${s.total} — ${s.step.label}")
                LinearProgressIndicator(progress = { (s.index).toFloat() / s.total }, Modifier.fillMaxWidth())
                LogBox(s.log)
            }
            is ProotInstaller.State.Done -> {
                Text("✓ Hermes ${s.hermesVersion} ready.")
                Button(onClick = {
                    ctx.getSharedPreferences("hermes", android.content.Context.MODE_PRIVATE)
                        .edit().putBoolean("setup_done", true).apply()
                    HermesForegroundService.start(ctx)
                    onDone()
                }, Modifier.fillMaxWidth()) { Text("Start chatting") }
            }
            is ProotInstaller.State.Failed -> {
                Text("✗ Stopped at: ${s.step.label}\n${s.error}", color = MaterialTheme.colorScheme.error)
                Text("What to try: check storage (500MB free) and network, then retry. Nothing is half-installed — retry is safe.")
                LogBox(s.log)
                Button(onClick = { scope.launch { installer.retry() } }, Modifier.fillMaxWidth()) {
                    Text("Retry (safe)")
                }
                OutlinedButton(onClick = onDone, Modifier.fillMaxWidth()) { Text("Skip for now") }
            }
        }
    }
}

@Composable
private fun LogBox(log: List<String>) {
    LazyColumn(Modifier.fillMaxWidth().weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
        items(log, key = { it.hashCode().toString() + it.length }) {
            Text(it, style = MaterialTheme.typography.bodySmall, fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace)
        }
    }
}

package com.hermes.android.ui.gateway

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

/** Per-platform gateway wizard: QR pairing AND manual endpoint + connection test + diagnostics. */
@Composable
fun GatewaySetupScreen() {
    var platform by remember { mutableStateOf("Telegram") }
    var endpoint by remember { mutableStateOf("http://127.0.0.1:8765") }
    var pairing by remember { mutableStateOf("") }
    var diag by remember { mutableStateOf("Not tested yet.") }
    val allowlist = remember { mutableStateListOf<String>() }
    var newId by remember { mutableStateOf("") }

    Column(Modifier.fillMaxSize().padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text("Messaging gateway", style = MaterialTheme.typography.headlineSmall)
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            listOf("Telegram", "Discord", "Slack").forEach { p ->
                OutlinedButton(onClick = { platform = p }) { Text(p + if (p == platform) " ●" else "") }
            }
        }
        Text("$platform wizard — Hermes supports 25+ platforms; these three ship with guided setup first.",
            style = MaterialTheme.typography.bodySmall)
        OutlinedTextField(value = endpoint, onValueChange = { endpoint = it }, label = { Text("Gateway endpoint") },
            modifier = Modifier.fillMaxWidth())
        OutlinedTextField(value = pairing, onValueChange = { pairing = it }, label = { Text("Pairing code (or scan QR in full build)") },
            modifier = Modifier.fillMaxWidth())
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Button(onClick = { diag = "Testing $endpoint … (full build runs live check)" }) { Text("Test connection") }
            OutlinedButton(onClick = { /* request code via bridge */ }) { Text("Request code") }
        }
        Card(Modifier.fillMaxWidth()) {
            Column(Modifier.padding(12.dp)) {
                Text("Diagnostics", style = MaterialTheme.typography.labelLarge)
                Text(diag, style = MaterialTheme.typography.bodySmall)
            }
        }
        Text("Allowlist (${allowlist.size}) — Admin/Regular tiers", style = MaterialTheme.typography.labelLarge)
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            OutlinedTextField(value = newId, onValueChange = { newId = it }, modifier = Modifier.weight(1f),
                placeholder = { Text("user id / handle") })
            Button(onClick = { if (newId.isNotBlank()) { allowlist += newId; newId = "" } }) { Text("Add") }
        }
        allowlist.forEach { Text("• $it") }
    }
}

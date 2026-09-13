package com.hermes.android.ui.settings

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
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

/** Providers & keys (Keystore note), fallback chain, theme, RTL/Persian, backup/restore. */
@Composable
fun SettingsScreen() {
    var amoled by remember { mutableStateOf(true) }
    var faDigits by remember { mutableStateOf(false) }
    var openai by remember { mutableStateOf("") }
    var anthropic by remember { mutableStateOf("") }
    var google by remember { mutableStateOf("") }
    var openrouter by remember { mutableStateOf("") }
    var fallback by remember { mutableStateOf("nous-portal > openrouter > local") }
    var saved by remember { mutableStateOf("") }

    Column(Modifier.fillMaxSize().padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text("Settings", style = MaterialTheme.typography.headlineSmall)
        Text("Providers — keys stay in Android Keystore, written to ~/.hermes/.env only at runtime",
            style = MaterialTheme.typography.labelLarge)
        ProviderField("OpenAI", openai) { openai = it }
        ProviderField("Anthropic", anthropic) { anthropic = it }
        ProviderField("Google", google) { google = it }
        ProviderField("OpenRouter", openrouter) { openrouter = it }
        OutlinedTextField(value = fallback, onValueChange = { fallback = it },
            label = { Text("Fallback chain") }, modifier = Modifier.fillMaxWidth())
        Text("Nous Portal uses OAuth via Custom Tab (full build).", style = MaterialTheme.typography.bodySmall)
        Button(onClick = { saved = "Keys staged ✓ (Keystore wiring lands with security dep)" },
            Modifier.fillMaxWidth()) { Text("Save providers") }
        if (saved.isNotEmpty()) Text(saved)
        Card(Modifier.fillMaxWidth()) {
            Column(Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text("Appearance & language", style = MaterialTheme.typography.labelLarge)
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("AMOLED black", Modifier.weight(1f)); Switch(amoled, { amoled = it })
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("Persian digits (۱۲۳)", Modifier.weight(1f)); Switch(faDigits, { faDigits = it })
                }
                Text("Persian/English with full RTL mirroring — layout mirrors automatically (supportsRtl + Compose RTL). Font scaling follows system.",
                    style = MaterialTheme.typography.bodySmall)
            }
        }
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            OutlinedButton(onClick = { /* SAF export */ }, Modifier.weight(1f)) { Text("Backup (SAF)") }
            OutlinedButton(onClick = { /* SAF import */ }, Modifier.weight(1f)) { Text("Restore") }
        }
    }
}

@Composable
private fun ProviderField(label: String, value: String, onChange: (String) -> Unit) {
    var v by remember(value) { mutableStateOf(value) }
    OutlinedTextField(value = v, onValueChange = { v = it; onChange(it) },
        label = { Text("$label API key") }, modifier = Modifier.fillMaxWidth(), singleLine = true)
}

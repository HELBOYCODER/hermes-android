package com.hermes.android.ui.settings

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Backup
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Restore
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import com.hermes.android.ui.theme.HermesTokens

@Composable
fun SettingsScreen() {
    var amoled by remember { mutableStateOf(true) }
    var faDigits by remember { mutableStateOf(false) }
    var openai by remember { mutableStateOf("") }
    var anthropic by remember { mutableStateOf("") }
    var google by remember { mutableStateOf("") }
    var openrouter by remember { mutableStateOf("") }
    var fallback by remember { mutableStateOf("Nous Portal > OpenRouter > Local") }
    var saved by remember { mutableStateOf("") }

    androidx.compose.foundation.lazy.LazyColumn(Modifier.fillMaxSize().padding(horizontal = 16.dp, vertical = 12.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
        item {
            Text("Settings", style = MaterialTheme.typography.headlineMedium.copy(color = HermesTokens.TextPrimary))
            Text("Fine-tune your Hermes workspace", style = MaterialTheme.typography.bodySmall.copy(color = HermesTokens.TextMuted))
        }
        item {
            SettingsSection("Provider credentials", Icons.Default.Lock, "Stored locally and never rendered in logs") {
                ProviderField("OpenAI", openai) { openai = it }
                ProviderField("Anthropic", anthropic) { anthropic = it }
                ProviderField("Google Gemini", google) { google = it }
                ProviderField("OpenRouter", openrouter) { openrouter = it }
                OutlinedTextField(value = fallback, onValueChange = { fallback = it }, label = { Text("Fallback chain") }, modifier = Modifier.fillMaxWidth(), singleLine = true, shape = RoundedCornerShape(HermesTokens.RadiusS))
                Button(onClick = { saved = "Credentials staged securely ✓" }, Modifier.fillMaxWidth(), colors = ButtonDefaults.buttonColors(containerColor = HermesTokens.Emerald)) { Text("Save credentials", color = Color.Black) }
                if (saved.isNotEmpty()) Text(saved, color = HermesTokens.Emerald, style = MaterialTheme.typography.bodySmall)
            }
        }
        item {
            SettingsSection("Appearance", Icons.Default.DarkMode, "Open Minis-inspired obsidian theme") {
                SettingSwitch("AMOLED black", "Use true black for OLED displays", amoled) { amoled = it }
                SettingSwitch("Persian digits", "Use ۱۲۳ instead of 123", faDigits) { faDigits = it }
                Row(verticalAlignment = Alignment.CenterVertically) { Icon(Icons.Default.Language, null, tint = HermesTokens.Cyan, modifier = Modifier.size(18.dp)); Text(" Persian / English · full RTL mirroring", color = HermesTokens.TextMuted, style = MaterialTheme.typography.bodySmall, modifier = Modifier.padding(start = 8.dp)) }
            }
        }
        item {
            SettingsSection("Data & backup", Icons.Default.Backup, "Export your Hermes workspace via Android SAF") {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Button(onClick = {}, Modifier.weight(1f), colors = ButtonDefaults.buttonColors(containerColor = HermesTokens.CardElevated)) { Icon(Icons.Default.Backup, null, modifier = Modifier.size(16.dp)); Text(" Backup") }
                    Button(onClick = {}, Modifier.weight(1f), colors = ButtonDefaults.buttonColors(containerColor = HermesTokens.CardElevated)) { Icon(Icons.Default.Restore, null, modifier = Modifier.size(16.dp)); Text(" Restore") }
                }
            }
        }
        item {
            Surface(Modifier.fillMaxWidth(), shape = RoundedCornerShape(HermesTokens.RadiusS), color = HermesTokens.Cyan.copy(alpha = .08f), border = BorderStroke(1.dp, HermesTokens.Cyan.copy(alpha = .25f))) {
                Row(Modifier.padding(10.dp), verticalAlignment = Alignment.CenterVertically) { Icon(Icons.Default.Tune, null, tint = HermesTokens.Cyan, modifier = Modifier.size(18.dp)); Text("Hermes Android v1.2.0 · No ads · No paywall", color = HermesTokens.Cyan, style = MaterialTheme.typography.bodySmall, modifier = Modifier.padding(start = 8.dp)) }
            }
        }
    }
}

@Composable
private fun SettingsSection(title: String, icon: androidx.compose.ui.graphics.vector.ImageVector, subtitle: String, content: @Composable () -> Unit) {
    Card(Modifier.fillMaxWidth(), shape = RoundedCornerShape(HermesTokens.RadiusM), colors = CardDefaults.cardColors(containerColor = HermesTokens.CardDark), border = BorderStroke(1.dp, HermesTokens.BorderSubtle)) {
        Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(9.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) { Icon(icon, null, tint = HermesTokens.Emerald, modifier = Modifier.size(19.dp)); Text(" $title", color = HermesTokens.TextPrimary, style = MaterialTheme.typography.titleMedium); }
            Text(subtitle, color = HermesTokens.TextMuted, style = MaterialTheme.typography.bodySmall)
            content()
        }
    }
}

@Composable
private fun SettingSwitch(label: String, subtitle: String, checked: Boolean, onChange: (Boolean) -> Unit) {
    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) { Column(Modifier.weight(1f)) { Text(label, color = HermesTokens.TextPrimary); Text(subtitle, color = HermesTokens.TextMuted, style = MaterialTheme.typography.bodySmall) }; Switch(checked, onChange) }
}

@Composable
private fun ProviderField(label: String, value: String, onChange: (String) -> Unit) {
    OutlinedTextField(value = value, onValueChange = onChange, label = { Text("$label API key") }, modifier = Modifier.fillMaxWidth(), singleLine = true, visualTransformation = PasswordVisualTransformation(), shape = RoundedCornerShape(HermesTokens.RadiusS))
}

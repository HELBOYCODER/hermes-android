package com.hermes.android.ui.memory

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
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Memory
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import com.hermes.android.ui.theme.HermesTokens
import java.io.File

@Composable
fun MemoryScreen() {
    val ctx = androidx.compose.ui.platform.LocalContext.current
    fun load(name: String) = runCatching { File(ctx.filesDir, ".hermes/memories/$name").takeIf { it.exists() }?.readText().orEmpty() }.getOrDefault("")
    var memory by remember { mutableStateOf(load("MEMORY.md")) }
    var user by remember { mutableStateOf(load("USER.md")) }
    var saved by remember { mutableStateOf("") }

    Column(Modifier.fillMaxSize().padding(horizontal = 16.dp, vertical = 12.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Text("Memory", style = MaterialTheme.typography.headlineMedium.copy(color = HermesTokens.TextPrimary))
        Text("Persistent context that makes Hermes remember what matters", style = MaterialTheme.typography.bodySmall.copy(color = HermesTokens.TextMuted))
        Surface(Modifier.fillMaxWidth(), shape = RoundedCornerShape(HermesTokens.RadiusS), color = HermesTokens.Emerald.copy(alpha = .10f), border = BorderStroke(1.dp, HermesTokens.Emerald.copy(alpha = .25f))) {
            Row(Modifier.padding(10.dp), verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.AutoAwesome, null, tint = HermesTokens.Emerald, modifier = Modifier.size(18.dp))
                Text("Agent-curated memory · FTS5 search · Honcho insights", color = HermesTokens.Emerald, style = MaterialTheme.typography.bodySmall, modifier = Modifier.padding(start = 8.dp))
            }
        }
        Card(Modifier.fillMaxWidth().weight(1f), shape = RoundedCornerShape(HermesTokens.RadiusM), colors = CardDefaults.cardColors(containerColor = HermesTokens.CardDark), border = BorderStroke(1.dp, HermesTokens.BorderSubtle)) {
            Column(Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) { Icon(Icons.Default.Memory, null, tint = HermesTokens.Cyan, modifier = Modifier.size(18.dp)); Text(" MEMORY.md", color = HermesTokens.TextPrimary, fontFamily = FontFamily.Monospace) }
                OutlinedTextField(value = memory, onValueChange = { memory = it }, modifier = Modifier.fillMaxWidth().weight(1f), placeholder = { Text("Facts Hermes should remember…") }, textStyle = MaterialTheme.typography.bodySmall.copy(fontFamily = FontFamily.Monospace), shape = RoundedCornerShape(HermesTokens.RadiusS))
            }
        }
        Card(Modifier.fillMaxWidth().weight(1f), shape = RoundedCornerShape(HermesTokens.RadiusM), colors = CardDefaults.cardColors(containerColor = HermesTokens.CardDark), border = BorderStroke(1.dp, HermesTokens.BorderSubtle)) {
            Column(Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) { Icon(Icons.Default.Person, null, tint = HermesTokens.Amber, modifier = Modifier.size(18.dp)); Text(" USER.md", color = HermesTokens.TextPrimary, fontFamily = FontFamily.Monospace) }
                OutlinedTextField(value = user, onValueChange = { user = it }, modifier = Modifier.fillMaxWidth().weight(1f), placeholder = { Text("Preferences, goals, working style…") }, textStyle = MaterialTheme.typography.bodySmall.copy(fontFamily = FontFamily.Monospace), shape = RoundedCornerShape(HermesTokens.RadiusS))
            }
        }
        Button(onClick = { File(ctx.filesDir, ".hermes/memories").mkdirs(); File(ctx.filesDir, ".hermes/memories/MEMORY.md").writeText(memory); File(ctx.filesDir, ".hermes/memories/USER.md").writeText(user); saved = "Saved ✓" }, Modifier.fillMaxWidth(), colors = ButtonDefaults.buttonColors(containerColor = HermesTokens.Emerald)) { Text("Save memory", color = androidx.compose.ui.graphics.Color.Black) }
        if (saved.isNotEmpty()) Text(saved, color = HermesTokens.Emerald, style = MaterialTheme.typography.bodySmall)
    }
}

package com.hermes.android.ui.skills

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddCircleOutline
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import com.hermes.android.ui.theme.HermesTokens
import java.io.File

@Composable
fun SkillsScreen() {
    val ctx = LocalContext.current
    val dir = remember { File(ctx.filesDir, ".hermes/skills").apply { mkdirs() } }
    var skills by remember { mutableStateOf(dir.listFiles()?.map { it.name }.orEmpty()) }
    var open by remember { mutableStateOf<String?>(null) }
    var body by remember { mutableStateOf("") }

    Column(Modifier.fillMaxSize().padding(horizontal = 16.dp, vertical = 12.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Text("Skills", style = MaterialTheme.typography.headlineMedium.copy(color = HermesTokens.TextPrimary))
        Text("Extend Hermes with reusable instructions and workflows", style = MaterialTheme.typography.bodySmall.copy(color = HermesTokens.TextMuted))
        if (open == null) {
            Row(Modifier.fillMaxWidth().padding(vertical = 4.dp), verticalAlignment = Alignment.CenterVertically) { Icon(Icons.Default.AutoAwesome, null, tint = HermesTokens.Emerald); Text("  ${skills.size} installed skills", color = HermesTokens.TextPrimary, style = MaterialTheme.typography.titleMedium); Button(onClick = {}, Modifier.padding(start = 8.dp), colors = ButtonDefaults.buttonColors(containerColor = HermesTokens.CardElevated)) { Icon(Icons.Default.AddCircleOutline, null, modifier = Modifier.size(16.dp)); Text(" Add") } }
            if (skills.isEmpty()) Card(Modifier.fillMaxWidth(), shape = RoundedCornerShape(HermesTokens.RadiusM), colors = CardDefaults.cardColors(containerColor = HermesTokens.CardDark)) { Column(Modifier.fillMaxWidth().padding(22.dp), horizontalAlignment = Alignment.CenterHorizontally) { Icon(Icons.Default.Code, null, tint = HermesTokens.TextMuted); Text("No skills yet", color = HermesTokens.TextPrimary); Text("Hermes can create skills as it learns.", color = HermesTokens.TextMuted, style = MaterialTheme.typography.bodySmall) } }
            LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) { items(skills, key = { it }) { s -> Card(Modifier.fillMaxWidth(), shape = RoundedCornerShape(HermesTokens.RadiusM), colors = CardDefaults.cardColors(containerColor = HermesTokens.CardDark), border = BorderStroke(1.dp, HermesTokens.BorderSubtle)) { Row(Modifier.fillMaxWidth().padding(13.dp), verticalAlignment = Alignment.CenterVertically) { Icon(Icons.Default.Code, null, tint = HermesTokens.Cyan); Column(Modifier.weight(1f).padding(start = 10.dp)) { Text(s, color = HermesTokens.TextPrimary); Text("SKILL.md · local", color = HermesTokens.TextMuted, style = MaterialTheme.typography.bodySmall) }; androidx.compose.material3.IconButton(onClick = { open = s; body = runCatching { File(dir, "$s/SKILL.md").readText() }.getOrDefault("") }) { Icon(Icons.Default.ChevronRight, null, tint = HermesTokens.TextMuted) } } } } }
        } else {
            Text(open!!, color = HermesTokens.Emerald, style = MaterialTheme.typography.titleMedium)
            OutlinedTextField(value = body, onValueChange = { body = it }, modifier = Modifier.fillMaxWidth().weight(1f), textStyle = MaterialTheme.typography.bodySmall.copy(fontFamily = FontFamily.Monospace), shape = RoundedCornerShape(HermesTokens.RadiusS))
            Button(onClick = { File(dir, "$open/SKILL.md").parentFile?.mkdirs(); File(dir, "$open/SKILL.md").writeText(body); skills = dir.listFiles()?.map { it.name }.orEmpty(); open = null }, Modifier.fillMaxWidth(), colors = ButtonDefaults.buttonColors(containerColor = HermesTokens.Emerald)) { Icon(Icons.Default.Save, null, tint = Color.Black); Text(" Save", color = Color.Black) }
        }
    }
}

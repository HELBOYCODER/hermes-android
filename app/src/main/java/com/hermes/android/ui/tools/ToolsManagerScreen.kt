package com.hermes.android.ui.tools

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Memory
import androidx.compose.material.icons.filled.Terminal
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.hermes.android.data.Toolsets
import com.hermes.android.ui.theme.HermesTokens

@Composable
fun ToolsManagerScreen() {
    val toggles = remember { mutableStateMapOf<String, Boolean>().apply { Toolsets.all.forEach { put(it.id, it.supported) } } }
    LazyColumn(Modifier.fillMaxSize().padding(horizontal = 16.dp, vertical = 12.dp), verticalArrangement = Arrangement.spacedBy(9.dp)) {
        item {
            Text("Tools", style = MaterialTheme.typography.headlineMedium.copy(color = HermesTokens.TextPrimary))
            Text("Control what Hermes can access and execute", style = MaterialTheme.typography.bodySmall.copy(color = HermesTokens.TextMuted))
        }
        item {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                SummaryChip("${Toolsets.all.count { it.supported }}", "available", HermesTokens.Emerald, Modifier.weight(1f))
                SummaryChip("${Toolsets.all.count { !it.supported }}", "desktop-only", HermesTokens.Amber, Modifier.weight(1f))
            }
        }
        item { Text("TOOLSETS", style = MaterialTheme.typography.labelSmall.copy(color = HermesTokens.TextMuted, letterSpacing = 1.sp)) }
        items(Toolsets.all, key = { it.id }) { t ->
            val icon = when (t.id) { "terminal", "file" -> Icons.Default.Terminal; "web", "browser" -> Icons.Default.Language; "memory", "session_search" -> Icons.Default.Memory; "code_execution", "debugging" -> Icons.Default.Code; else -> Icons.Default.Build }
            Card(Modifier.fillMaxWidth(), shape = RoundedCornerShape(HermesTokens.RadiusM), colors = CardDefaults.cardColors(containerColor = HermesTokens.CardDark), border = BorderStroke(1.dp, if (t.supported) HermesTokens.BorderSubtle else HermesTokens.Amber.copy(alpha = .25f))) {
                Row(Modifier.fillMaxWidth().padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                    Icon(icon, null, tint = if (t.supported) HermesTokens.Cyan else HermesTokens.Amber, modifier = Modifier.padding(end = 10.dp))
                    Column(Modifier.weight(1f)) { Text(t.label, color = HermesTokens.TextPrimary, style = MaterialTheme.typography.labelLarge); if (!t.supported) Text(t.whyNot, color = HermesTokens.TextMuted, style = MaterialTheme.typography.bodySmall) }
                    if (t.supported) { Icon(Icons.Default.CheckCircle, null, tint = HermesTokens.Emerald, modifier = Modifier.padding(end = 6.dp)); Switch(toggles[t.id] == true, { toggles[t.id] = it }, colors = SwitchDefaults.colors(checkedThumbColor = HermesTokens.Emerald, checkedTrackColor = HermesTokens.Emerald.copy(alpha = .3f))) } else Icon(Icons.Default.Warning, null, tint = HermesTokens.Amber)
                }
            }
        }
    }
}

@Composable
private fun SummaryChip(value: String, label: String, color: Color, modifier: Modifier) {
    Card(modifier, shape = RoundedCornerShape(HermesTokens.RadiusM), colors = CardDefaults.cardColors(containerColor = color.copy(alpha = .10f)), border = BorderStroke(1.dp, color.copy(alpha = .25f))) { Row(Modifier.padding(10.dp), verticalAlignment = Alignment.CenterVertically) { Text(value, color = color, style = MaterialTheme.typography.titleLarge); Text("  $label", color = HermesTokens.TextMuted, fontSize = 11.sp) } }
}

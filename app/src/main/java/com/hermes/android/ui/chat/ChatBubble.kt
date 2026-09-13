package com.hermes.android.ui.chat

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.HourglassEmpty
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
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
import com.hermes.android.chat.ChatMsg
import com.hermes.android.chat.ToolCall

@Composable
fun ChatBubble(role: String, text: String, tools: List<ToolCall>, nested: List<ChatMsg>) {
    val isUser = role == "user"
    Card(
        modifier = Modifier.fillMaxWidth(if (isUser) 1f else 0.98f)
            .padding(start = if (isUser) 40.dp else 0.dp, end = if (isUser) 0.dp else 8.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isUser) MaterialTheme.colorScheme.primaryContainer
            else MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(Modifier.padding(10.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            if (text.isNotBlank()) Text(text, style = MaterialTheme.typography.bodyMedium)
            tools.forEach { ToolCallCard(it) }
            nested.forEach { sub ->
                Text("↳ subagent thread (${sub.tools.size} tools)", style = MaterialTheme.typography.labelSmall)
                ChatBubble(sub.role, sub.text.toString(), sub.tools.toList(), sub.nested.toList())
            }
        }
    }
}

/** Expandable tool-call card with status colors. */
@Composable
fun ToolCallCard(t: ToolCall) {
    var open by remember { mutableStateOf(false) }
    val (icon, tint) = when {
        !t.done -> Icons.Default.HourglassEmpty to MaterialTheme.colorScheme.tertiary
        t.ok -> Icons.Default.CheckCircle to MaterialTheme.colorScheme.primary
        else -> Icons.Default.Error to MaterialTheme.colorScheme.error
    }
    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(Modifier.fillMaxWidth().padding(8.dp), verticalAlignment = Alignment.CenterVertically) {
            Icon(icon, t.name, tint = tint)
            Text(" ${t.name}", style = MaterialTheme.typography.labelLarge, modifier = Modifier.weight(1f))
            if (t.done && t.summary.isNotBlank()) Text(t.summary.take(40), style = MaterialTheme.typography.bodySmall)
            IconButton(onClick = { open = !open }) {
                Icon(if (open) Icons.Default.ExpandLess else Icons.Default.ExpandMore, "expand")
            }
        }
        if (open) Column(Modifier.padding(8.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text("input: ${t.input.take(500)}", fontFamily = FontFamily.Monospace, style = MaterialTheme.typography.bodySmall)
            Text(t.output.take(2000).toString(), fontFamily = FontFamily.Monospace, style = MaterialTheme.typography.bodySmall)
        }
    }
}

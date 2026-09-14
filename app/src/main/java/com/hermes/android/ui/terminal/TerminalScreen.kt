package com.hermes.android.ui.terminal

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Terminal
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.hermes.android.ui.theme.HermesTokens
import kotlinx.coroutines.launch

/** Minis-inspired embedded terminal: status pill, dark code surface, scrollback and key row. */
@Composable
fun TerminalScreen() {
    val lines = remember { mutableStateListOf("$ hermes", "Hermes shell ready — bash · python · git", "Type a command below to begin.") }
    var cmd by remember { mutableStateOf("") }
    val state = rememberLazyListState()
    val scope = rememberCoroutineScope()
    val clipboard = LocalClipboardManager.current

    Column(Modifier.fillMaxSize().padding(horizontal = 16.dp, vertical = 12.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            Column { Text("Terminal", style = MaterialTheme.typography.headlineMedium.copy(color = HermesTokens.TextPrimary)); Text("On-device shell connected to Hermes", color = HermesTokens.TextMuted, style = MaterialTheme.typography.bodySmall) }
            Surface(shape = RoundedCornerShape(HermesTokens.RadiusPill), color = HermesTokens.Emerald.copy(alpha = .14f)) { Row(Modifier.padding(horizontal = 9.dp, vertical = 5.dp), verticalAlignment = Alignment.CenterVertically) { Icon(Icons.Default.Terminal, null, tint = HermesTokens.Emerald, modifier = Modifier.size(13.dp)); Text(" READY", color = HermesTokens.Emerald, fontSize = 10.sp) } }
        }
        Card(Modifier.fillMaxWidth().weight(1f), shape = RoundedCornerShape(HermesTokens.RadiusM), colors = CardDefaults.cardColors(containerColor = HermesTokens.CodeBlockBg), border = BorderStroke(1.dp, HermesTokens.BorderSubtle)) {
            LazyColumn(Modifier.fillMaxSize().padding(12.dp), state = state, verticalArrangement = Arrangement.spacedBy(3.dp)) {
                itemsIndexed(lines, key = { i, line -> "$i-${line.hashCode()}" }) { _, line -> Text(line, color = if (line.startsWith("$")) HermesTokens.Emerald else Color(0xFFB8C7D9), fontFamily = FontFamily.Monospace, style = MaterialTheme.typography.bodySmall) }
            }
        }
        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) { listOf("TAB", "CTRL", "ESC", "|", "~", "/").forEach { key -> OutlinedButton(onClick = { cmd += when (key) { "TAB" -> "\t"; "ESC" -> "\u001B"; else -> key } }, modifier = Modifier.height(34.dp)) { Text(key, style = MaterialTheme.typography.labelSmall) } } }
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
            OutlinedTextField(value = cmd, onValueChange = { cmd = it }, modifier = Modifier.weight(1f), placeholder = { Text("run bash, python, git…") }, textStyle = TextStyle(fontFamily = FontFamily.Monospace, fontSize = 13.sp), singleLine = true, shape = RoundedCornerShape(HermesTokens.RadiusM))
            IconButton(onClick = { clipboard.setText(AnnotatedString(lines.joinToString("\n"))) }) { Icon(Icons.Default.ContentCopy, "Copy output", tint = HermesTokens.TextMuted) }
            Surface(shape = RoundedCornerShape(HermesTokens.RadiusM), color = HermesTokens.Emerald, modifier = Modifier.size(50.dp)) { IconButton(onClick = { if (cmd.isNotBlank()) { lines += "\$ $cmd"; lines += "(PTY → proot userland)"; cmd = ""; scope.launch { state.animateScrollToItem(lines.size - 1) } } }) { Icon(Icons.Default.PlayArrow, "Run", tint = Color.Black) } }
        }
        Row(verticalAlignment = Alignment.CenterVertically) { Icon(Icons.Default.Code, null, tint = HermesTokens.Cyan, modifier = Modifier.size(15.dp)); Spacer(Modifier.width(6.dp)); Text("Terminal output is redacted before logs leave the device.", color = HermesTokens.TextMuted, style = MaterialTheme.typography.bodySmall) }
    }
}

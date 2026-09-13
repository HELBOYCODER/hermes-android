package com.hermes.android.ui.terminal

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch

/**
 * Embedded PTY v1: scrollback + command line + special-keys row.
 * Full terminal-emulator view plugs into this seam (xterm-vt glue).
 */
@Composable
fun TerminalScreen() {
    val lines = remember { mutableStateListOf("\$ hermes — full Linux shell (bash, python, git)", "\$ type a command below") }
    var cmd by remember { mutableStateOf("") }
    val state = rememberLazyListState()
    val scope = rememberCoroutineScope()

    Column(Modifier.fillMaxSize().padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text("Terminal", style = MaterialTheme.typography.headlineSmall)
        LazyColumn(
            Modifier.weight(1f).fillMaxWidth().background(Color.Black).padding(8.dp),
            state = state, verticalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            itemsIndexed(lines, key = { i, l -> "$i-${l.hashCode()}" }) { _, line ->
                Text(line, color = Color(0xFF33FF66), fontFamily = FontFamily.Monospace,
                    style = MaterialTheme.typography.bodySmall)
            }
        }
        // special-keys row
        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            listOf("TAB", "CTRL", "ESC", "|", "~", "/").forEach { k ->
                androidx.compose.material3.OutlinedButton(onClick = { cmd += when (k) {
                    "TAB" -> "\t"; "ESC" -> "\u001B"; else -> k
                } }) { Text(k, style = MaterialTheme.typography.labelSmall) }
            }
        }
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            OutlinedTextField(value = cmd, onValueChange = { cmd = it }, modifier = Modifier.weight(1f),
                placeholder = { Text("bash…") }, fontFamily = FontFamily.Monospace)
            Button(onClick = {
                lines += "\$ $cmd"
                lines += "(exec via proot userland — full PTY lands with terminal-emulator dep)"
                cmd = ""
                scope.launch { state.animateScrollToItem(lines.size - 1) }
            }) { Text("Run") }
        }
    }
}

package com.hermes.android.ui.memory

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import java.io.File

@Composable
fun MemoryScreen() {
    val ctx = LocalContext.current
    fun load(name: String) = runCatching {
        File(ctx.filesDir, ".hermes/memories/$name").takeIf { it.exists() }?.readText().orEmpty()
    }.getOrDefault("")
    var memory by remember { mutableStateOf(load("MEMORY.md")) }
    var user by remember { mutableStateOf(load("USER.md")) }
    var saved by remember { mutableStateOf("") }

    Column(Modifier.fillMaxSize().padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text("Memory — agent-curated, persistent", style = MaterialTheme.typography.headlineSmall)
        Text("MEMORY.md", style = MaterialTheme.typography.labelLarge)
        OutlinedTextField(value = memory, onValueChange = { memory = it }, modifier = Modifier.fillMaxWidth().weight(1f))
        Text("USER.md (who you are — Honcho user modeling reads this)", style = MaterialTheme.typography.labelLarge)
        OutlinedTextField(value = user, onValueChange = { user = it }, modifier = Modifier.fillMaxWidth().weight(1f))
        Button(onClick = {
            File(ctx.filesDir, ".hermes/memories").mkdirs()
            File(ctx.filesDir, ".hermes/memories/MEMORY.md").writeText(memory)
            File(ctx.filesDir, ".hermes/memories/USER.md").writeText(user)
            saved = "Saved ✓"
        }, Modifier.fillMaxWidth()) { Text("Save memory") }
        if (saved.isNotEmpty()) Text(saved)
        Text("Nudge history + Honcho insights appear here once the backend syncs (FTS5 search lives in Chat /search).",
            style = MaterialTheme.typography.bodySmall)
    }
}

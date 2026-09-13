package com.hermes.android.ui.soul

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import java.io.File

/** SOUL.md personality editor + context-files manager (~/.hermes/). */
@Composable
fun SoulScreen() {
    val ctx = LocalContext.current
    val home = remember { File(ctx.filesDir, ".hermes").apply { mkdirs() } }
    fun load(name: String) = runCatching { File(home, name).takeIf { it.exists() }?.readText().orEmpty() }.getOrDefault("")
    var soul by remember { mutableStateOf(load("SOUL.md")) }
    var saved by remember { mutableStateOf("") }
    val extra = remember { mutableStateListOf<String>().apply {
        addAll(home.listFiles()?.filter { it.isFile && it.name.endsWith(".md") }?.map { it.name }.orEmpty())
    } }
    var newFile by remember { mutableStateOf("") }

    Column(Modifier.fillMaxSize().padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text("Soul — personality & context files", style = MaterialTheme.typography.headlineSmall)
        Text("SOUL.md", style = MaterialTheme.typography.labelLarge)
        OutlinedTextField(value = soul, onValueChange = { soul = it },
            modifier = Modifier.fillMaxWidth().weight(1f))
        Button(onClick = {
            File(home, "SOUL.md").writeText(soul)
            saved = "SOUL.md saved ✓"
        }, Modifier.fillMaxWidth()) { Text("Save SOUL.md") }
        if (saved.isNotEmpty()) Text(saved)
        Text("Context files in ~/.hermes/", style = MaterialTheme.typography.labelLarge)
        extra.forEach { Text("• $it") }
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            OutlinedTextField(value = newFile, onValueChange = { newFile = it }, modifier = Modifier.weight(1f),
                placeholder = { Text("NOTES.md") })
            OutlinedButton(onClick = {
                if (newFile.isNotBlank()) {
                    File(home, newFile).apply { if (!exists()) writeText("# $newFile\n") }
                    extra.clear()
                    extra.addAll(home.listFiles()?.filter { it.isFile && it.name.endsWith(".md") }?.map { it.name }.orEmpty())
                    newFile = ""
                }
            }) { Text("Add") }
        }
    }
}

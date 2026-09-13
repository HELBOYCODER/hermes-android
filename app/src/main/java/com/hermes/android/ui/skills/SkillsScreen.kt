package com.hermes.android.ui.skills

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Card
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
fun SkillsScreen() {
    val ctx = LocalContext.current
    val dir = remember { File(ctx.filesDir, ".hermes/skills").apply { mkdirs() } }
    var skills by remember { mutableStateOf(dir.listFiles()?.map { it.name }.orEmpty()) }
    var open by remember { mutableStateOf<String?>(null) }
    var body by remember { mutableStateOf("") }

    Column(Modifier.fillMaxSize().padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text("Skills — browse, edit, Skills Hub", style = MaterialTheme.typography.headlineSmall)
        if (open == null) {
            if (skills.isEmpty()) Text("No skills yet — Hermes can create them autonomously as you work (self-improving skills).")
            LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                items(skills, key = { it }) { s ->
                    Card(Modifier.fillMaxWidth()) {
                        Column(Modifier.padding(12.dp)) {
                            Text(s, style = MaterialTheme.typography.labelLarge)
                            Button(onClick = {
                                open = s
                                body = runCatching { File(dir, "$s/SKILL.md").readText() }.getOrDefault("")
                            }) { Text("Edit SKILL.md") }
                        }
                    }
                }
            }
        } else {
            Text(open!!, style = MaterialTheme.typography.labelLarge)
            OutlinedTextField(value = body, onValueChange = { body = it },
                modifier = Modifier.fillMaxWidth().weight(1f))
            Button(onClick = {
                File(dir, "$open/SKILL.md").writeText(body)
                skills = dir.listFiles()?.map { it.name }.orEmpty()
                open = null
            }, Modifier.fillMaxWidth()) { Text("Save") }
        }
    }
}

package com.hermes.android.ui.cron

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Card
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
import androidx.compose.ui.unit.dp

data class CronJob(val id: String, val what: String, var paused: Boolean = false, val nextRun: String = "—")

@Composable
fun CronScreen() {
    val jobs = remember { mutableStateListOf<CronJob>() }
    var input by remember { mutableStateOf("") }
    Column(Modifier.fillMaxSize().padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text("Cron — natural-language scheduling", style = MaterialTheme.typography.headlineSmall)
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            OutlinedTextField(value = input, onValueChange = { input = it }, modifier = Modifier.weight(1f),
                placeholder = { Text("e.g. every morning at 8 summarize my calendar") })
            Button(onClick = {
                if (input.isNotBlank()) { jobs += CronJob("j${jobs.size}", input); input = "" }
            }) { Text("Add") }
        }
        Text("Note: Android may pause jobs when the screen is off unless battery optimization is disabled (see Setup).",
            style = MaterialTheme.typography.bodySmall)
        LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            items(jobs, key = { it.id }) { j ->
                Card(Modifier.fillMaxWidth()) {
                    Column(Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text(j.what, style = MaterialTheme.typography.bodyMedium)
                        Text("next run: ${j.nextRun}${if (j.paused) " · paused" else ""}",
                            style = MaterialTheme.typography.bodySmall)
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            OutlinedButton(onClick = { j.paused = !j.paused }) {
                                Text(if (j.paused) "Resume" else "Pause")
                            }
                            OutlinedButton(onClick = { /* run now via bridge */ }) { Text("Run now") }
                            OutlinedButton(onClick = { jobs.remove(j) }) { Text("Remove") }
                        }
                    }
                }
            }
        }
    }
}

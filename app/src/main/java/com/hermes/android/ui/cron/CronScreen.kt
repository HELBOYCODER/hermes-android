package com.hermes.android.ui.cron

import androidx.compose.foundation.BorderStroke
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.hermes.android.ui.theme.HermesTokens

data class CronJob(val id: String, val what: String, var paused: Boolean = false, val nextRun: String = "Not scheduled")

@Composable
fun CronScreen() {
    val jobs = remember { mutableStateListOf<CronJob>() }
    var input by remember { mutableStateOf("") }
    LazyColumn(
        Modifier.fillMaxSize().padding(horizontal = 16.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        item {
            Text("Automation", style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold, color = HermesTokens.TextPrimary))
            Text("Schedule Hermes tasks with natural language", style = MaterialTheme.typography.bodySmall.copy(color = HermesTokens.TextMuted))
        }
        item {
            Card(Modifier.fillMaxWidth(), shape = RoundedCornerShape(HermesTokens.RadiusM), colors = CardDefaults.cardColors(containerColor = HermesTokens.CardDark), border = BorderStroke(1.dp, HermesTokens.BorderSubtle)) {
                Column(Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Schedule, null, tint = HermesTokens.Emerald, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(8.dp))
                        Text("New scheduled task", color = HermesTokens.TextPrimary, fontWeight = FontWeight.SemiBold)
                    }
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.Bottom) {
                        OutlinedTextField(value = input, onValueChange = { input = it }, modifier = Modifier.weight(1f), placeholder = { Text("every morning at 8 summarize…") }, singleLine = true, shape = RoundedCornerShape(HermesTokens.RadiusS))
                        Button(onClick = { if (input.isNotBlank()) { jobs += CronJob("j${jobs.size}", input.trim()); input = "" } }, colors = ButtonDefaults.buttonColors(containerColor = HermesTokens.Emerald)) {
                            Icon(Icons.Default.Add, null, tint = androidx.compose.ui.graphics.Color.Black, modifier = Modifier.size(17.dp))
                        }
                    }
                }
            }
        }
        item {
            Surface(Modifier.fillMaxWidth(), shape = RoundedCornerShape(HermesTokens.RadiusS), color = HermesTokens.Amber.copy(alpha = .10f), border = BorderStroke(1.dp, HermesTokens.Amber.copy(alpha = .25f))) {
                Text("Android can pause background work. Keep Hermes foreground service active and disable battery optimization for reliable jobs.", color = HermesTokens.Amber, fontSize = 11.sp, modifier = Modifier.padding(10.dp))
            }
        }
        item { Text("SCHEDULED (${jobs.size})", style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, color = HermesTokens.TextMuted, letterSpacing = 1.sp)) }
        if (jobs.isEmpty()) item {
            Card(Modifier.fillMaxWidth(), shape = RoundedCornerShape(HermesTokens.RadiusM), colors = CardDefaults.cardColors(containerColor = HermesTokens.AssistantBubble)) {
                Column(Modifier.fillMaxWidth().padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(Icons.Default.CalendarMonth, null, tint = HermesTokens.TextMuted, modifier = Modifier.size(30.dp))
                    Spacer(Modifier.height(8.dp)); Text("No automations yet", color = HermesTokens.TextPrimary, fontWeight = FontWeight.SemiBold); Text("Your scheduled jobs will appear here.", color = HermesTokens.TextMuted, fontSize = 12.sp)
                }
            }
        }
        items(jobs, key = { it.id }) { j ->
            Card(Modifier.fillMaxWidth(), shape = RoundedCornerShape(HermesTokens.RadiusM), colors = CardDefaults.cardColors(containerColor = HermesTokens.CardDark), border = BorderStroke(1.dp, if (j.paused) HermesTokens.BorderSubtle else HermesTokens.Emerald.copy(alpha = .35f))) {
                Row(Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Schedule, null, tint = if (j.paused) HermesTokens.TextMuted else HermesTokens.Emerald, modifier = Modifier.size(20.dp))
                    Spacer(Modifier.width(10.dp))
                    Column(Modifier.weight(1f)) { Text(j.what, color = HermesTokens.TextPrimary, fontSize = 13.sp); Text("${j.nextRun}${if (j.paused) " · paused" else ""}", color = HermesTokens.TextMuted, fontSize = 11.sp) }
                    IconButton(onClick = { j.paused = !j.paused }) { Icon(if (j.paused) Icons.Default.PlayArrow else Icons.Default.Pause, null, tint = HermesTokens.Cyan) }
                    IconButton(onClick = { jobs.remove(j) }) { Icon(Icons.Default.DeleteOutline, null, tint = HermesTokens.Error) }
                }
            }
        }
    }
}

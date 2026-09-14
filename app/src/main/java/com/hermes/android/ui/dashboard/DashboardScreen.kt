package com.hermes.android.ui.dashboard

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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Chat
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Memory
import androidx.compose.material.icons.filled.Terminal
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.hermes.android.ui.theme.HermesTokens

/** Native Minis-style overview: compact stat cards, runtime health, and activity feed. */
@Composable
fun DashboardScreen() {
    LazyColumn(
        Modifier.fillMaxSize().padding(horizontal = 16.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Column {
                Text(
                    "Overview",
                    style = MaterialTheme.typography.headlineMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = HermesTokens.TextPrimary
                    )
                )
                Text(
                    "Your Hermes workspace at a glance",
                    style = MaterialTheme.typography.bodySmall.copy(color = HermesTokens.TextMuted)
                )
            }
        }
        item {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                MetricCard("Sessions", "—", Icons.Default.Chat, HermesTokens.Emerald, Modifier.weight(1f))
                MetricCard("Tool calls", "—", Icons.Default.Bolt, HermesTokens.Cyan, Modifier.weight(1f))
            }
        }
        item {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                MetricCard("Memory", "—", Icons.Default.Memory, HermesTokens.Amber, Modifier.weight(1f))
                MetricCard("Cron jobs", "—", Icons.Default.CalendarMonth, HermesTokens.Info, Modifier.weight(1f))
            }
        }
        item {
            Card(
                Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(HermesTokens.RadiusM),
                colors = CardDefaults.cardColors(containerColor = HermesTokens.CardDark),
                border = BorderStroke(1.dp, HermesTokens.BorderSubtle)
            ) {
                Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.CheckCircle, null, tint = HermesTokens.Emerald, modifier = Modifier.size(18.dp))
                            Spacer(Modifier.width(8.dp))
                            Text("Runtime health", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold, color = HermesTokens.TextPrimary))
                        }
                        Surface(shape = RoundedCornerShape(HermesTokens.RadiusPill), color = HermesTokens.Emerald.copy(alpha = .14f)) {
                            Text("READY", color = HermesTokens.Emerald, fontSize = 10.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp))
                        }
                    }
                    HealthRow(Icons.Default.Terminal, "Hermes gateway", "Waiting for local connection", HermesTokens.TextMuted)
                    HealthRow(Icons.Default.Memory, "Persistent memory", "~/.hermes/memories", HermesTokens.TextMuted)
                    HealthRow(Icons.Default.TrendingUp, "Usage analytics", "Live data appears after first session", HermesTokens.TextMuted)
                }
            }
        }
        item {
            Text("Recent activity", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold, color = HermesTokens.TextPrimary))
        }
        item {
            Card(
                Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(HermesTokens.RadiusM),
                colors = CardDefaults.cardColors(containerColor = HermesTokens.AssistantBubble),
                border = BorderStroke(1.dp, HermesTokens.BorderSubtle)
            ) {
                Row(Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Chat, null, tint = HermesTokens.Cyan, modifier = Modifier.size(20.dp))
                    Spacer(Modifier.width(10.dp))
                    Column {
                        Text("No sessions yet", color = HermesTokens.TextPrimary, fontWeight = FontWeight.SemiBold)
                        Text("Start a chat to populate your activity timeline.", color = HermesTokens.TextMuted, fontSize = 12.sp)
                    }
                }
            }
        }
    }
}

@Composable
private fun MetricCard(label: String, value: String, icon: androidx.compose.ui.graphics.vector.ImageVector, tint: Color, modifier: Modifier) {
    Card(modifier, shape = RoundedCornerShape(HermesTokens.RadiusM), colors = CardDefaults.cardColors(containerColor = HermesTokens.CardDark), border = BorderStroke(1.dp, HermesTokens.BorderSubtle)) {
        Column(Modifier.padding(13.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Icon(icon, null, tint = tint, modifier = Modifier.size(19.dp))
            Text(value, style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold, color = HermesTokens.TextPrimary))
            Text(label, style = MaterialTheme.typography.bodySmall.copy(color = HermesTokens.TextMuted))
        }
    }
}

@Composable
private fun HealthRow(icon: androidx.compose.ui.graphics.vector.ImageVector, label: String, detail: String, tint: Color) {
    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        Icon(icon, null, tint = tint, modifier = Modifier.size(16.dp))
        Spacer(Modifier.width(9.dp))
        Text(label, color = HermesTokens.TextPrimary, fontSize = 13.sp, modifier = Modifier.weight(1f))
        Text(detail, color = HermesTokens.TextMuted, fontSize = 11.sp)
    }
}

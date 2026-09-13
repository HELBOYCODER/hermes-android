package com.hermes.android.ui.more

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.Memory
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Wifi
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.hermes.android.Dest
import com.hermes.android.ui.theme.HermesTokens

data class MoreItem(val dest: Dest, val title: String, val subtitle: String, val icon: ImageVector)

@Composable
fun MoreScreen(onGo: (Dest) -> Unit) {
    val items = listOf(
        MoreItem(Dest.SETTINGS, "Settings", "Configure API keys, language, themes & backup", Icons.Default.Settings),
        MoreItem(Dest.TOOLS, "Tools Manager", "Toggle web, files, terminal, python & agent tools", Icons.Default.Code),
        MoreItem(Dest.MEMORY, "Memory & Persona", "Edit MEMORY.md and USER.md persistent knowledge", Icons.Default.Memory),
        MoreItem(Dest.SKILLS, "Skills Hub", "Autonomous skills, custom tools and workflows", Icons.Default.Psychology),
        MoreItem(Dest.CRON, "Cron Automation", "Natural-language scheduled jobs on-device", Icons.Default.Schedule),
        MoreItem(Dest.GATEWAY, "Messaging Gateway", "Connect Telegram, Discord, Slack & 20+ bots", Icons.Default.Wifi),
        MoreItem(Dest.SOUL, "Soul & Directives", "Fine-tune agent personality and system instructions", Icons.Default.Security)
    )

    Column(
        Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Text(
            "System & Capabilities",
            style = MaterialTheme.typography.headlineSmall.copy(
                fontWeight = FontWeight.Bold,
                color = HermesTokens.TextPrimary
            )
        )
        Text(
            "Full suite of Hermes Agent features — unlocked & ad-free",
            style = MaterialTheme.typography.bodySmall.copy(color = HermesTokens.TextMuted)
        )

        LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            items(items, key = { it.dest }) { item ->
                Card(
                    shape = RoundedCornerShape(HermesTokens.RadiusM),
                    colors = CardDefaults.cardColors(containerColor = HermesTokens.CardDark),
                    border = BorderStroke(1.dp, HermesTokens.BorderSubtle),
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onGo(item.dest) }
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(item.icon, contentDescription = null, tint = HermesTokens.Emerald, modifier = Modifier.size(24.dp))
                        Spacer(Modifier.width(12.dp))
                        Column(Modifier.weight(1f)) {
                            Text(
                                item.title,
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.SemiBold,
                                    color = HermesTokens.TextPrimary
                                )
                            )
                            Text(
                                item.subtitle,
                                style = MaterialTheme.typography.bodySmall.copy(color = HermesTokens.TextMuted)
                            )
                        }
                        Icon(Icons.Default.ChevronRight, contentDescription = null, tint = HermesTokens.TextMuted)
                    }
                }
            }
        }
    }
}

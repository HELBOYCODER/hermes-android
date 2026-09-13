package com.hermes.android.ui.more

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.hermes.android.Dest

/** Hub for secondary cloud-backed Hermes capabilities. */
@Composable
fun MoreScreen(onGo: (Dest) -> Unit) {
    val rows = listOf(
        Dest.TOOLS to "🧰 Tools manager",
        Dest.CRON to "⏰ Cron jobs",
        Dest.GATEWAY to "📡 Messaging gateway",
        Dest.SKILLS to "🧠 Skills",
        Dest.SOUL to "✨ Soul & context",
    )
    Column(Modifier.fillMaxSize().padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text("All features", style = MaterialTheme.typography.headlineSmall)
        Text("Cloud-provider mode · no ads · no paywall.", style = MaterialTheme.typography.bodySmall)
        rows.forEach { (destination, label) ->
            Button(onClick = { onGo(destination) }, Modifier.fillMaxWidth()) { Text(label) }
        }
    }
}

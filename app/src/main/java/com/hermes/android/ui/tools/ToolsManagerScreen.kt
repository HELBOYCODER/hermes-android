package com.hermes.android.ui.tools

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.hermes.android.data.Toolsets

@Composable
fun ToolsManagerScreen() {
    val toggles = remember { mutableStateMapOf<String, Boolean>().apply {
        Toolsets.all.forEach { put(it.id, it.supported) }
    } }
    Column(Modifier.fillMaxSize().padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text("Tools Manager", style = MaterialTheme.typography.headlineSmall)
        Text("${Toolsets.all.count { it.supported }} available on Android · ${Toolsets.all.count { !it.supported }} need desktop",
            style = MaterialTheme.typography.bodySmall)
        LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            items(Toolsets.all, key = { it.id }) { t ->
                Card(Modifier.fillMaxWidth()) {
                    Row(Modifier.fillMaxWidth().padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                        Column(Modifier.weight(1f)) {
                            Text(t.label, style = MaterialTheme.typography.labelLarge)
                            if (!t.supported) Text(t.whyNot, style = MaterialTheme.typography.bodySmall)
                        }
                        Switch(
                            checked = toggles[t.id] == true,
                            enabled = t.supported,
                            onCheckedChange = { toggles[t.id] = it }
                        )
                    }
                }
            }
        }
    }
}

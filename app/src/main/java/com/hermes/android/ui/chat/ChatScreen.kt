package com.hermes.android.ui.chat

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CleaningServices
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material.icons.filled.Terminal
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.hermes.android.backend.TermuxDetector
import com.hermes.android.backend.TermuxServiceStatus
import com.hermes.android.chat.ChatViewModel
import com.hermes.android.data.SlashCommands
import com.hermes.android.ui.theme.HermesTokens
import kotlinx.coroutines.launch

@Composable
fun ChatScreen(vm: ChatViewModel = viewModel()) {
    val msgs by vm.msgs.collectAsState()
    val busy by vm.busy.collectAsState()
    val activeModel by vm.activeModelId.collectAsState()
    val providers by vm.providers.collectAsState()
    val context = LocalContext.current
    val termuxDetector = remember { TermuxDetector(context) }
    val termuxStatus by termuxDetector.status.collectAsState()

    var input by remember { mutableStateOf("") }
    var showModelSelector by remember { mutableStateOf(false) }
    var showAddCustomModelDialog by remember { mutableStateOf(false) }
    var customModelInput by remember { mutableStateOf("") }

    val listState = rememberLazyListState()
    val scope = rememberCoroutineScope()
    val showJump by remember {
        derivedStateOf {
            val last = listState.layoutInfo.visibleItemsInfo.lastOrNull()?.index ?: 0
            (msgs.size - 1 - last) > 2
        }
    }

    LaunchedEffect(Unit) {
        termuxDetector.probe()
    }

    LaunchedEffect(msgs.size, msgs.lastOrNull()?.text?.length) {
        if (!showJump && msgs.isNotEmpty()) listState.animateScrollToItem(msgs.size - 1)
    }

    Scaffold(
        topBar = {
            // Minis-Style Top Header Bar
            Surface(
                color = HermesTokens.BgDark,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Model Selector Pill
                    Surface(
                        shape = RoundedCornerShape(HermesTokens.RadiusPill),
                        color = HermesTokens.CardElevated,
                        border = BorderStroke(1.dp, HermesTokens.BorderSubtle),
                        modifier = Modifier.clickable { showModelSelector = true }
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(8.dp)
                                    .clip(CircleShape)
                                    .background(HermesTokens.Emerald)
                            )
                            Spacer(Modifier.width(6.dp))
                            Text(
                                activeModel,
                                style = MaterialTheme.typography.labelMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = HermesTokens.TextPrimary,
                                    fontSize = 13.sp
                                )
                            )
                            Icon(
                                Icons.Default.ArrowDropDown,
                                contentDescription = null,
                                tint = HermesTokens.TextMuted,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }

                    // Right Actions (Termux Status indicator + Clear Session)
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        if (termuxStatus is TermuxServiceStatus.Active) {
                            Surface(
                                shape = RoundedCornerShape(HermesTokens.RadiusPill),
                                color = HermesTokens.Emerald.copy(alpha = 0.15f),
                                modifier = Modifier.padding(end = 6.dp)
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                ) {
                                    Icon(Icons.Default.Terminal, contentDescription = null, tint = HermesTokens.Emerald, modifier = Modifier.size(12.dp))
                                    Spacer(Modifier.width(4.dp))
                                    Text("Termux", style = MaterialTheme.typography.labelSmall.copy(color = HermesTokens.Emerald, fontSize = 10.sp))
                                }
                            }
                        }

                        IconButton(onClick = { vm.clearChat() }, modifier = Modifier.size(32.dp)) {
                            Icon(Icons.Default.CleaningServices, contentDescription = "Clear Chat", tint = HermesTokens.TextMuted, modifier = Modifier.size(18.dp))
                        }
                    }
                }
            }
        },
        floatingActionButton = {
            if (showJump) {
                FloatingActionButton(
                    onClick = { scope.launch { listState.animateScrollToItem(msgs.size - 1) } },
                    containerColor = HermesTokens.CardElevated,
                    contentColor = HermesTokens.Emerald,
                    modifier = Modifier.size(42.dp)
                ) {
                    Icon(Icons.Default.ArrowDownward, "Jump to latest", modifier = Modifier.size(20.dp))
                }
            }
        }
    ) { pad ->
        Column(
            Modifier
                .fillMaxSize()
                .padding(pad)
        ) {
            // Messages List
            LazyColumn(
                Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .padding(horizontal = 10.dp),
                state = listState,
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                if (msgs.isEmpty()) {
                    item {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 40.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Surface(
                                shape = CircleShape,
                                color = HermesTokens.Emerald.copy(alpha = 0.12f),
                                modifier = Modifier.size(54.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Text("H", style = MaterialTheme.typography.headlineMedium.copy(color = HermesTokens.Emerald, fontWeight = FontWeight.Bold))
                                }
                            }
                            Spacer(Modifier.height(12.dp))
                            Text(
                                "Hermes Agent",
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = HermesTokens.TextPrimary
                                )
                            )
                            Text(
                                "Connected to $activeModel",
                                style = MaterialTheme.typography.bodySmall.copy(color = HermesTokens.TextMuted)
                            )
                        }
                    }
                }

                items(msgs, key = { it.id }) { m ->
                    ChatBubble(
                        role = m.role,
                        text = m.text.toString(),
                        modelName = m.modelName,
                        tools = m.tools.toList(),
                        nested = m.nested.toList()
                    )
                }
            }

            // Slash autocomplete palette
            val completions = if (input.startsWith("/")) SlashCommands.complete(input) else emptyList()
            if (completions.isNotEmpty()) {
                Surface(
                    color = HermesTokens.CardElevated,
                    shape = RoundedCornerShape(HermesTokens.RadiusS),
                    border = BorderStroke(1.dp, HermesTokens.BorderSubtle),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 4.dp)
                ) {
                    Column(Modifier.padding(8.dp)) {
                        completions.take(4).forEach { c ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { input = c.trigger + " " }
                                    .padding(vertical = 4.dp)
                            ) {
                                Text(c.trigger, style = MaterialTheme.typography.labelMedium.copy(color = HermesTokens.Emerald, fontWeight = FontWeight.Bold))
                                Spacer(Modifier.width(8.dp))
                                Text(c.hint, style = MaterialTheme.typography.bodySmall.copy(color = HermesTokens.TextMuted))
                            }
                        }
                    }
                }
            }

            // Minis-Style Composer Input Bar
            Surface(
                color = HermesTokens.BgDark,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.Bottom
                ) {
                    OutlinedTextField(
                        value = input,
                        onValueChange = { input = it },
                        modifier = Modifier.weight(1f),
                        placeholder = { Text("Message Hermes… (/ for commands)", color = HermesTokens.TextMuted, fontSize = 14.sp) },
                        shape = RoundedCornerShape(HermesTokens.RadiusL),
                        maxLines = 5
                    )

                    // Action Button (Send / Stop)
                    Surface(
                        shape = CircleShape,
                        color = if (busy) HermesTokens.Amber else HermesTokens.Emerald,
                        modifier = Modifier
                            .size(48.dp)
                            .clickable {
                                if (busy) {
                                    vm.interrupt()
                                } else if (input.isNotBlank()) {
                                    vm.send(input)
                                    input = ""
                                }
                            }
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                if (busy) Icons.Default.Stop else Icons.Default.Send,
                                contentDescription = if (busy) "Stop" else "Send",
                                tint = Color.Black,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                }
            }
        }
    }

    // Model Selector Modal
    if (showModelSelector) {
        AlertDialog(
            onDismissRequest = { showModelSelector = false },
            title = {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Select Model", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold))
                    TextButton(onClick = { showAddCustomModelDialog = true }) {
                        Icon(Icons.Default.Add, contentDescription = null, tint = HermesTokens.Emerald, modifier = Modifier.size(16.dp))
                        Spacer(Modifier.width(4.dp))
                        Text("Custom", color = HermesTokens.Emerald)
                    }
                }
            },
            text = {
                LazyColumn(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    providers.forEach { provider ->
                        item {
                            Text(
                                provider.name.uppercase(),
                                style = MaterialTheme.typography.labelSmall.copy(
                                    color = HermesTokens.Amber,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 10.sp
                                ),
                                modifier = Modifier.padding(top = 6.dp)
                            )
                        }
                        items(provider.models, key = { provider.id + it.id }) { model ->
                            val isSelected = activeModel == model.id
                            Card(
                                shape = RoundedCornerShape(HermesTokens.RadiusS),
                                colors = CardDefaults.cardColors(
                                    containerColor = if (isSelected) HermesTokens.Emerald.copy(alpha = 0.2f) else HermesTokens.CardElevated
                                ),
                                border = BorderStroke(
                                    1.dp,
                                    if (isSelected) HermesTokens.Emerald else HermesTokens.BorderSubtle
                                ),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        vm.selectModel(provider.id, model.id)
                                        showModelSelector = false
                                    }
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 12.dp, vertical = 8.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        model.name,
                                        style = MaterialTheme.typography.bodyMedium.copy(
                                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                            color = if (isSelected) HermesTokens.Emerald else HermesTokens.TextPrimary
                                        )
                                    )
                                    if (isSelected) {
                                        Icon(Icons.Default.Check, contentDescription = null, tint = HermesTokens.Emerald, modifier = Modifier.size(16.dp))
                                    }
                                }
                            }
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showModelSelector = false }) { Text("Close") }
            }
        )
    }

    // Quick Add Custom Model Dialog
    if (showAddCustomModelDialog) {
        AlertDialog(
            onDismissRequest = { showAddCustomModelDialog = false },
            title = { Text("Add & Use Custom Model") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Type any custom model identifier to connect without restrictions:", style = MaterialTheme.typography.bodySmall)
                    OutlinedTextField(
                        value = customModelInput,
                        onValueChange = { customModelInput = it },
                        placeholder = { Text("e.g. gpt-4o, claude-3-7-sonnet, qwen2.5-coder") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (customModelInput.isNotBlank()) {
                            val providerId = providers.firstOrNull()?.id ?: "termux-hermes"
                            vm.repository.addCustomModel(providerId, customModelInput.trim())
                            vm.selectModel(providerId, customModelInput.trim())
                            showAddCustomModelDialog = false
                            showModelSelector = false
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = HermesTokens.Emerald)
                ) {
                    Text("Activate", color = Color.Black)
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddCustomModelDialog = false }) { Text("Cancel") }
            }
        )
    }
}

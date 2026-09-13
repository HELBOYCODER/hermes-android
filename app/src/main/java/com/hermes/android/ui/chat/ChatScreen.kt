package com.hermes.android.ui.chat

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.hermes.android.chat.ChatViewModel
import com.hermes.android.data.SlashCommands
import kotlinx.coroutines.launch

/**
 * Chat: streaming markdown (plain-tokens v1, highlight lands with the
 * renderer dep), expandable ToolCallCards, interrupt-and-redirect,
 * smart auto-scroll + "jump to latest" FAB.
 */
@Composable
fun ChatScreen(vm: ChatViewModel = viewModel()) {
    val msgs by vm.msgs.collectAsState()
    val busy by vm.busy.collectAsState()
    var input by remember { mutableStateOf("") }
    val listState = rememberLazyListState()
    val scope = rememberCoroutineScope()
    val showJump by remember {
        derivedStateOf {
            val last = listState.layoutInfo.visibleItemsInfo.lastOrNull()?.index ?: 0
            (msgs.size - 1 - last) > 2
        }
    }

    LaunchedEffect(msgs.size, msgs.lastOrNull()?.text?.length) {
        if (!showJump && msgs.isNotEmpty()) listState.animateScrollToItem(msgs.size - 1)
    }

    Scaffold(
        floatingActionButton = {
            if (showJump) FloatingActionButton(onClick = {
                scope.launch { listState.animateScrollToItem(msgs.size - 1) }
            }) { Icon(Icons.Default.ArrowDownward, "Jump to latest") }
        }
    ) { pad ->
        Column(Modifier.fillMaxSize().padding(pad)) {
            LazyColumn(
                Modifier.weight(1f).fillMaxWidth().padding(horizontal = 12.dp),
                state = listState,
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(msgs, key = { it.id }) { m ->
                    ChatBubble(
                        role = m.role,
                        text = m.text.toString(),
                        tools = m.tools.toList(),
                        nested = m.nested.toList()
                    )
                }
            }
            // slash autocomplete palette
            val completions = if (input.startsWith("/")) SlashCommands.complete(input) else emptyList()
            if (completions.isNotEmpty()) {
                Column(Modifier.padding(horizontal = 12.dp)) {
                    completions.take(5).forEach { c ->
                        Text("  ${c.trigger} — ${c.hint}", style = MaterialTheme.typography.bodySmall,
                            modifier = Modifier.padding(vertical = 1.dp))
                    }
                }
            }
            Row(Modifier.fillMaxWidth().padding(8.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = input,
                    onValueChange = { input = it },
                    modifier = Modifier.weight(1f),
                    placeholder = { Text("Message Hermes… (/ for commands)") },
                    singleLine = false, maxLines = 5
                )
                if (busy) {
                    IconButton(onClick = { vm.interrupt() }) { Icon(Icons.Default.Stop, "Interrupt") }
                } else {
                    androidx.compose.material3.Button(onClick = {
                        vm.send(input); input = ""
                    }) { Text("Send") }
                }
            }
            if (busy) Text(
                "working… tap ■ to interrupt and redirect",
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.padding(horizontal = 16.dp)
            )
        }
    }
}

package com.hermes.android.ui.chat

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.HourglassEmpty
import androidx.compose.material.icons.filled.Terminal
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.hermes.android.chat.ChatMsg
import com.hermes.android.chat.ToolCall
import com.hermes.android.ui.theme.HermesTokens
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun ChatBubble(role: String, text: String, modelName: String = "", tools: List<ToolCall>, nested: List<ChatMsg>) {
    val isUser = role == "user"
    val clipboard = LocalClipboardManager.current
    var copied by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(
                start = if (isUser) 48.dp else 4.dp,
                end = if (isUser) 4.dp else 48.dp
            ),
        contentAlignment = if (isUser) Alignment.CenterEnd else Alignment.CenterStart
    ) {
        Card(
            shape = RoundedCornerShape(
                topStart = HermesTokens.RadiusM,
                topEnd = HermesTokens.RadiusM,
                bottomStart = if (isUser) HermesTokens.RadiusM else 4.dp,
                bottomEnd = if (isUser) 4.dp else HermesTokens.RadiusM
            ),
            colors = CardDefaults.cardColors(
                containerColor = if (isUser) HermesTokens.UserBubble else HermesTokens.AssistantBubble
            ),
            border = BorderStroke(
                1.dp,
                if (isUser) HermesTokens.Emerald.copy(alpha = 0.3f) else HermesTokens.BorderSubtle
            )
        ) {
            Column(
                modifier = Modifier.padding(12.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                // Header row for assistant (shows model badge + copy button)
                if (!isUser) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(
                            shape = RoundedCornerShape(HermesTokens.RadiusPill),
                            color = HermesTokens.Emerald.copy(alpha = 0.15f)
                        ) {
                            Text(
                                if (modelName.isNotBlank()) modelName else "Hermes",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    color = HermesTokens.Emerald,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 11.sp
                                ),
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                            )
                        }

                        IconButton(
                            onClick = {
                                clipboard.setText(AnnotatedString(text))
                                copied = true
                                scope.launch {
                                    delay(2000)
                                    copied = false
                                }
                            },
                            modifier = Modifier.size(24.dp)
                        ) {
                            Icon(
                                if (copied) Icons.Default.Check else Icons.Default.ContentCopy,
                                contentDescription = "Copy",
                                tint = if (copied) HermesTokens.Emerald else HermesTokens.TextMuted,
                                modifier = Modifier.size(14.dp)
                            )
                        }
                    }
                }

                // Message Text
                if (text.isNotBlank()) {
                    Text(
                        text,
                        style = MaterialTheme.typography.bodyMedium.copy(
                            color = HermesTokens.TextPrimary,
                            lineHeight = 22.sp
                        )
                    )
                }

                // Tool Calls
                if (tools.isNotEmpty()) {
                    Spacer(Modifier.height(4.dp))
                    tools.forEach { ToolCallCard(it) }
                }

                // Subagents
                nested.forEach { sub ->
                    Text("↳ subagent thread (${sub.tools.size} tools)", style = MaterialTheme.typography.labelSmall)
                    ChatBubble(sub.role, sub.text.toString(), sub.modelName, sub.tools.toList(), sub.nested.toList())
                }
            }
        }
    }
}

/** Expandable tool-call card with status colors. */
@Composable
fun ToolCallCard(t: ToolCall) {
    var open by remember { mutableStateOf(false) }
    val (icon, tint) = when {
        !t.done -> Icons.Default.HourglassEmpty to HermesTokens.Amber
        t.ok -> Icons.Default.CheckCircle to HermesTokens.Emerald
        else -> Icons.Default.Error to HermesTokens.Error
    }

    Card(
        shape = RoundedCornerShape(HermesTokens.RadiusS),
        colors = CardDefaults.cardColors(containerColor = HermesTokens.CodeBlockBg),
        border = BorderStroke(1.dp, HermesTokens.BorderSubtle),
        modifier = Modifier
            .fillMaxWidth()
            .clickable { open = !open }
    ) {
        Column(Modifier.padding(8.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(icon, contentDescription = null, tint = tint, modifier = Modifier.size(16.dp))
                Spacer(Modifier.width(6.dp))
                Icon(Icons.Default.Terminal, contentDescription = null, tint = HermesTokens.Cyan, modifier = Modifier.size(14.dp))
                Spacer(Modifier.width(4.dp))
                Text(
                    t.name,
                    style = MaterialTheme.typography.labelMedium.copy(
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold,
                        color = HermesTokens.TextPrimary
                    ),
                    modifier = Modifier.weight(1f)
                )
                if (t.done && t.summary.isNotBlank()) {
                    Text(
                        t.summary.take(30),
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = HermesTokens.TextMuted,
                            fontSize = 11.sp
                        )
                    )
                    Spacer(Modifier.width(4.dp))
                }
                Icon(
                    if (open) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                    contentDescription = null,
                    tint = HermesTokens.TextMuted,
                    modifier = Modifier.size(16.dp)
                )
            }

            if (open) {
                Spacer(Modifier.height(6.dp))
                if (t.input.isNotBlank()) {
                    Text(
                        "in: ${t.input.take(400)}",
                        fontFamily = FontFamily.Monospace,
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = HermesTokens.Amber,
                            fontSize = 11.sp
                        )
                    )
                }
                if (t.output.isNotBlank()) {
                    Spacer(Modifier.height(2.dp))
                    Text(
                        t.output.take(1500).toString(),
                        fontFamily = FontFamily.Monospace,
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = HermesTokens.TextMuted,
                            fontSize = 11.sp
                        )
                    )
                }
            }
        }
    }
}

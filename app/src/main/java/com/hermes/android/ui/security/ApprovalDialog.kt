package com.hermes.android.ui.security

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp

/**
 * Command-approval dialog: once / always / deny + masked sudo prompt.
 * Secrets are redacted before reaching logs (see [redact]).
 */
@Composable
fun ApprovalDialog(
    command: String,
    onOnce: () -> Unit,
    onAlways: () -> Unit,
    onDeny: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDeny,
        title = { Text("Approve command?") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text(redact(command), fontFamily = FontFamily.Monospace)
                Text("Hermes asked to run this on your device. 'Always' remembers this exact command.",
                    style = MaterialTheme.typography.bodySmall)
            }
        },
        confirmButton = {
            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                TextButton(onClick = onDeny) { Text("Deny") }
                OutlinedButton(onClick = onOnce) { Text("Once") }
                OutlinedButton(onClick = onAlways) { Text("Always") }
            }
        }
    )
}

/** Masks sudo prompts + redacts KEY/TOKEN/SECRET values in log lines. */
fun redact(line: String): String {
    var s = line
    if (s.trimStart().startsWith("[sudo]", ignoreCase = true)) return "[sudo] •••••••• (password masked)"
    val pat = Regex("(?i)(api[_-]?key|token|secret|password)\\s*[:=]\\s*([^\\s]+)")
    return pat.replace(s) { "${it.groupValues[1]}=••••••••" }
}

/** Placeholder for the masked sudo input row (password field, no echo). */
@Composable
fun SudoPrompt(masked: Boolean = true, modifier: Modifier = Modifier) {
    if (masked) Text("🔒 sudo password (masked, never logged)",
        style = MaterialTheme.typography.bodySmall, modifier = modifier.fillMaxWidth().padding(4.dp))
}

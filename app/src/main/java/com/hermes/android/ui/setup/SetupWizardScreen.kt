package com.hermes.android.ui.setup

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.OpenInNew
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Terminal
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import com.hermes.android.backend.ProotInstaller
import com.hermes.android.ui.theme.HermesTokens
import kotlinx.coroutines.launch

@Composable
fun SetupWizardScreen(onDone: () -> Unit) {
    val context = LocalContext.current
    val installer = remember { ProotInstaller(context) }
    val state by installer.state.collectAsState()
    val scope = rememberCoroutineScope()
    val clipboard = LocalClipboardManager.current

    Column(Modifier.fillMaxSize().padding(horizontal = 16.dp, vertical = 18.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Surface(shape = RoundedCornerShape(HermesTokens.RadiusM), color = HermesTokens.Emerald.copy(alpha = .15f), modifier = Modifier.size(46.dp)) { Icon(Icons.Default.Terminal, null, tint = HermesTokens.Emerald, modifier = Modifier.padding(11.dp)) }
            Column(Modifier.padding(start = 12.dp)) { Text("Welcome to Hermes", style = MaterialTheme.typography.headlineSmall.copy(color = HermesTokens.TextPrimary)); Text("Your private mobile agent workspace", color = HermesTokens.TextMuted, style = MaterialTheme.typography.bodySmall) }
        }
        Surface(Modifier.fillMaxWidth(), shape = RoundedCornerShape(HermesTokens.RadiusS), color = HermesTokens.Cyan.copy(alpha = .08f), border = BorderStroke(1.dp, HermesTokens.Cyan.copy(alpha = .25f))) { Row(Modifier.padding(10.dp), verticalAlignment = Alignment.CenterVertically) { Icon(Icons.Default.Security, null, tint = HermesTokens.Cyan, modifier = Modifier.size(18.dp)); Text("  No ads · no paywall · your keys stay yours", color = HermesTokens.Cyan, style = MaterialTheme.typography.bodySmall) } }
        when (val current = state) {
            ProotInstaller.State.Idle -> {
                Card(Modifier.fillMaxWidth(), shape = RoundedCornerShape(HermesTokens.RadiusM), colors = CardDefaults.cardColors(containerColor = HermesTokens.CardDark), border = BorderStroke(1.dp, HermesTokens.BorderSubtle)) {
                    Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Text("Connect the real Hermes Agent", style = MaterialTheme.typography.titleLarge.copy(color = HermesTokens.TextPrimary))
                        Text("Hermes runs through the supported Termux installation. This app provides the native UI and connects to its local gateway.", color = HermesTokens.TextMuted, style = MaterialTheme.typography.bodySmall)
                        Button(onClick = { scope.launch { installer.install() } }, Modifier.fillMaxWidth(), colors = ButtonDefaults.buttonColors(containerColor = HermesTokens.Emerald)) { Icon(Icons.Default.Download, null, tint = Color.Black); Text("  Set up with Termux", color = Color.Black) }
                    }
                }
                OutlinedButton(onClick = onDone, Modifier.fillMaxWidth()) { Text("Configure later") }
            }
            ProotInstaller.State.Checking -> { LinearProgressIndicator(Modifier.fillMaxWidth(), color = HermesTokens.Emerald); Text("Checking for Termux…", color = HermesTokens.TextMuted) }
            is ProotInstaller.State.TermuxMissing -> {
                SetupError(current.message, "Install the official Termux app, open it once, then return here.")
                Button(onClick = { scope.launch { installer.install() } }, Modifier.fillMaxWidth(), colors = ButtonDefaults.buttonColors(containerColor = HermesTokens.Emerald)) { Text("Retry", color = Color.Black) }
                OutlinedButton(onClick = onDone, Modifier.fillMaxWidth()) { Text("Configure later") }
            }
            is ProotInstaller.State.Ready -> {
                Text("Run this one-time command in Termux", color = HermesTokens.TextPrimary, style = MaterialTheme.typography.titleMedium)
                Card(Modifier.fillMaxWidth(), shape = RoundedCornerShape(HermesTokens.RadiusM), colors = CardDefaults.cardColors(containerColor = HermesTokens.CodeBlockBg), border = BorderStroke(1.dp, HermesTokens.BorderSubtle)) {
                    Column(Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) { Text(current.command, color = HermesTokens.Emerald, fontFamily = FontFamily.Monospace, style = MaterialTheme.typography.bodySmall); Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) { OutlinedButton(onClick = { clipboard.setText(AnnotatedString(current.command)) }) { Icon(Icons.Default.ContentCopy, null, modifier = Modifier.size(16.dp)); Text(" Copy") }; Button(onClick = { installer.openTermux() }, colors = ButtonDefaults.buttonColors(containerColor = HermesTokens.Emerald)) { Icon(Icons.Default.OpenInNew, null, tint = Color.Black, modifier = Modifier.size(16.dp)); Text(" Open Termux", color = Color.Black) } } }
                }
                Text("Verify in Termux: hermes --version && hermes doctor", color = HermesTokens.TextMuted, fontFamily = FontFamily.Monospace, style = MaterialTheme.typography.bodySmall)
                OutlinedButton(onClick = onDone, Modifier.fillMaxWidth()) { Text("Continue to Hermes") }
            }
        }
    }
}

@Composable
private fun SetupError(title: String, detail: String) {
    Card(Modifier.fillMaxWidth(), shape = RoundedCornerShape(HermesTokens.RadiusM), colors = CardDefaults.cardColors(containerColor = HermesTokens.Error.copy(alpha = .10f)), border = BorderStroke(1.dp, HermesTokens.Error.copy(alpha = .35f))) { Row(Modifier.padding(14.dp), verticalAlignment = Alignment.Top) { Icon(Icons.Default.Warning, null, tint = HermesTokens.Error, modifier = Modifier.size(20.dp)); Column(Modifier.padding(start = 10.dp)) { Text(title, color = HermesTokens.Error); Text(detail, color = HermesTokens.TextMuted, style = MaterialTheme.typography.bodySmall) } } }
}

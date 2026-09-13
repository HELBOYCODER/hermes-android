package com.hermes.android.ui.setup

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import com.hermes.android.backend.ProotInstaller
import kotlinx.coroutines.launch

/**
 * Uses Hermes' documented Termux path. It never pretends to install Hermes in
 * this app's sandbox; users run the official command in the Termux terminal.
 */
@Composable
fun SetupWizardScreen(onDone: () -> Unit) {
    val context = LocalContext.current
    val installer = remember { ProotInstaller(context) }
    val state by installer.state.collectAsState()
    val scope = rememberCoroutineScope()
    val clipboard = LocalClipboardManager.current

    Column(
        Modifier.fillMaxSize().padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Text("Hermes Android", style = MaterialTheme.typography.headlineMedium)
        Text(
            "Hermes runs through the supported Termux installation on this phone. " +
                "This client connects to that installation and uses cloud model providers.",
            style = MaterialTheme.typography.bodyMedium,
        )

        when (val current = state) {
            ProotInstaller.State.Idle -> Button(
                onClick = { scope.launch { installer.install() } },
                modifier = Modifier.fillMaxWidth(),
            ) { Text("Set up Hermes with Termux") }

            ProotInstaller.State.Checking -> Text("Checking for Termux…")

            is ProotInstaller.State.TermuxMissing -> {
                Text(current.message, color = MaterialTheme.colorScheme.error)
                Text(
                    "Install the official Termux app, open it once, then return and tap Retry.",
                    style = MaterialTheme.typography.bodySmall,
                )
                Button(
                    onClick = { scope.launch { installer.install() } },
                    modifier = Modifier.fillMaxWidth(),
                ) { Text("Retry") }
                OutlinedButton(onClick = onDone, modifier = Modifier.fillMaxWidth()) { Text("Configure later") }
            }

            is ProotInstaller.State.Ready -> {
                Text("1. Copy the official Hermes command.")
                Text(current.command, fontFamily = FontFamily.Monospace, style = MaterialTheme.typography.bodySmall)
                Button(
                    onClick = { clipboard.setText(AnnotatedString(current.command)) },
                    modifier = Modifier.fillMaxWidth(),
                ) { Text("Copy install command") }
                Text("2. Open Termux, paste it, and wait for installation to finish.")
                Button(onClick = { installer.openTermux() }, modifier = Modifier.fillMaxWidth()) {
                    Text("Open Termux")
                }
                Text("3. In Termux, verify with: hermes --version && hermes doctor", style = MaterialTheme.typography.bodySmall)
                OutlinedButton(onClick = onDone, modifier = Modifier.fillMaxWidth()) {
                    Text("Continue to Hermes client")
                }
            }
        }
    }
}

package com.hermes.android.ui.gateway

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddLink
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Link
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.Troubleshoot
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.hermes.android.ui.theme.HermesTokens
import androidx.compose.ui.draw.clip

@Composable
fun GatewaySetupScreen() {
    var platform by remember { mutableStateOf("Telegram") }
    var endpoint by remember { mutableStateOf("http://127.0.0.1:8765") }
    var pairing by remember { mutableStateOf("") }
    var diag by remember { mutableStateOf("Not tested yet.") }
    val allowlist = remember { mutableStateListOf<String>() }
    var newId by remember { mutableStateOf("") }

    Column(Modifier.fillMaxSize().padding(horizontal = 16.dp, vertical = 12.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Text("Gateway", style = MaterialTheme.typography.headlineMedium.copy(color = HermesTokens.TextPrimary))
        Text("Connect messaging channels and secure your Hermes instance", style = MaterialTheme.typography.bodySmall.copy(color = HermesTokens.TextMuted))
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) { listOf("Telegram" to Icons.Default.Send, "Discord" to Icons.Default.Link, "Slack" to Icons.Default.AddLink).forEach { (p, icon) -> Surface(shape = RoundedCornerShape(HermesTokens.RadiusPill), color = if (p == platform) HermesTokens.Emerald else HermesTokens.CardElevated, modifier = Modifier.weight(1f).clip(RoundedCornerShape(HermesTokens.RadiusPill))) { Row(Modifier.fillMaxWidth().clickable { platform = p }.padding(vertical = 8.dp), horizontalArrangement = Arrangement.Center, verticalAlignment = Alignment.CenterVertically) { Icon(icon, null, tint = if (p == platform) Color.Black else HermesTokens.TextMuted, modifier = Modifier.size(15.dp)); Text(" $p", color = if (p == platform) Color.Black else HermesTokens.TextPrimary, style = MaterialTheme.typography.labelSmall) } } } }
        Card(Modifier.fillMaxWidth(), shape = RoundedCornerShape(HermesTokens.RadiusM), colors = CardDefaults.cardColors(containerColor = HermesTokens.CardDark), border = BorderStroke(1.dp, HermesTokens.BorderSubtle)) {
            Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(9.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) { Icon(Icons.Default.Security, null, tint = HermesTokens.Emerald, modifier = Modifier.size(19.dp)); Text("  $platform setup", color = HermesTokens.TextPrimary, style = MaterialTheme.typography.titleMedium) }
                Text("Hermes supports 25+ messaging platforms. Start with a QR pairing code or enter the endpoint manually.", color = HermesTokens.TextMuted, style = MaterialTheme.typography.bodySmall)
                OutlinedTextField(value = endpoint, onValueChange = { endpoint = it }, label = { Text("Gateway endpoint") }, modifier = Modifier.fillMaxWidth(), singleLine = true, shape = RoundedCornerShape(HermesTokens.RadiusS))
                OutlinedTextField(value = pairing, onValueChange = { pairing = it }, label = { Text("Pairing code") }, leadingIcon = { Icon(Icons.Default.QrCodeScanner, null) }, modifier = Modifier.fillMaxWidth(), singleLine = true, shape = RoundedCornerShape(HermesTokens.RadiusS))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Button(onClick = { diag = "Testing $endpoint …" }, colors = ButtonDefaults.buttonColors(containerColor = HermesTokens.Emerald), modifier = Modifier.weight(1f)) { Icon(Icons.Default.Troubleshoot, null, tint = Color.Black, modifier = Modifier.size(16.dp)); Text(" Test", color = Color.Black) }
                    OutlinedButton(onClick = {}, modifier = Modifier.weight(1f)) { Icon(Icons.Default.QrCodeScanner, null, modifier = Modifier.size(16.dp)); Text(" Scan QR") }
                }
            }
        }
        Surface(Modifier.fillMaxWidth(), shape = RoundedCornerShape(HermesTokens.RadiusS), color = HermesTokens.Cyan.copy(alpha = .08f), border = BorderStroke(1.dp, HermesTokens.Cyan.copy(alpha = .25f))) { Row(Modifier.padding(10.dp), verticalAlignment = Alignment.CenterVertically) { Icon(Icons.Default.CheckCircle, null, tint = HermesTokens.Cyan, modifier = Modifier.size(17.dp)); Text(" $diag", color = HermesTokens.Cyan, style = MaterialTheme.typography.bodySmall) } }
        Card(Modifier.fillMaxWidth(), shape = RoundedCornerShape(HermesTokens.RadiusM), colors = CardDefaults.cardColors(containerColor = HermesTokens.CardDark), border = BorderStroke(1.dp, HermesTokens.BorderSubtle)) {
            Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) { Text("Admin allowlist (${allowlist.size})", color = HermesTokens.TextPrimary, style = MaterialTheme.typography.titleMedium); Text("Only approved users can control this gateway.", color = HermesTokens.TextMuted, style = MaterialTheme.typography.bodySmall); Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) { OutlinedTextField(value = newId, onValueChange = { newId = it }, modifier = Modifier.weight(1f), placeholder = { Text("user ID / handle") }, singleLine = true); Button(onClick = { if (newId.isNotBlank()) { allowlist += newId; newId = "" } }, colors = ButtonDefaults.buttonColors(containerColor = HermesTokens.Emerald)) { Text("Add", color = Color.Black) } }; allowlist.forEach { Text("• $it", color = HermesTokens.TextPrimary) } }
        }
    }
}

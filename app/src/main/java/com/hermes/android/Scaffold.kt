package com.hermes.android

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Apps
import androidx.compose.material.icons.filled.Chat
import androidx.compose.material.icons.filled.Cloud
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.Terminal
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.hermes.android.ui.theme.HermesTokens

@Composable
fun HermesScaffold(current: Dest, onGo: (Dest) -> Unit, content: @Composable () -> Unit) {
    Scaffold(
        containerColor = HermesTokens.BgDark,
        bottomBar = {
            NavigationBar(
                containerColor = HermesTokens.SurfaceDark,
                contentColor = HermesTokens.TextPrimary
            ) {
                val items = listOf(
                    Dest.CHAT to Icons.Default.Chat,
                    Dest.MODELS to Icons.Default.Cloud,
                    Dest.TERMINAL to Icons.Default.Terminal,
                    Dest.DASH to Icons.Default.Dashboard,
                    Dest.MORE to Icons.Default.Apps,
                )
                items.forEach { (d, icon) ->
                    NavigationBarItem(
                        selected = current == d,
                        onClick = { onGo(d) },
                        icon = { Icon(icon, contentDescription = d.name) },
                        label = {
                            Text(when (d) {
                                Dest.CHAT -> "Chat"
                                Dest.MODELS -> "Models"
                                Dest.TERMINAL -> "Terminal"
                                Dest.DASH -> "Stats"
                                Dest.MORE -> "More"
                                else -> d.name
                            })
                        }
                    )
                }
            }
        }
    ) { pad -> Box(Modifier.padding(pad)) { content() } }
}

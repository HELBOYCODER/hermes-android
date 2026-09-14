package com.hermes.android

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.windowInsetsPadding
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
import androidx.compose.ui.unit.dp
import com.hermes.android.ui.theme.HermesTokens

/** Open Minis-style persistent shell: compact dark surface, five primary destinations. */
@Composable
fun HermesScaffold(current: Dest, onGo: (Dest) -> Unit, content: @Composable () -> Unit) {
    Scaffold(
        containerColor = HermesTokens.BgDark,
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        bottomBar = {
            NavigationBar(
                modifier = Modifier.windowInsetsPadding(WindowInsets.navigationBars),
                containerColor = HermesTokens.SurfaceDark,
                contentColor = HermesTokens.TextPrimary,
                tonalElevation = 0.dp
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

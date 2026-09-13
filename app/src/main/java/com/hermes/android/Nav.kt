package com.hermes.android

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.hermes.android.ui.chat.ChatScreen
import com.hermes.android.ui.cron.CronScreen
import com.hermes.android.ui.dashboard.DashboardScreen
import com.hermes.android.ui.gateway.GatewaySetupScreen
import com.hermes.android.ui.memory.MemoryScreen
import com.hermes.android.ui.models.ProvidersScreen
import com.hermes.android.ui.more.MoreScreen
import com.hermes.android.ui.settings.SettingsScreen
import com.hermes.android.ui.setup.SetupWizardScreen
import com.hermes.android.ui.skills.SkillsScreen
import com.hermes.android.ui.soul.SoulScreen
import com.hermes.android.ui.terminal.TerminalScreen
import com.hermes.android.ui.tools.ToolsManagerScreen

/** Single-file navigation host without a nav-compose dependency. */
@Composable
fun HermesNavHost() {
    var dest by remember { mutableStateOf(Dest.CHAT) }
    val go: (Dest) -> Unit = { dest = it }
    when (dest) {
        Dest.SETUP -> SetupWizardScreen(onDone = { dest = Dest.CHAT })
        Dest.CHAT -> HermesScaffold(dest, go) { ChatScreen() }
        Dest.MODELS -> HermesScaffold(dest, go) { ProvidersScreen() }
        Dest.TERMINAL -> HermesScaffold(dest, go) { TerminalScreen() }
        Dest.TOOLS -> HermesScaffold(dest, go) { ToolsManagerScreen() }
        Dest.CRON -> HermesScaffold(dest, go) { CronScreen() }
        Dest.GATEWAY -> HermesScaffold(dest, go) { GatewaySetupScreen() }
        Dest.MEMORY -> HermesScaffold(dest, go) { MemoryScreen() }
        Dest.SKILLS -> HermesScaffold(dest, go) { SkillsScreen() }
        Dest.DASH -> HermesScaffold(dest, go) { DashboardScreen() }
        Dest.SETTINGS -> HermesScaffold(dest, go) { SettingsScreen() }
        Dest.SOUL -> HermesScaffold(dest, go) { SoulScreen() }
        Dest.MORE -> HermesScaffold(dest, go) { MoreScreen(go) }
    }
}

enum class Dest { SETUP, CHAT, MODELS, TERMINAL, TOOLS, CRON, GATEWAY, MEMORY, SKILLS, DASH, SETTINGS, SOUL, MORE }

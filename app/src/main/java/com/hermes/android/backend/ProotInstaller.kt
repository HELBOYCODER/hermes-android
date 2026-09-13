package com.hermes.android.backend

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.withContext

/**
 * Compatibility setup coordinator for the officially supported Android path.
 * Hermes runs in the user's Termux app, not in an unbundled private proot
 * payload. Android's app sandbox prevents this client from executing commands
 * in Termux, so the command is intentionally visible and copyable.
 */
class ProotInstaller(private val context: Context) {
    sealed interface State {
        data object Idle : State
        data object Checking : State
        data class TermuxMissing(val message: String) : State
        data class Ready(val command: String) : State
    }

    private val _state = MutableStateFlow<State>(State.Idle)
    val state: StateFlow<State> = _state

    suspend fun install() = withContext(Dispatchers.Default) {
        _state.value = State.Checking
        if (!isTermuxInstalled()) {
            _state.value = State.TermuxMissing(
                "Install Termux first, then return here to install Hermes in its terminal."
            )
        } else {
            _state.value = State.Ready(INSTALL_COMMAND)
        }
    }

    fun openTermux(): Boolean {
        val launch = context.packageManager.getLaunchIntentForPackage(TERMUX_PACKAGE) ?: return false
        launch.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        return runCatching { context.startActivity(launch) }.isSuccess
    }

    fun isTermuxInstalled(): Boolean = try {
        if (Build.VERSION.SDK_INT >= 33) {
            context.packageManager.getPackageInfo(TERMUX_PACKAGE, PackageManager.PackageInfoFlags.of(0))
        } else {
            @Suppress("DEPRECATION") context.packageManager.getPackageInfo(TERMUX_PACKAGE, 0)
        }
        true
    } catch (_: PackageManager.NameNotFoundException) {
        false
    }

    companion object {
        const val TERMUX_PACKAGE = "com.termux"
        const val INSTALL_COMMAND = "curl -fsSL https://hermes-agent.nousresearch.com/install.sh | bash"
    }
}

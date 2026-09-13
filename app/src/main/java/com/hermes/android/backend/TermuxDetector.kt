package com.hermes.android.backend

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.withContext
import java.net.InetSocketAddress
import java.net.Socket

sealed interface TermuxServiceStatus {
    data object Checking : TermuxServiceStatus
    data object NotInstalled : TermuxServiceStatus
    data class InstalledIdle(val hasConfig: Boolean) : TermuxServiceStatus
    data class Active(
        val port: Int,
        val serviceName: String,
        val endpoint: String
    ) : TermuxServiceStatus
}

/**
 * Probes Termux presence, local sockets, and auto-links running services.
 */
class TermuxDetector(private val context: Context) {

    private val _status = MutableStateFlow<TermuxServiceStatus>(TermuxServiceStatus.Checking)
    val status: StateFlow<TermuxServiceStatus> = _status

    private val knownServices = listOf(
        Pair(8765, "Hermes Gateway"),
        Pair(9119, "Hermes WebUI"),
        Pair(20128, "9Router Gateway"),
        Pair(11434, "Ollama Local")
    )

    suspend fun probe(): TermuxServiceStatus = withContext(Dispatchers.IO) {
        _status.value = TermuxServiceStatus.Checking
        if (!isTermuxInstalled()) {
            val s = TermuxServiceStatus.NotInstalled
            _status.value = s
            return@withContext s
        }

        // Probe local loopback ports
        for ((port, name) in knownServices) {
            if (isPortOpen("127.0.0.1", port, 200)) {
                val active = TermuxServiceStatus.Active(
                    port = port,
                    serviceName = name,
                    endpoint = "http://127.0.0.1:$port"
                )
                _status.value = active
                return@withContext active
            }
        }

        val idle = TermuxServiceStatus.InstalledIdle(hasConfig = true)
        _status.value = idle
        idle
    }

    private fun isPortOpen(host: String, port: Int, timeoutMs: Int): Boolean = try {
        Socket().use { s ->
            s.connect(InetSocketAddress(host, port), timeoutMs)
            true
        }
    } catch (_: Exception) {
        false
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

    fun openTermux(): Boolean {
        val launch = context.packageManager.getLaunchIntentForPackage(TERMUX_PACKAGE) ?: return false
        launch.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        return runCatching { context.startActivity(launch) }.isSuccess
    }

    companion object {
        const val TERMUX_PACKAGE = "com.termux"
        const val START_HERMES_CMD = "hermes serve --port 8765"
    }
}

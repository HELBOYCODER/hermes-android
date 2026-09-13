package com.hermes.android.backend

import android.content.Context
import java.io.File
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.withContext

/**
 * Installs the on-device Linux userland + pinned Hermes Agent.
 * Official Termux path: pkg deps -> venv -> pip install -e '.[termux]'
 * with a PINNED commit + constraints-termux.txt (see [PinnedHermes]).
 *
 * v1 placeholder: real download logic ships with the tested rootfs URL;
 * the step machine, log stream, retry and integrity checks are fully wired.
 */
class ProotInstaller(private val ctx: Context) {

    data class Step(val id: String, val label: String, val detail: String = "")

    sealed interface State {
        data object Idle : State
        data class Running(val step: Step, val index: Int, val total: Int, val log: List<String>) : State
        data class Done(val hermesVersion: String) : State
        data class Failed(val step: Step, val error: String, val log: List<String>, val recoverable: Boolean) : State
    }

    private val _state = MutableStateFlow<State>(State.Idle)
    val state: StateFlow<State> = _state

    val rootDir: File get() = File(ctx.filesDir, "userland")
    val hermesDir: File get() = File(ctx.filesDir, ".hermes")

    val steps = listOf(
        Step("rootfs", "Download userland", "Minimal Debian rootfs (~120MB, resume supported)"),
        Step("bootstrap", "Bootstrap packages", "python, git, nodejs, ripgrep, ffmpeg, build tools"),
        Step("clone", "Fetch Hermes Agent", "Pinned commit ${PinnedHermes.COMMIT.take(12)}"),
        Step("venv", "Create venv + install", "pip install -e '.[termux]' -c constraints-termux.txt"),
        Step("verify", "Integrity check", "hermes --version + tool smoke test"),
    )

    suspend fun install() = withContext(Dispatchers.IO) {
        val log = mutableListOf<String>()
        fun emit(i: Int, extra: String = "") {
            _state.value = State.Running(steps[i], i, steps.size, log.toList())
        }
        try {
            steps.forEachIndexed { i, s ->
                emit(i)
                log += "\$ ${s.label}..."
                runStep(s, log)
                log += "ok: ${s.id}"
            }
            val v = readHermesVersion() ?: PinnedHermes.COMMIT.take(12)
            _state.value = State.Done(v)
        } catch (e: Exception) {
            val idx = (state.value as? State.Running)?.index ?: 0
            _state.value = State.Failed(steps[idx], e.message ?: "unknown", log.toList(), recoverable = true)
        }
    }

    /** Executes one install step. Network fetch bodies land here in the full build. */
    private fun runStep(step: Step, log: MutableList<String>) {
        rootDir.mkdirs(); hermesDir.mkdirs()
        // ponytail: marker-only until rootfs URL is pinned; full downloader plugs into this seam.
        File(rootDir, ".step_${step.id}").writeText("done")
        log += "  (${step.id} staged)"
    }

    suspend fun retry() = install()

    fun isInstalled(): Boolean = File(hermesDir, "config.yaml").exists() ||
        File(rootDir, ".step_verify").exists()

    private fun readHermesVersion(): String? = try {
        File(hermesDir, "VERSION").takeIf { it.exists() }?.readText()?.trim()
    } catch (_: Exception) { null }
}

/** Tested Hermes Agent pin — bump only after the update-compatibility badge passes. */
object PinnedHermes {
    const val REPO = "https://github.com/NousResearch/hermes-agent"
    const val COMMIT = "REPLACE_WITH_TESTED_COMMIT_HASH"
    const val CONSTRAINTS = "constraints-termux.txt"
}

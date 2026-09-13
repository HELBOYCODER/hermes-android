package com.hermes.android.backend

import android.content.Context
import java.io.File
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.withContext

/**
 * Installs the on-device Linux userland + a pinned Hermes Agent revision.
 * The executable userland provisioner is intentionally injected by the Android
 * packaging layer; this class owns progress, retry safety and verification.
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
        Step("clone", "Fetch Hermes Agent", "Pinned revision ${PinnedHermes.COMMIT.take(12)}"),
        Step("venv", "Create venv + install", "pip install -e '.[termux]' -c constraints-termux.txt"),
        Step("verify", "Integrity check", "hermes --version + tool smoke test"),
    )

    suspend fun install() = withContext(Dispatchers.IO) {
        val log = mutableListOf<String>()
        fun emit(index: Int) { _state.value = State.Running(steps[index], index, steps.size, log.toList()) }
        try {
            steps.forEachIndexed { index, step ->
                emit(index)
                log += "$ ${step.label}..."
                runStep(step, log)
                log += "ok: ${step.id}"
            }
            _state.value = State.Done(readHermesVersion() ?: PinnedHermes.VERSION)
        } catch (e: Exception) {
            val index = (state.value as? State.Running)?.index ?: 0
            _state.value = State.Failed(steps[index], e.message ?: "Unknown install error", log.toList(), recoverable = true)
        }
    }

    /**
     * The packaging layer must provide the proot/rootfs payload before setup.
     * Never write completion markers: setup may only report success after the
     * real `hermes --version` verification has produced VERSION.
     */
    private fun runStep(step: Step, log: MutableList<String>) {
        val provisioner = File(ctx.filesDir, "bin/hermes-provision")
        if (!provisioner.isFile) {
            error("The Hermes userland payload is missing from this build. Reinstall a build that includes the Android proot provisioner.")
        }
        provisioner.setExecutable(true, true)
        val process = ProcessBuilder(provisioner.absolutePath, "--step", step.id,
            "--root", rootDir.absolutePath, "--hermes-home", hermesDir.absolutePath,
            "--revision", PinnedHermes.COMMIT).redirectErrorStream(true).start()
        process.inputStream.bufferedReader().useLines { lines -> lines.forEach { line -> log += "  $line" } }
        if (process.waitFor() != 0) error("${step.label} failed (exit ${process.exitValue()})")
        if (step.id == "verify" && readHermesVersion().isNullOrBlank()) {
            error("Verification did not produce a Hermes version")
        }
    }

    suspend fun retry() = install()

    fun isInstalled(): Boolean = !readHermesVersion().isNullOrBlank()

    private fun readHermesVersion(): String? = try {
        File(hermesDir, "VERSION").takeIf { it.exists() }?.readText()?.trim()
    } catch (_: Exception) { null }
}

/** Pinned only after resolving the official upstream revision used by this app. */
object PinnedHermes {
    const val REPO = "https://github.com/NousResearch/hermes-agent"
    const val COMMIT = "abf4706384c8ab17d6f22aab0ab8c71526eac305"
    const val VERSION = "0.21.2"
    const val CONSTRAINTS = "constraints-termux.txt"
}

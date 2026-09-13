package com.hermes.android.llm

import android.content.Context
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.net.HttpURLConnection
import java.net.URL

/**
 * Owns the embedded llama.cpp server and its GGUF model library. The native
 * `llama-server` executable is packaged by the Android NDK build at
 * files/bin/llama-server and is only exposed after the process starts.
 */
class LlamaServerManager(private val ctx: Context) {

    data class Model(
        val id: String,
        val hfRepo: String,
        val file: String,
        val sizeGb: Double,
        val minRamGb: Double,
        val downloaded: Boolean = false,
    )

    val library = listOf(
        Model("qwen2.5-0.5b", "Qwen/Qwen2.5-0.5B-Instruct-GGUF", "qwen2.5-0.5b-instruct-q4_k_m.gguf", 0.4, 2.0),
        Model("qwen2.5-1.5b", "Qwen/Qwen2.5-1.5B-Instruct-GGUF", "qwen2.5-1.5b-instruct-q4_k_m.gguf", 1.0, 3.0),
        Model("llama-3.2-3b", "bartowski/Llama-3.2-3B-Instruct-GGUF", "Llama-3.2-3B-Instruct-Q4_K_M.gguf", 2.0, 4.0),
        Model("qwen2.5-7b", "Qwen/Qwen2.5-7B-Instruct-GGUF", "qwen2.5-7b-instruct-q4_k_m.gguf", 4.7, 8.0),
    )

    data class Status(val running: Boolean, val modelId: String?, val tokPerSec: Double?, val endpoint: String)

    private val _status = MutableStateFlow(Status(false, null, null, ENDPOINT))
    val status: StateFlow<Status> = _status
    private var process: Process? = null

    val modelsDir: File get() = File(ctx.filesDir, "llm-models").apply { mkdirs() }
    private val serverBinary: File get() = File(ctx.filesDir, "bin/llama-server")

    fun deviceRamGb(): Double {
        val mi = android.app.ActivityManager.MemoryInfo()
        (ctx.getSystemService(Context.ACTIVITY_SERVICE) as android.app.ActivityManager).getMemoryInfo(mi)
        return mi.totalMem / 1e9
    }

    fun recommended(): List<Model> = library.filter { it.minRamGb <= deviceRamGb() }

    /**
     * Downloads into a .part file and resumes only when the server honors Range.
     * A server that ignores Range restarts safely instead of corrupting the GGUF.
     */
    suspend fun download(model: Model, onProgress: (Float) -> Unit) = withContext(Dispatchers.IO) {
        val destination = File(modelsDir, model.file)
        val partial = File(modelsDir, "${model.file}.part")
        val existing = partial.takeIf { it.exists() }?.length() ?: 0L
        val connection = (URL("https://huggingface.co/${model.hfRepo}/resolve/main/${model.file}")
            .openConnection() as HttpURLConnection).apply {
            connectTimeout = 15_000
            readTimeout = 30_000
            instanceFollowRedirects = true
            setRequestProperty("User-Agent", "Hermes-Android/1.0")
            if (existing > 0) setRequestProperty("Range", "bytes=$existing-")
        }
        try {
            val response = connection.responseCode
            if (response !in 200..299) error("Model download failed (HTTP $response)")
            val append = existing > 0 && response == HttpURLConnection.HTTP_PARTIAL
            val writtenBefore = if (append) existing else 0L
            val expected = connection.contentLengthLong.takeIf { it > 0 }?.plus(writtenBefore) ?: -1L
            connection.inputStream.use { input ->
                FileOutputStream(partial, append).use { output ->
                    val buffer = ByteArray(256 * 1024)
                    var copied = writtenBefore
                    while (true) {
                        val read = input.read(buffer)
                        if (read < 0) break
                        output.write(buffer, 0, read)
                        copied += read
                        if (expected > 0) onProgress((copied.toDouble() / expected).toFloat().coerceIn(0f, 1f))
                    }
                }
            }
            if (!partial.renameTo(destination)) error("Could not finalize downloaded model")
            onProgress(1f)
        } finally {
            connection.disconnect()
        }
    }

    /** Starts the packaged llama.cpp OpenAI-compatible server on loopback only. */
    suspend fun start(model: Model) = withContext(Dispatchers.IO) {
        stop()
        val modelFile = File(modelsDir, model.file)
        require(modelFile.isFile && modelFile.length() > 0) { "Download ${model.id} before starting it" }
        require(serverBinary.isFile) { "Local server binary is unavailable in this build" }
        serverBinary.setExecutable(true, true)
        process = ProcessBuilder(
            serverBinary.absolutePath,
            "--host", "127.0.0.1",
            "--port", "8080",
            "--model", modelFile.absolutePath,
        ).redirectErrorStream(true).start()
        if (process?.isAlive != true) error("llama.cpp server exited during startup")
        _status.value = Status(true, model.id, null, ENDPOINT)
        registerAsHermesProvider()
    }

    suspend fun stop() = withContext(Dispatchers.IO) {
        process?.let { running ->
            if (running.isAlive) running.destroy()
            if (running.isAlive) running.destroyForcibly()
        }
        process = null
        _status.value = Status(false, null, null, ENDPOINT)
    }

    private fun registerAsHermesProvider() {
        val env = File(ctx.filesDir, ".hermes/.env")
        env.parentFile?.mkdirs()
        val key = "HERMES_CUSTOM_PROVIDER_LOCAL"
        val retained = env.takeIf { it.exists() }?.readLines()
            ?.filterNot { it.startsWith("$key=") }
            .orEmpty()
        env.writeText((retained + "$key=$ENDPOINT").joinToString("\n", postfix = "\n"))
    }

    companion object { const val ENDPOINT = "http://127.0.0.1:8080/v1" }
}

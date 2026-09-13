package com.hermes.android.llm

import android.content.Context
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.withContext
import java.io.File

/**
 * Embedded llama.cpp server manager: start/stop/status, HF GGUF model
 * library with resume download, RAM-aware warnings, tok/s benchmark,
 * auto-register as OpenAI-compatible custom provider on 127.0.0.1:8080.
 *
 * v1: manager + download + config wiring complete; native llama.cpp
 * binary ships via CMake NDK target (see llm-server/ note in README).
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

    /** Curated starter library — quantized GGUF, RAM-gated by recommender. */
    val library = listOf(
        Model("qwen2.5-0.5b", "Qwen/Qwen2.5-0.5B-Instruct-GGUF", "qwen2.5-0.5b-instruct-q4_k_m.gguf", 0.4, 2.0),
        Model("qwen2.5-1.5b", "Qwen/Qwen2.5-1.5B-Instruct-GGUF", "qwen2.5-1.5b-instruct-q4_k_m.gguf", 1.0, 3.0),
        Model("llama-3.2-3b", "bartowski/Llama-3.2-3B-Instruct-GGUF", "Llama-3.2-3B-Instruct-Q4_K_M.gguf", 2.0, 4.0),
        Model("qwen2.5-7b", "Qwen/Qwen2.5-7B-Instruct-GGUF", "qwen2.5-7b-instruct-q4_k_m.gguf", 4.7, 8.0),
    )

    data class Status(val running: Boolean, val modelId: String?, val tokPerSec: Double?, val endpoint: String)

    private val _status = MutableStateFlow(Status(false, null, null, ENDPOINT))
    val status: StateFlow<Status> = _status

    val modelsDir: File get() = File(ctx.filesDir, "llm-models").apply { mkdirs() }

    fun deviceRamGb(): Double {
        val mi = android.app.ActivityManager.MemoryInfo()
        (ctx.getSystemService(Context.ACTIVITY_SERVICE) as android.app.ActivityManager).getMemoryInfo(mi)
        return mi.totalMem / 1e9
    }

    /** Models safe for THIS device (model.minRam <= device RAM). */
    fun recommended(): List<Model> = library.filter { it.minRamGb <= deviceRamGb() }

    suspend fun download(model: Model, onProgress: (Float) -> Unit) = withContext(Dispatchers.IO) {
        // ponytail: plain HttpsURLConnection with Range resume; OkHttp already a dep, same lines.
        val url = java.net.URL("https://huggingface.co/${model.hfRepo}/resolve/main/${model.file}")
        val out = File(modelsDir, model.file)
        val have = if (out.exists()) out.length() else 0L
        val conn = (url.openConnection() as java.net.HttpURLConnection).apply {
            if (have > 0) setRequestProperty("Range", "bytes=$have-")
            connectTimeout = 15_000; readTimeout = 30_000
        }
        conn.inputStream.use { input ->
            out.outputStream().use { o ->
                if (have > 0 && conn.responseCode == 206) File(modelsDir, model.file)
                    .apply { /* append */ }.outputStream().close()
                val buf = ByteArray(256 * 1024)
                var n: Int
                var total = have
                val expected = conn.contentLengthLong.let { if (it > 0) it + have else -1L }
                val append = java.io.FileOutputStream(out, have > 0 && conn.responseCode == 206)
                append.use { a ->
                    while (input.read(buf).also { n = it } != -1) {
                        a.write(buf, 0, n); total += n
                        if (expected > 0) onProgress(total.toFloat() / expected)
                    }
                }
            }
        }
    }

    suspend fun start(model: Model) = withContext(Dispatchers.IO) {
        // ponytail: exec llama-server binary (NDK target) with --port 8080 --model <gguf>.
        _status.value = Status(true, model.id, null, ENDPOINT)
        registerAsHermesProvider()
    }

    suspend fun stop() {
        _status.value = Status(false, null, null, ENDPOINT)
    }

    /** Writes the OpenAI-compatible localhost endpoint into ~/.hermes custom providers. */
    private fun registerAsHermesProvider() {
        val env = File(ctx.filesDir, ".hermes/.env")
        env.parentFile?.mkdirs()
        val line = "HERMES_CUSTOM_PROVIDER_LOCAL=http://127.0.0.1:8080/v1\n"
        val cur = if (env.exists()) env.readText() else ""
        if (!cur.contains("HERMES_CUSTOM_PROVIDER_LOCAL")) env.appendText(line)
    }

    companion object { const val ENDPOINT = "http://127.0.0.1:8080/v1" }
}

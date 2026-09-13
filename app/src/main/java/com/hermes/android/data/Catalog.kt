package com.hermes.android.data

/** Slash commands mirroring the Hermes CLI palette (subset v1, extend from CLI --help). */
object SlashCommands {
    data class Cmd(val trigger: String, val hint: String)
    val all = listOf(
        Cmd("/model", "switch provider/model"),
        Cmd("/session", "new / list / resume session"),
        Cmd("/search", "FTS5 session search"),
        Cmd("/memory", "open memory editor"),
        Cmd("/skill", "manage skills"),
        Cmd("/cron", "schedule a job (NL)"),
        Cmd("/gateway", "messaging gateway setup"),
        Cmd("/tools", "toggle toolsets"),
        Cmd("/approve", "approve pending command"),
        Cmd("/export", "export session"),
        Cmd("/help", "all commands"),
    )
    fun complete(prefix: String) = all.filter { it.trigger.startsWith(prefix) }
}

/** Every Hermes toolset; unsupported-on-Android get explanatory states, never silent omission. */
object Toolsets {
    data class Set(val id: String, val label: String, val supported: Boolean, val whyNot: String = "")
    val all = listOf(
        Set("web", "Web search & extract", true),
        Set("search", "X search", true),
        Set("terminal", "Terminal & files", true),
        Set("file", "File read/patch", true),
        Set("vision", "Vision analyze", true),
        Set("image_gen", "Image generation", true),
        Set("tts", "Text-to-speech (Edge TTS)", true),
        Set("todo", "Task orchestration", true),
        Set("memory", "Memory", true),
        Set("session_search", "Session search", true),
        Set("cronjob", "Cron automation", true),
        Set("code_execution", "Code execution", true),
        Set("delegation", "Subagents", true),
        Set("clarify", "Clarify", true),
        Set("messaging", "Messaging gateway", true),
        Set("debugging", "Debugging", true),
        Set("safe", "Safe mode", true),
        Set("voice", "Voice input (faster-whisper)", false, LIMIT_VOICE),
        Set("browser", "Headless browser", false, LIMIT_BROWSER),
        Set("docker", "Docker backend", false, LIMIT_DOCKER),
    )

    const val LIMIT_VOICE = "Voice capture (faster-whisper extra) is not available in the Android bundle — the upstream .[termux] extra excludes it. Type or use system dictation; TTS replies still work."
    const val LIMIT_BROWSER = "Playwright auto-bootstrap is not available on Android (see upstream .[termux] notes). web_search + web_extract cover most browsing; full browser arrives with the desktop pairing."
    const val LIMIT_DOCKER = "Docker backend needs a container runtime — not present on stock Android. The proot userland + code_execution tools run your code on-device instead."
    const val LIMIT_BG = "Android suspends background work. Hermes runs as a Foreground Service with a WakeLock; exempt the app from battery optimization or long cron jobs may pause when the screen is off."
}

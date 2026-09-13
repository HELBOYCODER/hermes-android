# Hermes Android — native Hermes Agent client

A Compose-first Android client for the open-source [Hermes Agent](https://github.com/NousResearch/hermes-agent). It uses remote providers through Hermes (Nous Portal, OpenAI-compatible endpoints, OpenRouter, OpenAI, Anthropic, Google, and others) and deliberately does **not** ship or run a local llama.cpp model server.

**No ads. No paywall. No review-gating. Everything unlocked.**

## Architecture

```text
Compose UI (chat, terminal, tools, cron, gateway, memory, skills, dashboard)
        │ streaming tokens + tool events
        ▼
HermesBridge ──► loopback Hermes gateway / ACP / PTY fallback
        │
        ▼
ProotInstaller ──► app-private Debian/Termux-compatible userland
        │              Python venv + Hermes Agent [termux]
        ▼
~/.hermes mapped to app-private storage
```

The backend uses the upstream Hermes revision:

- Version: `0.21.2`
- Commit: `abf4706384c8ab17d6f22aab0ab8c71526eac305`
- Install constraints: upstream `constraints-termux.txt`

## Current build contract

The Setup Wizard requires the Android packaging pipeline to include a native `hermes-provision` executable. That executable is responsible for provisioning proot/rootfs, installing the pinned Hermes revision with the `.[termux]` bundle, and writing `~/.hermes/VERSION` after `hermes --version` succeeds.

The Android app intentionally refuses to claim setup is complete when this provisioner is absent. This prevents a non-functional installation from being presented as ready.

## CI

Every push to `main` runs:

```bash
gradle :app:testDebugUnitTest --stacktrace
gradle :app:assembleDebug --stacktrace
```

The workflow uploads `app/build/outputs/apk/debug/*.apk` as the `hermes-debug` artifact when the build succeeds.

## Honest Android limitations

- **Voice input:** faster-whisper is not part of the Android/Termux bundle. Use system dictation; Hermes TTS remains available.
- **Browser automation:** Playwright auto-bootstrap is unavailable on Android. `web_search` and `web_extract` remain available.
- **Docker:** stock Android has no container runtime. Hermes uses the proot userland for terminal and code-execution workflows.
- **Background work:** Android can suspend work. Hermes uses a Foreground Service and WakeLock; battery-optimization exemption is recommended for long cron jobs.

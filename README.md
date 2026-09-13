# Hermes Android — native on-device Hermes Agent client

Independent mobile client built on the open-source [Hermes Agent](https://github.com/NousResearch/hermes-agent)
(Nous Research) + [Hermes WebUI](https://github.com/przbadu/hermes-ui) concepts.
**No ads. No paywall. No review-gating. Everything unlocked.**

> Internal placeholder v1 — Chat, Tools, Cron, Gateway, Memory, Skills, Terminal,
> Dashboard, Local models, Soul, Settings + Setup wizard + Foreground Service are
> scaffolded with real logic seams; proot bootstrap, PTY exec, and gateway pairing
> complete in the full build.

```
┌─────────────────────────────────────────────────────────┐
│  UI (Jetpack Compose, Material 3, HermesTheme gold)     │
│  Chat│Terminal│Tools│Cron│Gateway│Memory│Skills│Dash     │
│  Models│Soul│Settings│SetupWizard│ApprovalDialog        │
├─────────────────────────────────────────────────────────┤
│  ChatViewModel ◄── HermesBridge ──► backend             │
│  StreamParser (line-JSON/SSE → Token/ToolStart/Out/End) │
├──────────────┬────────────────────┬─────────────────────┤
│ PROOT        │ IPC                │ LOCAL LLM           │
│ ProotInstaller│ ACP → gateway     │ LlamaServerManager  │
│ Debian/Termux │ HTTP/WS 127.0.0.1 │ llama.cpp :8080     │
│ .[termux] pin │ else PTY+parser   │ HF GGUF downloader  │
├──────────────┴────────────────────┴─────────────────────┤
│ HermesForegroundService (dataSync+WakeLock) │ BootRecv  │
│ ~/.hermes → app-private │ SAF backup │ Keystore keys    │
└─────────────────────────────────────────────────────────┘
```

**Modules** (`app/src/main/java/com/hermes/android/`):
`backend/ProotInstaller` (pinned Hermes commit, constraints-termux.txt, live-log steps,
auto-retry) · `ipc/HermesBridge` + `ipc/StreamParser` (+unit tests) ·
`llm/LlamaServerManager` (library, RAM guard, benchmark, OpenAI-compat endpoint) ·
`service/HermesForegroundService` + `BootReceiver` · `chat/ChatViewModel` ·
`data/Catalog` (slash commands, toolsets + honest unsupported states) ·
`ui/{theme,chat,tools,cron,gateway,memory,skills,terminal,dashboard,models,settings,setup,soul,more,security}`.

**Backend (full build):** Termux/DEBIAN proot → pkg deps
(python, clang, rust, make, libffi, openssl, nodejs, ripgrep, ffmpeg) → venv →
`pip install -e '.[termux]' -c constraints-termux.txt` at pinned commit.

**Test plan:** `StreamParserTest` (5 tests: token/tool-lifecycle/SSE/plain/session-error) —
`./gradlew :app:testDebugUnitTest`. On-device: setup wizard green → chat streams →
interrupt → tool card → local model tok/s → cron pause/resume → gateway test → SAF backup.

**Honest limitations (exact in-app copy):**
- Voice: *"Voice capture (faster-whisper extra) is not available in the Android bundle… Type or use system dictation; TTS replies still work."*
- Browser: *"Playwright auto-bootstrap is not available on Android… web_search + web_extract cover most browsing."*
- Docker: *"Docker backend needs a container runtime — not present on stock Android. The proot userland + code_execution tools run your code on-device instead."*
- Background: *"Android suspends background work. Hermes runs as a Foreground Service with a WakeLock; exempt the app from battery optimization or long cron jobs may pause."*

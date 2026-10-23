# VisualBook TTS Probe — Android research only

**This is not the VisualBook story player.** It performs small, reproducible tests on an actual Android phone to resolve our narration architecture risk.

- No internet permission or analytics.
- Lists installed TTS voices and advertized offline support.
- Sends three separate live Tagalog TTS utterances and logs boundary callbacks.
- Synthesizes one sentence to a WAV file and then plays it via MediaPlayer, explicitly logging *synthesis completion vs audible completion*.
- Shows optional word-range callbacks when the engine supports them.
- Share button copies diagnostic text into the user's chosen Android share target; nothing is uploaded automatically.
- Does not prove production-quality Tagalog speech, nor anything about our 2D renderer.

## Build

GitHub Actions workflow [tts-probe-build.yml](../../../../.github/workflows/tts-probe-build.yml) builds a debug APK when this experiment branch changes. Local build, with JDK 17, Android SDK 35, Gradle 8.11.1:

\`\`\`sh
cd research/experiments/android-audio/probe
gradle --no-daemon assembleDebug
\`\`\`

Debug APK path: \`app/build/outputs/apk/debug/app-debug.apk\`. Debug Android package name: \`com.visualbook.ttsprobe\`.

## Device protocol

1. Open the app on a real Android device (preferably Android 15 + low-memory phone).
2. Enable airplane mode, then test all five controls: list voices, live sequence, prepared sequence, stop during speech, share report.
3. Do not take successful initialization as confirmation of Tagalog voice quality. A default English voice can synthesize Tagalog text poorly.
4. Listen for audible discontinuities, record approximate audible differences, and report actual installed voice name / locale / network-required flag.
5. Repeat on a second available device, then repeat with airplane mode off only if consented.
6. Do not edit captured reports to turn processing timestamps into sample-accurate audio timing.

## Known limitations

The live callback is close to audio playback but not guaranteed sample-accurate. Prepared-file TTS callbacks are about file writes, not audible sound. Voice selection is opportunistic: first advertised offline \`fil\`/\`tl\` voice if available; if none, the chosen default remains and may not be Tagalog-capable.

The experiment currently probes *one* prepared utterance and *three* live utterances; it is not a full 30-phrase or p95 latency benchmark. It also does not test Media3 background service, audio focus, process recovery or rendered visuals. Those remain future experiments.

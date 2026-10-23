# VisualBook TTS Probe v0.3 — Android research only

**Device feedback:** v0.1 showed occasional UI unresponsiveness when Stop was followed by switching playback mode. v0.2 moves TTS/MediaPlayer stop and commands to a serial background HandlerThread, limits UI log rendering, adds a heartbeat and timing diagnostics, and validates range callbacks. See [Samsung SM-A066B observation](../DEVICE_TEST_SM-A066B_2026-10-24.md).

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

Debug APK path: \`app/build/outputs/apk/debug/app-debug.apk\`. Debug Android package name for v0.3: \`com.visualbook.ttsprobe.v3\` (separate from v0.1, because independent CI debug-signing keys may differ).

## Device protocol

1. Open the app on a real Android device (preferably Android 15 + low-memory phone).
2. Enable airplane mode, then test the available controls: list voices, live sequence, prepared sequence, stop during speech, share report.
3. Do not take successful initialization as confirmation of Tagalog voice quality. A default English voice can synthesize Tagalog text poorly.
4. Listen for audible discontinuities, record approximate audible differences, and report actual installed voice name / locale / network-required flag.
5. Repeat on a second available device, then repeat with airplane mode off only if consented.
6. Do not edit captured reports to turn processing timestamps into sample-accurate audio timing.

## Known limitations

The live callback is close to audio playback but not guaranteed sample-accurate. Prepared-file TTS callbacks are about file writes, not audible sound. Voice selection is explicit via the two Filipino comparison buttons; if no offline candidates are advertised, the original active voice is retained. Advertised offline voices can still fail if local voice data is missing.

The experiment currently probes *one* prepared utterance and *three* live utterances; it is not a full 30-phrase or p95 latency benchmark. It also does not test Media3 background service, audio focus, process recovery or rendered visuals. Those remain future experiments.

## v0.2 regression exercise

On Android 15 (SM-A066B), rapidly perform live → stop → prepared → stop → live and record any freeze. Examine `tts.stop() duration_ms` and `UI-MAIN gap` markers. A successful Gradle compile cannot establish a runtime freeze is fixed; share a new report after reinstalling the APK. If UI still freezes, use Perfetto or Android ANR trace to capture main thread and binder state.

The v0.2 APK installs as a **separate application**, not an update to v0.1; use the `VB TTS Probe v0.2` icon. Uninstall old experiment APKs later when no longer needed.

## v0.3: Compare Filipino offline voices on Samsung SM-A066B

**What the user actually observed in v0.2:** voice inventory had 473 entries; `fil-PH` support=1, `tl-PH` support=-2; active voice still `en-US-language`. A local voice listing is *not proof* installed data synthesizes audible natural Tagalog. The first test did not invoke `setVoice`.

1. Put the phone in airplane mode. Open **VB TTS Probe v0.3** (installed side-by-side with v0.1 and v0.2).
2. Once TTS ready, tap `List installed voices`; capture engine name, active voice, local Filipino candidate names, voice quality and latency metadata.
3. Tap `Next OFFLINE Filipino voice + LIVE` once. Check log for `FILIPINO VOICE candidate=`, `selectStatus=0` and matching `effective=`. Listen to three sentences.
4. Repeat for each candidate, rating Filipino pronunciation, intonation, clarity and pauses. `quality` is *engine metadata*, not an objective Tagalog voice-quality score.
5. Repeat promising voices using `Next OFFLINE Filipino voice + PREP` (cycles one voice each click). Prepared audio playback must use actual MediaPlayer completion, **never** synthesizeToFile.onDone.
6. Share logs, subjective results and whether airplane mode was active. A voice may advertise local capability but still fail synthesis if data is missing.

**No actual file/audio model is bundled, no cloud access permission in the app.** This still does not prove high-quality bundled offline Tagalog on a fresh phone; it examines installed TTS engines only.

## Samsung v0.3 voice failure discovery

On SM-A066B Android 15, first two local advertised Filipino voice names **selected successfully** but synthesis failed with **`onError -7`**, confirmed Android `ERROR_NETWORK_TIMEOUT` (NOT `-9 ERROR_NOT_INSTALLED_YET`). See [detailed device report](../DEVICE_TEST_SM-A066B_V03_TIMEOUT_2026-10-24.md). Before changing engines, inspect Settings → General management → Text-to-speech → Samsung preferred-engine gear → Install voice data. If Filipino is available, install with an appropriate connection, and then retry in airplane mode. If Samsung still fails, change preferred TTS engine to Google's if installed, restart this probe and compare. Voice-data installation is an unverified hypothesis; source language availability and `setVoice SUCCESS` are not production proof.

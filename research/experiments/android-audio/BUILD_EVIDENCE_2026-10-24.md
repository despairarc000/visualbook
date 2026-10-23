# VisualBook Android TTS Probe — Build Evidence

**Date:** 2026-10-24 Asia/Manila. **Type:** Android diagnostic app **BUILD**, NOT on-device validation of TTS or narration quality.

## Reproducible build

- Repository: `despairarc000/visualbook`
- Research branch: `research/android-tts-probe`
- Known-good build source commit: `21aa89e8d5570c73d7b12054f4c5ea03489863a4`
- GitHub Actions workflow: [TTS Probe Build](../../../.github/workflows/tts-probe-build.yml)
- Successful run: [40019414406](https://github.com/despairarc000/visualbook/actions/runs/40019414406), build + artifact upload both succeeded.
- Artifact ID: `12530336090`, name `visualbook-tts-probe-debug`, created 2026-10-23 20:28 UTC, expires 2026-10-30 20:28 UTC. Do not present this temporary URL as permanent storage.
- Debug APK: `app-debug.apk` inside artifact; 17,425 bytes; SHA-256 `d0fc6eb255c3d0c494dc47c4345ada02b23616f32b12c3ef00850f2083a2ef8d`.
- The binary was retrieved from GitHub artifact and inspected as a valid ZIP/APK archive with `classes.dex`, `classes2.dex`, binary manifest and resources. **No install/run has been verified.**

## Build failure history

1. Original run [39969481456](https://github.com/despairarc000/visualbook/actions/runs/39969481456) failed during `android-actions/setup-android@v3`. This action requested retired SDK package `tools`.
2. Attempt [40019328420](https://github.com/despairarc000/visualbook/actions/runs/40019328420) removed the failing action but `sdkmanager` was not on PATH.
3. Corrected workflow uses `android-actions/setup-android@v4` with `packages: platform-tools` and Android SDK platform 35. The resulting run [40019414406](https://github.com/despairarc000/visualbook/actions/runs/40019414406) passed.

## What the probe does

- Enumerates available voices and advertized network requirement flags, checks `fil-PH` and `tl-PH` availability.
- Queues 3 short Tagalog test utterances; logs live utterance start/end and optional word-range callbacks.
- Synthesizes one utterance into a file; logs that `onDone` means **file preparation**, then separately plays it using `MediaPlayer` and logs actual player completion.
- Increments a generation counter when stopping to ignore stale results.
- Displays memory PSS snapshot and shares diagnostics through explicit user action.
- Declares no INTERNET permission; this does **not** establish that a separately installed/system TTS engine never uses network. Test in airplane mode.

## What is still untested

No physical device installation, Tagalog intelligibility, TTS accent, offline voice presence, accurate audible segment gaps, word boundaries, headset/audio focus, interruption, screen-off operation, recovery, RAM/thermal stability, production `Media3`, or integration with VisualBook's story compiler and scene renderer.

## Immediate device test

Read [probe README](probe/README.md). On Android, install the research APK from the successful workflow; put phone into airplane mode; record voice list; run live and prepared tests; press Stop during playback; share diagnostic text and subjective comments (accent, gaps, voice naturalness). Do not share personal device identifiers unrelated to the experiment.

## Gate

**PASS:** source builds as an installable APK in CI.  
**OPEN:** any conclusion about TTS quality or real callback timing on target hardware.

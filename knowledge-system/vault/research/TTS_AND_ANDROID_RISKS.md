# TTS / Android Investigation Register

**Status:** PROVISIONAL / OPEN. Research snapshot last gathered 2026-10-23, no Android benchmark performed.

## Evidence from earlier v0.3 work

- Android `UtteranceProgressListener` supports audible start/done semantics; range callback depends on particular installed engine. `synthesizeToFile` can prepare audio for our own player. Inspect [v0.3 research](../history/v0.3/research/01_FINDINGS_AND_SOURCES.md) for primary documentation URLs and caveats.
- Preferred runtime contract is prepared segment audio with actual playback-position clock, live Android TTS fallback coarse segment anchors and pre-recorded audio as another producer.
- Media3 MediaSessionService provides a candidate for background narration; practical interruptions, restart, resumption and focus need device testing.
- Meta MMS Tagalog checkpoint exists with **CC BY-NC 4.0** licensing; cannot assume commercial redistribution. A fast inference backend like sherpa-onnx does not imply an available Filipino voice. Kokoro public language list does not prove native Tagalog support.
- libGDX Android embedding is plausible, but real measured rendering/power/memory with concurrent synthesis is missing.

## Required research procedure

Use 30-line Tagalog test corpus from v0.3 source research pack when available; reproduce on target low-memory Android plus at least one modern device. Record Android build, local voice enumeration, offline airplane mode, audio samples with consent, subjective ratings, synthesis duration and p50/p95 pauses, CPU/RAM, background service behavior. Evaluate risk and legal distributions before selecting a bundled model.

## Major blockers

No actual high-quality offline Tagalog voice selected; an **experimental debug APK builds**, but no physical device or simultaneous render/TTS profile has been demonstrated. Never claim its compiled code establishes audible accuracy or good Tagalog speech.

## New build evidence (2026-10-24)

The [TTS Probe Android diagnostic](../../../research/experiments/android-audio/probe/README.md) builds on GitHub Actions. [Build report](../../../research/experiments/android-audio/BUILD_EVIDENCE_2026-10-24.md) distinguishes SDK tooling errors from actual Java compile success. The next blocker is **real device execution in airplane mode**. `android-actions/setup-android@v3` requested the retired `tools` package; moving to v4 fixed CI setup. No neural model was shipped.

## First user device evidence (2026-10-24)

[Samsung SM-A066B report](../../../research/experiments/android-audio/DEVICE_TEST_SM-A066B_2026-10-24.md): live utterance start/end callbacks succeeded for two full runs and prepared-file output completed, but user reported UI freeze after Stop followed by another mode. No actual ANR/Perfetto trace yet. Preliminary code review implicates main-thread stop/release and per-callback log re-layout as possible causes; v0.2 experimental fix on branch `fix/tts-probe-switch-freeze`. Live optional word callback exists, but invalid reordered file-range data means neither audio path should assume reliable fine-grained range alignment without validation. Offline voice support not established.

## Samsung v0.2 stress evidence (2026-10-24)

On SM-A066B Android 15, [v0.2 rapid switching test](../../../research/experiments/android-audio/DEVICE_TEST_SM-A066B_V02_2026-10-24.md) was reported smooth, with `tts.stop()` generally 0–4 ms on worker, MediaPlayer cleanup 49–54 ms, no new UI heartbeat-gap report during the stress sequence. Only one initial 514-ms startup UI gap occurred, preceding TTS init. The engine advertises `fil-PH` (country-level support), several local Filipino candidates, but **active voice was en-US-language**. Therefore voice quality/availability is the *next* hard blocker. An advertised network-free voice is not proven available until it actually synthesizes offline; the new v0.3 probe cycles and confirms these voices.

# Android TTS Probe v0.2 — Build and Fix Verification

**Status:** VERIFIED-CI-BUILD / NOT VERIFIED-ON-DEVICE

- Device complaint: Samsung SM-A066B, Android 15; TTS v0.1 intermittent UI freeze after Stop followed by switching modes. Original user log and hypotheses: [SM-A066B observation](DEVICE_TEST_SM-A066B_2026-10-24.md).
- Fix PR: [#3](https://github.com/despairarc000/visualbook/pull/3), merged into `main` with SHA `ac19c138f6f2e81d823cdad83dbccb3bdbf05cea`.
- Source commit tested: `a54f2060f86cbe0bd74f676fa55bd3e2d38579b4`.
- Android APK CI [run 40025942636](https://github.com/despairarc000/visualbook/actions/runs/40025942636): SUCCESS, `assembleDebug` and artifact upload success.
- Knowledge doctor CI [run 40025942618](https://github.com/despairarc000/visualbook/actions/runs/40025942618): SUCCESS.
- Uploaded artifact ID `12532876701`, name `visualbook-tts-probe-debug` (temporary Actions artifact).
- Extracted `app-debug.apk`: **20,437 bytes**, SHA-256 **`67be5455cae7ff83ac9ca69efedc5316b50005f3fcd4734938a155b21ae39569`**. Android package archive ZIP integrity tested successfully.
- Android application ID changed to `com.visualbook.ttsprobe.v2`, label `VB TTS Probe v0.2`, to permit side-by-side installation with v0.1 even if CI debug signatures differ.

## Changes under test

1. Commands `tts.stop()`, `TextToSpeech.speak()`, `synthesizeToFile()`, voice enumeration and `MediaPlayer` setup/release are serialized on a background `HandlerThread`, instead of UI click handlers.
2. Monotonic generation token immediately invalidates pending commands and stale callbacks when Stop/Live/Prep buttons are pressed rapidly.
3. Visible log updates are batched (at most once per 200 ms) and show last 9,000 characters; a bounded full diagnostic report can still be shared.
4. UI heartbeat checks for main-thread scheduling gaps; timed command logs expose blocking audio service operations.
5. Invalid `onRangeStart` offsets from prepared-file engine callbacks are flagged and ignored.

## Important limits

A successful GitHub Gradle build **does not prove** freeze is fixed. No v0.2 physical device test, ANR trace, synchronized high-rate waveform measurement, offline voice inventory or Tagalog naturalness verdict was supplied.

## Follow-up device test

1. Install the v0.2 APK as a separate app; ensure the title says `v0.2`.
2. Let TTS init complete. Press List voices, then LIVE; while speech is active, tap STOP then PREP. Reverse the order and repeat several times.
3. Look for `AUDIO COMMAND begin/finished`, `tts.stop() duration_ms=`, `UI-MAIN gap`, `AUDIO COMMAND pending`, and `ENGINE RANGE ANOMALY`.
4. Note whether screen itself becomes unresponsive or only audio stops. Export diagnostic text.
5. If freeze recurs, capture Android ANR/Perfetto thread stacks; prioritize main Looper, BinderProxy and TTS service interactions.

Voice-model selection and the final VisualBook narration scheduler remain open until verified device evidence is collected.

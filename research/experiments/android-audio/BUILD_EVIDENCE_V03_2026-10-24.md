# TTS Probe v0.3 — Offline Filipino voice selection build evidence

**Status:** BUILT and artifact-inspected; ON-DEVICE VOICE TEST NOT RUN.

- Repository: `despairarc000/visualbook`
- [PR #4](https://github.com/despairarc000/visualbook/pull/4) merged as `2fee9270d2de5000756b3a6a3734a122f8990959`.
- CI source head: `b5549df1ac39213a464addd9363e919ea4c7ae32`.
- [Android build 40027370638](https://github.com/despairarc000/visualbook/actions/runs/40027370638): success; Gradle assembleDebug and artifact upload succeeded.
- [Knowledge doctor 40027370449](https://github.com/despairarc000/visualbook/actions/runs/40027370449): success.
- Temporary GitHub Actions artifact: id `12533900255`, name `visualbook-tts-probe-debug` (expires 2026-10-30 21:32 UTC).
- Extracted `app-debug.apk`: 22,305 bytes, SHA-256 `fd78ac545662674aa103ab394aac7ae91085ec63406b902641fd722a0d27bc96`. Verified ZIP integrity and Android manifest entry.
- Android app ID `com.visualbook.ttsprobe.v3`, label `VB TTS Probe v0.3`; separate install from original v0.1 and fixed v0.2.
- No network permission in app manifest; TTS service may have its own network access, so use airplane mode for meaningful offline proof.

## What changed

The user-supplied SM-A066B v0.2 stress test reported smooth switching but revealed `fil-PH` availability and advertised local Filipino voice variants while **active voice was en-US-language**. See [device observations](DEVICE_TEST_SM-A066B_V02_2026-10-24.md).

v0.3 adds two buttons to cycle through explicitly selected non-network Filipino candidate `Voice` objects: LIVE and PREP. The probe logs selected name, language tag, `isNetworkConnectionRequired`, declared quality and latency, setVoice result and **effective getVoice name**. That gives direct evidence which voice is actually used for each speech sample.

## Test protocol

1. Install separate `VB TTS Probe v0.3`. Put phone in airplane mode and initialize TTS.
2. Run List voices; verify currently active/default engine.
3. Tap Next OFFLINE Filipino voice + LIVE several times. Record the selected/effective name, any error, intelligibility and naturalness (1–5).
4. If a voice sounds promising, repeat on prepared playback path (note this button also advances to the next voice).
5. Export diagnostic log, report whether internet was disabled, and compare perceptual gaps/prosody.
6. Do not interpret metadata quality as human rating or callback gap as exact acoustic silence.

**Exit gate:** at least one usable local voice verified to synthesize intelligible Filipino in offline mode, with recorded exact voice ID and observed playback. Until then, voice backend selection remains provisional.

# Android Narration Experiment — Targeted Plan

**Status: NOT RUN.** No APK/device logs/audio samples yet. Do not mark as measured.

## Hypotheses

H1: prepared segment audio + player-owned audible clock is more suitable for exact seek/pause/scene cues than live `TextToSpeech.speak` with callback-only boundaries.

H2: short segment boundaries can be heard as unnatural pauses or different intonation; avoid optimizing merely for callbacks.

H3: on-device offline Tagalog support and naturalness depend on installed engines and licensed neural model availability. Never infer from an inference library's marketing list.

## Documented Android API facts (verify at experiment time)

- [UtteranceProgressListener](https://developer.android.com/reference/android/speech/tts/UtteranceProgressListener): `onStart` is shortly before audible playback for `speak()`; `onDone` after audio finishes **for audible speech**, but after file bytes written **for synthesizeToFile**; `onRangeStart` is optional; callback methods may be called from multiple threads. Must serialize events through playback adapter and reject stale generation.
- [TextToSpeech](https://developer.android.com/reference/android/speech/tts/TextToSpeech): allows synthesis to file and voice inventory where engines provide it; `setVoice` not equal guaranteed offline support.
- [Media3 background service](https://developer.android.com/media/media3/session/background-playback): keep media session/player inside a service rather than Activity.

## Two test adapters (same scripted corpus)

A. **Live TTS:** queue utterances with IDs containing scene visit, block, utterance and generation. Record callback timestamps and audio on-device via allowed test harness; onStop/error must not increment story position.

B. **Prepared audio:** synthesize to a controlled media cache, verify output integrity, queue in Media3 ExoPlayer and measure **player position**/media end for narration events. Never treat synthesizeToFile.onDone as audible completion. Invalidate cache on text + backend + voice + rate + language + synthesis config changes.

## Test matrix

- 30 original Tagalog sentences: [test corpus](../../../knowledge-system/vault/history/v0.3/research/TAGALOG_TTS_TEST_CORPUS.tsv).
- 4GB RAM low-memory Android baseline; at least one second device. Record exact Android build/TTS engine/voice locale/available voices/network/airplane-mode status.
- Record cold/warm synthesis duration, per-segment audible gap (p50/p95), CPU/RAM/thermal/frame pacing, voice intelligibility/naturalness and errors.
- Pause midway, resume, skip/seek to sentence, rotate, lock screen, process kill/reopen, audio focus loss, unplug headset, change voice while buffering, change speed.
- End-to-end trigger tests: cue fires only once after audible completion; stale callbacks suppressed after seek/voice change; display scene state reconstructed on seek.
- Capture licensing of each candidate Tagalog model separately. Never bundle a model without redistributable rights.

## Success criteria — proposal, not benchmark result

- Zero skipped/duplicated spoken utterances over normal continuous playback and stress seek scenarios.
- No scene/visual event triggered by synthesis completion alone.
- No crash on missing voice and explicit text-only fallback.
- Correct subtitle/active speaker at utterance start/end with a debug trace.
- Quantify real gaps before setting numeric acceptance targets. Human listenability judgment takes precedence over shortest possible chunk.

## Next action

Create a **small, disposable Android experiment branch** after the format/reducer interface is agreed; run it on target device and upload measured evidence. Do not choose libGDX or neural model based on this document alone.

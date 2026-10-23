# SM-A066B — Five Filipino voice profiles successfully synthesizing

**Status:** USER-SUPPLIED REAL DEVICE CALLBACK LOG (2026-10-24). Test app: VisualBook TTS Probe v0.3. Hardware Samsung SM-A066B, Android 15 SDK 35. **Perceptual voice qualities and airplane mode are unverified.** User stated they used **Google's offline** speech configuration, but the diagnostic's `getDefaultEngine()` returned `com.samsung.SMT`. Do not resolve that contradiction by assumption.

## Key reversal from previous failure

Earlier v0.3 on the same phone: `fil-PH-language` live and `fil-ph-x-cfc-local` prepared both failed with `onError -7`. **Current log** reports these same two voice IDs accepting/producing speech, plus three more completing a full live 3-utterance test. The cause of this transition is not established (voice data installation, network environment, engine preference, an update, or a different TTS service response).

## Exact voice IDs and results

| Candidate (sorted by API name) | Requested selected `setVoice` | Observed synthesis/playback | Duration from callbacks |
|---|---|---|---|
| `fil-PH-language` | SUCCESS, `effective` matches | LIVE starts, word-range callbacks; user switches before it finishes; later retries also interrupted | No completed single-utterance duration |
| `fil-ph-x-cfc-local` | SUCCESS, `effective` matches | PREP synthesizes 191,688 byte audio file and MediaPlayer plays to completion; later LIVE starts but interrupted | Prepared audio: 4,326 ms playback callback interval |
| `fil-ph-x-fic-local` | SUCCESS, `effective` matches | LIVE completes all 3 sentences, live word ranges present | 3,663 ms; 3,098 ms; 1,730 ms |
| `fil-ph-x-fid-local` | SUCCESS, `effective` matches | LIVE completes all 3 sentences, live word ranges present | 3,674 ms; 3,166 ms; 1,689 ms |
| `fil-ph-x-fie-local` | SUCCESS, `effective` matches | LIVE completes all 3 sentences, live word ranges present | 3,924 ms; 3,033 ms; 1,567 ms |

In this run all five returned `requiresNetwork=false` and engine metadata `quality=400`, `latency=200`. These numeric metadata values were the **same across all five** and do **not** measure listening preference, voice gender, perceived quality or actual device inference latency. The voice suffixes are opaque identifiers: `cfc/fic/fid/fie` must not be decoded into human vocal properties without vendor evidence.

No `TTS onError -7` is visible in the user-supplied new excerpt.

## Important engine discrepancy

At approximately `92118807` the app prints:
```text
Voice inventory count=473 default_engine=com.samsung.SMT active=en-US-language
```
This comes from `TextToSpeech.getDefaultEngine()`, which [Android specifies](https://developer.android.com/reference/android/speech/tts/TextToSpeech#getDefaultEngine()) as the *default engine preference*, **not a direct attestation of the currently bound service**. The probe instantiates `new TextToSpeech(context, listener)` with no explicit requested package, so it should normally bind the preferred engine; if binding fails, Android may fall back to another engine. Given the Samsung-looking IDs and reported default, the test **probably** involved Samsung TTS, but the app did not independently log the actual bound provider package. It is incorrect to label these five voices "Google voices" without an explicit engine-bound comparison.

For a definitive provider A/B test, enumerate installed engines via `tts.getEngines()`; instantiate `TextToSpeech(context, listener, "com.samsung.SMT")` and a separate instance for `"com.google.android.tts"` if installed; capture `setVoice/getVoice`, exact sample voice IDs, real synthesis and successful audio callbacks per provider. Check for fallback to another engine, because requesting an engine does not guarantee it was loaded. The current public Android API does not provide a normal supported `getCurrentEngine()` method; on-device logs and provider-specific IDs may supplement requested-engine diagnostics.

## Audio architecture implications

- The provider (engine package), voice ID, exact locale, configured rate/pitch, provider version and whether it has **successfully synthesized offline** are separate fields.
- Five listed `fil-PH` IDs are *candidate profiles*, not guaranteed five perceptually distinct or usable character voices. A human audition is needed.
- Live TTS utterances in this run expose `onRangeStart` word offsets, making optional advanced word-synced visual reactions plausible **for this engine**. Do not require range callbacks for baseline stories.
- Do not use callback timestamp differences as acoustic silence measurements; utterance durations here reflect callback intervals.
- Narration/caption state only advances on reported audible playback boundaries for a successful played segment; never on `synthesizeToFile` completion, failed synth, canceled commands or stale generation.
- Support per-character voice assignments and previewed choices in a future VisualBook cast manager. Keep author speaker IDs independent of concrete provider IDs; allow a default narrator and two different selected voices, with stable playback and fallback to text/captions.

## Next validation procedure (non-destructive)

1. Confirm whether the **audio actually sounded different** across `cfc`, `fic`, `fid`, `fie` and whether Filipino pronunciation was intelligible, natural or robotic. Gather qualitative 1–5 ratings *after listening*.
2. Test with airplane mode (confirm actual state) for one full live run on at least two voice IDs, plus a prepared clip. Verify no download/network dependency.
3. Verify actual provider by comparison of explicit Samsung and Google TTS engine instances, if both installed. Avoid assuming device preference change is reflected in an already-initialized app instance.
4. Re-run on another available Android device or clean install, if possible, to establish portability; bundled licensing is still open.
5. Only then define default VisualBook voice profile and per-character presets. Visual system work can proceed in parallel.

Prior report: [v0.3 timeout trace](DEVICE_TEST_SM-A066B_V03_TIMEOUT_2026-10-24.md). This is a **new real-device success**, not proof of distributable voices or final narration quality.

## Human listening feedback (additional user report, 2026-10-24)

After hearing the candidate voices, the user answered **"Kinda different, no same"** when asked whether `cfc`, `fic`, `fid`, and `fie` sound like different people or mostly the same voice. Interpret conservatively: **the voice outputs are perceptibly different to the listener, though the degree of difference was not rated**. Do not claim any particular male/female presentation, identity, age, expressive range, or naturalness score from this qualitative report.

This provides initial user validation of **audible variety for a multi-character cast**, stronger than merely distinct API voice IDs. It does NOT establish stable differences across phones, actual engine identity, quality of pronunciation, rights to redistribute voices, or offline operation when disconnected. Future audition: play equal sample texts at matched rate/pitch without switching mid-utterance, rank clarity/naturalness, and assign voices to narrator/characters through an independent VoiceProfile table instead of hardcoded `@c1` genders.

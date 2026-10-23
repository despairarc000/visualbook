# 06 — Offline Tagalog Narration, Voice Intent, and Runtime Budgets

## Voice types: do not conflate

1. `SpeakerId`: author marker such as `c1`, `c2`, `n`.
2. `VoiceIntent`: locale (e.g., `fil-PH`, alternate `tl-PH` mapping when supported), desired register, optional presentation traits.
3. `VoiceProfile`: user/story selection and allowed fallback.
4. `VoiceModel`: actual licensed model assets/config/frontend/input tokenizer/speaker embedding.
5. `SpeechBackend`: Android TTS, supported neural ONNX engine, recorded-audio provider; backend capabilities are independent of whether any particular Tagalog voice is installed.

A male/female suffix is a speaker attribute, not a voice-model ID and not guaranteed voice availability. In a system with only one suitable Tagalog voice, two characters may temporarily share it but must retain distinct speaker identities and visual labels. Do not randomly assign an English voice to Tagalog text without an explicit degraded-mode choice.

## Resolving a voice

```
1. Enumerate available runtime providers and compatible installed voices.
2. Validate locale/language, explicitly offline requirement, installed files/decoder rights.
3. Apply user-selected compatible voice, else story preferred voice, else recommended default.
4. If preferred missing, use declared fallback policy and inform user of significant loss.
5. If none can speak chosen language offline, offer install/import compatible voice pack or text-only.
```

Model packages are data only; never execute shipped Python/native libraries. Manifest must include verified license, hashes, decoder family/version, sample rate, speaker IDs, tokenizer/phonemizer, memory profile, language tags, and model format. Native permissions/inference API solely held by trusted application.

## Candidate categories (not final winners)

| Option | What research supports | Blocker |
|---|---|---|
| Installed Android TTS | Built-in engine enumeration, `getVoices`, `isNetworkConnectionRequired`, `speak` and file synthesis | Quality, `fil-PH` presence and true offline behavior differ by phone and installed data |
| MMS-TTS Tagalog | A published Tagalog checkpoint exists | CC BY-NC 4.0; conversion, size, voice quality and Android performance unverified |
| sherpa-onnx family | Android-capable local inference for compatible voice model families | No evidence that a Tagalog model we can ship is supported and licensed |
| Kokoro | Apache-2.0 weights, modern local TTS candidate | Language coverage does not establish native Tagalog; can't claim it works well |
| Recorded narration | Excellent deterministic timing when supplied by author | Much bigger files; not a general text-to-voice solution |

## Benchmark suite (run on real low-resource Android hardware)

Text corpus: 50+ lines across Tagalog, Taglish, honorifics/abbreviations, numbers, expressive questions, long dialogue, laughter cues, semantically ambiguous punctuation, user names and intentionally malformed text. Three speakers: narrator, c1, c2. Test cold and warm synthesis, single long request vs 1/2/4-sentence chunks, 30 min continuous story, screen-off playback and rapid seek.

Record: real-time factor (synthesis seconds / output audio seconds); time-to-first-playable-segment; p50/p95 inter-segment silence; pronunciation intelligibility assessed by native Tagalog speakers; naturalness ratings with blind pairwise A/B; word omission; peak Java/native/GPU RSS; CPU %; thermal state and frame jank; battery drain proxy; APK/model storage; rate of recovery from engine errors. Do not declare unsupported sample-accurate range sync.

**Proposed device targets**, not measured: 4GB RAM class and Android 15 baseline, with economy mode as fallback. Track budget by separate components (renderer decoded textures, audio buffers, model resident memory, native decoding/inference, process overhead); do not merely sum APK compressed sizes. 4000x3000 RGBA image consumes ~48 MB decoded, regardless of small compressed image disk size; bound texture dimensions, loads and worker concurrency.

## Synthesis/caching design

- Preserve displayed text separately from normalized spoken text (numbers and abbreviations may be expanded for pronunciation).
- Cache key = normalized speech + backend ID/version + model hash + voice ID + locale + synthesis parameters + text normalization version.
- Lookahead bounded in number of clips, total bytes, expected time to prepare and peak model usage. Synthesis completion only caches output, **not** story progress.
- `SynthesisResult` must include audio sample rate/channel/duration; alignment timestamps optional. Invalid output or empty audio is recoverable error, not a completed utterance.
- Audio-only listening can suspend GL draws while preserving audio service and semantic state; resuming UI reconstructs visuals.
- Switching a voice invalidates downstream prepared cache only where fingerprints change; keep global author text unchanged.

## Ship/no-ship gate for 'fully offline Tagalog TTS'

A voice must satisfy all: tested offline, legally redistributable or reliably present through controlled installation, acceptable native intelligibility, device performance within measured resource budgets, stable pause/resume semantics, and predictable fallback. If no candidate satisfies all, product may ship as an **offline story renderer/reader with optional installed TTS**, but must not claim bundled high-quality Tagalog narration.
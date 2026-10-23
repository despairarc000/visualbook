# 08 — Investigated Roadmap, Test Evidence and Stop Conditions

## Completed this iteration (2026-10-23)

- Research verified Android TTS event distinctions, media service, model license and renderer technology candidates. See research file for primary source links.
- Audited previous asset seed: 101 entries, 101 distinct IDs, all fallback targets exist (audited in Python; media binaries not inspected because none included).
- Implemented runnable restricted authoring compiler prototype that parses simple speaker markers and emits speaker registry, speech blocks, TTS segments and block-boundary expression cues.
- Added **29 passing automated tests** (22 authoring/segmentation, 7 fake narration reducer) as of October 23, 2026. See `tests/` and rerun; this is **NOT** a real TTS/audio playback integration test.
- Compiled minimal and named-character Tagalog example scripts to JSON IR prototypes. Smoke-tested the earlier ~100KB Tagalog anthology as plain TXT: **1,765 blocks**, **4,035 proposed utterances**, **0 compiler errors** (not a speech quality test). Built a 30-line TTS benchmark corpus.

## Phase gates, ordered by rewrite risk

| Gate | Required artifact and evidence | Status |
|---|---|---|
| G0: product semantics | source-of-truth narrative model, plain TXT, implicit character rules and fallback semantics | **Principles adopted** |
| G1: formal language | precise grammar with byte/line spans, stable authored IDs, escaping, incremental editing, fixtures and JSON schema | **Prototype only** |
| G2: deterministic event reducer | model-based test harness with random seek/pause/flush/cancel and one-shot suppression | **Fake reducer tested, production not implemented** |
| G3: Tagalog voice selection | documented model license, successful offline 4GB phone audio, listening/latency benchmarks | **BLOCKED on measurement** |
| G4: renderer proof | backend compare, 9:16/16:9/cutout layouts, rotation and GL context recovery | **Not yet implemented** |
| G5: built-in art production | coherent art bible, actual first 3 backgrounds/2 characters, licensing and crop tests | **Catalog and production plan only** |
| G6: secure package import | supported manifest, capability negotiation, zip bomb/path tests, explicit voice model policy | **Threat model drafted** |
| G7: end-to-end 2-min slice | narrated Juan/Pedro, subtitles on/off, cue, scene fade, resume, offline proof | **Not yet implemented** |

Do not represent G0/G1 as finished implementation; completed prototype demonstrates syntax feasibility only.

## Work order — pursue in next engineering iteration

1. Freeze human authoring grammar and compile schema. Implement a real lexer/parser with source spans; maintain golden fixture corpus and language invariants.
2. Implement **pure core playback reducer with synthetic fake audio provider**. Test generations, seek, buffer and `block:end` triggers exhaustively, prior to Android integration.
3. Build tiny Android lab: query real installed TTS voices and whether network required; synthesize Tagalog/Taglish corpus to file; compare playback and run in airplane mode.
4. Benchmark alternate local neural TTS *only after verifying model/frontend/license*; do not rush to 'convert' noncommercial models for distribution.
5. Prototype rendering in libGDX embedded alongside Compose UI, with reusable scene state; compare Canvas if overhead high.
6. Produce original coherent procedural generic art (neutral gradient, two silhouettes) followed by classroom/street/living room art, anchors, crop variants, then effects/audio.
7. Integrate Media3 service, buffered speech, event reducer and one responsive scene; fuzz untrusted package importer.
8. Run acceptance test slice: two minutes Tagalog, two speaker markers, fallback art, missing BG, 4GB-class phone, portrait/landscape, screen-off/resume, continuous audio, no silent errors.

## Acceptance tests — first full slice

- NAR-01 `@n`, `@c1m`, `@c2m` compile to stable identities, dialogue text unchanged.
- NAR-02 multi-sentence block emits exact `block:end` only after final audible segment.
- NAR-03 audio prepared ahead does not cause visual transition before listener hears it.
- NAR-04 switch voice mid-block at safe boundary, stale callback ignored.
- NAR-05 rapid rewind/forward never duplicates one-shot SFX or wrong speaker expressions.
- NAR-06 audio focus/phone-call interruptions pause narration-led animations.
- VIS-01 generic silhouette remains identifiable in portrait and landscape and behind subtitle safely.
- VIS-02 background is always underneath cast regardless of author directive order.
- VIS-03 broken background falls back to neutral with diagnostic; story still plays.
- AUD-01 no compatible voice -> explicit text-only UI, not a fake success.
- AUD-02 installed offline voice produces intelligible Tagalog *without data*.
- AUD-03 prepared speech chunks don't cause unacceptable p95 gaps or semantic text loss.
- SEC-01 malicious archive paths/bombs/corrupt media safely rejected.
- AI-01 generated script with only bare speaker markers compiles with no dependency on AI during playback.

## What would invalidate current recommendations?

- If no admissible offline Tagalog model works locally, change launch promise, not story file grammar.
- If sentence synthesis is perceptibly fragmented, combine narration chunks while retaining block-boundary cues, or use generated audio alignment rather than shrinking all chunks further.
- If libGDX integration harms memory/lifecycle, swap renderer backend behind port, not story IR or script syntax.
- If simple heuristics mis-segment Tagalog dialogue, use language-aware segmentation and explicit block-level boundaries; do not silently change displayed text.
- If background cropping cannot preserve character framing, use scene-specific portrait/landscape layout overrides and neutral extension instead of stretching.

## Open decision register

- What is the actual legally redistributable Filipino/Tagalog neural voice, if any, meeting device quality target?
- Does a live Android TTS callback mode sound better/worse than prepared audio for the same backend?
- Exact audio-format choice for synthesized clips: PCM container vs Opus/other; compare generation/caching/playback overhead.
- Precise asset art pipeline and color/shape guide after testing reference batch.
- Text-only fallback experience when installation fails vs user chooses no narrator.
- Names and API of advanced script directives; defer until reducer and baseline language freeze.
- Package content size/entry limits after voice model strategy chosen.
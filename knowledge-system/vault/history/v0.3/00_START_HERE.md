# Cinematic Story Engine: Investigated Architecture, v0.3

**Date:** 2026-10-23. **Stage:** Phase 1 research + executable script proof-of-concept. **NOT an Android APK** or production-ready story format. This pack supersedes the v0.2 plan, which is preserved under `prior_v0_2/`.

## Non-negotiable product behavior

An offline-first Android story player that turns plain prose or progressively annotated scripts into a narrated, captioned, illustrated story. Narration audible position is the source of truth for narrative progression; scene, character and effects follow speech anchors. The smallest multispeaker annotation is `@n`, `@c1`, `@c2m`, `@c3f`; identities and optional sex metadata are resolved independently of presentation and voices. Missing *optional* creative detail is filled with coherent, safe defaults, not fabricated facts. Any viewer may replace character look/voice without altering the original story.

**Honesty constraint:** 'valid script is playable' includes a readable text-only fallback; 'all stories work with audible Tagalog while offline' requires us to ship or discover an *actually compatible, licensed, installed* Tagalog voice. This requirement is NOT currently demonstrated. Do not advertise unconditional offline narration before completing the voice acceptance gate.

## Investigation summary

1. **Language contract is tractable.** A minimal Python script compiler has been built to validate speaker markers, implicit characters, scene instructions, and block-relative cues, with source line diagnostics. A second pure fake-audio reducer tests the difference between synthesized/prepared audio and *actually played* audio, including seek, pause and stale events. Both have no Android/graphics dependencies. See `prototype/storytool.py` and `tests/test_storytool.py` and `tests/test_fake_playback.py`.
2. **Prepared utterance audio is the preferred clock.** Android `synthesizeToFile` exists; Android TTS `onStart/onDone` can support coarse live callback mode. Word ranges are not guaranteed. No audio synthesis or physical device experiment was performed yet.
3. **Candidate app architecture:** Android Kotlin/Compose UI + Media3 background audio + independent story core + 2D scene backend (provisionally libGDX). The backend has not been validated on target hardware.
4. **Licensed offline Tagalog model is a launch blocker.** Meta MMS-TTS Tagalog checkpoint exists under CC BY-NC 4.0. Kokoro is permissively licensed but its advertised language lineup does not establish Tagalog support. sherpa-onnx is a backend, not a guarantee of a Filipino voice. No device voice quality benchmark performed.
5. **Generic art must be style-coherent.** v0.2 has 101 *logical* resource IDs, zero included media files. v0.3 defines the first production batch, contracts, fallback validation and rendering role semantics.

## Read in order

- `research/01_FINDINGS_AND_SOURCES.md` — verified evidence, uncertainties, alternatives and decisive experiments. `research/TAGALOG_TTS_TEST_CORPUS.tsv` is the initial 30-line speech evaluation corpus.
- `contracts/02_ADRS_AND_MODULE_BOUNDARIES.md` — architecture decisions, dependency graph and alternatives.
- `contracts/03_SCRIPT_LANGUAGE_V0_3.md` — small authoring language, exact baseline semantics and evolution.
- `contracts/04_NARRATION_CLOCK_AND_EVENTS.md` — audio clock, segmentation, callbacks, visual triggers, concurrency and seeking.
- `contracts/05_GENERIC_CONTENT_SYSTEM.md` — default backgrounds, generic characters, expressions, sound/effects, art-direction and fallback policy.
- `contracts/06_VOICES_AND_PERFORMANCE.md` — voice packs, licensing, benchmark rubric and 4GB-class target.
- `contracts/07_PACKAGE_SECURITY_AND_COMPAT.md` — package structure, import safety, portable references and capability negotiation.
- `contracts/08_VALIDATION_AND_ROADMAP.md` — contract tests, architecture spikes and phase gates.
- `prototype/` and `examples/` — runnable **prototype compiler + fake playback reducer only** and representative generated canonical JSON.
- `prior_v0_2/` — previous documentation and logical asset catalog; not shipped binaries.

## Run proof-of-concept (Python 3.10+, stdlib)

```bash
python prototype/storytool.py examples/minimal.story.md --out /tmp/minimal.json
python prototype/storytool.py examples/plain_story.txt
python -m unittest discover -s tests -v
```

The prototype is deliberately restricted: it does **not** implement the advanced syntax, a full language grammar, real resource resolution, lifecycle, custom neural model loading, TTS, graphics, Android integration or security-safe archive unpacking. Compiler IDs are deterministic for unchanged documents, **not yet stable across arbitrary editing**. Its segmenter is a minimal heuristic and not a full Tagalog tokenizer. Prototype output is design evidence, **not** the frozen production IR schema.

## Status labels

- **ADOPT** — stable *planning principle*; implementation may still reveal details.
- **PROVISIONAL** — recommended implementation until benchmark/contract test.
- **OPEN** — decision blocked by evidence or unresolved design.
- **DEFER** — intentionally beyond first usable build, with a reserved interface.

## Research caveat

Public documentation was checked October 23, 2026. Library/API changes after that date and actual runtime behavior on a given phone may differ. Links in research notes point to original vendor/spec/model pages. No repository was modified and no Android hardware was accessed.
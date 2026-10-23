# CURRENT — VisualBook project checkpoint

**Date:** 2026-10-23 (Asia/Manila). **Stage:** Phase 1 documentation + reproducible proof-of-concept. **GitHub:** `despairarc000/visualbook`, `main`.

## Canonical product vision

Offline-first Android cinematic story player: load plain text or minimal `@n/@c1/@c2m/@c3f` scripted dialogue; unspecified characters/backgrounds/sounds/effects resolve to coherent generic defaults; advanced scripts specialize them. Narration's **audibly played** position, primarily utterance boundaries, is the trigger source of truth for visual state and subtitles. Viewer voice/appearance overrides remain separate from original story. No high-quality default Tagalog offline voice is yet demonstrated.

## Repository now contains

- Root `README.md`, `NAVIGATION.md`, `IDEA_PLAN.md`, `IMPLEMENTATION_PLAN.md`, root/scoped `AGENTS.md`.
- Markdown knowledge vault with requirements, modular design, research/open risks, decision register, evidence, phase gates, historical v0.2/v0.3 plans and this checkpoint.
- ICM deterministic task router (9 routes) with doctor; Graphify conceptual dependency graph (32 nodes, 40 edges) and Python AST source-level scanner.
- Preserved v0.3 compiler and fake playback Python experiments, 29 original tests, examples/compiled fixtures, 30-phrase Tagalog TTS evaluation corpus, package security notes and checksum manifest.
- Full 100KB original Tagalog anthology under `samples/` for long-story stress testing.
- GitHub Actions CI for links, Graphify, ICM, knowledge/fixture tests, archived Python tests and strict SHA-256 original-source audit.

## Latest verified completed CI before this checkpoint

[GitHub Actions run 39965364380](https://github.com/despairarc000/visualbook/actions/runs/39965364380) **SUCCESS** (2026-10-23):
- ICM: 9 routes, 0 problems
- Conceptual Graphify: 32 nodes, 40 edges, 0 problems
- AST source graph: 58 nodes, 104 edges, 0 parsing problems
- Live Markdown doctor: 28 files, 0 broken links
- Tests: 10 knowledge/fixture tests + 29 original prototype tests passed
- Original v0.3 source SHA-256: 7 byte-identical, 0 divergence

This checkpoint adds two more integrity tests (long sample file and speech corpus). Verify newest CI before reporting those two tests as passed.

## NOT implemented

No Android app/APK, no actual mobile visual renderer, no model-distribution rights confirmed, no installed high-quality Tagalog offline TTS, no actual shipped backgrounds, characters or effects, and no real-device timing/RAM benchmarks. Earlier corpus segmentation results were parser simulations only.

## Recommended next actions

1. Freeze canonical versioned production IR, source grammar and deterministic ID policy.
2. Prove live and prepared TTS segment continuity/quality; test licensing and offline Tagalog voice on Android.
3. Benchmark audio plus 2D rendering on 4GB-class Android; lock the backend only after evidence.
4. Produce an actual coherent first content batch (neutral, classroom, street, living room, two generic characters, captions, basic fade, a few explicit SFX).
5. Build vertical Android slice under implementation branch and add device tests with real trace data.

## Agent startup / handoff

Read `AGENTS.md`, `NAVIGATION.md`, `IDEA_PLAN.md`, `IMPLEMENTATION_PLAN.md`, route task through `knowledge-system/icm/icm.py`, inspect Graphify before modifying interfaces and validate with `python3 tools/doctor.py`. Always update evidence and checkpoint with commit/test results; distinguish historical evidence from implementation.

## New research branch: canonical IR v0.4 (2026-10-23)

Branch `research/canonical-ir-v04` adds a **PROVISIONAL** versioned IR bridge, JSON Schema, semantic validation, minimal script regression tests and explicit Android TTS experiment plan. This is a pure Python study. **Do not mark Phase 1 contract or Android audio gate complete merely because this branch exists.** After PR CI is green, record its exact run and outcome here.

Remaining issues: stable edit IDs, true source spans, real scene silent beats, Android audio callback testing and offline Filipino voice licensing.

## Android speech probe — build verified, device test open (2026-10-24)

- PR #1 (canonical IR 0.4 research and schema conformance) merged as `2629fe69a3600bb9eb221776c9315d140c795c48`; post-merge knowledge CI [39968958633](https://github.com/despairarc000/visualbook/actions/runs/39968958633) succeeded. This is still a draft IR, not production format.
- Research branch `research/android-tts-probe`, draft PR #2, contains minimal Java Android TTS probe; corrected setup to Android SDK action v4. Debug APK built and uploaded successfully by [run 40019414406](https://github.com/despairarc000/visualbook/actions/runs/40019414406).
- [Exact build evidence and errors](../../../research/experiments/android-audio/BUILD_EVIDENCE_2026-10-24.md) records commit, artifact, APK size and SHA-256.
- **Not yet proven:** APK installed on physical device, offline Tagalog voice quality, callback accuracy, Media3 playback, target 4GB-memory performance or default story visuals.
- Next action: run diagnostic APK in airplane mode on low-memory Android phone, collect diagnostics + subjective listening report, then choose real narration adapter. Do not mark Android feasibility gate complete from a green Gradle job.

## Device observation / freeze investigation (2026-10-24)

The project initiator tested **TTS Probe v0.1** on Samsung SM-A066B, Android 15. Logs show two complete 3-utterance live runs, two prepared clips played, and optional live range timing data; prep mode emits suspiciously permuted range fields. User reports UI unresponsiveness when tapping Stop then another mode. This is not yet a confirmed root-cause ANR.

[Detailed user-supplied evidence](../../../research/experiments/android-audio/DEVICE_TEST_SM-A066B_2026-10-24.md). Research branch `fix/tts-probe-switch-freeze` introduces a v0.2 background-thread switch/stop experiment and throttled log UI. **Device fix not yet verified;** build and rerun on phone required. Offline voice/quality and precise audible word timing remain open.

## Verified fix build and merged PR #3 (2026-10-24)

- [TTS Probe v0.2 build evidence](../../../research/experiments/android-audio/BUILD_EVIDENCE_V02_2026-10-24.md): debug APK built on GitHub and verified as valid APK/ZIP, SHA-256 `67be5455cae7ff83ac9ca69efedc5316b50005f3fcd4734938a155b21ae39569`.
- PR [#3](https://github.com/despairarc000/visualbook/pull/3) **merged** at `ac19c138f6f2e81d823cdad83dbccb3bdbf05cea`; build [40025942636](https://github.com/despairarc000/visualbook/actions/runs/40025942636) and knowledge [40025942618](https://github.com/despairarc000/visualbook/actions/runs/40025942618) both successful.
- Fix under test: serial HandlerThread TTS/MediaPlayer cleanup/commands, safe generation tokens, throttled log UI, heartbeat/operation timings, invalid range rejection. New application ID `com.visualbook.ttsprobe.v2` for side-by-side v0.1 comparison.
- **OPEN:** user has not run v0.2 on Samsung phone; freeze fix is a candidate, not a proven solution. No selected offline Tagalog voice or fully rendered VisualBook app yet.

## v0.2 real-device regression PASSED in reported scenario (2026-10-24)

- User tested v0.2 on Samsung SM-A066B (Android 15) under repeated LIVE/PREP/STOP switching; reported **"Smooth"**, with continued audio command completion and no new UI heartbeat-gap warnings during stress sequence.
- [Detailed verified-observation report](../../../research/experiments/android-audio/DEVICE_TEST_SM-A066B_V02_2026-10-24.md) captures timings, voice inventory and limits. This is device evidence, not a controlled multi-device/ANR root-cause study.
- Critical discovery: engine reported `fil-PH=1`, `tl-PH=-2` and 4+ advertised non-network Filipino voice variants, yet **active voice remained `en-US-language`**. Offline Tagalog speech quality was never tested by selecting local `fil-PH` voice.
- New research branch `research/tagalog-offline-voice-comparison` adds separate v0.3 Android APK experiment. It explicitly selects and confirms Filipino voices, cycles each through live or prepared audio and logs actual selected/observed IDs. On-device v0.3 tests OPEN.
- Next: real airplane-mode Tagalog pronunciation audition of local voices, then determine if system TTS suffices or external licensed offline neural models are needed. In parallel define initial generic character/background/prop/effect package contract.

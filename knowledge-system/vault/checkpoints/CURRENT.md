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

## v0.3 Filipino voice-selection research merged (2026-10-24)

- [Build evidence](../../../research/experiments/android-audio/BUILD_EVIDENCE_V03_2026-10-24.md). [PR #4](https://github.com/despairarc000/visualbook/pull/4) merged as `2fee9270d2de5000756b3a6a3734a122f8990959`. Both Android build and knowledge PR CI passed. v0.3 APK was extracted and its ZIP integrity inspected.
- Unique Android application ID `com.visualbook.ttsprobe.v3` for side-by-side installation. Adds explicit local Filipino voice selection by `tts.setVoice` and `getVoice` confirmation before live/prepared playback.
- **OPEN:** no v0.3 airplane-mode voice auditions on device yet. No model selected, voice quality unproven.
- Immediately after usable speech evidence: build minimal generic content kit (neutral/classroom backgrounds + two distinct characters + captions + simple fade) and the first Android compiled-script-to-renderer vertical slice. Avoid endless diagnostic app expansion.

## Filipino voice synthesis timeout on real SM-A066B (2026-10-24)

- v0.3 on Samsung TTS `com.samsung.SMT` confirmed `fil-PH` advertised local voices and successful `setVoice()`, **but Filipino live and prepared synthesis failed with `onError -7` (~2s network timeout each)**. Baseline active `en-US-language` live and prepared tests worked. [Exact observation and interpretation](../../../research/experiments/android-audio/DEVICE_TEST_SM-A066B_V03_TIMEOUT_2026-10-24.md).
- Official Android constant: `-7 ERROR_NETWORK_TIMEOUT`, **not** missing voice data (`-9 ERROR_NOT_INSTALLED_YET`). Missing/undownloaded Filipino data remains a hypothesis. Real airplane-mode setting not stated.
- Next gate: check Samsung **Install voice data → Filipino** if available, compare all local voices and try Google TTS engine/voice, then verify offline actual speech. Keep voice provider and asset plans independent.
- Architectural correction: voice inventory + `setVoice SUCCESS` **cannot qualify a voice as ready**. Require synthesis/playback capability probes, explicit failure states and safe text-only fallback without silently using English.

## Samsung voice settings screenshots (2026-10-24)

The user supplied Samsung TTS settings screenshots: v`3.5.02.16` marked latest, visible voices **English (India), English (US; selected), Hindi (India)**. No Filipino and no Install voice data control visible on that page. Separate Settings search routes Text-to-speech through TalkBack settings. [Documented observations and next safe path](../../../research/experiments/android-audio/SAMSUNG_TTS_SETTINGS_SM-A066B_2026-10-24.md). These visuals strengthen the missing-ready-data hypothesis but **do not** establish the cause of Android `-7 ERROR_NETWORK_TIMEOUT`. Next compare Google Speech Recognition & Synthesis via system-wide Preferred engine selection and validate actual `fil-PH` offline playback. Do not reset app data or enable TalkBack unnecessarily.

## Selected Filipino voices now synthesize successfully on SM-A066B (2026-10-24)

[Fresh five-voice device report](../../../research/experiments/android-audio/SM-A066B_FIVE_FILIPINO_VOICES_2026-10-24.md) supersedes the earlier impression that Filipino TTS never works: 3 named local `fil-PH` variants completed full 3-sentence LIVE runs, a 4th generated/played a complete prepared clip, and generic `fil-PH-language` produced audible-start/range callbacks before user cancellation. All 5 `setVoice` results match `getVoice`. Prior `-7` failure remains historical true evidence; *cause of improvement unknown*.

**Important:** user says they used Google's offline settings, but probe still reports `default_engine=com.samsung.SMT`; `getDefaultEngine()` is a default-preference report and not a direct bound-engine attestation. Do not claim the 5 variants belong to Google. Next: explicit-provider A/B audition, offline airplane-mode proof, user ratings of voice timbre/prosody, then cast voice profiles. Generic visual asset kit can proceed independently.

## Voice variety — first perceptual report (2026-10-24)

User reports the Filipino voices sound **somewhat different rather than identical**. Logged in [voice evidence](../../../research/experiments/android-audio/SM-A066B_FIVE_FILIPINO_VOICES_2026-10-24.md). This supports pursuing selectable voice profiles for separate characters, but timbre/gender, naturalness, engine identity and airplane-mode feasibility remain unverified. Resume next with **simple narrator + two actor voice picker**, initial generic asset layout and player vertical slice, not more speculative voice-engine development.

## First runnable story player implementation branch (2026-10-24)

Branch `implementation/first-animated-story` introduces a **narrow Android vertical slice**: Java pure `StoryCore` parser + generation-safe `PlaybackState`, procedural responsive classroom + two actor silhouettes (guest fallback), TXT/`.story.md` Android document import, editable built-in Juan/Pedro story, voice selectors for narrator/c1/c2, subtitle UI, and live TTS audible-boundary animation. Explicitly no script execution, asset downloads, model bundling or external network permission.

[Source & scope](../../../implementation/visualbook-player/README.md) · [CI APK workflow](../../../.github/workflows/visualbook-player-build.yml).

**Do not mistake it for the finalized canonical IR v0.4 or a production player.** Initial prototype only supports `@n`, `@cNm`, `@cNf`, `@bg classroom/neutral`; no scene packages, sound packs, true pause-at-position, multi-scene transitions, saved progress or real device validation. It retains sex hints without making gendered voice assumptions. No real Android installation test has been performed on this new player yet. Next: confirm CI and download APK, test TTS boundaries and visual legibility on SM-A066B, then iterate on actual issues.

## FIRST ANIMATED STORY APK BUILT AND MERGED (2026-10-24)

The project now contains the **first actual Android VisualBook story player** under [implementation/visualbook-player](../../../implementation/visualbook-player/README.md). [PR #9](https://github.com/despairarc000/visualbook/pull/9) merged into `main` as `e671a0979f20da0fe342e968c93c883dff838667`. Both [Android build](https://github.com/despairarc000/visualbook/actions/runs/40032106784) and [knowledge checks](https://github.com/despairarc000/visualbook/actions/runs/40032106812) passed. [Verifiable build details and APK SHA-256](../../../implementation/visualbook-player/BUILD_EVIDENCE_V01_2026-10-24.md). Core Java tests: **26 passed**. APK output `com.visualbook.player.slice`, SHA-256 `a6ed36116372738946ca2138e4ee362081924d8bcb1440f61c1d09d5aaa0f7e6`.

This is a **build-verified prototype**, not a real-device verified multimedia app. At this point no Samsung SM-A066B test of first app, measured visual/audio sync or finalized character assets/production player. The old generic project statement "no APK exists" above is historical and superseded for **this experimental player only**. Actual user test and quality gates remain OPEN. Next: install this APK, try demo/import/voice selectors, capture screenshot and failures; implement improvements grounded in observed device behavior, then progress to full IR/asset pack.

## First Android player device success — implicit @c3 guest (2026-10-24)

The user installed/tried the first VisualBook story player and reported **"Nice, it works"**. They created an `@c3` speaking character; the stage generated a visible guest only during that turn, removing it afterward. This is concrete **real-device visual behavior**, beyond CI compilation, with limited coverage. [Device acceptance record](../../../implementation/visualbook-player/DEVICE_TEST_V01_FIRST_SUCCESS_2026-10-24.md).

**Known scoped rendering behavior:** `c1/c2` are permanently staged; `c3+` only render as active guest, not persistent scene participants. This is a valid provisional MVP; follow-up architecture must separate character identity, stage presence and active speaker before adding entrance/exit and arbitrary cast staging. Do not claim full voice/audio/video quality or all controls validated by this report. Next validation: screenshot + explicit TTS interaction, then improve default character presence and scalable layouts.

## Temporary mid-story art style scopes — v0.2 implementation (2026-10-24)

User requested simple PHP-like start/end markers to switch styles mid-story, especially an anime-style normal→chibi→normal gag. Experimental Android branch `feature/scoped-art-styles-v02` introduces `@chibi` / `@end` (and `@style chibi|normal|cartoon` / `@end`), nested restore stack with validation, immutable per-speech-segment `artStyle`, and a **genuinely different procedural chibi character renderer**. Audio cursor still advances only on valid TTS audible completion; style is applied at audible start. Dedicated v0.2 package `com.visualbook.player.styles.v02` preserves user's v0.1 app.

[Full scoped-style contract](../design/SCOPED_ART_STYLES_V02.md) · [prototype README](../../../implementation/visualbook-player/README.md).

**Evidence gate:** source implemented, pure Java parser tests augmented; Android compile and user phone playback must be confirmed separately. No claim of multiple custom asset pack systems; chibi and normal are the only implemented visual modes. Next user test should include style nested with `@c3`.

## VisualBook Styles v0.2 APK build VERIFIED and merged (2026-10-24)

[Temporary art-style scoping](../design/SCOPED_ART_STYLES_V02.md) is now implemented as actual Android **normal/chibi visual rendering**, not only a plan. [PR #11](https://github.com/despairarc000/visualbook/pull/11) merged into `main` at `a3845299a6cb07c9291a3101fbfd80d65d70b7c8`. [CI run 40037026012](https://github.com/despairarc000/visualbook/actions/runs/40037026012) verified Android `assembleDebug` plus **42 passing pure Java assertions**; [knowledge CI 40037026020](https://github.com/despairarc000/visualbook/actions/runs/40037026020) passed. Extracted debug APK SHA-256: `cb1d5408afd8ea1ac7e04b2afb8f639537d2b333d3bead969d8cceff4996efbe`, app ID `com.visualbook.player.styles.v02` (side-by-side v0.1). [Build evidence](../../../implementation/visualbook-player/BUILD_EVIDENCE_STYLES_V02_2026-10-24.md).

**OPEN:** no v0.2 smartphone art-style playback inspection yet; physical device result for the v0.1 player still stands independently. Next: verify chibi entry/exit during audible narration, `@c3` guest during chibi, and pause/resume, then design per-actor style targeting and compatible asset packs without coupling story semantics to artwork.

## Virtual canvas geometry implementation (2026-10-24)

User requested exact geometric composition ("own point (0,0), 100×100, circle at 77,56 and triangle…") so AI can create highly unusual shapes without a predefined creature generator or imported artwork. New Android prototype branch `feature/virtual-canvas-geometry-v03` adds typed data-only `VectorFigure` definitions, basic geometric paths, validated `@figure`/`@endfigure`, `@show`/`@hide` and per-segment persistent shape-visibility state. **Two coordinate frames:** figure-local artboard and normalized 100×100 story stage; precise placement `@show spirit at:77,56 width:27` is not device pixels. A debug stage coordinate grid helps test. [Design, syntax and limitations](../design/VIRTUAL_CANVAS_GEOMETRY_V03.md) · [Android player source](../../../implementation/visualbook-player/README.md).

**Scope is provisional**: only bounded 2D vector drawing from explicit coordinates; no natural-language-to-image model, arbitrary imported SVG, 3D, rig animation or all-stage layout solver. Geometry is tied to existing audible utterance timing. Do not report device validation until tested on SM-A066B. Next decision after CI/phone proof: reusable part-local coordinate transforms and parent-child pivots.

## Virtual Canvas Geometry v0.3 APK built and merged (2026-10-24)

[PR #12](https://github.com/despairarc000/visualbook/pull/12) merged into `main` (`94fab04868e84d4caf01dae7379dcb2112ed71b6`). Final [Android build 40039492709](https://github.com/despairarc000/visualbook/actions/runs/40039492709) and [knowledge doctor 40039492697](https://github.com/despairarc000/visualbook/actions/runs/40039492697) **passed**. Pure JVM tests: StoryCore 61 assertions, vector geometry 43 assertions. Downloaded debug APK from same code (artifact 12538228595), SHA-256 `04a71c789e790801efc926ba19f15b9094c4a359aee328509b479221ad36c757`; package `com.visualbook.player.canvas.v03`. [Full artifact evidence and phone test instructions](../../../implementation/visualbook-player/BUILD_EVIDENCE_VIRTUAL_CANVAS_V03_2026-10-24.md).

**Still OPEN:** user acceptance on SM-A066B for authored figure shapes, normalized placement grid, rendering performance and shape aesthetics. Next: test and iterate; design reusable nested part transforms and pivots before claiming generalized morphable creatures.

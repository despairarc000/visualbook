
# Validation Gates / Exit Criteria

**Status:** PROVISIONAL planning checklist; checked only when supported by verifiable evidence.

## Knowledge setup gate

- [x] Original user vision and progressive authoring documented in [idea plan](../../../IDEA_PLAN.md).
- [x] Original v0.2 and v0.3 Markdown research and 101 logical planned assets preserved in history.
- [x] Archived v0.3 Python parser/reducer sources, tests, 30-line Tagalog TTS corpus and compiled sample fixtures in GitHub.
- [x] Source SHA-256 fidelity checked for 7 tracked v0.3 files (strict CI success 2026-10-23).
- [x] ICM routes, Graphify concept graph and navigation links validated in GitHub CI for previous stable commit.
- [ ] Full bytewise checksums for every historical document and v0.2 asset entry reconciled.
- [x] Validate latest sample/route/graph updates and AST code scanner via [green GitHub Actions run](https://github.com/despairarc000/visualbook/actions/runs/39965364380).
- [ ] Verify archive byte integrity beyond the seven tracked v0.3 originals.

## Phase 1 spec gate

- [ ] Typed canonical production IR definitions and schema.
- [ ] Minimal authoring grammar plus Unicode offsets/source mapping fully specified.
- [ ] Audio clock reducer including seeking, pause/resume, silent beat and boundary ordering frozen.
- [ ] Generic asset types/metadata/responsive layout and fallbacks frozen.
- [ ] Package import, model limits, trust policies and version capability contracts frozen.
- [ ] Golden conformance cases and diagnostics with stable code IDs for production.

## Phase 2 Android feasibility gate

- [ ] Actually compatible/legal offline Tagalog voice and audio benchmark.
- [ ] Tagalog sentence segmentation prosody and inter-chunk gap measurement.
- [ ] Media3 background playback, focus, process recovery.
- [ ] 2D backend portrait/landscape/device memory baseline.
- [ ] Concurrent speech+visual frame/memory/thermal measurements.

## Phase 3 functioning app gate

- [ ] Real installable Android APK imports TXT/simple script/storypack.
- [ ] Narration/caption synchronization and two distinct generic actors.
- [ ] Safe fallbacks, pause/seek/rotation and true audio event handling.
- [ ] Real device tests with logs and exact commit/version.

Never mark an Android feature complete from a Python-only simulation or documentation.

## v0.4 prototype research branch (not Phase 1 completion)

- [x] v0.4 canonical IR bridge tests pass in PR/main CI; draft schema still provisional.
- [x] Independently validate v0.4 JSON Schema against generated IR (Python jsonschema in CI).
- [ ] Define editing-stable IDs and real source-span behavior.
- [ ] Implement and test real Android audible progress; current event plan is simulated.

## Android TTS probe subgate (2026-10-24)

- [x] Research Android Java TTS diagnostic compiles; GitHub Action run [40019414406](https://github.com/despairarc000/visualbook/actions/runs/40019414406).
- [x] A debug APK was published as a temporary CI artifact; [build evidence](../../../research/experiments/android-audio/BUILD_EVIDENCE_2026-10-24.md).
- [ ] Install on actual low-memory device and obtain offline voice inventory, logs and human listenability results.
- [ ] Compare live and prepared audio segment gaps with reproducible recordings/timing, not only callback printouts.
- [ ] Validate Media3 service/audio focus and concurrent visual renderer memory budget.

**Important:** this build does not satisfy the Phase 2 audio feasibility gate.

## Samsung v0.2 UI freeze experiment

- [x] Migrate TTS/service calls from UI thread to serial background command handler and throttle diagnostic TextView.
- [x] Build v0.2 independent Android APK + record exact binary checksum in [evidence](../../../research/experiments/android-audio/BUILD_EVIDENCE_V02_2026-10-24.md).
- [ ] Re-test rapid Stop → Live / Stop → Prep switching on SM-A066B Android 15.
- [ ] If UI freeze persists, capture a main-thread/IPC stack or Perfetto trace to isolate cause.

## Latest v0.2 device evidence and next voice gate

- [x] Samsung SM-A066B (Android 15) rapid live/prepared/stop switching retested with v0.2; user reported "Smooth" and operation log showed no new stress-interval UI stalls. [Evidence](../../../research/experiments/android-audio/DEVICE_TEST_SM-A066B_V02_2026-10-24.md).
- [ ] Root cause of v0.1 freeze isolated via ANR/Perfetto or controlled A/B tests; successful v0.2 mitigation alone cannot prove individual culprit.
- [ ] Explicitly select each compatible non-network `fil-PH` voice and test in airplane mode; record best and failures.
- [ ] Audition segmentation prosody and measure actual gaps with synthesized audio/recordings, not callback differences alone.

## Filipino voice comparison (v0.3)

- [x] Build instrumented Filipino voice comparison app with explicit local candidate selection and source verification; [CI evidence](../../../research/experiments/android-audio/BUILD_EVIDENCE_V03_2026-10-24.md).
- [ ] Test actual selected offline `fil-PH` voice on Samsung phone with airplane mode enabled, listen and rank.
- [ ] Decide whether native local TTS meets first-release minimum, or a separately licensed packaged offline neural voice is required.
- [ ] Freeze first generic asset-kit contract and resume implementation vertical slice after testing audio on the intended device.

## Samsung Filipino voice selection outcome (v0.3)

- [x] Actual `fil-PH` voice selected (two candidate names) and effective voice verified on SM-A066B.
- [x] Observe error codes from live and file synthesis with a selected Filipino voice: repeated `ERROR_NETWORK_TIMEOUT=-7`, even for voices advertising `requiresNetwork=false`. [Evidence](../../../research/experiments/android-audio/DEVICE_TEST_SM-A066B_V03_TIMEOUT_2026-10-24.md).
- [ ] Determine whether missing/unfinished local voice-pack download is involved; record actual installation state.
- [ ] Reproduce/resolve Filipino synthesis with Samsung voice data installed, or compare Google TTS engine using same corpus.
- [ ] Prove usable speech **in airplane mode** and rank Tagalog prosody before selecting a production default.

## Samsung voice settings inspection (2026-10-24)

- [x] Inspect user-supplied local Samsung TTS settings screenshot: English India/US and Hindi only, latest version v3.5.02.16; Filipino not visible. [Evidence](../../../research/experiments/android-audio/SAMSUNG_TTS_SETTINGS_SM-A066B_2026-10-24.md).
- [ ] Locate system-wide Preferred engine selector on actual SM-A066B One UI 7; check available Google provider.
- [ ] If compatible, install/configure Google Speech Recognition & Synthesis, download Filipino voice pack if offered and verify airplane-mode synthesis.
- [ ] Decide production voice availability through **actual audible successful local synthesis**, not locale enumerations or `setVoice=0`.

## Filipino voice partial-feasibility success (2026-10-24)

- [x] Real SM-A066B: at least 3 Filipino named local TTS profiles complete full three-utterance live speech; one fourth completes prepared WAV playback. [Evidence](../../../research/experiments/android-audio/SM-A066B_FIVE_FILIPINO_VOICES_2026-10-24.md).
- [ ] Identify actual bound TTS engine; current log only reports `getDefaultEngine=com.samsung.SMT`, despite user's statement about Google offline TTS.
- [ ] Complete offline airplane-mode playback (document connectivity) and human audition for each voice candidate.
- [ ] Decide voice defaults and stable cast `characterId→voiceProfile` contract from acoustic evidence, not just voice IDs.

## First subjective voice diversity signal

- [x] Listener reports voices are **somewhat different** rather than identical on SM-A066B in current probe.
- [ ] Score equal-text samples for actual voice distinction, Filipino pronunciation and character suitability; no automatic male/female assignment.
- [ ] Verify stable selected provider and true airplane-mode playback before marking production voice gate completed.

## First animated story vertical slice v0.1 (prototype, 2026-10-24)

- [x] Code portable script grammar, sentence segmentation and generation-safe cursor with main-method test harness.
- [x] Code procedural classroom/neutral stage, narrator/character active visuals, mouth animation with subordinate frame clock, and native caption display.
- [x] Code three selectable voice profiles without inferring from `m/f`, story edit and text-only import via Android SAF.
- [ ] Confirm latest GitHub Actions pure-core tests and `assembleDebug` both pass on final branch commit; record APK hash.
- [ ] Install APK on Samsung SM-A066B, check default Juan/Pedro scene, screen dimensions and state transitions.
- [ ] Confirm TTS onStart/onDone updates visuals only when corresponding spoken utterance is audible, not on pre-generation.
- [ ] Test pause/resume sentence restart, Stop, manual Skip, voice errors, malformed scripts, orientation/process recreation and memory.
- [ ] Upgrade to shared canonical IR, multi-scene support, prepared-audio clock, real character rig/asset pack and Media3 background session in later phases.

**Note:** Android debug build is not equivalent to a passed on-device playback test.

## VisualBook v0.1 build verification update

- [x] Final v0.1 Android app and 26 pure Java tests compiled successfully in [Actions 40032106784](https://github.com/despairarc000/visualbook/actions/runs/40032106784); see [APK evidence](../../../implementation/visualbook-player/BUILD_EVIDENCE_V01_2026-10-24.md).
- [x] [Knowledge CI](https://github.com/despairarc000/visualbook/actions/runs/40032106812) passed; PR #9 merged.
- [ ] Physical device v0.1 gameplay/audio/render/rotation/voice acceptance. A verified **build** is not verified story narration.

## First device player feedback — milestone (2026-10-24)

- [x] First Android story player executed on target user's device, user reports functioning.
- [x] Implicit extra speaker `@c3` produces a visible generic guest without needing declared assets; user verified turn-limited appearance.
- [ ] Record screenshot and test exact audible speech/animation sync per actor, pause/stop/skip, imported plain TXT and complex scripts.
- [ ] Decide persistent scene-presence defaults for `c3+` (enter on first turn vs speaking-only) and test multiple actors in responsive layouts.
- [ ] Continue voice-provider/offline portability and memory tests independently of successful rendering.

## Mid-story scoped visual style — v0.2 prototype (2026-10-24)

- [x] Code author-friendly `@chibi` and `@end`, generalized `@style` override with stack restoration.
- [x] Code actual different procedural chibi renderer, preserving background, implicit speakers and TTS audible clock.
- [x] Add parser regression assertions: nested scopes, speaker switch within style, plain TXT literal tags, malformed/unbalanced styles.
- [ ] Record passing final CI pure Java and Android APK build, exact SHA-256 and app ID.
- [ ] Verify on device that chibi really looks different, is readable, and reverts at the right narration boundary.
- [ ] Design style-family asset compatibility contract and rig state transfer for future anime/comic/storybook style packs.

## v0.2 scoped art-style verified build

- [x] GitHub Actions 40037026012 passed 42 portable parser/playback assertions, Gradle Android compile, APK artifact upload.
- [x] Knowledge CI 40037026020 passed and [PR #11](https://github.com/despairarc000/visualbook/pull/11) merged.
- [x] [APK SHA-256 and side-by-side app ID](../../../implementation/visualbook-player/BUILD_EVIDENCE_STYLES_V02_2026-10-24.md) recorded.
- [ ] Real SM-A066B confirms chibi style visibly starts/ends where intended.
- [ ] Scale up style registry for per-character changes and consistent facing/pose/rig support.

## Virtual coordinate render control — v0.3 (2026-10-24)

- [x] Define figure-local `100x100` virtual artboard and normalized 100×100 stage placement, independent of real screen pixels. [Design contract](../design/VIRTUAL_CANVAS_GEOMETRY_V03.md).
- [x] Implement bounded circle, triangle, polygon, M/L/Q/C/Z vector primitives and deterministic fill/stroke geometry.
- [x] Implement `@figure`/`@endfigure`, `@show`/`@hide` as silent instructions; snapshot visibility per upcoming spoken utterance; stage toggle grid for visual coordinate inspection.
- [ ] Confirm pure Java regression tests, Android Gradle build, knowledge doctor and APK hash on latest branch.
- [ ] User verifies precise at:77,56 placement on SM-A066B, style coexistence and valid error reporting.
- [ ] Add transform hierarchy and reusable shape part pivots for detailed AI-authored creatures.
- [ ] Generalize automatic staging, imported image fallback and non-audible cue timeline in later architecture.

## v0.3 virtual stage build-verified result

- [x] Latest pure JVM geometry + story-core tests **104 assertions total** passed; [CI details](../../../implementation/visualbook-player/BUILD_EVIDENCE_VIRTUAL_CANVAS_V03_2026-10-24.md).
- [x] Android app build and final knowledge doctor succeeded; PR #12 merged; APK CRC/SHA-256 recorded.
- [ ] Verify runtime rendering, figure construction controls and stage-coordinate grid on actual Samsung SM-A066B.
- [ ] Design reusable nested geometry groups and transforms for creature body-part rigging and controlled direction changes.

## Shape Engine v0.4 — reusable parts and parent-local pivots (2026-10-24)

- [x] Implement `@part` / `@endpart` reusable groups with independent 16..1024 local canvas.
- [x] Implement `@use` with parent anchor, signed rotation, positive scale and horizontal mirror about pivot.
- [x] Implement nested DAGs, expansion/depth safety budgets, deterministic draw order and Canvas transform rendering.
- [x] Add tests for exact pivot mapping, mirrored coordinates, nested part composition, parser failures and budget rejection.
- [ ] Obtain passing latest Android/JVM CI and record build artifact hash.
- [ ] Real phone test: compare wing/eye/horn placement, multi-instance rendering, chibi coexistence and FPS.
- [ ] Define future authoring shorthand, asset-pack handles, transform animation, orientation rig support and corrective preview workflow.

## Shape Engine v0.4 build verification

- [x] PR #13 merged, [Android build 40044410923](https://github.com/despairarc000/visualbook/actions/runs/40044410923) and knowledge validation passed.
- [x] [142 Java assertions, APK CRC and hash](../../../implementation/visualbook-player/BUILD_EVIDENCE_SHAPE_RIG_V04_2026-10-24.md) recorded.
- [ ] User confirms wing/head/eye rig visibility and transform controls on Samsung Galaxy A06 5G.
- [ ] Measure/inspect actual stage frame quality on low-memory phone before expanding animated multi-part rigs.

## Speech-driven procedural part motion v0.5

- [x] Add optional named root instance handles to reusable shape parts.
- [x] Compile bounded @animate/@still instructions into immutable per-utterance motion state.
- [x] Animate Canvas rotation around declared part pivots while TTS onStart→onDone marks audible speech; preserve older code and app IDs.
- [x] Add regression checks for waveform-free sine envelope, bad target/period/amplitude, and plain TXT.
- [ ] Verify final Android CI, Java tests, knowledge doctor and artifact hash.
- [ ] User tests actual wing flapping and Pause/Resume/Stop responsiveness on Samsung A06 5G.
- [ ] Implement AI-friendly preview/feedback and more nuanced animation after evidence.

## v0.5 animated shape rig build result

- [x] PR #14 merged and Android Actions build 40047828597 passed.
- [x] 79 story-core and 85 vector assertions passed, 52,891-byte APK ZIP CRC and SHA-256 recorded in [build evidence](../../../implementation/visualbook-player/BUILD_EVIDENCE_MOTION_V05_2026-10-24.md).
- [x] Knowledge doctor checks passed.
- [ ] User validates audible wing movement, pause/resume and stop behavior on Samsung A06 5G.
- [ ] Build a real render-preview/correction loop before marketing arbitrary shapes as high-quality automatically generated art.

## Render-preview inspection and correction v0.6

- [x] Implement silent segment scene preview, independent of real audible TTS progress/cursor.
- [x] Inspector uses exact virtual stage and figure coordinate mapping with safe aspect-ratio-preserving fitting.
- [x] Copy scene diagnostics for AI revision and jump to selected @figure source.
- [x] Add portable Java verification for hit/no-hit, hidden figures, report state and invalid view inputs.
- [ ] Android CI, Knowledge Doctor and final artifact hash validated.
- [ ] Phone acceptance: preview controls/copy/jump, no phantom audio effects, realistic coordinate results and usable screen layout.
- [ ] Improve visual-loop autonomy with user-approved screenshots and bounded shape selection/correction.

## v0.6 render preview Android verified build

- [x] PR #15 merged; [Android CI](https://github.com/despairarc000/visualbook/actions/runs/40049542638) and knowledge doctor success; **186 pure Java assertions**.
- [x] APK SHA-256 and artifact CRC checks [recorded](../../../implementation/visualbook-player/BUILD_EVIDENCE_PREVIEW_V06_2026-10-24.md).
- [ ] Samsung A06 5G acceptance: tap coordinates, copied report, Edit shape jump, silent preview, and playback independence.
- [ ] Real image preview/export, path-level hit-tests, undo, visual diff, and automated geometry correction still future improvements.

## v0.7 complete-episode series milestone

- [x] Bundle Episode 1 as an Android asset; separate package from v0.6; list E2–E8 as Coming Soon.
- [x] Add five playable chapter entry points and deterministic `@scene` and `@name` metadata.
- [x] Make long-form story narration resume across app relaunch with hash-validated cursor and stale TTS invalidation.
- [x] Keep 430 spoken utterances and existing cast IDs unchanged from original E01.
- [x] Add distinct lightweight procedural scenes for guava/plaza (bell missing or restored)/library/bridge/river/warehouse/forest/Balete.
- [ ] Confirm final Android Gradle and knowledge doctor checks; physical SM-A066B test remains separate.
- [ ] Confirm actual 24-minute TTS session, resumed position and all five chapters visually/audibly on phone.
- [ ] Next: persistent actor roster, emotion/pose controls, per-speaker voice matching, image assets and visual quality refinement.

## v0.7 accepted build artifact
- [x] [PR #17](https://github.com/despairarc000/visualbook/pull/17) merged; Android and Knowledge Doctor passed final SHA.
- [x] 1,516 total Java assertions; 79,275-byte v0.7 debug APK CRC/SHA-256 verified and [recorded](../../../implementation/visualbook-player/BUILD_EVIDENCE_SERIES_V07_2026-10-25.md).
- [ ] User phone acceptance: actual 24-minute uninterrupted TTS, persistence and chapter/navigation touch behavior, transitions between scene presets.
- [ ] Future production: imported image assets, complete persistent cast staging, each-character voice selection, cinematic direction, safe offline TTS guarantees.

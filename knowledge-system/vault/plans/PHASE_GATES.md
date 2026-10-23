
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


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

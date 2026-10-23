# Validation Gates / Exit Criteria

**Status:** PROVISIONAL planning checklist; checked means supported by verifiable evidence.

## Knowledge setup gate

- [x] Original user vision documented in current [idea plan](../../../IDEA_PLAN.md).
- [x] Preserved source documents for historical v0.2 and most core v0.3 planning.
- [ ] Original ZIP's prototype Python sources/tests embedded or otherwise fully restored and rerun in repository.
- [ ] ICM routes and Graphify validated by repository CI.
- [ ] Active navigation links verified in CI.

## Phase 1 spec gate

- [ ] Canonical IR typed definitions and JSON schema.
- [ ] Minimal authoring grammar and Unicode source mapping frozen.
- [ ] Audio clock event sequence, seek/pause/restart and silence handling frozen.
- [ ] Generic asset types, metadata, fallback and responsive-layout contract frozen.
- [ ] Package import/voice security quotas and version capabilities frozen.
- [ ] Conformance cases with expected state output and error codes.

## Phase 2 Android feasibility gate

- [ ] Actual offline Tagalog voice on supported device, rights and model inventory.
- [ ] Narration gap and quality test with sentence segmentation.
- [ ] Media3 + background audio tests, focus and process recovery.
- [ ] Portrait/landscape renderer and memory baseline.
- [ ] Combined concurrent speech + visuals thermal/RAM/frame tests.

## Phase 3 player gate

- [ ] Imports TXT, minimal annotated script, compiled package.
- [ ] Generic characters and scenery with available fallbacks.
- [ ] Accurate visual start/end triggers and captions.
- [ ] Pause/seek/rotation and voice/asset failure.
- [ ] Exported and installable Android APK with actual device smoke test.

Never mark phases as complete solely because a document or mock-up exists.

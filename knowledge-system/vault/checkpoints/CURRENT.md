# CURRENT — VisualBook project checkpoint

**Timestamp:** 2026-10-23 (Asia/Manila). **Stage:** Phase 1 architecture consolidation / knowledge-system setup.

## Confirmed product vision

Narration/audio cursor is source of truth. Sentence-based start/end cues enable robust defaults if word timings unavailable. `@n/@c1/@c2m/@c3f` identity markers are minimum for multispeaker stories; character sex hint optional; all other creative details have neutral generic fallbacks. Advanced creators and AI add script/scene/voice/asset directions progressively. Characters and viewer overrides are independent of authored story.

## Verified in historical local research (not current GitHub CI)

Previous v0.3 Python proof reported 29/29 unit tests for parser/fake playback reducer, and an earlier 101 logical-asset draft was audited. In the current setup repo, archived docs are source snapshots, **not** evidence of Android audio or released media. Do not claim reproduced tests until code/tests are committed and run again.

## GitHub state after setup

- Repo: `despairarc000/visualbook`; initial empty `main` initialized 2026-10-23.
- Canonical idea and implementation plan, navigation and root/scoped AGENTS prepared.
- Historical v0.2/v0.3 source documents preserved under `knowledge-system/vault/history`; missing originals tracked by evidence register.
- ICM/Graphify/doctor are lightweight GitHub-native helpers: verify actual runnable and CI outcomes separately.

## No implementation yet

No Android APK, no deployed app, no selected/high-quality redistributable offline Tagalog voice, no packaged animations or finished background/character images, no real-device performance or timing benchmarks.

## Top priorities

1. Complete archive restoration of prototype/tests and machine-readable speech corpus; verify hashes.
2. Validate ICM, Graphify, navigation and link integrity in CI and local runner.
3. Freeze minimal script grammar and canonical IR; produce compatibility and failure fixtures.
4. Android audio/TTS experiment, voice/license check and 4GB device profiling.
5. One complete minimal scene with actual generic assets + generated audio before enlarging library.

## Handoff

Follow `AGENTS.md` and `knowledge-system/vault/templates/HANDOFF.md`. Update this checkpoint only after verified work. Use branch/tag/commit SHA in future handoffs; current commit depends on end-of-setup validation.

# CURRENT — VisualBook project checkpoint

**Date:** 2026-10-23 (Asia/Manila). **Stage:** Phase 1 specification, GitHub-native knowledge setup and reproducible research. **Repository:** `despairarc000/visualbook`, `main`.

## Stable vision

An offline-first Android illustrated story player with progressive authoring. Plain TXT is narrator content; `@n`, `@c1`, `@c2m`, `@c3f` markers are enough to create implicit speaker identities with optional sex hint, defaults for other details. The **audibly played** narration position drives segment-boundary visual triggers, subtitles and story progress. Advanced script and attached media can specialize characters, cameras, sounds, art and voice packs without replacing the engine.

## GitHub setup — verified

- Empty repo initialized; root [idea plan](../../../IDEA_PLAN.md), [implementation plan](../../../IMPLEMENTATION_PLAN.md), [navigation](../../../NAVIGATION.md) and [AGENTS](../../../AGENTS.md) committed.
- Current Markdown vault with status-coded requirements/design/research/decision register/phase gates/evidence and this checkpoint.
- Original v0.2 and v0.3 research documents archived; original v0.3 storytool and fake playback Python source, tests, corpus and two golden script/IR examples committed.
- ICM deterministic route CLI and Graphify conceptual dependency CLI committed, with agent-scoped workflow and GitHub Actions validator.
- Tagalog joke/story anthology preserved under `samples/` as a long plain-text stress input.

## Evidence gates (last checked CI before this checkpoint)

- GitHub Actions knowledge workflow on earlier commit `bb13d7aa047fac891b91998012175cbd547422f7` **success**: ICM 9 routes, Graphify 31 nodes / 39 relationships, 27 active Markdown docs with 0 broken links; nine additional knowledge/fixture tests and 29 original parser/fake-player tests passed.
- SHA-256 integrity audit: **7 of 7** specifically mapped original v0.3 source files byte-identical. Other archived text documents are preserved but not yet all hash-compared.
- This checkpoint adds an anthology route/graph node and will have a fresh CI validation; use the latest green run before marking those updated counts VERIFIED.
- Python experiments are not Android device tests; sample clips, voices, renderers and assets have not been measured.

## What has NOT been built

No APK or live mobile scene renderer, no finished bundled illustrations or sound files, no high-quality redistributable default offline Tagalog TTS selected, no real-device audio gap or frame/RAM testing, no final canonical IR or `.storypack` implementation.

## Highest-priority next steps

1. Freeze the minimal parser and canonical IR contracts plus compatibility fixtures.
2. Validate real Android TTS and offline Tagalog voice candidates for latency, gaps, license and prosody with the saved corpus.
3. Benchmark 2D rendering and audio concurrently on a 4GB-class phone.
4. Produce the first neutral/classroom/street/living room asset family with two coherent generic characters and subtitles.
5. Integrate one end-to-end slice only after clocks, fallbacks and device budgets are proved.

## Agent handoff

Read root `AGENTS.md`, `NAVIGATION.md`, route task via ICM, inspect relevant Graphify neighbors, update the decision register and checkpoint with evidence. Keep implementation-specific work in separate branches and clarify simulated vs real-device verification.

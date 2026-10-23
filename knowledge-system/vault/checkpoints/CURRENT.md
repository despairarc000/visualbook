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

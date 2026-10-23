# VisualBook — Implementation Plan

**Status:** PROVISIONAL phase sequencing; no production Android build as of 2026-10-23. Work by gates, not arbitrary dates. Preserve planning on `main`, and create a separate implementation branch for substantial code when Phase 1 gates are satisfied.

## Architecture and dependencies

```text
TXT / story.md / storypack
  -> Import Security / Parser -> Semantic Compiler / Defaults -> Canonical IR
  -> Playback Coordinator -> Narration Audio Clock -> Event Reducer -> Scene State
                                       |                    |
                Voice Resolver / TTS  |                    +-> 2D Renderer / Layout
                Audio Player (Media3) +---------------------> Captions + Audio/SFX
  Shared typed Asset Registry / Cache / Provenance -> all resolvers
  Android UI (Compose) -> controls + library + customization
```

Core model, compiler and deterministic reducer must have **no** dependency on Android view, libGDX, a particular TTS backend, or real-time network. Backend adapters implement explicit interfaces. Android app owns media lifecycle and permissions. Renderer executes states, not narrative logic.

## Phase 0 — source consolidation [DONE: documentation baseline]

- Preserve original v0.2/v0.3 research and logical catalog without modifying their claims.
- Root docs, live vault, ICM navigation, Graphify dependencies, root/scoped AGENTS and project checkpoint.
- Make source truth/status explicit and validate internal cross-links.

## Phase 1 — executable specifications [CURRENT]

1. Freeze `@n/@cNm/@cNf` parser behavior, whitespace/Unicode/directive escaping, source spans, unknown directives, duplicates, conflicting metadata and source preservation.
2. Specify one canonical IR: package/story/chapter/scene visit/block/utterance/resource/cue/voice-intent/state; version negotiation, deterministic ID generation and editing migration policy.
3. Freeze event semantics: audible vs synthesis completion; start/end boundaries; event ordering; parallel cues; blockers; silent beats; pause/seek/cancellation; stale generation guard; snapshot restoration and idempotent one-shot effects.
4. Freeze default expansion: typed resource lookup, zero-asset procedural fallback, semantic layer ordering, responsive placement and safe subtitle area.
5. Freeze asset manifest: roles, mime/dimensions, anchors, portrait/landscape crop policy, transparent padding, animations, variants, provenance, fallbacks and cycles.
6. Freeze security: untrusted ZIP quotas/traversal/path normalization; strict formats; limits on deep nesting, effects, audio durations, model size/memory; no code execution.
7. Define implementation-neutral interfaces and conformance fixtures. Write ADRs when evidence changes a design.

**Exit gate:** exact schemas + linter cases; no ambiguous fallback/timing behavior; `doctor` passes; unanswered environment-dependent decisions explicitly remain provisional.

## Phase 2 — targeted architecture experiments [NOT STARTED]

- Android 15, 4 GB class: enumerate actually installed `fil-PH` or compatible offline voices; audition 30-phrase Tagalog test corpus, record prosody and errors; evaluate permissible neural models, memory, licensing and latency.
- Compare live `UtteranceProgressListener` against `synthesizeToFile` + controllable prepared clip player; record p50/p95 gaps and seeking behavior.
- Compose/libGDX integration spike: texture size, portrait/landscape/foldable safe area, Activity rotation, frame pacing with concurrent synthesis; compare Canvas option.
- Validate 1 background + two characters + speaker emphasis + fade + subtitles from one canonical scene.
- Package import threat tests, bounded decompression, corrupted media, camera crop and cache eviction.

**Exit gate:** benchmark report with reproduction environment, pass criteria, licensing and final backend choices (avoid conjecture).

## Phase 3 — vertical slice [NOT STARTED]

- Android app import → compile/validate → preview → play → pause/seek → close/reopen.
- Generic `@c1m` / `@c2m` dialogue, built-in silhouette sprites, classroom/street/neutral backgrounds, captions, expression/change animations.
- Voice resolver and on-device acceptable audio fallback. Text-only fallback with explicit recovery controls.
- Track deterministic source-to-audio segments and reference-aware cache. Never write user overrides back into authored package.
- Instrument frame time, startup time, RAM and narration gaps.

**Gate:** real-device runnable demo and checked conformance suite; provenance recorded.

## Phase 4 — packaged stories and asset expansion [NOT STARTED]

- Import manifest, canonical IR/assets, compatibility, checksums, dedupe, shared packs, sandbox quotas.
- Asset palette `storybook-flat-v1`: backgrounds, generic cast, expressions, props and sound/effect roles; test 9:16/16:9/4:3 safe crop.
- Source authoring toolkit + compiler CLI/linter, repairable machine-readable diagnostics.
- Background audio MediaSession and lifecycle recovery.
- Accessibility and audio mix (TTS dominates; default music/ambience muted).

## Phase 5 — advanced authored control and user personalization [NOT STARTED]

- Authoring semantic advanced timeline: before/after, local offsets, controlled parallel effects, optional audio word alignment.
- Voice and character overrides, expression sets, sprites/rig attachments with validated anchors, camera/parallax where supported.
- Story package model references with bounded compatible inference, explicit author licenses and user consent.
- Optional visual editor without abandoning text-first AI authoring.

## Phase 6 — release hardening [NOT STARTED]

- Fuzz parser/ZIP; device matrix; memory/thermal and process-death tests; power/background behavior; asset/music licenses.
- Stable schema version 1.0 and migration tests; reproducible builds; user documentation.

## Strict implementation order

`Contracts -> pure compiler/reducer -> real-device audio proof -> renderer proof -> minimal Android story player -> reusable assets -> advanced features -> hardening`.

Do **not** implement advanced rigging, voice cloning, procedural music or a large media pack before the audio clock and fallback contract are demonstrated.

## Branch/workflow

`main`: neutral project truth, plans and baseline research. Work branch examples: `implementation/android-vertical-slice` or `research/tagalog-voices`. PRs should link evidence and update [checkpoint](knowledge-system/vault/checkpoints/CURRENT.md). Never claim model compatibility or test passes from an unexecuted spec.

Read [Phase gates](knowledge-system/vault/plans/PHASE_GATES.md) and [Open questions](knowledge-system/vault/research/OPEN_QUESTIONS.md).

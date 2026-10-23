# 04 — Phase 1 Completion Criteria, Experiments and Risk Register

## A. Planning gates
**Gate A — Source/file specification frozen**: precise grammar, parser examples, escapes, block semantics, stable IDs; typed IR, schema and capability version rules; package/archive format and security bounds.

**Gate B — Narration contract proven**: mock speech provider emits synthetic audio/callbacks; seeking, pause/resume, stale callback rejection and `@after` are deterministic and testable. Benchmark actual Android TTS and prepared-audio variants.

**Gate C — Generic content contract finished**: full logical catalog, style guide, fallback chains, portrait/landscape metadata and license fields. Minimum artwork/audio package scoped. Distinguish catalog placeholder from shipped media.

**Gate D — Rendering architecture chosen**: prototype libGDX/Compose-hosted view across rotation/aspect ratios; scene model independent of renderer; resource lifecycle proven on representative devices.

**Gate E — Voice decision**: credible offline Tagalog voice(s) with measured latency/naturalness and redistribution rights identified, OR explicitly declare an initial Android-installed-engine-dependent mode, with honest offline capability messaging.

**Gate F — Integration architecture traceable**: no circular dependencies; each subsystem has interfaces, invariants, telemetry, failure behavior and independent conformance fixtures.

Only then begin production implementation. Disposable spike experiments can happen during Phase 1 after spec drafts.

## B. Conformance test fixture matrix
| ID | Input/test | Expected |
|--|--|--|
| S01 | plain 2 KB TXT, no directives | narrates entire text, neutral background, readable captions |
| S02 | basic @n/@c1m/@c2m | three stable speaker identities; no declaration required |
| S03 | @c1m later @c1 | same character identity preserved |
| S04 | @c1m later @c1f | metadata conflict diagnostic; no silent reidentification |
| S05 | @after following multi-sentence block | fires after full speech BLOCK finishes, not after first sentence |
| S06 | TTS chunks differently on two engines | block-level triggers still fire once at correct logical boundary |
| S07 | TTS backend has no onRangeStart | segment-level captions remain correct, word highlight marked unavailable |
| S08 | screen rotates during dialogue | audio position retained; characters recomposed; subtitles legible |
| S09 | missing BG + char art + music | neutral stage + labeled silhouettes; music silent; story progresses |
| S10 | missing required branch target | compilation rejected with source-span error |
| S11 | no compatible offline voice on device | explicit text-only status/recovery; never claim voiced offline playback |
| S12 | seek backward across expression cue | state reconstructed without duplicate SFX or animation firing |
| S13 | seek forward to scene not visited | state reconstructs without having played previous chapters |
| S14 | very rapid seek during TTS generation | stale callbacks ignored via generation token |
| S15 | sleep/screen off | audio-first service retains narration; visuals restore on return |
| S16 | missing sound asset | silence with one diagnostic, not trigger deadlock |
| S17 | portrait extreme + display cutout | faces/speaker cue never under subtitle/UI unsafe region |
| S18 | malicious ZIP traversal/zip bomb/model file | reject before import and before allocating unsafe resources |
| S19 | unknown @directive | actionable parse error; not spoken or executed silently |
| S20 | voice switch mid-utterance | switch at declared safe boundary; no dropped or duplicated block |
| S21 | 3 simultaneous visual cues at block end | stable priority/order; deterministic final scene state |
| S22 | 10 characters with implicit registry | distinct IDs; graceful crowded-scene layout fallback |
| S23 | storypack references shared voice not installed | capability/preflight warning and compatible fallback policy |
| S24 | accessibility reduced motion, captions enlarged | layout constraints and static animation replacements respected |
| S25 | cache evicted during background play | no deletion of originals; recover or stop explicitly |
| S26 | author manually declares conflicting layers | compiler error or documented resolution, never random ordering |
| S27 | quoted text containing `@` | escaping parser keeps literal prose intact |
| S28 | two narrative lines with same text | independent stable block IDs; cues not cross-fired |
| S29 | dialogue speaker never assigned a name | remains labeled by ID, no invented name |
| S30 | text normalization changes TTS tokens | displayed original preserved, audio cache invalidates correctly |

## C. Research/spike matrix and measurements
| Prototype | Measurements | Pass evidence required |
|--|--|--|
| Android TTS segmented `speak` | segment transition gaps, callback timing, voice compatibility | logs and captured audio with multiple engine types |
| synthesize-to-file then play | preparation latency, clip consistency, seek responsiveness | measured duration/position, exact segment boundaries |
| offline neural Tagalog | naturalness/listener feedback, memory, cold-start, sustained inference | device-dependent benchmark with model/license provenance |
| libGDX in Compose host | frame time, context recovery, portrait/landscape resize, overhead | screenshots across actual device ratios, memory trace |
| procedural idle motion | battery, CPU/GPU, animation accessibility | frame-time and visual review |
| generic content pack | decoded texture peak, audio volume balance, art consistency | sample story playback and style review |
| asset import security | zip traversal, decompression bounds, corrupted image/model | automated rejection tests |
| long playback | 30m+ continuous narration, pause/background/audio focus | saved checkpoints, repeatable crash/ANR-free sessions |

## D. Risk register
1. **Tagalog voice quality and license — critical**: no unrestricted assumption that a research voice is commercially redistributable; investigate alternative supported models / user-installed Android voices. Meta MMS Tagalog checkpoint: CC BY-NC 4.0.
2. **Per-sentence audio gaps — high**: prefetch + normalization; evaluate small variation in durations/intonation; consider chunking multiple sentences when no intervening cue.
3. **Misleading scene inference — high**: conservative defaults; inference produces suggestions; semantics never inferred as facts without confidence/author confirmation.
4. **Aspect ratio / safe area failure — high**: algorithmic layout not just FitViewport; test wide/narrow/orientation/cutouts and font scaling.
5. **Asset style mismatch — medium**: one visual style theme and provenance catalog; avoid throwing heterogeneous packs together.
6. **Memory blowout from images/models — high**: separate quality tiers, texture sampling, cache eviction and lookahead budgets.
7. **Package incompatibility — high**: strict capability contract, pinned format versions and conformance fixtures.
8. **AI hallucinated directives/paths — high**: formal grammar and machine-readable diagnostics, no automatic reinterpretation of unknown directives.
9. **Model import execution risk — high**: whitelist supported inference families and verify checksum/shape/resource budgets before import.
10. **Premature large feature matrix — medium**: design contracts for rigging/branching/word sync while implementing small, measured slices first.

## E. Recommended decision order
1. Freeze plain/bare-minimum author experience and speaker marker semantics.
2. Freeze canonical entities/IR and how source spans & stable IDs survive editing.
3. Freeze utterance/block boundary trigger semantics and playback state machine.
4. Freeze baseline generic asset taxonomy and semantic drawing/layout rules.
5. Benchmark renderer + Tagalog TTS candidates on low-resource hardware.
6. Freeze package format, capability fallback and offline readiness claims.
7. Build conformance toolchain & independent fixture stories.
8. Only then commit to production engine modules and first Android UI.

## F. Smallest honest demonstration
A simple ~2 minute Tagalog classroom scene with narrator, `c1m`, `c2m`, basic generic characters, a classroom/neutral background, sentence TTS, block boundary triggers, fade, subtitle toggle and voice fallback. Prove portrait/landscape, pause/seek, missing asset, and no-internet behavior. Do not expand to rigging or branching before this passes.

## G. Definition of Phase 1 done
- Every architectural subsystem has written responsibilities, public contracts, dependency direction, assumptions and failure semantics.
- A formal grammar and canonical schema exist, with positive and negative fixtures and source-mapped diagnostics.
- Narration pointer and timing rules have precise start/end/seek/cancel specifications.
- Built-in catalog has logical IDs, visual guides, fallback chains, intended binaries, size/quality targets and license requirements.
- Renderer/TTS candidates have actual measured evidence on low-resource device class.
- No unresolved *foundational* question forces reworking script syntax or canonical IR later. Experimental quality enhancements may remain open.

Status (2026-10-23): **NOT YET COMPLETE**; this pack is a reviewed design draft and a plan for closing remaining gaps.
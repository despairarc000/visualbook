# 02 — Architecture Decisions and Module Boundaries

All ADRs are *planning* status as of v0.3; validation is tracked in `08_VALIDATION_AND_ROADMAP.md`.

| ADR | Status | Decision | Explicit non-decision |
|---|---|---|---|
| ADR-001 | ADOPT | Narration-led state machine; audio playback position authoritative when available | TTS synthesis-completion is NOT story-completion |
| ADR-002 | ADOPT | Authoring progressive: `.txt` narrator; `.story.md` with `@n`, `@c1m`, `@c2f`; advanced additive grammar | No separate player for simple vs advanced |
| ADR-003 | ADOPT | Canonical typed scene/story IR independent of Android and rendering | JSON serialization of final IR remains provisional |
| ADR-004 | ADOPT | Every optional creative detail has typed, deterministic fallback | No arbitrary image or voice guessing when reference ambiguous |
| ADR-005 | ADOPT | Narrative cues attached to stable *authored speech blocks*, not incidental TTS chunk cuts | Segmenter may revise chunk sizes without changing cues |
| ADR-006 | PROVISIONAL | Prepared utterance clips + app-owned playback; live TTS adapter as compatibility | Need benchmark for gap/prosody vs latency |
| ADR-007 | PROVISIONAL | Kotlin / Android UI, Media3 service and independent scene renderer port | libGDX backend subject to 4GB-class test |
| ADR-008 | ADOPT | Scene categories determine z-order, explicit local depth within role | Script order never silently reorders background above characters |
| ADR-009 | ADOPT | User preference overlays immutable story profile | Safety/required semantics are never overridden |
| ADR-010 | DEFER | Rigging, word-level lip sync, branching, packaged custom neural models | Reserve capability/version/extension points, not implement all |

## Correct dependency direction

```
android-ui -----> application/playback-coordinator <------ Android Media3 service
       |                    |                                      |
       |                    v                                      v
       |                story-core <---------------------- speech/audio ports
       |                    |                                      ^
       v                    v                                      |
   renderer-port       content-resolver ----------------- model/adapters
       ^                    |
       |                    v
 renderer-libgdx        importer/validation
```

- `story-core`: immutable Story IR, semantic state, event reducer, semantic checkpoints, capability negotiation; no Android imports.
- `authoring`: lexer/parser, AST, narrator attribution, semantic analysis, blocks/utterances, type resolver, source diagnostics; no media generation or image decode.
- `speech-api`: `VoiceCapability`, `SynthesisRequest`, `SynthesisResult` (audio bytes/URI, duration, optional alignment), cancel; never manipulates renderer.
- `audio-player-api`: playable media, `positionMs`, seek, position discontinuities, route interruptions; emits audible start/end, not generate completion.
- `runtime-controller`: owns narrative position and event dispatch; rejects out-of-generation events; checkpoint and on-screen state rebuilding.
- `resource-manager`: typed handles, checksums, budget-aware decode/preload, licensed asset provenance and fallback chain; not story content interpreter.
- `renderer-api`: render desired scene state + animation interpolation; no advance-story methods. `renderer-libgdx` is an adapter.
- `subtitles`: text/source-span presentation and user accessibility controls; coordinates kept screen-fixed not world-fixed.
- `android-shell`: import picker, notifications/media controls, lifecycle, power/network state, audio focus, diagnostics, player UI.

## Strong invariants

1. **Single writer** of narrative cursor: playback reducer; asynchronous workers only emit results tagged with generation/identity.
2. **Single canonical speech block identity** unaffected by voice choice or segment subdivision. User-selected voice cannot rewrite source text silently.
3. **Scene state is serializable.** GL texture handles/model sessions are ephemeral and cannot appear in checkpoints.
4. **Idempotent state cues.** Applying `setExpression(c1, happy)` twice yields same logical state; SFX one-shots have separate consumption semantics.
5. **Fallback never crosses type boundaries.** `background -> background/neutral`, `character -> placeholder`, not character image used as background.
6. **Missing optional media cannot deadlock narration.** Nonrecoverable primary speech failure enters explicit paused/text-only recovery, never fake successful voiced playback.
7. **On/offline claims are runtime-proven**, not inferred from model package name or a marketing description.

## Product modes, one engine

- **Minimum reading:** any valid TXT is a narrated *intent* with narrator, captions and neutral scene; if offline speech unavailable, it remains a complete readable presentation and user sees why voice cannot play.
- **Minimal multispeaker:** `@c1m` etc create speaker identities and default voices, neutral sprites and labels. Metadata suffix influences defaults, not identity.
- **Directed story:** optional explicit scene, casting, voice, effects, camera, transitions.
- **Advanced:** precise anchors and parallel/conditional actions require named story capability and stricter validation.

## Planned outputs of the architecture phase

Source grammar + schema/IR + event transition matrix + fallback catalog + art bible + compatibility/security standard + CLI validators + quantified benchmark evidence. Do not begin full Android product until irreversible decisions pass their gates.
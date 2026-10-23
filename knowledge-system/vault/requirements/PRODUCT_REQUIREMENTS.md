# VisualBook — Product Requirements

**Status:** CONFIRMED product direction, with acceptance language added for testability. Last reconciled 2026-10-23.

## User scenarios

| ID | Requirement | Minimum acceptance |
|---|---|---|
| VB-R01 | Load unannotated story text | UI can import ordinary UTF-8 .txt; entire literal text remains available and can be displayed |
| VB-R02 | Minimal multi-speaker annotation | `@n/@c1/@c2m/@c3f` speaker identity resolves without separate character declarations |
| VB-R03 | Optional metadata | Gender/sex, name, artwork, background and voice are not mandatory for compiling a valid multi-speaker story |
| VB-R04 | Complete but generic presentation | Every valid script has minimal visual defaults even if no art files are included |
| VB-R05 | Narration progress is authoritative | Changes follow audible utterance start/completion, not synthesis or wall-clock guesses |
| VB-R06 | Responsive composition | All stage art remains undistorted in portrait/landscape; critical actors/captions remain accessible |
| VB-R07 | Subtitles optional | On/off and accessibility overrides without changing narration progression |
| VB-R08 | Character personalization | Viewer can override visuals/voice without mutating source story |
| VB-R09 | Portable stories | Support script plus optional media/model references in self-contained package with typed fallback |
| VB-R10 | Offline-first | Core import/reading, rendering, defaults and installed/offline-compatible voice work without network |
| VB-R11 | Bounded and safe | Untrusted packages cannot execute arbitrary code; archives/assets/models obey quotas and type checks |
| VB-R12 | Natural narration | Sentence-aware segmentation preserves spoken content and minimizes audible inter-sentence gaps |
| VB-R13 | Missing dependencies | Nonessential assets degrade; unsupported required capabilities are explicit errors |
| VB-R14 | Long stories | Incremental parsing/loading, checkpoints and background audio designed for long-form playback |
| VB-R15 | AI-authored stories | Plain grammar, deterministic compiler, machine-readable actionable diagnostics, progressive enhancements |

## Clarified limitations

- Not every uploaded text automatically yields believable character identity, named location or emotion. Ambiguous cases fall back to neutral, not confident inventions.
- `@c2m` stores a character metadata hint; it is not a locked voice, outfit or speaker-model name.
- A generic usable reader can function without TTS; audible **offline Tagalog** requires a tested licensed compatible engine. No such built-in model is claimed shipped.
- A basic plain text import must never try executing embedded directives that are not explicitly interpreted as scripting.
- Default ambience/music silence is valid; unprompted audio effects may interfere with listening.

## Nonfunctional intent

Deterministic core and idempotent event playback; documented versioned content IR; accessible captions and reduced motion; performance tests on budget 4GB Android device; legal/license provenance for redistributable binary media; testable bounded resource use.

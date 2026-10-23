# Canonical IR v0.4 — Executable Draft

**Status: PROVISIONAL, RESEARCH ONLY.** This is the first versioned, structurally described representation that can be lowered from the archived v0.3 script parser. It is **not** frozen for shipped Android stories. Schema: [story-ir-v0.4-draft.schema.json](../../../schemas/story-ir-v0.4-draft.schema.json). Bridge: [story_ir.py](../../../research/prototypes/v04/story_ir.py). Contract tests: [test_story_ir.py](../../../research/prototypes/v04/tests/test_story_ir.py).

## Why it exists

Prior experiments mixed parser-specific block IDs and playback behavior. A stable *format boundary* is essential for a future Kotlin compiler, Media3 audio player, renderer and package builder. Our top invariant: one source story, regardless of author effort, resolves to a **typed playable logical model** with a generic fallback story stage. It never assumes image bytes or a voice model are present simply because a logical ID exists.

## Document structure

- `format`: `visualbook.story-ir`
- `ir_version`: `0.4-draft` (not wire-compatible guarantee for 1.0)
- `source`: normalized-source SHA-256, import mode and compiler provenance
- `story`: deterministic provisional identity derived from source content
- `capabilities`: declared required/optional features (future package negotiation)
- `defaults`: generic art style, neutral background, *silent* ambient/music policy, captions available
- `characters`: narrator `n` plus implicit or explicitly declared cN, visual fallback, speaker voice **intent** (not installed voice choice)
- `chapters/scenes`: current draft produces one chapter; each scene visit has unique ordinal ID regardless of reused logical name
- `scene.blocks`: authored text, normalized speech text, one-based line origin, speaker ID and derived utterances
- `scene.cues`: supported typed expression changes anchored to authored block start/end (after final spoken utterance)
- No real audio times, waveforms, neural model references, user customization or rendered images are present in this pure IR.

## Identity and stability

IDs are deterministic **for the same source + parser/segmentation/configuration**. Current block/scene/utterance IDs are ordinal and WILL SHIFT after upstream edits. Until a stable author-assigned ID and migration policy is designed, persisted bookmarks/caches must use content fingerprints + explicit version and invalidate safely across edits. Do not claim edit-stability from determinism.

## Text / safety

`.txt` is always literal, `.story.md` is opt-in script. UTF-8 BOM and newline normalization occur before source hash. The historic parser stores block text with internal newlines and separately normalizes spoken_text; byte/column-accurate spans are not yet available. Source reconstruction through blocks alone is not lossless: retain original imported source independently.

If any legacy parser error or semantic IR error occurs, compiler returns `ok:false, ir:null, diagnostics:[...]`. A partial scene must not masquerade as valid. A silent-only scene is currently an explicit unsupported error; a future typed silent beat will allow it. An unrecognized required capability cannot be ignored.

## Cue/utterance semantics

`@after` attaches to last authored **speech block** end, not after every sentence in that block. Cues are only logically ordered until real audio reports observable progress. The `event_plan()` function is a **static simulation**, not wall-clock scheduling and not a substitute for actual media callbacks. At runtime: finish preparing audio ≠ spoken; to dispatch an `audible.end` event there must be real acknowledged player progress.

## Defaults and fallbacks

Character appearance is a *logical* `generic.silhouette` plus deterministic color accent; requires an eventual bundled sprite or procedural renderer to fulfill. `bg.neutral` is a logical preferred/default background plus a procedural-neutral last resort. No audio effects/music unless explicitly directed. Voice `language:und` means unspecified; no assumption that `fil-PH` offline voice is installed. A UI may offer text-only playback if speech unavailable; it may not falsely announce audible narration.

## Validation layers

1. JSON Schema 2020-12 structural/type check (schema file; external validator optional).
2. Semantic validator in `validate_ir`: unique character/scene/block/utterance/cue IDs; block→speaker; cue→same-scene block and known actor; backgrounds typed; exact utterance concatenation; nonempty utterances.
3. Playback adapter preflight: available resources, capability mapping, voice model availability, quotas, accessibility and cross-version policy. **Not yet implemented.**

## Explicit open issues for v0.5/1.0

Precise spans (UTF-8/scalars/graphemes), chapter authoring, editing-persistent IDs, advanced cues/parallel event ordering, silent beats, interruption/scene revisit rules, fixed asset manifests and security quotas, exact voice capabilities, deterministic voice cache invalidation, full schema validation via an independent standard validator.

## Reproduce locally

```bash
python3 research/prototypes/v04/story_ir.py knowledge-system/vault/history/v0.3/examples/minimal.story.md --mode script
python3 -m unittest discover -s research/prototypes/v04/tests -v
```

This pure Python proof is deliberately isolated from production Kotlin. Preserve the original archived v0.3 sources unmodified.

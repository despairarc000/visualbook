# Module Boundaries / Interfaces

**Status:** PROVISIONAL implementation architecture.

**Dependency direction:** core has no Android or libGDX imports. Parsing, IR validation and reducer are pure core. Android/platform adapters implement downstream contracts.

- `ImportGateway`: bytes/content URI -> validated source and materialized package resource set (quota enforcement).
- `ScriptCompiler`: source + grammar version + catalog -> immutable canonical IR + diagnostics + source spans.
- `CharacterRegistry`: implicit/explicit identities, metadata hints, named overrides; no voice engine classes.
- `AssetResolver`: typed resource request + effective override policy -> validated handle or degradation, with provenance.
- `SpeechBackend`: enumerate support, synthesize/prepare utterance, report errors; never advance narrative state.
- `VoiceResolver`: character voice intent + installed models + language capabilities + user choice -> compatible speech backend voice or text-only error.
- `NarrationPlayer`: playable clips, actual audible position/start/end, pause/seek/speed and focus.
- `PlaybackReducer`: serialized generation-guarded inputs -> immutable story/scene state + effect intents.
- `RendererAdapter`: render resolved world/camera/screen state and observe frame diagnostics, no story logic.
- `SubtitlePresenter`: consumes text spans/audio anchors + user accessibility settings.
- `StateStore`: checkpoints and user overrides separate from source packages.

Candidate Android app: Kotlin, Compose, Media3, 2D backend provisional libGDX. Persisted file format is versioned; presentation capabilities use explicit required/optional feature flags. Alternative renderers must be able to implement the same canonical stage interface.

Further detail: [Historical architecture ADR](../history/v0.3/contracts/02_ADRS_AND_MODULE_BOUNDARIES.md).

## Voice-profile clarification from first five-voice device success (2026-10-24)

`VoiceIntent` from a story/character (language, optional sex/presentation hint, style) is NOT a TTS `Voice` name. `AvailableVoiceProfile` must contain provider package/version, opaque voice ID, locale, metadata, confirmed selected/effective ID, actual supported synthesis operations, offline-tested flag, user-rated intelligibility/naturalness (optional), last verified time and failure reason. `VoiceAssignment` maps `n/c1/c2` to a chosen profile and must not depend on markup-level sex hints except as an optional suggestion. Any failure resolves to an explicitly communicated fallback, never changes narration state as if words were audibly spoken. See [Samsung five-voice evidence](../../../research/experiments/android-audio/SM-A066B_FIVE_FILIPINO_VOICES_2026-10-24.md).

## Implemented experimental adapter (2026-10-24)

The [first Android player](../../../implementation/visualbook-player/README.md) provides a concrete and intentionally small test of independent core logic: `StoryCore.compile(source,script)` + `PlaybackState` remain pure Java, TTS boundary callbacks drive current segment, Canvas renders procedural generic state, user voice assignment maps only by speaker ID. An Android document picker imports TXT/source without archive/code execution. These classes are not final production interfaces or full v0.4 canonical IR.

`PlaybackState.begin()` is queued/prepared only; `audibleStart(id)` changes speaking state; `audibleDone(id)` advances once; pause/stop/error invalidate generation. User manual skip is distinct and explicit. The current prototype uses live TTS, not prepared clips, and pause restarts at sentence boundary; precisely timed mid-sentence seek is deferred.

## Stage presence must be independent of speaker activity

[First device test](../../../implementation/visualbook-player/DEVICE_TEST_V01_FIRST_SUCCESS_2026-10-24.md) showed a new `@c3` character only during their utterance. The current prototype implements `activeSpeaker` as both a voice/speech cue and guest-visibility condition. For a fuller visual storytelling model, distinguish **story identity**, **scene presence** and **currently speaking**. Scene presence should be a deterministic reducer property, reconstructed after seek or resume, and changed by actual scene/cue semantics rather than opportunistic TTS generation callbacks. Preserve the no-configuration generic fallback and avoid conflating `m/f` metadata with mandatory voices or sprite genders. The auto-presence policy for undefined entrance/exit markers is still a design choice.

## Art-style scopes as presentation state, not scripted speech

[Scoped art styles v0.2](SCOPED_ART_STYLES_V02.md) establishes a source/IR boundary: `@chibi` and `@end` compile to **effective style values on speech segments**; an Android renderer chooses a corresponding procedural style at audible start. `PlaybackState` remains independent of Canvas, style assets, and directives. For future production, `StyleRegistry.resolve(styleId,characterRigCapabilities)` and `StageRenderer` should share a portable style semantic slot contract. One story may mix normal, chibi, anime-lite, comic, etc., but **only normal/chibi are actually implemented** now.

## Virtual shape geometry v0.3 — two coordinate domains

[Virtual canvas geometry contract](VIRTUAL_CANVAS_GEOMETRY_V03.md): a self-contained `VectorFigure` defines pure validated primitives in its own unit artboard; a `Placement` chooses a normalized stage center and width. `StoryCore.Segment.visibleFigures` records each active visible object at its audible boundary while imported figure definitions are immutable story resources. Android Canvas transforms from local to stage pixels with a uniform meet scale. Do not conflate *position of eye inside creature* with *position of creature on phone stage* or convert the stage into raw physical screen coordinates. Future part/group transforms should obey a parent→child scene graph.

## Vector rig v0.4 local coordinate hierarchy

[Shape Rigging and Transform Contract](SHAPE_RIGS_AND_TRANSFORMS_V04.md): `VectorFigure` now has immutable root `elements` and a validated registry of named `Group` definitions, each containing vector primitives and/or already-built `Instance` references. `StageRenderer` composes group-local pivot transforms through save/translate/rotate/scale/translate/clip/recursive draw/restore. Text scripting never directly executes code or controls Android `Canvas` methods; parsing and geometry validation remain pure Java. The hierarchy is capped to protect low-memory devices.

## Procedural motion contract v0.5

[Audible part motion](AUDIBLE_PART_MOTION_V05.md) extends the story-core/renderer contract: `StoryCore.Segment.motions` is immutable declarative **presentation state**, not an audio cue or synthetic narration event. Valid TTS start chooses the stage animation profile; spoken completion remains exclusive advancement authority. `VectorFigure.Motion.offsetAt(monotonicElapsedMs)` computes a bounded oscillation on a root instance's declared pivot. No part of Canvas or a visual timer can cause `PlaybackState.audibleDone`.

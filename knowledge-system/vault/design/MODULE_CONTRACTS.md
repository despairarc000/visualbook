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

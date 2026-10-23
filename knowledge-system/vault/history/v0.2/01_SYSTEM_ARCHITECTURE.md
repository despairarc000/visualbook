# 01 — Consolidated System Architecture

## A. Product contract

**LOCKED PRINCIPLES**
1. Offline-first playback; a story must not depend on streaming video. Actual offline voice capability requires an installed compatible voice or included permitted audio/model; if unavailable, text-only is a last-resort degraded state and the user must be told.
2. Narration-led: the authoritative semantic cursor is `chapter / scene / utterance / positionWithinUtterance`. The audible playback clock, when under app control, drives synchronized state. The synthesis engine's workload progress is not playback progress.
3. Sentence/utterance boundary triggers are baseline; clause/word anchors only with declared support or carefully authored shorter segments.
4. All valid narrative inputs compile to one canonical, typed, versioned story model. Plain TXT defaults to one narrator; multi-speaker scripts use speaker markers. Missing decorative creative information is not a fatal error.
5. Character identity is independent of sex metadata, voices, artwork, naming, presentation, and user customization.
6. Optional creative content has safe hierarchical fallbacks. Missing required narrative logic fails validation rather than inventing a new story.
7. The engine interprets declarative data; untrusted stories never execute app code or model-supplied plugins.

**NON-GOALS (initial release)** Live 3D worlds; real-time generative-video synthesis; arbitrary Python/JS inside stories; fine-grained cinematic lip sync without alignments; universal import of arbitrary third-party voice-model formats; automated semantic understanding of any prose without uncertainty.

## B. Runtime component model

```
TXT / STORY.MD / STORYPACK
      |
      v
Importer -> Grammar parser -> Typed AST -> Semantic analyzer
      |                             |
      |                      Default resolver
      v                             |
Package security -> Asset/voice requirements checker
      |                             |
      +----------> Canonical Story IR <---------+
                       |
                 Playback Controller
           (semantic cursor + state reducer)
                       |
            Narration coordination
             /       |        \
    Speech backend  Media player  Trigger scheduler
             \       |        /
                   Scene state
         /          |           \
    2D renderer  subtitle view  audio mixer
         \          |           /
          resource/cache/diagnostics
```

**Ownership rules**
- Parser/compiler creates semantic definitions; NEVER runs TTS or GL drawing.
- Playback controller owns **story progress**, scene admission, branching, pauses, checkpoints, and seek restoration.
- Speech provider owns synthesize/readiness/error; narration player owns *audible* playback position when pre-generated audio is used.
- Renderer reads resolved immutable scene state + animation parameters; it may not mutate story logic directly.
- Asset resolver maps logical asset IDs to installed file, override, builtin, procedural fallback.
- Android shell owns lifecycle, import picker, UI, accessibility, audio focus, media session, permissions.
- Imported story packages never supply executable libraries or scripts.

## C. Data entities

| Entity | Meaning / invariants |
|--|--|
| `Story` | identity, author language, format/version, ordered/branching chapters, capabilities, policy |
| `Chapter` | reference-stable narrative unit containing scenes |
| `Scene` | logical setting, initial presentation, sequence/branch, asset requests |
| `Speaker` / `Character` | stable ID, name optional, voice traits optional, sex metadata optional, appearance traits optional |
| `SpeechBlock` | authored prose attached to a speaker; retains author source offsets |
| `Utterance` | compiler-assigned TTS chunk (normally sentence or cohesive clause) with stable parent block |
| `Cue` | idempotent scene/audio action anchored to named speech block/utterance start/end or supported relative boundary |
| `AssetRef` | semantic ID, desired type, preferred resource, fallback chain, presentation constraints |
| `VoiceProfile` | language, intended register, preferred installed providers/models, fallback traits, licensing |
| `ResolvedSceneState` | current background, cast, transforms, active effects, camera state; independent of resource handles |
| `PlaybackCheckpoint` | semantic cursor + deterministic state + pending waits + run/version/override fingerprints |
| `UserOverride` | immutable story remains intact; user voice/costume/appearance/subtitle overrides stored separately |

Canonical IR may be JSON for tooling or a typed binary/in-memory equivalent in Android. JSON Schema 2020-12 validates structure; a second semantic pass checks graph consistency, reference existence, runtime capabilities, asset classifications, and safety.

## D. Narration timing and reliable triggers

### D1. Playback modes
- **Preferred:** synthesize utterance audio ahead, decode through owned media player; derive position from playback clock; align subtitle at block/utterance boundary; optionally use provider word times.
- **Compatibility:** Android TTS `speak` and utterance ID progress listener for starts/completions; callbacks have scheduling latency and are not exact audio sample boundaries. `onRangeStart` is optional/provider-dependent.
- **Recorded audio:** same segment contract with known clip offsets and no synthesis.

### D2. Canonical cursor
```
NarrationCursor {
  storyId, chapterId, sceneId,
  speechBlockId, utteranceId,
  positionMs?,   // valid only with known buffered/recorded playback
  playbackState, // PREPARING, READY, PLAYING, PAUSED, WAITING, ENDED, ERROR
  generation     // monotonic token for canceling stale callbacks
}
```
- `positionMs` is not a second uncoordinated storytelling clock; it is within currently audible utterance.
- Segment IDs and start/end are **logical** boundaries; `onDone` is a coarse event in live TTS mode.
- A cue is applied at most once per linear passage in a playback run; seeking invokes state reconstruction rather than replaying one-shot audio/events.
- Audio is synthesized asynchronously in lookahead windows; generation completion is NOT a trigger to show a new scene.
- At scene admission: resolve current background, active cast and safe UI FIRST, then permit speech start. To avoid race, every scene exposes `scene_ready`; user-facing transition waits or degrades on preparation timeout.
- Visual-only pauses are explicit narrative holds (e.g. hold 700 ms after a speech boundary). Their local timer is subordinate to the controller; they do not make visuals an independent narrator.

### D3. Playback transitions
```
NOT_LOADED -> VALIDATING -> PREPARING -> READY -> PLAYING
                     \               \               |
                       ERROR <------- RECOVERING <------+
PLAYING <-> PAUSED
PLAYING -> SEEKING -> PREPARING -> READY -> PLAYING/PAUSED
PLAYING -> WAITING_FOR_CHOICE -> PLAYING
PLAYING -> ENDED
```
- Separate `synthesis-ready`, `audio-buffered`, and `scene-ready` conditions.
- Discard callbacks from previous generations after seek, chapter change or voice switch.
- Audio focus loss: pause/duck according to settings and platform policy; make behavior consistent in background.
- On narration interruption: freeze semantic cursor, stop speech animation; attempt retry / alternate voice / text-only recovery per explicit policy.
- On rapid seek: cancel obsolete lookahead, restore nearest snapshot + pure state reducer to requested boundary, then load media. No re-triggered old sounds.

### D4. Cue contracts
Supported anchors start with `block.start`, `block.end`, `utterance.start`, `utterance.end`, `scene.enter`, `scene.exit`, and `choice.selected`. Optional `word` only with alignment capability. Timed offsets must specify an anchor, no implicit wall-clock trigger. Ambiguous simultaneous cues sort by phase, priority, source order, stable ID. Active transitions evaluate as a function of media-relative/local time and reduce-motion settings. Cue handlers return success / soft-fallback / hard-fail with diagnostics; no infinite wait.

## E. Character and voice system
- `@c2m` declares char ID `c2` plus initial optional sex metadata `male`; later `@c2` references same ID. `@n` is reserved narrator identity. Conflicting suffixes raise a visible diagnostic; they do not silently create new characters.
- No suffix => unspecified sex. Do not guess sex from IDs or enforce voice pitch. Give neutral/distinct silhouettes and choose best available voice, with accessibility override.
- Names optional. If prose mentions "Juan" and "Pedro" without mapping them to speaker IDs, label the speakers generically; human/AI author can optionally map `c1=Juan`. Never make false semantic claims about identity.
- Character definition comprises `ID`, optional metadata, 2D drawable, expression set, placement preferences, voice intent, active state, audience override.
- Two visual representations: flat whole-body sprite (baseline), optional composited layered parts (later). Rig compatibility must be declared; no mix-and-match incompatible component geometry.
- Voice resolver: User-selected voice -> story-required compatible voice -> story-preferred voice -> app-installed compatible voice -> approved generic fallback -> text-only; distinguish **required** vs **preferred** voice. Language compatibility and user consent beat default sex selection.
- Story-provided models may be accepted only for recognized runtime backends and license/size/shape/hash/security constraints. A file called `model.onnx` is not sufficient to claim compatible voice. Embedded models increase package size; sharing installed voice packs is default.
- Speech cache key should incorporate normalized audio input, original ID, voice/model version, synthesis settings, language, normalizer version, and applicable speaker embeddings. Keep original display text separate.
- Tagalog offline voice quality, speed and redistributable rights remain an **unresolved release blocker**. Meta MMS-TTS Tagalog is CC BY-NC 4.0 and cannot be treated as a general commercial default.

## F. Visual composition system
- Semantic draw families in increasing depth: environment rear -> middle -> characters/props interleaved by explicit depth -> foreground occlusion -> scene effects -> captions/system UI. Allow effects to be behind characters via explicit allowed sublayer.
- Renderer receives scene graph (position, anchor, depth, transform, resource identity). It cannot infer semantic story truth from visibility.
- Stage world coordinates independent of screen/UI coordinates. Use responsive **composition profiles** (portrait, landscape, intermediate) with safe zones, ground baseline, focal point, max crop and priority; no nonuniform stretch.
- Background: default `cover` and optional pan; must preserve focal point; if cover would cut essential content, prefer art extension / parallax / blurred fill / matte rather than stretch.
- Character: preserve proportional scaling and ground anchor; adjust group spread and active speaker focus; never crop face or subtitle zone by default.
- Scene camera can move independently of subtitles, accessibility overlays and player controls.
- Renderer candidates: libGDX/Scene2D recommended for spike; Compose for shell; keep rendering adapter contract separate. libGDX's `FitViewport` letterboxes and `ExtendViewport` expands world view: neither alone solves semantic re-composition. Need our own layout solver on top.

## G. Package and import
- Inputs: plain `.txt`, proposed `.story.md`, `.storypack` ZIP container (extension/protocol still PROPOSED).
- Provisional package: `manifest.json`, `script/main.story.md` and/or compiled `scenes/*.json`, `assets/*`, optional `voices/*`, checksums, licenses. Manifest declares *exact package format version*, required capabilities, optional capabilities and resource permissions.
- Two package classes: **self-contained offline** (required resources included/licensed or guaranteed builtin) and **reference package** (depends on installed shared packs/voices). Player must report capability before claiming offline readiness.
- Import staging: scan archive safely -> validate manifest and media -> resolve resources -> allocate library record -> copy to managed storage atomically -> only then expose as playable.
- Reject traversal paths, oversized decompression, unsupported code, symlinks/special entries, remote-file dependencies, dangerous media encodings, duplicate conflicting IDs, resource exhaustion; require bounded scene/action count and audio model memory.
- Asset packs independent versioned resources; stable logical IDs and content hashes; pack remapping must not silently change semantic meaning.

## H. Resource/quality management
- Budget for low-resource Android hardware (including 4 GB-class devices) using measured physical RAM and real app working set, not a claimed guaranteed free memory amount.
- Separate compressed disk size from decoded texture size; downsample and pack atlases carefully; stream music; preload next scene; release unused textures.
- TTS voice models are often larger than generic artwork; measure model warm-up, CPU demand, sustained thermals, and synthetic-audio cache size.
- Quality tiers: Economy / Balanced / Rich. Lower effects, texture dimensions and preload window before sacrificing intelligible narration or subtitles.
- Persist library separately from cache; derived speech can be evicted and regenerated; imported original packages are never silently deleted.
- Use Media3 MediaSessionService for audio-first playback when backgrounded; coordinate foreground service restrictions with Android policies.

## I. Correctness and failure classes
| Class | Rule |
|--|--|
| Missing visual asset | resolve installed/builtin/procedural fallback and warn once |
| Missing optional audio/SFX | remain silent; record non-blocking diagnostic |
| Missing preferred voice | resolve compatible fallback; if none available, text-only with explicit status |
| Missing model necessary for required artistic feature | fail capability check; do not claim equivalent result |
| Missing required branch destination | compile failure |
| Unrecognized directive | compile failure with source span and repair guidance |
| Voice or media failed during play | freeze pointer, avoid double-firing cues, try explicit recovery |
| GPU context loss / rotation | rebuild render resources, restore logical state, retain narration position |
| No scene available | neutral background + captions rather than black or crash |

## J. Remaining design choices (do not lock prematurely)
1. Exact authoring grammar and escaping/inline marker rules; ID preservation across edits.
2. Audio strategy and how to avoid gaps during segmented TTS on representative 4 GB Android phones.
3. Tagalog voice backend, rights, naturalness and multi-speaker voice availability.
4. Renderer adapter selection after rotation/thermal/low-RAM prototypes.
5. Built-in art theme: coherent vector/silhouette design recommended for initial pack, but artwork needs visual-style review.
6. Semantically safe automatic background inference vs neutral fallbacks; on-device AI optional only.
7. Canonical metadata for rigged sprite compatibility; defer implementation until proven worthwhile.
8. Scope of branching/interactive decisions v1: should be possible in schema but not necessarily included in MVP.
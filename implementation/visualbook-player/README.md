# VisualBook First Animated Story — Vertical Slice v0.1

**Implementation branch:** `implementation/first-animated-story`. This is a real Android player **prototype**, not a full production app or finished generic-media library.

### Architecture

`story-core/` contains dependency-free Java script parsing, sentence segmentation and generation-safe logical playback state, with a main-method test suite. `app/` is the Android adapter: TTS runs through a worker, Canvas draws the generic classroom/cast, Android UI hosts voice selectors and captions.

### Scope

- Demo Juan/Pedro story preloaded.
- Android text document picker (UTF-8 TXT / .story.md inferred from extension), bounded 200-KB import; no storage permission.\n- User-editable script accepting `@n`, `@c1`, `@c2m`, `@c3f`, `@bg classroom` / `@bg neutral`, or plain TXT toggle.
- Implicit cast: no character manifest required. Speaker-specific generic silhouette fallback including a "guest" actor for `c3+`.
- Three actor voice selectors (n, c1, c2), automatically proposing distinct local Filipino voice candidates if present; optional on-device voice engine, no bundled models.
- Live Android TTS utterance callbacks drive visible speaking animation and progression. No fake timer-based story advancement. Animation frames use a subordinate clock only while speaking.
- Stop, restart, pause (stops TTS; resumes from start of interrupted sentence), manual Skip when failed. Errors display visibly without auto-skipping.
- Caption and voice chooser are Android UI widgets; Canvas uses no external graphical assets or storage permissions.

### Limitations

This is a **narrow tested subset**, not the complete v0.4 spec: no ZIP/storypack import, full import validation, @after expressions, multiple background scene visits, audio clip caching, exact interior audio seek, saved progress, actor-specific custom sprites, soundtrack, screen-off foreground service, rate/pitch controls or offline-engine guarantee. Editor accepts up to 100k characters and 500 utterances; Android UI loads input directly into memory. Parser preserves imported text in the in-memory `Story`, though no persisted state yet.

The old research probe remains unmodified. Android TTS provider identity and installed voices vary; fail visibly if no playable voice. No model/audio/media licensing to redistribute is assumed.

### Build

JDK 17 / Android API 35 / Gradle 8.11.1. GitHub Actions `visualbook-player-build.yml` produces the debug APK and runs pure-core tests.

```sh
# pure core
javac -d /tmp/visualbook-core-test story-core/src/main/java/com/visualbook/core/*.java story-core/src/test/java/com/visualbook/core/StoryCoreTest.java
java -cp /tmp/visualbook-core-test com.visualbook.core.StoryCoreTest

# Android debug APK
gradle --no-daemon assembleDebug
```

Android app ID: `com.visualbook.player.slice` to avoid colliding with any research probe. First phone test should verify synchronization, Tagalog pronunciation, voice switching, screen rotation and 4 GB memory behavior. Audio engine/listening results are unknown until tested.

## Build checkpoint

[Version 0.1 build record](BUILD_EVIDENCE_V01_2026-10-24.md) confirms 26 passing pure Java assertions, a successfully compiled 32,419-byte debug APK with recorded SHA-256, and merged PR #9. Physical Samsung phone testing remains open.

## First real phone feedback

[Device observation record](DEVICE_TEST_V01_FIRST_SUCCESS_2026-10-24.md): the user successfully ran the app and created `@c3` without explicit assets; the generic guest appeared while `c3` spoke and vanished on subsequent turns. Full audio-sync quality/voice testing is not yet confirmed. Future cast rendering will separate scene presence from active speaker; current v0.1 behavior is intentional narrow-prototype fallback.

## v0.2: Easy temporary art-style scopes

The new prototype recognizes `@chibi` / `@end`, or `@style chibi` / `@end` as equivalent, plus nested `@style normal` (alias `@style cartoon`) overrides. **One marker per line**. Only two actual procedural modes are supported so far: normal/cartoon, and a visibly redesigned chibi body/head/eyes/mouth with pastel accents. `@end` restores the parent style (nested scopes work); unmatched ends, unclosed blocks and unknown modes raise compiler errors with line locations. Plain TXT still treats markers literally.

Example:

```text
@n
Pedro, anong nangyari?
@chibi
@c1m
NAKALIMUTAN KO!
@c2m
ANO?!
@end
@n
Bumalik sa normal ang lahat.
```

The story core attaches `artStyle` to every spoken segment. **Changing the visual mode must not trigger speech**; the scene updates on a valid live TTS `onStart` for that segment, with obsolete callbacks guarded by the audio cursor. Pause retains the current look, Stop resets to normal, and explicit Skip can show the upcoming section's look. This is **global scene styling** in v0.2, not a per-actor style swap; no cross-style part-rigged custom character packs, @style anime assets or automatic morph animation yet.

App ID: `com.visualbook.player.styles.v02`, installed side-by-side with v0.1 to avoid GitHub debug-signing incompatibility; label **VisualBook Styles v0.2**. The example story now demonstrates a chibi comedic reaction automatically.

## v0.2 verified build

[Verified scoped art style build record](BUILD_EVIDENCE_STYLES_V02_2026-10-24.md): PR #11 merged, 42 pure Java assertions passed, Android APK compiled and archive inspected; separate v0.2 app. On-device chibi rendering remains to be tested.

## v0.3: Virtual vector artboards — experimental

The story can now supply a custom self-contained figure with a virtual coordinate system independent of physical screen pixels. Sample included in the built-in Juan/Pedro demo:

```text
@figure spirit 100x100
triangle 11,84 49,8 88,84 fill:#8465B9 stroke:#5A357B width:2
circle 77 56 11 fill:#F6D16B stroke:#714A95 width:2
path M 15 64 C 20 14 59 26 74 18 stroke:#F7E2B3 width:3
@endfigure
@show spirit center
@n
Isang mahiwagang nilalang ang lumitaw.
@hide spirit
```

The shape definition is built once at compile time and `@show`/`@hide` persist across speaker turns. Each immutable utterance records the visible figure IDs and slots (left/center/right), and Android Canvas displays each figure using uniform aspect ratio-preserving scaling. Geometry is sandboxed, non-executable data: **circle**, **triangle**, **polygon** (3..32 points), **path** with absolute M/L/Q/C/Z, 6/8-hex colors, stroke width, bounded artboards (16..1024), up to 64 primitives per figure, at most 8 definitions and 4 simultaneously visible. No network or files needed. Shapes are validated and clipped to their own artboard.

`@figure` blocks and show/hide cues are NOT narration. Their effects are applied to the next speech segment at an actual TTS audible start; a future silent-cue timeline needs a separate clock. Existing `@chibi` and `@end` remain style-only and unchanged. Import remains UTF-8 text/annotated markdown, and untagged plain TXT is literal.

**Limitations:** global figure placement slot only, no nested groups/transforms/rigged animation, image assets, 3D, external SVG import, anti-overlap scene layout or per-figure style transformation yet. Source is not a general unconstrained natural-language-to-image model. Figure content must be explicitly authored in geometric instructions by a human or AI. New app ID `com.visualbook.player.canvas.v03`, installed alongside prior apps.

### Precise stage coordinates

Figures have TWO independent coordinate systems. The figure itself can be drawn on its local `100x100` artboard. To place that entire figure on the story stage, the stage also exposes a normalized `100x100` coordinate space, independent of phone resolution:

```text
@show spirit at:77,56 width:27
```

This places the *center* of the figure at stage (77,56), reserving 27% of the stage width, preserving its own aspect ratio. Preset `left/center/right` remains the easy mode. Geometry must be authored as data, not Java or executable code. No arbitrary pixel assumptions, no unnatural stretching. Some placement can clip at stage edges deliberately; automatic occlusion avoidance is still future work.

## Virtual Canvas v0.3 verified build

[Build and SHA-256 record](BUILD_EVIDENCE_VIRTUAL_CANVAS_V03_2026-10-24.md): 61 StoryCore and 43 geometry assertions passed; Android Gradle and knowledge CI succeeded; PR #12 merged. User phone acceptance remains open.

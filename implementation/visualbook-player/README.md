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

## v0.4: Reusable vector parts and pivots (experimental)

This build adds **named shape parts** that have their own virtual artboards, independent pivots, and multiple instances. Put `@part id WxH pivot:x,y` inside an `@figure`; draw local primitives, close with `@endpart`; instantiate anywhere in the parent with `@use id at:x,y [rotate:degrees] [scale:factor] [mirror:true|false]`. You can nest instances of **previously completed** parts inside later parts, making hierarchical geometry without arbitrary executable code. The built-in creature demo uses reusable eyes, horns, wings and a composed head with symmetrical transforms.

```text
@figure being 100x100
@part eye 20x20 pivot:10,10
circle 10 10 8 fill:#FFCD54
@endpart
@part face 50x50 pivot:25,25
circle 25 25 23 fill:#A788D4
@use eye at:16,27
@use eye at:34,27 mirror:true
@endpart
@use face at:50,45 rotate:10 scale:1.1
@endfigure
@show being at:50,40 width:40
@n
A new creature appeared.
```

Each part's own (0,0) is its top-left; `pivot:x,y` selects the point that lands on the parent `at:x,y` coordinate. The transform is `T(at) * R(degrees) * S(scale*mirrorX, scale) * T(-pivot)` in Android's screen coordinates; positive degrees rotate clockwise. Child objects draw in source order, retaining old `@figure` commands and existing `@chibi` scopes. Previous referenced groups must be closed first, so cyclic references cannot form. Max 12 named groups, 64 geometry/instance nodes, maximum expanded draw cost 256, maximum hierarchy depth 4; scale 0.1–4 and rotation -360° to 360°. Invalid commands fail the entire script with a line-numbered error, not silently.

**Not supported yet:** animated pivots over time, skeletal deformation/IK, arbitrary skew/mesh warps, auto-generated art from unrestricted natural language, imported PNG/SVG, or per-figure texture/material style changes. Those require separate asset-control contracts. A figure's geometric construction is pure, deterministic and portable across screen resolutions.

Package `com.visualbook.player.rig.v04`, separately installed from the v0.1/v0.2/v0.3 research builds.

## Shape Engine v0.4 verified build

[Android source, tests and APK evidence](BUILD_EVIDENCE_SHAPE_RIG_V04_2026-10-24.md): PR #13 merged, Android build succeeded, 66 core and 76 geometry assertions passed; separate v0.4 app with a modular mythical-creature demonstration. Physical device check still required.

## v0.5: Audio-bounded optional part motion

Shape Engine v0.5 adds an explicit **instance ID** on a root `@use` and story-level declarations for gentle procedural oscillation:

```text
@figure spirit 100x100
@part wing 40x50 pivot:5,45
path M 5 45 Q 5 15 32 8 L 38 30 Z fill:#8873CB
@endpart
@use wing at:26,44 id:leftwing rotate:-24
@use wing at:74,44 id:rightwing rotate:24 mirror:true
@endfigure
@show spirit center
@animate spirit leftwing swing:22 period:850
@animate spirit rightwing swing:22 period:850
@n
The creature's wings slowly moved.
@still spirit leftwing
@still spirit rightwing
@hide spirit
```

- Named IDs are optional, unique **at figure root** (nested instance targeting is deferred).
- `@animate figure instance swing:degrees period:milliseconds` applies to the following narration sentences and persists until `@still figure instance`.
- Only a bounded sine-wave rotation around authored pivot is implemented (0–75° amplitude, 250–3000ms cycle); at most eight active motions.
- Motion begins on Android `UtteranceProgressListener.onStart` and **stops on completion/pause/error**. It cannot advance spoken text or fake completion. On pause and Play replay, the period restarts from the current sentence's audible start; Stop returns to the original geometry. The viewer's reduced-motion flag disables this effect.
- Animations are procedural frame updates while audible speech plays; no keyframes, IK, animation during silence, audio waveform mouth-sync, named nested subpart overrides or rich motion curves yet.
- Actual visual correctness and 4GB-RAM performance remain phone-test gates.

Separate Android package: `com.visualbook.player.motion.v05`.

## v0.5 artifact

[Build verification and phone test guide](BUILD_EVIDENCE_MOTION_V05_2026-10-24.md): PR #14 merged, 164 regression assertions, Android CI built separate v0.5 APK. No v0.5 phone playback evidence yet.


## v0.6 render preview and correction report — experimental Android slice

v0.6 adds a **Preview** button to the Android player. It pauses audio and displays the exact immutable scene snapshot captured for a chosen spoken segment. Controls:

1. **Preview** opens silent inspection mode at segment 1, automatically enables the 0–100 stage grid (restored to the viewer's previous setting when closed).
2. **◀ / ▶** step through the scene states without playing or advancing the audio cursor; style, cast selection, scene object visibility and speech text reflect each segment.
3. **Tap the stage** to record stage coordinates and (when a visible custom figure's *bounding rectangle* is hit) its exact local artboard coordinates. An overlay crosshair marks the tap. This is bounding-box picking, not precise shape-silhouette picking.
4. **Copy** copies a deterministic human/AI-readable scene diagnostic report to Android's clipboard. It contains segment/source line, narration, style, background, visible geometry and placement, motion definitions, and the last tapped position.
5. **Edit shape** scrolls and selects the relevant \`@figure\` declaration in the integrated script editor. Apply script, then open Preview again to compare geometry revisions.
6. **Close preview** restores the earlier grid preference. Audio playback remains separate; choosing Play exits preview and resumes from the actual playback cursor.

There is no dependence on the network or a working TTS voice to inspect story scenes. The pure Java \`RenderInspector\` does not parse mutable Android UI elements, cannot execute untrusted scripts, and uses the same center-and-uniform-fit math as the renderer. This is **a human-assisted preview and AI correction handoff**, not yet an autonomous computer-vision feedback loop: the AI can use copied reports and user screenshots to improve geometry, but the app does not independently interpret its rendered pixels.

The device's class and screen size affect pixel positions, but not the 0–100 stage coordinate space or each \`WxH\` figure-local artboard. The original v0.1–v0.5 apps are unaffected: v0.6 has independent package ID \`com.visualbook.player.inspector.v06\`. Physical rendering and tap-navigation acceptance remain device-test gates.

## v0.6 verified build

[Render-preview APK evidence](BUILD_EVIDENCE_PREVIEW_V06_2026-10-24.md): PR #15 merged, 186 regression assertions passed, Android assembly succeeded, standalone APK extracted and checksummed. User should validate preview/clipboard/editor touch UI before claiming it works on device.

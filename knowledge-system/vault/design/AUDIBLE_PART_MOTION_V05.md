# VisualBook shape-part animation v0.5 — audible-bounded optional motion

**Implementation status:** EXPERIMENTAL. Actual source and tests shipped in PR #14; a real-device animation success is not inferred from successful compilation. User has confirmed the prior v0.4 app **works** on their phone, but did not separately verify all rig operations, smoothness or exact TTS visual sync.

## Goal

Let AI and human authors add gentle movement to already constructed body parts without rewriting shapes. A dragon's wings, a floating crystal, a robot's arm and a character's head should be able to rotate around a declared local pivot during a spoken segment. Retain the architecture: spoken audio completion moves the narration cursor; visual animation never does.

## Minimal script

```text
@figure spirit 100x100
@part wing 40x50 pivot:6,42
path M 6 42 Q 0 10 32 9 L 38 29 Z fill:#B7A3E4
@endpart
@use wing at:25,45 id:leftwing rotate:-20
@use wing at:75,45 id:rightwing rotate:20 mirror:true
@endfigure
@show spirit center
@animate spirit leftwing swing:25 period:850
@animate spirit rightwing swing:25 period:850
@n
The mythical creature's wings began to move.
@still spirit leftwing
@still spirit rightwing
@hide spirit
```

The `id:` gives a **root instance** a stable animation handle. `@animate` and `@still` are silent authoring directives. `swing` is an oscillating rotation amplitude in degrees, `period` a 250–3000 ms presentation-clock cycle. Animated rotation is *base rotation + amplitude × sin(2π × elapsed/period)*. Positive means clockwise in Android coordinates. Two wings can use different authored base angles or mirrored geometry.

## Deterministic playback model

- Script compiler validates figure and root-instance references, `swing` range (strictly above zero through 75 degrees), period (250–3000 ms), and at most 8 simultaneous motion declarations; no unbounded expressions, code execution or dynamic asset loading.
- The immutable `StoryCore.Segment.motions` snapshot captures persistent animation declarations at each utterance boundary, including across speakers and sentence splitting. `@still` removes the motion only for subsequent segments. Explicit user skip adopts the next segment's state without pretending audio finished.
- Android stage receives the snapshot *only when a valid audio `onStart` arrives*. The monotonic visual frame clock `SystemClock.uptimeMillis()` is subordinate to the audio callback: frames run only while the segment is marked as audibly playing. Existing TTS `onDone` and pause/error/stop paths end speaking state and stop motion.
- Pause→Play repeats the interrupted sentence from the beginning and restarts the motion envelope at time zero when playback actually begins; it is **not** sample-accurate phoneme animation. Silent sections cannot create autonomous motion in v0.5.
- The same base creature geometry is drawn throughout; motion is rendered via `Canvas.rotate(base + offset)` around the existing pivot transform. Unnamed and nested instances remain static and backward compatible.
- Viewer reduced-motion settings (currently exposed as a stage method, not a dedicated UI switch) disable the extra rotation; internal pacing is 48 ms while audible motion is active, approximately 20 FPS, reducing unnecessary redraw work on 4GB phones.

## Safety and architectural boundaries

Only named **root-level** instances support v0.5 motion; this avoids ambiguous selectors for repeatedly nested definitions. No keyframes, IK, inverse transforms, animated non-root joints, audio beat sync, background independent motion or morph interpolation. No motion-driven narration advancement. Future production forms should migrate these into typed IR cue definitions with clear asset/rig capability requirements and a genuine audiovisual timeline.

Defining a reusable geometry rig remains separate from defining animation instructions. This preserves the user goal: low/medium quality stories can have scripted reusable movement with just one line, while ultra-high custom stories can eventually provide complex motion trajectories.

## Validation checklist

- [x] Named root part handles and math helper implemented.
- [x] Segment motion snapshots and validation compiled.
- [x] Canvas application and separate v0.5 APK ID.
- [x] Unit assertions added for amplitudes, period, persistence, invalid instance IDs and plain text fallback.
- [ ] Android Gradle/JVM tests and knowledge doctor PASS final revision.
- [ ] v0.5 APK SHA/size recorded with installable artifact.
- [ ] Samsung SM-A066B real-world test: wings visibly move during voice playback, freeze at Pause/Stop, resume correctly with Play, and remain stationary when no animation is declared.
- [ ] Confirm animation frame pacing with longer paragraphs and multiple simultaneously animated parts on device.

See [Shape Rig v0.4](SHAPE_RIGS_AND_TRANSFORMS_V04.md) and [Android story player](../../../implementation/visualbook-player/README.md).

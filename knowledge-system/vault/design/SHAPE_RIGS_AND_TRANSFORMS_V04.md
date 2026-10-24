# VisualBook Shape Engine v0.4 — reusable parts and pivot-controlled transforms

**Status:** IMPLEMENTED EXPERIMENTAL PROTOTYPE. Real-device output and visual quality remain to be validated.

## Goal

An AI author should not manually recalculate dozens of vertex coordinates to make a mythical creature turn its head, grow a horn, or use multiple wings. The renderer needs **reusable geometry units** with explicit local coordinate systems, pivot anchors and composable transformation operations. Built-in art, user-supplied assets and constructed geometry remain separate acquisition strategies that may converge on the same stage object model.

## Coordinate frames and transforms

v0.3 established two independent spaces: *figure-local artboard* and *100×100 normalized stage*. v0.4 adds **part-local artboards** and reusable instances within the figure. Each frame uses origin (0,0) at upper-left and positive Y downward.

Given a part-local point `p`, part pivot `q`, parent anchor `a`, scale `s`, clockwise rotation `θ`, and optional horizontal mirror `m`, the authored transform is:

```text
parent(p) = translate(a) · rotate(θ) · scale(m ? -s : s, s) · translate(-q) · p
```

That sequence is applied as Android Canvas transforms, so part pivot `q` lands exactly at parent `a` regardless of rotation and scale. Instances of already-completed parts may appear inside other part definitions; parent parts contain independent local coordinates. Figure-level placement stays as `@show creature at:50,40 width:45`, preserving aspect ratio on the device.

## AI-friendly grammar

```text
@figure celestial 100x100
@part eye 20x20 pivot:10,10
circle 10 10 8 fill:#FFE277
@endpart

@part head 50x50 pivot:25,25
circle 25 25 23 fill:#B7A3E4
@use eye at:16,28 scale:0.8
@use eye at:34,28 scale:0.8 mirror:true
@endpart

@use head at:50,45 rotate:10 scale:1.1
@endfigure

@show celestial at:50,40 width:45

@n
A mysterious being appeared.
```

- `@part id WxH [pivot:x,y]`: create reusable local-coordinate geometry definition inside a figure. Default pivot is artboard center.
- `@endpart`: close and validate non-empty part.
- `@use id at:x,y [rotate:degrees] [scale:n] [mirror:true|false]`: append an instance at the requested **parent-local** pivot location. Optional fields can be in any order, once each.
- Part definitions must be completed before they are referenced. This prevents cycles by construction and supports nested compositions of older part definitions.
- Drawing is deterministic, ordered exactly by root and nested node order, and adds no spoken dialogue or artificial story time.
- Unchanged features: v0.3 circle/triangle/polygon/absolute Bézier paths, stage `@show`/`@hide`, v0.2 `@chibi`/`@end`, voice assignments and `@c3` cast grammar. **Global chibi does not automatically deform authored custom geometry yet.**

## Validations / strict budgets

| Limit | v0.4 |
|---|---|
| Part definitions per figure | 12 |
| Total primitive/instance nodes, including unused part definitions | 64 |
| Expanded scene-cost cap per built part and rendered figure | 256 |
| Recursive instance hierarchy depth | 4 |
| Part / figure artboard size | 16–1024 per axis |
| Rotation | finite ±360 degrees |
| Uniform scale | finite 0.1–4 |
| Mirroring | X axis around the declared part pivot |
| Anchor and pivot coordinates | finite and within the immediate parent/part artboard |

Expansion costs include referenced part instances recursively. Up-front bounds preclude runtime reference cycles, unbounded recursion and accidental exponential redraw. Repeated drawing does not rerun the text compiler. The Android renderer caches paths by immutable shape identity.

A malformed command rejects the story with a line-numbered error. No JavaScript, Java, SVG/XML parsing, executable callbacks, file access, internet access or graphics shader code is evaluated from the script.

## Architecture implications

- The reusable group DAG is a first **geometry scene graph**, not a full skeletal rig. It is data-only, referenced by a `VectorFigure` inside the typed `StoryCore.Story`.
- A production rig can add stable part IDs, keyframed per-instance overrides, skeletal constraints, IK, material slots, style adapters and imported bitmap/vector asset handles without changing basic local coordinate semantics.
- **Do not conflate shape description and storytelling**: the creator may store geometry separately and reference by stable IDs in a future storypack.
- Style switching across normal/chibi and arbitrary custom renderables requires explicit rig-profile compatibility or fallback behavior; current v0.4 custom vector geometry retains its authored shapes.

## Test and acceptance gates

- [x] Pure JVM primitive-geometry parser and figure builder extended with `@part`/`@use` and transform calculations.
- [x] Android Canvas renderer recursively visits reusable parts and composes local transforms.
- [x] Built-in demo uses independently authored eye, horn, wing and composed head to form a complex mythical creature.
- [x] Added strict budget/malformation/transform regression assertions.
- [ ] Latest branch has **passing** portable JVM tests and Android APK build.
- [ ] Samsung SM-A066B confirms the actual new figure appears correctly, is layered properly, preserves a stable pivot when rotated and mirrored, and doesn't stall the app.
- [ ] A larger stress test with 100+ expanded elements maintains usable frame pacing and memory on 4GB devices.
- [ ] Later project: structured animation cues to override transforms over speech-boundary time, and per-actor style adaptation.

See [v0.3 coordinate foundation](VIRTUAL_CANVAS_GEOMETRY_V03.md), [implemented core](../../../implementation/visualbook-player/story-core/src/main/java/com/visualbook/core/VectorFigure.java), [Android renderer](../../../implementation/visualbook-player/app/src/main/java/com/visualbook/player/StoryStageView.java).

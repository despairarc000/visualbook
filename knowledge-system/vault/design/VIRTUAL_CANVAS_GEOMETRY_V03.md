# Virtual Canvas Geometry — VisualBook v0.3 experimental contract

**Status: IMPLEMENTED prototype, NOT production geometry specification.** The user proposed: "choose a point on our own screen that's (0,0), specify 100×100, create circle at 77,56 and triangle at..." This captures the core requirement: AI can author **arbitrary controlled shapes**, not only pick existing dragon/person/preset objects.

## Architecture decision: independent coordinate spaces

1. **Figure-local artboard.** Example `@figure spirit 100x100` establishes a 100×100 2D virtual space, origin (0,0) at upper-left, positive X right, positive Y down. Child shape coordinates are relative to that space and do not depend on the phone display.
2. **Normalized story stage.** The actual `StoryStageView` supports its own conceptual 100×100 space. `@show spirit at:77,56 width:27` places the figure's **center** at 77% stage width and 56% stage height, reserving 27% stage width; it does not relocate any circles inside the spirit.
3. **Actual Android screen.** Stage view rectangle may be portrait or landscape. Mapping uses a **uniform meet scale** `min(allowedWidth/figureWidth, allowedHeight/figureHeight)`; the figure is never stretched, preserving circular eyes and character proportions. Stage UI/captions are separate from the stage viewport.
4. **Future hierarchy.** A shape family can nest part-local coordinate spaces for head, body, wings, etc., then apply translations/rotations around stable pivots. Separate group transformations, inverse picking, animation bones, and clipping are explicitly *future* features, not implemented in v0.3.

This is analogous to SVG's [viewBox](https://developer.mozilla.org/en-US/docs/Web/SVG/Reference/Attribute/viewBox) and [preserveAspectRatio](https://developer.mozilla.org/en-US/docs/Web/SVG/Reference/Attribute/preserveAspectRatio) concepts; **VisualBook does not import/execute SVG or HTML**.

## AI-friendly initial script

```text
@bg neutral

@figure starbeast 100x100
triangle 10,80 48,10 88,80 fill:#7356A7 stroke:#49336E width:2
circle 77 56 10 fill:#FBCB6B stroke:#6C4498 width:2
path M 12 54 C 15 16 64 16 87 50 stroke:#F5DBAB width:3
@endfigure

@show starbeast at:77,56 width:27

@n
May lumitaw na kakaibang nilalang.

@hide starbeast

@c1
Saan ito pumunta?
```

Inside `@figure`, each primitive is a data-only command. Chosen geometry, paths, colors and layer order define the visual. The example doesn't guarantee a sophisticated creature on its own; **quality depends on the supplied geometry**. Built-in templates and imported image assets remain alternative render sources, not prerequisites for handcrafted geometric shapes.

### Supported geometry, precisely

| Primitive | Author command | Meaning |
|---|---|---|
| Circle | `circle 77 56 10 fill:#FFE277` | center x,y and radius |
| Triangle | `triangle 10,80 48,10 88,80 fill:#7356A7` | three absolute vertices |
| Polygon | `polygon 10,10 20,15 30,10 45,40 fill:#8064D2` | 3–32 vertices |
| Path | `path M 10 40 C 25 0 65 100 90 40 stroke:#663399 width:2` | absolute M move, L line, Q quadratic, C cubic, Z close |

Paint tokens: `fill:#RRGGBB`, `stroke:#RRGGBB`, optionally `#AARRGGBB` and `width:n`. `none` is supported when the other paint remains visible. Draw order is definition order. Artboard dimensions 16–1024 units; 64 parts/figure; 8 figures/story; 4 simultaneously visible. Currently coordinates must be finite and inside artboard bounds; circles must fit. Paths are deliberately a strict **subset**, not full SVG path syntax. Geometric definitions do not execute code, scripts, XML, URLs or network requests.

### Show/hide and stage coordinates

- `@show starbeast center` (also `left` or `right`) is the easy path with stable staging defaults.
- `@show starbeast at:77,56 width:27` provides precise percentage stage placement (x,y between 0 and 100; width 5..70). This is a figure-level *position*, not an individual shape's local position.
- `@hide starbeast` ends figure visibility.
- Currently the "at" anchor is the **center** of the figure, not top-left; keep authoring and rendering consistent.
- For debugging, the first Android player adds a **Show 0..100 coordinate grid** toggle under Voices. These grid coordinates refer to the stage area, not the whole phone or caption UI.

## Narration and state semantics

`@figure`/`@endfigure` define a figure without creating a speech event. `@show`/`@hide` update a compile-time persistent scene-visibility state, copied into each `StoryCore.Segment.visibleFigures`. These changes become visible at the next **validated audible `onStart`** event; the TTS callback clock remains authoritative for speech. Next-to-last actions without a following utterance do not have an independent silent cue clock yet. Speech pointer pause/restart/skip must not be faked by shape events.

`@chibi`/`@end` remain scoped style tags and are not conflicted with `@endfigure`. The prototype's chibi renderer only transforms built-in actors, not the authored vector figure. That is a declared limitation; later style adapters need part semantics or explicit transform rules.

## Quality, security and UX boundaries

- Validate script before execution: duplicate IDs, unknown figures, invalid placement, malformed color/path, non-finite dimensions/positions, command budgets.
- Keep compiled geometry immutable, cache Android paths between frames, don't load bitmap/network data implicitly.
- Small artboards are logical units, **not a resolution limit**: using `100x100` never makes the rendered image a 100-pixel raster.
- Figure geometry and stage positions are reproducible from script text; creator and AI can reason about placements using the grid and deterministic results.
- A natural-language prompt like "render a cosmic entity" is **not** compiled directly by this module. AI needs to produce controlled vector geometry (or an imported asset), and the app validates that geometry.
- Bounds validation does not guarantee a beautiful or anatomically coherent creature, nor collision-free stage layout. Auto-staging, pose rigs, group transforms, bitmap/vector import, natural-language-to-vector generation, richer materials, keyframes and true silent cue scheduling remain future phases.

## Immediate acceptance

- [x] Implement typed virtual figure geometry and stage placement in portable Java and Android Canvas.
- [x] Add core regression tests for bounds, transforms, invalid tokens, script snapshots and chibi coexistence.
- [ ] CI Android compilation and JVM tests pass on final SHA.
- [ ] Real user runs v0.3 side-by-side with working v0.1/v0.2 and confirms the mythical figure appears at the expected coordinates.
- [ ] Check stage grid, edge clipping, portrait/landscape mapping, stop/restart and several generated complex paths on SM-A066B.

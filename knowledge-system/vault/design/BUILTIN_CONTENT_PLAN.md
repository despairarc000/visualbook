# Generic Built-in Content and Asset Production Plan

**Status:** ACCEPTED typed/default behavior; finished artwork/sounds not yet created. The [101-ID catalog](../history/v0.2/builtin_asset_catalog_seed.json) is a draft logical registry, not included playable media.

## Style direction

Working shared look `storybook-flat-v1`: consistent flattened 2D illustrations, readable proportions and colors, interchangeable silhouettes, simple face/pose variants; avoid assembling incompatible art styles. Choose and test one palette, contour thickness, camera convention and pivot/baseline standard before producing the rest.

## Minimum usable content P0

- Procedural neutral/gradient stage, always renderable without external media.
- Two distinguishable generic characters `c1`, `c2`, with deterministic accessible accent colors and labels, no sex assumptions if omitted.
- Speaker highlight and minimal idle/speaking states that need no face rig. Narrative remains intelligible with silhouettes.
- Coherent classroom, street, living room backgrounds as first illustrative content, plus neutral. Test focal points in portrait (9:16), landscape (16:9) and tablet (4:3).
- Subtitle treatment: speaker label, accessible font/contrast/size, safe-area placement and visibility toggle.
- Simple fade/crossfade, reduced-motion equivalent, missing-asset immediate state fallback.
- Music/ambience off unless requested. Initial explicit SFX: door, footsteps, knock; no random automatically inferred sounds.

## Expanded taxonomy (planned, no media yet)

| Type | Original v0.2 logical count | Default behavior |
|---|---:|---|
| background | 16 | cover/crop or neutral extended fill |
| character | 7 | silhouette / generic compatible whole sprite |
| character-accent | 8 | deterministic accent palette |
| expression | 11 | compatible neutral if absent |
| prop | 14 | world coordinate + anchors |
| procedural-effect | 11 | soft/none fallback |
| sfx | 22 | silence fallback |
| ambience | 6 | silence fallback |
| music | 3 | silence fallback |
| ui-sfx | 3 | user-configured silence fallback |

## Asset contract

`id, type, role, styleId, license/provenance, fileHash?, size/dimensions?, crop/focal point, safe region, ground baseline, pivot/anchors, transparent bounds, quality tier, variant IDs, fallback IDs, media status`.

For production binary, source bytes, attribution/license and hash are mandatory. A request `bg.school.classroom` must never return a sprite or random concept image. Cyclic fallbacks, missing logical IDs and incompatible rigs are compile/asset-lint errors.

Fallback layers: approved user override → explicit story asset → compatible installed shared asset → bundled generic → procedural shape/neutral color → legible subtitle/text. Required content semantics cannot be overwritten by fallback.

## Scene model

Layer roles back to front: background, world/midground, depth-sorted characters + props, foreground, scene effects, protected caption/UI. Directive order cannot force a background in front of actors. Silent environment transitions are allowed if anchored to narrated completion or explicit silent beat.

## Responsive design

World coords independent of viewport and screen UI. Never stretch sprites/backgrounds. Place key characters with responsive anchors, preserve faces/feet and subtitle reserved area. Art can contain extended bleed beyond screen for varying aspect ratios. Fallback to fit + neutral extension when crop cannot safely preserve important content.

## Production pipeline and quality check

Role brief → create media → style review → normalize anchors/padding/camera regions → test rotations and subtitles → compress/check decoded memory → provenance/license review → hash + manifest registration → emulator/device visual check → fallback resolution tests. No media file is considered present just because a manifest names it.

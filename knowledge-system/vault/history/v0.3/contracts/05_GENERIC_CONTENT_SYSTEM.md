# 05 — Generic Background / Character / Props / Effects / Sound Library

## Fundamental requirement

A valid story with no specified scenes/assets/voices must produce a readable, intentional visual presentation. If narration is available, show an illustrated/captioned story; if not, remain readable with an actionable voice-install message. All default media is **typed, coherent and reliably replaceable** by explicit story or user overlays. The 101 catalog items inherited from v0.2 are `PLANNED_NOT_INCLUDED`, *not* files that exist.

## One coherent design style (working proposal)

`storybook-flat-v1`: readable simplified 2D cartoon, moderate contrast, soft shading, limited outlines, simple modular faces, few animation frames, easily generated/edited by AI or human. Choose one palette system and coherent silhouettes rather than mash up photo-real scenery with unrelated sprites. Generic assets should communicate roles **without assuming specific fictional identities**.

Production asset manifest metadata:
```
resourceId, resourceType, styleId, sourceHash, path?, createdBy,
licenseId/licenseText/provenance, dimensions, pivot, bounds, groundY,
variants, bleed, safeRegion, focalPoint, fallbackIds, qualityTier, locale?
```

For each asset, a logical ID must have an explicit renderer role and agreed coordinate behavior. Until media is produced, `sourceHash/path` remain absent and `status=PLANNED`; app can fall back to procedural drawing, not pretend the asset exists.

## First production slice (build this before 101 finished resources)

| Priority | Asset class | Smallest actual content | Behavior |
|---|---|---|---|
| P0 | Universal visual | Neutral gradient + abstract silhouette stage | Always usable; procedural asset, no external media |
| P0 | Generic humans | 2 visibly distinct silhouettes/flat sprites + name labels | Stable c1/c2 IDs, no gender assumptions if unspecified |
| P0 | Basic poses | neutral / speaking / listening | During voice, pulse or mouth change if present; else subtle focus/highlight |
| P0 | Backgrounds | classroom, street, living room + neutral | Every bg has focal point and background/ground safe data |
| P0 | Subtitles | speaker label + legible caption surface | Explicit on/off independent of text content |
| P0 | Transitions | crossfade, basic scene fade; zero-motion alternative | No custom video necessary |
| P0 | Sound system | quiet/none default; 3 effects: footsteps, door, knock | NEVER infer audible sound solely from verb in text |
| P1 | Character expressions | happy, surprised, confused, sad, sleepy, angry | Reusable face layers or whole sprites with same anchor |
| P1 | Environmental audio | rain, birds, room tone | Off unless story requests or listener opts in |
| P1 | Props | chair, desk, door, book | Assign world-layer/occlusion role and anchors |
| P2 | Bigger library | additional backgrounds, costumes, rigged actors, music | Expand after style/compatibility tests |

## Current 101-ID taxonomy (seed preserved)

`background:16`, `character:7`, `character-accent:8`, `expression:11`, `prop:14`, `procedural-effect:11`, `sfx:22`, `ambience:6`, `music:3`, `ui-sfx:3`. These are separate logical resource types. Some generic character/expression terms may be refined: `talking` and `listening` are runtime *activity* states, not emotions, though the simple authoring interface may expose both colloquially.

## Asset ordering and coordinates

Typed layer stack (back-to-front): `background -> world/backdrop -> midground -> characters/props (depth-sort locally) -> foreground -> scene effects -> speech overlays -> app UI`. Do not allow author directive order to circumvent layer classes. Permit `character behind desk` through same-world occlusion rather than inserting it into background. Allow full-scene transition overlay above ordinary scene but below protective system UI unless transition explicitly hides scene UI.

Three coordinate spaces: **world** (author placement), **camera** (viewport/crop/pan), **screen** (captions/UI/safe insets). Default no stretching. Use aspect-preserving cover crop for background and **composition-aware adaptive placement** of characters. The crop behavior `FillViewport`/`ExtendViewport` analogs cannot preserve actor faces on their own; layout engine must respect `faceSafeBox`, `captionReservedArea`, `groundBaseline`, `speakerFocus`, `minimumVisibleArea`, `min/max scale`. Compose UI handles system insets and portrait/landscape separately.

Background source art: prefer larger-than-canonical logical canvas with reasonable bleed on all sides; default focal point may vary by screen ratio. Very tall portrait and wide landscape can choose composition override when supplied. If an asset cannot crop without losing important content, render aspect-preserving fit with neutral extension rather than distort.

**Example semantic layout**: one character center; two split left/right; three stage front-center speaker, others recessed; more than three focus active speakers and indicate off-screen participants with labels. This is a **presentation policy**, not new story plot.

## Default character resolver

`Character(cId)` owns identity, name? sex? (optional), age? (unset), visual profile, voice intent, emotion, activity, layout. First `@c2m` registers `c2` and `sex=m`; `@c2` later references same ID. Character names inferred by text are suggestions **not guaranteed facts**; explicit `@character c2 name="Pedro"` is binding. No declared sex -> neutral generic character. Sex label can influence a default preset but never restrict a user to certain voice profiles or enforce appearance stereotypes.

For cast with no custom sprites, use consistent silhouettes generated from a small palette + named labels and stable visual accent (`hash(cId, storyId)` constrained to accessible distinguishable palette). Label speakers even when two are visually similar. If no character on scene yet, speaker panel alone still identifies who talks.

## Typed fallback system

1. user-approved customization (if relevant, compatible) 2. story asset (if valid) 3. installed shared asset 4. bundled asset 5. procedural fallback 6. minimum neutral/text. Runtime preferences determine precedence among story and user layers; semantic types and immutable story logic always win over them.

**Examples:**
- background `bg.school.classroom` -> built-in `bg.school.classroom` -> `bg.neutral` -> procedural gradient.
- character asset -> generic role-compatible whole sprite -> silhouette + name.
- expression `confused` -> compatible pose/face swap -> neutral + subtle focal animation (avoid spoofing an unsupported expression).
- sound missing -> no sound, cue considered fulfilled; NEVER substitute random noise or stop narration.
- voice missing -> appropriate installed/offline compatible voice -> alternate allowed voice -> user-visible text-only mode. Do not claim narrated fallback if it produced no audio.

Resolver requires exact asset role/type and must reject circular fallback chains, missing target IDs, unsupported animations, mismatched portrait/sprite attachment profiles and absent license metadata for shipped binary resources. Explicitly test the 101 entry seed.

## Sound design safety and readability

Narrator/dialogue is primary. Default environmental sound volume = silence unless script/user wants it. Narration mix bus separately controlled from SFX/ambience/music. Duck environmental bus under speech and enforce hard limiter to avoid clipping. Accessibility can disable sounds and reduce motion without altering narration content. Procedural speaker highlight is safer than attempting unsynchronized lips by default.

## Authoring production workflow

For each media file: choose logical role -> generate/draw -> ensure palette/style match -> position anchor/transparent padding/face box -> generate day/night variants if needed -> review at 9:16, 16:9, 4:3 -> compression/alpha test -> audit license/provenance -> assign checksum -> register catalog -> run fallback checker. AI-generated media is a *requested candidate asset* until actual file bytes exist and pass validation.

## External reference material

- https://kenney.nl/support (CC0 policy of game asset pages; verify per-pack license)
- https://libgdx.com/wiki/graphics/viewports (fit/fill/extend trade-offs)
- https://developer.android.com/topic/performance/memory/guide/bitmaps (decoded memory)
# 02 — Built-in Generic Content Library (Proposed v0.2)

## Purpose and hard rule
A plain multi-speaker story must remain visually coherent and listenable without any imported art, voice configuration, music or timed cues. **Coherent** means one recognizable art style, consistent spatial proportions, predictable behavior, readable subtitles, and semantic fallback; not high-fidelity motion for its own sake. The builtin pack is a _designed starter world_, NOT a random set of placeholder assets.

Assets in `builtin_asset_catalog_seed.json` are **logical catalog entries only**. They are a plan; actual artwork/audio must be produced or licensed and quality-checked.

## A. Art direction
**PROPOSED**: neutral 2D storybook/visual-novel aesthetic with simple shapes, tasteful flat shading, restrained motion, and an intentionally modest palette. Generic silhouettes should be visually distinct without falsely claiming a detailed identity. Background scenery and character proportions should match (e.g., don't combine photoreal interiors with tiny pixel-art figures). User-supplied custom scenes are allowed to override style mismatches, with preview warnings where useful.

Palette tokens: `paper-light`, `sky-day`, `sky-evening`, `sky-night`, `earth`, `leaf`, `interior-warm`, `interior-cool`, `character-accent-1...8`, `caption-high-contrast`. Do not bake subtitle text into background images.

## B. Background asset contract
```
BackgroundAsset {
  id, family, localeTags?, styleId, sourceLicense,
  layers: [rear, middle?, foreground?],
  authoredAspectRatio, sourcePixelDimensions,
  normalizedFocalPoint:[0..1,0..1],
  safeSubjectRegion?, groundBaseline?, horizon?,
  cameraPanBounds?, allowParallax, isTileable,
  environmentPalette, variants:[day,night,rain,...],
  renderPolicy: cover|contain|extend|procedural,
  fallbackId
}
```
- **Background first** is a semantic depth rule, not command order. Player HUD/subtitles draw after scene visual layers.
- Author backgrounds with safe focal regions and meaningful extra bleed beyond canonical landscape frame. Do not assume adding width alone covers portrait; portrait may need dedicated reframe or extended vertical artwork.
- Essential content must stay in the safe subject region; allow background clipping of expendable edges. Use focal point and background layering to avoid distorted/stretch images.
- For unsupported aspect ratios, progressively use `cover` with safe focal crop -> `extend` supported layers -> procedural extension/soft blurred fill -> letterbox as necessary. Absolutely no nonuniform stretching.
- Asset metadata declares whether extending/tiling is permitted; do not repeat buildings/faces naively to extend scenery.

## C. Minimum background families
Proposed **initial art production shortlist**: 16 base compositions + selected low-cost day/night/color variants. Do not create 100 full-resolution bitmaps before testing memory.

| Built-in family | Reference ID | Story uses | Suggested variation |
|--|--|--|--|
| Plain neutral | `bg.neutral` | Unknown context; guaranteed fallback | light, dark, warm |
| Abstract color gradient | `bg.abstract` | Poem, inner monologue | palette driven |
| Classroom | `bg.school.classroom` | Juan/Pedro school scenes | day/evening |
| School hallway | `bg.school.hallway` | Hallway conversations | day |
| Home living room | `bg.home.living` | Family, conversation | day/night |
| Kitchen | `bg.home.kitchen` | Cooking, family jokes | day |
| Bedroom | `bg.home.bedroom` | Sleeping, waking up | day/night |
| Neighborhood street | `bg.outdoor.street` | Travel, meetings | day/night |
| Plaza / barangay | `bg.outdoor.plaza` | Community scenes | day/evening |
| Market / small shop | `bg.commerce.market` | Buying, bargaining | day |
| Office | `bg.work.office` | Workplace stories | day/night |
| Park | `bg.nature.park` | Walks, casual talk | day/evening |
| Countryside | `bg.nature.countryside` | Province, fields | day |
| Road / bus stop | `bg.travel.roadside` | Commute, waiting | day/night |
| Hospital / clinic generic | `bg.service.clinic` | Routine medical-setting stories | day |
| Dream / mystery abstract | `bg.abstract.dream` | Weird/mysterious stories | palette driven |

These are semantic fallback families. A tag like `market` should map to a compatible scene, not pick any image simply because a file name contains 'market'. For a known conflict, `bg.neutral` is always valid.

### Production strategy
Build the first 4 high-impact compositions (`neutral`, `classroom`, `street`, `living`) as finished assets; use abstract fallback for other catalog IDs until verified. Then complete the 16 families in measured batches. Each artwork should have a standard metadata sheet and portrait/landscape snapshots. Make background layers optional: initial flat image, later decomposed for parallax without changing its asset ID.

## D. Character starter library
The minimum cast must be **recognizably distinct**, not necessarily gender-encoded. Proposed basic visual variants:
- `char.generic.neutral` — unspecified look, general-purpose silhouette.
- `char.generic.masculine` — optional style match for author-declared metadata.
- `char.generic.feminine` — optional style match for author-declared metadata.
- `char.generic.child`, `char.generic.adult`, `char.generic.senior` — only if age is explicitly provided; default to ageless/neutral shapes.
- `char.generic.group` — crowd fallback; should not imply all crowd members are active speakers.

Create **eight distinct character accents** / visual differentiation tokens for `c1...c8`, based on silhouettes + palette + label, not false identities. For 9+ characters, support deterministic color and shape cycling with speaker labels; warn if an overcrowded scene cannot show all cast members legibly. Character count need not equal all visible characters in every scene: visible cast should be conservative and reflect explicit staging, active dialogue and context.

### Flat sprite kit v1 (each style)
- Essential poses: `neutral`, `talk`, `listen` (or simple highlighting), `idle`, `offscreen` (logical only).
- Optional expressions: `happy`, `sad`, `surprised`, `angry`, `confused`, `sleepy`, `laughing`, `worried`.
- Neutral fallback expression MUST always exist.
- Mouth variants: closed/open generic. Blink may be procedural eyelid overlay or alternate pose; avoid complex rig commitment.
- Animation primitives: appear/disappear, crossfade, small vertical breathe, blink, gentle sway, talk pulse, emphasis/shake, focus/defocus.
- Preserve a stable `feetAnchor`, `faceAnchor` and comfortable dialog occlusion regions. A simple label panel should be available when sprite art is missing.

### Layered sprites (later)
Use compatible part families sharing anchor schema: base/head/eyes/mouth/hair/outfit/accessory. Declare rig-profile ID. Mixing independently authored pieces without matching profile is invalid and should fall back to a flat sprite.

## E. Props, foreground and effects
**Props:** desk, chair, table, door, window, bed, phone, book, bag, chalkboard/signboard, vehicle silhouette, tree, market stall; initially prefer background-embedded props or simple vector overlays to avoid object explosion.

**Foreground occluders:** desk front, curtain, tree branch, doorway, fence. Their `occlusionClass` and compatible backdrop/placement tags must be declared.

**Effects (procedural first):** fade to/from black, cross-dissolve, pan/zoom, highlight current speaker, small camera shake, light rain overlay, dust/sparkle, speech emphasis, idle drift. The renderer should compute these via tweens/shader passes or small repeatable sprites, not require video assets.

**Accessibility:** if reduced motion is enabled, replace strong shake, flash, fast zoom and prolonged parallax with crossfade/static emphasis; flashing effects require hard safety limits.

## F. Sounds: distinguish categories
| Audio type | Example logical IDs | Default policy |
|--|--|--|
| Foley / action SFX | `sfx.step.wood`, `sfx.door.open`, `sfx.book.drop`, `sfx.knock` | Only if author explicitly requests or cue is semantically reliable |
| Environmental ambience | `amb.classroom.soft`, `amb.rain`, `amb.street.day`, `amb.park.birds` | Very low volume, optional/off by default in generic stories |
| UI feedback | `ui.tap`, `ui.chapter`, `ui.choice` | Optional player UI; avoid interfering with narration |
| Emotional stingers | `stinger.comedy`, `stinger.mystery` | Opt-in, never invent emotional interpretation from plain text |
| Music beds | `music.calm`, `music.playful`, `music.tense` | Off by default unless explicitly authored/user enabled |
| Voice/narration | `voice.default.fil`, `voice.profile.c1` | Essential if speech playback is desired; installed capability required |

A story saying 'the door slammed' does not automatically authorize a loud startling SFX. Generic inference should err on the side of silence. Expose separate narrator/music/ambience/effects gains, duck music under speech, cap peak output, and honor user's mute/quiet choices.

### Starter audio inventory
Target a small, curated license-reviewed **20–30 effect clips** to start (doors, steps, page turning, knocks, light crowd, rain, school bell, soft transition). Then potentially add ~8 looping ambience tracks and a few optional music beds. Don't invent exact binary sizes before sourcing and encoding. Use compressed streamed audio where appropriate, tiny decoded clips where latency matters. Avoid codecs/headers unsupported by chosen Android audio backend.

## G. Default visual choreography
- At story start: show safe neutral scene immediately, then replace with compatible declared/inferred background when ready. Avoid black frames.
- Speaker starts: visible actor gets subtle scale/highlight or talk pulse; inactive actors listen/idle; subtitle shows authored text.
- Speaker ends: return to neutral state, hold until next utterance becomes audible.
- First character referenced: implicit character exists, but only show if scene-composition heuristic or authored direction justifies it. In plain script, show active speakers and immediate counterpart at most; refrain from unlimited phantom people.
- Between chapters: default crossfade or simple cut with transition duration bounded and narration held until scene-ready.
- Missing character art: distinct named silhouette/card; missing background: neutral/abstract gradient; missing animation: static final state; missing sound: silence.

## H. Responsive composition rules
- Stage coordinates normalized around conceptual world anchors; UI/HUD always screen-space.
- Group visible characters by active speaker and predicted speaking order, with adjustable left/center/right arrangements and neutral standing baseline.
- Portrait: reduce horizontal spread, allow staggered side compositions, larger focus on active speaker, reserve vertically stable subtitle region.
- Landscape: expand stage world and allow more environment art, keeping characters at manageable relative sizes.
- Accessibility safe areas, screen cutouts and user-configured captions win over background aesthetics.
- Layout failures are detected: illegal overlap with captions, face crop, props covering narrator, unable to satisfy min character size. Recover by simpler layout (e.g., single active speaker + labeled other-speaker chip) rather than stretch.
- On rotation, recompute stage transforms **from scene semantic state** and keep narration cursor unchanged.

## I. Builtin asset catalog schema / provenance
Proposed common fields:
```
AssetCatalogEntry {
 id, kind, role, styleId, version, languageTags?,
 tags[], variants?, sourceAsset?, licenseId, attribution?,
 intrinsicSize?, colorProfile?, anchors?, focalPoint?,
 safeArea?, fallbackChain[], qualityTier?, memoryHint?
}
```
- `sourceAsset` null means planned or procedural entry, not an actual asset. Never claim it's bundled until the binary exists and passes validation.
- License metadata may require per-pack attribution, redistribution permission, derivative restrictions and whether an asset can be embedded in user-exported stories.
- Build the catalog and runtime resolver **before** mass asset generation. Any downloaded third-party asset needs source/license review and stylistic vetting. For example, Kenney explicitly publishes many CC0 game and audio packs, but visual compatibility must be checked rather than assumed.

## J. Generic content completion criteria
- TXT with no explicit cast = narrated text, neutral background, captions, no crash.
- @c1/@c2 with no additional metadata = two distinct visible/identifiable characters and voices when compatible voices exist; never falsely claim distinct actual voice timbres if only one voice is installed.
- Missing preferred visual/sound triggers no crash, no invisible black screen.
- Every built-in background works on portrait, landscape and extreme device aspect ratios via reframe/fill/letterbox fallback.
- Every character style exposes an actual neutral fallback, speaker label and layout anchors.
- No visual asset can render above subtitle/system UI by accident; no strong motion over reduced-motion settings.
- Built-in pack includes provenance/license manifest and content-hash validation.
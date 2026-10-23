# Scoped art styles — authoring and rendering contract v0.2

**Status:** ACCEPTED as **experimental first-story v0.2 feature**, not production VisualBook schema. Implemented code: [pure grammar](../../../implementation/visualbook-player/story-core/src/main/java/com/visualbook/core/StoryCore.java), [Android stage](../../../implementation/visualbook-player/app/src/main/java/com/visualbook/player/StoryStageView.java), [Android callback adapter](../../../implementation/visualbook-player/app/src/main/java/com/visualbook/player/MainActivity.java).

## User experience

Like how anime inserts an exaggerated chibi reaction into an otherwise normal-looking scene, authors should be able to **temporarily change the visual presentation** without replacing character identities or inventing new asset references.

Minimal example:

```text
@c1m
Pedro, nasaan ang assignment mo?

@chibi
@c2m
NAKALIMUTAN KO!

@c1
ANO?!
@end

@c2
Joke lang.
```

`@chibi` is an easy alias for `@style chibi`; `@end` closes **the most recently opened style scope** and restores the previous mode. `@style normal` / `@style cartoon` selects the current baseline style *within a temporary scope*; it is not a magical exit command.

## Current grammar

- `@chibi` opens global chibi scope.
- `@style chibi` also opens global chibi scope.
- `@style normal`, `@style cartoon` open temporary normal/cartoon scope.
- `@end` closes most recent visual-style scope and restores previous style (nested scopes up to 8).
- Markers must be on their own line, not embedded within spoken words. No unclosed block, unmatched `@end`, unknown art mode or argument accepted.
- Existing `@n`, `@c1m`, `@c2f`, `@bg classroom/neutral` keep original meaning. In plain `.txt` mode, style-looking text stays **literal narration**, not an instruction.
- **Style scope is global** in v0.2: all visible characters receive the same rendering profile. Per-actor style transforms are postponed to a general `@style chibi target:c2` design with typed selectors.

## Data model and narration clock

This experimental parser compiles styles into each immutable `StoryCore.Segment.artStyle` value. Visual style is **not a TTS utterance** and doesn't consume story playback time. A well-formed `@chibi`/`@end` block can cover any number of speakers, lines or sentences.

The Android stage changes style in the current utterance's validated `onStart` callback and does not cause `PlaybackState.audibleDone` itself; `onDone` advances the same logical cursor as before. Seek/restart can derive the style deterministically from the segment at the cursor; stale audio callbacks are already filtered through generation checks. The viewer's settings (reduce motion, style substitutions) should remain separate from the script's authored intent.

## Visual renderer version 0.2

Only **two genuinely rendered profiles** exist so far:

- `normal`/`cartoon`: previous procedural character proportions, eyes and mouth.
- `chibi`: reshaped large head and eyes, shorter body and limbs, blush cheeks, pastel stage accents, speaking emphasis.

No imported layered sprite rigs, image-generation pipelines, anime packs, comic packs or per-character transforms are implemented. Future renderer architecture uses a **style pack registry** with compatible semantic slots (character identity, facing, pose, expression, costume, body rig and lighting), to swap art families while preserving story continuity. A renderer may degrade gracefully if an optional style pack is missing but must declare whether it substituted art and should not silently treat unsupported required styles as successful.

## Compatibility and semantic expectations

1. Style scope changes **visual presentation**, not the identity, voice assignment, physical location or scene membership of a character.
2. An authored sex hint isn't an art-style selector; speakers remain valid implicit cast members across a style transition.
3. The normal→chibi→normal transition must be deterministic after stop, pause and manual skip; nested scopes restore parent values exactly.
4. Boundary changes are attached to authored **next speech segment's audible start**. For future deliberate *silent* visual style changes, introduce typed cues plus a separate timeline contract; do not fake speech completion.
5. Fast style changes should reuse assets and rig states; avoid loading large bitmaps on UI thread. For low-end Android test frame time, allocations, memory and layout overlap.
6. Production architecture should encode `style_scope_start`/`style_scope_end` or compiled effective `visualStyle` in the canonical IR, not parse ad-hoc source lines in the renderer.

## Next test gates

- [ ] User installs standalone v0.2 APK and confirms normal→chibi→normal on Samsung SM-A066B.
- [ ] User checks `@c3` within a chibi block, nested styles, pause/replay, and original v0.1 normal story compatibility.
- [ ] Verify readable scaling, FPS and peak memory on 4 GB RAM device.
- [ ] Validate portable character pack compatibility before claiming support for multiple non-procedural art families.

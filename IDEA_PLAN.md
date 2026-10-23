# VisualBook — Idea Plan

**Record:** canonical product vision. **Status:** CONFIRMED intent; designs following it are not necessarily implemented. **Last reconciled:** 2026-10-23.

## Product purpose

Create a genuine offline-first mobile runtime that turns portable narrative material into **a video-like illustrated listening experience** without pre-rendering full video. It is a story player, not just an ebook reader, slideshow, speech demo, or a visual-only clone of visual-novel tools.

## User journey

1. Import plain text, a minimally annotated script or a self-contained story package.
2. Preview the text, chapters, speakers and automatically resolved assets.
3. Play: narration speaks through available compatible TTS or pre-recorded media; the audible narration location controls visual change.
4. Watch generic or richly directed characters, backgrounds, transitions, effects and optional subtitles.
5. Customize the character look and voice mid-story without changing the author's original package.
6. Continue or seek later, including landscape/portrait and screen-off audio where supported.

## Progressive authoring is mandatory

- **Level 0:** ordinary `.txt` produces one implicit narrator, text, segment schedule, a neutral visual stage, optional captions; never require manually defining characters.
- **Level 1:** `.story.md` uses concise markers `@n`, `@c1`, `@c2m`, `@c3f`; speaker marker creates character existence, optional sex metadata, persistent ID, and a default compatible persona. No JSON required.
- **Level 2:** optional names, scenes, backgrounds, basic expression changes, timing anchors and assets.
- **Level 3:** advanced package with custom character rigs/sprites, personalized voices, audio/visual events, transitions, camera, multiple channels, precise alignment and optional supported local neural model dependencies.
- **All levels compile into one canonical validated story model.** More authored detail overrides defaults without forcing the author to rewrite existing dialogue.

## Narration is source of truth

Track audible segment identity and position. Text→utterance segmentation provides start/end boundaries even when exact word-range timing callbacks are unavailable. A cue may be anchored to block start, final utterance end, optional aligned word boundary, or explicitly modeled silent beats. Synthesis completion is **not** narration completion.

Pausing/speed/seek and voice switch must not trigger phantom events or let obsolete asynchronous callbacks mutate the current scene. Animations and subtitles follow the story's audible position and default to reduced-motion/accessibility variants when requested.

## Coherent built-in content

A story missing art/sounds/voice assignments still has the most complete safe presentation possible with the resources installed: neutral scenery, stable generic character silhouettes with clear speaker labels, simple animation/highlight, captions and nonintrusive transitions. Never fabricate actual plot information to fill creative gaps.

Default background, character, props, effects, ambience and UI/sound resources are **typed, versioned, style-coherent, resolution-aware and degradable**. Initial style proposal: `storybook-flat-v1`. Art extends beyond narrow camera framing when supplied; preserve aspect ratio, reposition critical characters and respect captions/system insets.

## Voice system

A story's character ID, voice intent, concrete speaker/model and sound output are different objects. TTS backend can be Android system TTS, approved offline neural TTS, or prerecorded audio. Characters may be customized by viewer; high-quality built-in offline Tagalog is a *goal* with unresolved model rights/performance, not a demonstrated ability. A readable text-only playback is a guaranteed degradation tier even if no speech engine is available.

## Container and authoring goals

Prefer a human/AI-readable source with compiler diagnostics. A ZIP-compatible `.storypack` bundles manifest, scripts/canonical IR, artwork, audio, model references and provenance. Support packaged compatible neural weights only with strict bounded decoding and license/security checks, not arbitrary executable provider code. Shared installed asset/voice packs prevent repeated large model weights in every story.

## Non-goals for first release

A full film rendering suite; arbitrary scripting code execution; general-purpose game simulation; interactive multiplayer; content generation requiring cloud/network; importing unrestricted model runtimes; guaranteed humanlike speech in every language/device; thousands of finished default assets before core tests.

## Product quality gates

- Plain and minimally annotated stories play or degrade to readable experience without requiring creator to fill production metadata.
- Narration, subtitle and visual triggers are logically synchronized over pause/resume/seek/rotation.
- Default characters remain visually distinct without forced sex/age stereotypes.
- Neutral fallback is preferable to factually wrong auto-inference.
- 4-GB-class Android baseline should be explicitly profiled under concurrent TTS/rendering; performance is not presumed.
- Stories preserve source text and attribution, protect from untrusted package paths/decompression/injected instructions, and record asset/voice license provenance.

See [Implementation Plan](IMPLEMENTATION_PLAN.md), [Requirements](knowledge-system/vault/requirements/PRODUCT_REQUIREMENTS.md), and [Decisions](knowledge-system/vault/decisions/DECISION_REGISTER.md).

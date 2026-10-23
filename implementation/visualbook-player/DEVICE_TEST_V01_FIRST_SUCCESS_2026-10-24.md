# VisualBook First Story v0.1 — Initial real-device acceptance

**Date:** 2026-10-24; **evidence:** user message in development conversation. **Status:** OBSERVED-DEVICE-TEST, LIMITED (not a measured performance or synchronization benchmark).

## Observed behavior

After installing the first story-player APK on an Android phone, the user reported: **"Nice, it works"**, adding that they authored an `@c3` speaker and that the extra character **appeared only on that speaker's turn and disappeared afterward**. The user welcomed that behavior as an initial success.

The speaker grammar therefore supports, in this tested path, implicit characters beyond the two main cast slots; the generic guest sprite appears conditionally for an active `c3` utterance. This is consistent with the current source code, in which the stage renders the two fixed actors and dynamically draws other active speaker IDs only while they are the selected actor.

This feedback establishes the APK launches and the user can exercise a custom script with `@c3` and observe an active-guest rendering change. It does **not** establish that all character voice assignments worked, that audio-word alignment is exact, that the app runs offline in airplane mode, or that rotation, persistence, performance or error paths have passed.

## Known v0.1 design limitation, not a parser failure

`StoryStageView` currently has fixed `c1` and `c2` positions. `c3+` guests are drawn only when their ID equals the current `active` speaker. A guest therefore pops in for their speaking turn, then vanishes when another speaker activates. This is an intentional narrow MVP fallback, not the long-term semantic model of character presence.

## Next implementation seam (proposal, not yet accepted)

Separate at least three orthogonal scene concepts:

1. `CharacterRegistry`: identity, optional sex hint, artwork and **voice assignment** for any implicit `cN`; identity persists for the whole story.
2. `ScenePresence`: who is visually in the current scene, with optional entrance/exit cues and deterministic default policy for unannotated scripts.
3. `ActiveSpeaker`: who is presently narrating, authoritative only when a matching utterance's *audible* playback begins/ends.

**Default-policy question remains OPEN:** should implicit `c3` stay after its first dialogue until a scene change, or only appear during dialogue for compatibility? Proposed for next experiment: keep the current v0.1 mode, offer an alternate auto-presence-on-first-speech mode, test staging with 3–6 actors and subtitles, then decide with actual screenshots.

Do not silently invent a large set of characters on screen from every mentioned name; scripts can still specify a separate character's presence, while simple dialogue-based creation should remain complete without advanced markup.

## Suggested acceptance tests

- `@n`, `@c1m`, `@c2m`, `@c3` resolve to expected cast and audio units without explicit asset declarations.
- Test `@c3` before `@c1` and with multiple contiguous turns; no crashes or off-screen placement.
- Stop/restart/pause with guest active should not leave a stale speaker highlight.
- Future presence-on-entry behavior should not mutate `StoryCore` narration cursor or fabricate audible speech completion.

# Render Preview and Correction Inspector — VisualBook v0.6

**Status:** EXPERIMENTAL IMPLEMENTATION. Actual Android code in [PR #15](https://github.com/despairarc000/visualbook/pull/15). Build tests and phone acceptance are separate quality gates.

## User goal

After building arbitrary 100×100 vector artboards, reusable pivots and bounded wing motions, authors need a way to **see and correct what they constructed**. AI-written geometry cannot be called artistically successful from syntax alone. The first preview loop is human-in-the-loop: show a scene, inspect a specific point, copy a reproducible report, revise text and recompile. Autonomous image perception is not claimed.

## First preview capability

1. A **Preview** button opens a read-only snapshot of the first narrated segment; pauses TTS and invalidates the active utterance token so the inspector cannot accidentally advance playback. **◀/▶** browse all segment snapshots, independently of TTS engine availability.
2. Apply only compiled state: each segment's style, visible figure placements, motion definitions and speaker. No fake speech events, no changes to `PlaybackState.index()`, `begin()`, `skip()` or `audibleDone()`. Preview is static (the actual motion code is audible-bounded).
3. Enable the existing 0–100 scene grid while preview is open, restoring the user's previous grid toggle on exit.
4. Tap the Canvas stage to record a crosshair with normalized stage coordinates and (if the tap falls inside a visible figure's displayed **bounding rectangle**) figure-local coordinates. The same **aspect-preserving meet transform** as the stage drawing contract is used. Most recently painted visible artboard wins overlaps; precise inside-vector-silhouette testing is not part of v0.6.
5. The **Copy** button writes a deterministic scene report to clipboard: segment and source line, speaker, story text, style and background, visible figures with local artboard dimensions, normalized stage coordinates, reusable part/root node counts, motion definitions, and the selected local coordinate.
6. **Edit shape** finds the selected figure's `@figure ID` declaration in the original script and highlights its line in the existing editor. The author or AI can revise the geometry, Apply, and reopen preview to verify. No hidden rewriting of the canonical source.
7. **Close Preview** restores normal playback presentation controls. Play exits preview and resumes from the actual audio cursor, not the preview index.

## Existing source and new pure module

- [RenderInspector.java](../../../implementation/visualbook-player/story-core/src/main/java/com/visualbook/core/RenderInspector.java): dependency-free math and scene diagnostics. This is suitable for pure Java regression testing and future machine-readable diagnostics.
- [StoryCore.java](../../../implementation/visualbook-player/story-core/src/main/java/com/visualbook/core/StoryCore.java): unchanged compiler/segment semantics, reused by preview.
- [StoryStageView.java](../../../implementation/visualbook-player/app/src/main/java/com/visualbook/player/StoryStageView.java): overlays a tap crosshair after drawing, without letting inspection alter source or timing.
- [MainActivity.java](../../../implementation/visualbook-player/app/src/main/java/com/visualbook/player/MainActivity.java): Android preview controls, clipboard, editor jump, preserved TTS control.
- [RenderInspectorTest.java](../../../implementation/visualbook-player/story-core/src/test/java/com/visualbook/core/RenderInspectorTest.java): exact normalized conversion at portrait-stage viewport, hide/style snapshots, reports, invalid dimensions, no-hit conditions.

**Important:** `@figure` artboard dimensions are logical vector units. `@show figure at:77,56 width:27` sets stage placement, not the eye's position inside a figure. Stage control buttons/captions are outside the 0–100 stage viewport.

## Safeguards, constraints, next phases

- No network or image-generator integration. Clipboard content stays local until user pastes it elsewhere.
- Source comments and named part identifiers remain unchanged; errors in scripts stay explicit and line-numbered.
- Preview never claims that audio is playing. Motion tracks shown in the report are *defined* but not driven by silent preview time.
- Bounding boxes select an artboard, including transparent space; selected local coordinates are not guaranteed to lie on an opaque vector stroke.
- This is a **report-based correction loop**, not AI visual perception of rendered pixels, screenshot export, sprite painting, a fully interactive vector editor, or automatic repair.
- Follow-up improvement: export a shareable preview image + exact IR, then support bounded iterative AI correction with before/after diffs, coordinate and transform gizmos and safe undo. All must respect the 4GB Android target's graphics/memory limits.

## Acceptance gates

- [x] Source code, pure JVM inspection test, and CI configuration authored.
- [ ] Latest feature branch passes pure Java story/geometry/inspection tests, Android Gradle, and knowledge doctor.
- [ ] APK installed on Samsung A06 5G; preview stepping does not unexpectedly speak or advance the actual audio cursor.
- [ ] Stage tap coordinates agree with a selected artboard on-device; Copy report can be pasted to ChatGPT; Edit shape navigates correctly.
- [ ] Controls remain accessible without crushing the stage on the user's device.

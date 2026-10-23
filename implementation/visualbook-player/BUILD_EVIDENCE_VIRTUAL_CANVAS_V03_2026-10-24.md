# Virtual Canvas Geometry v0.3 — Android build evidence

**Evidence class:** VERIFIED-CI-BUILD; **not yet verified on physical Samsung SM-A066B phone.**

- [PR #12](https://github.com/despairarc000/visualbook/pull/12) merged as `94fab04868e84d4caf01dae7379dcb2112ed71b6`.
- Feature head `a49eb03554286a368c236fae653ae6517c77efdb` (documentation), final code head `d2970262a80a55edad803adea30383839aa5bb94`. No code changes between them.
- [Final Android job run 40039492709](https://github.com/despairarc000/visualbook/actions/runs/40039492709): **SUCCESS**, pure StoryCore **61 assertions**, pure VectorFigure geometry **43 assertions**, Gradle **BUILD SUCCESSFUL**, debug APK uploaded.
- [Final knowledge doctor run 40039492697](https://github.com/despairarc000/visualbook/actions/runs/40039492697): **SUCCESS**.
- [Installable APK evidence run 40039374714](https://github.com/despairarc000/visualbook/actions/runs/40039374714): same final Android code, earlier than final docs-only commit; uploaded artifact ID `12538228595`, ephemeral (expires 2026-11-06).
- Extracted `app-debug.apk`: **46,159 bytes**, SHA-256 `04a71c789e790801efc926ba19f15b9094c4a359aee328509b479221ad36c757`; artifact ZIP and APK CRC integrity both checked.
- APK Android application ID `com.visualbook.player.canvas.v03`, label `VisualBook Canvas v0.3`. A separate install from previous `v0.1` first-story and `v0.2` chibi applications.

## What was checked

The pure Java suite exercises accepted figure primitives (circles, triangles, polygons, paths with absolute M/L/Q/C/Z), positive virtual artboard bounds, exact (77,56) coordinates, fit scaling, rejected malicious/invalid shapes, per-utterance figure visibility snapshots, normalized stage placement, chibi scope coexistence and generation-safe narration cursor. The Android CI compiled the actual stage view with Canvas path drawing, color fills, strokes, coordinate grid toggle, story parser wiring and TTS callback updates.

## What remains unknown

CI does **not** show whether this APK's creature looks visually coherent on the A06 5G, whether arbitrary AI-authored figures are sufficiently expressive, frame timing on phone, visibility in different screen orientations, exact audio-visual synchronization, or how it behaves with dozens of complicated curve paths. The built-in sample figure is intentionally primitive, not AI image generation or a general procedural mythical creature synthesizer.

## Suggested phone test

1. Install `VisualBook Canvas v0.3` alongside earlier versions. Run demo: the triangle + circle + curved stroke should render as a mystical figure during the first narrated sentence, then disappear when `@hide spirit` takes effect at the next audible narration boundary.
2. Open **Voices**, enable **Show 0..100 coordinate grid** and confirm normalized on-stage coordinates.
3. In **Edit**, change the geometry `circle 77 56 11` and `@show spirit at:77,56 width:27`, apply, replay, and observe differences.
4. Confirm stop/replay restores state; malformed `@figure` or out-of-bounds geometry should show a line-numbered error, not crash.
5. Send screenshots for normal/chibi and after importing custom `.story.md` figures.

# VisualBook Render Preview v0.6 — Android build evidence

**Status:** VERIFIED GitHub Actions Java/Android build; **NOT** yet verified visually/on device.

- [PR #15](https://github.com/despairarc000/visualbook/pull/15) merged to `main` as `bd3648193c4e564ab6d6a4c170b87154f47d6a2e`.
- Code SHA `65fdbad27ac6053d621c555495dd5a00bf85e12c`; subsequent tip `25a35a48c4e68d918e3b63d8f0e516015039f74a` added only design/vault metadata.
- [Build run 40049453526](https://github.com/despairarc000/visualbook/actions/runs/40049453526) **success**: StoryCoreTest **79**, VectorFigureTest **85**, RenderInspectorTest **22** = **186 assertions**, Android Gradle `assembleDebug` **BUILD SUCCESSFUL**. Latest documentation-tip Android [build 40049542638](https://github.com/despairarc000/visualbook/actions/runs/40049542638) also **success**.
- Knowledge doctor [run 40049542642](https://github.com/despairarc000/visualbook/actions/runs/40049542642) **success**.
- GitHub Actions artifact `visualbook-first-animated-story-debug` ID `12542453503` from code SHA `65fdbad27ac6053d621c555495dd5a00bf85e12c` expires 2026-11-07.
- Artifact ZIP `VisualBook_Preview_v06_artifact.zip`: **55,369 bytes**, archive CRC test passed. Inner `app-debug.apk` extracted to `VisualBook_Preview_v0.6.apk`: **58,459 bytes**, APK archive CRC test passed.
- APK SHA-256 **`f7d9dfea68118d12870af775f75b3e5811fbaf43969df456b44fc280f76999c4`**.
- Android app ID `com.visualbook.player.inspector.v06`, label **VisualBook Preview v0.6**, distinct from previous v0.5 app.

## What was verified by build

Silent render-preview navigation with ◀/▶, optional stage coordinate-grid, device/stage/artboard bounding-box hit inspection, Copy scene report to system clipboard, Edit shape navigation to authored `@figure` declaration; immutable per-utterance scene snapshots remain separate from audio playback cursor. No screenshot/OCR or network dependency.

## What is NOT verified

The preview layout on Samsung Galaxy A06 5G, keyboard behavior and scene visibility when opening editor, actual clipboard delivery, correct taps around overlapping characters, user perception of grid/readability, performance and memory under complex shapes. No autonomous AI image-analysis loop is implemented; the user supplies inspection reports and optionally screenshots to AI.

## Suggested phone acceptance test

1. Install v0.6 separately and tap **Preview**. Without audio playback, first scene should show the custom creature and stage grid.
2. Tap the custom figure near its center; verify a stage coordinate and an approximate figure-local coordinate are displayed.
3. Use **Copy**, paste the report in ChatGPT, and check figure IDs, source line, style, and stage/artboard coordinates.
4. Press **Edit shape** and verify the correct `@figure spirit` declaration is selected. Edit one primitive/instance, Apply Script, and reenter Preview.
5. Use ◀/▶ to inspect the chibi portion and later hidden-figure scene without narrating. Close preview, then Play to ensure actual audible playback resumes independently.

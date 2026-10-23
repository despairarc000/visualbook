# VisualBook — First Animated Story v0.1 build evidence

**Evidence class:** VERIFIED-CI-BUILD, NOT verified Android-device execution.

- Repository: `despairarc000/visualbook`.
- Feature PR: [#9](https://github.com/despairarc000/visualbook/pull/9), **merged into main** as `e671a0979f20da0fe342e968c93c883dff838667`.
- Application source commit proven by CI: `db90239d0e73063ef447204d6e9ee42d50140a7b` (docs-only update after code commit `56048b73a2403566f8ab2c9ccedfcd8ec138b836`).
- First fully built APK source [GitHub Actions run 40031944426](https://github.com/despairarc000/visualbook/actions/runs/40031944426), compiled version with Android import. PR check [run 40032106784](https://github.com/despairarc000/visualbook/actions/runs/40032106784) separately completed with Android Gradle build and artifact upload successful. [Knowledge doctor](https://github.com/despairarc000/visualbook/actions/runs/40032106812) also successful.
- Pure Java test suite: **26 assertions passed** in GitHub Actions.
- Android build result: `BUILD SUCCESSFUL` with Android SDK 35, JDK 17, AGP 8.9.2, Gradle 8.11.1.
- Artifact from run `40031944426`: GitHub Actions artifact id `12535362396`, `visualbook-first-animated-story-debug`; expires 2026-11-06. It is ephemeral.
- Retrieved `app-debug.apk`: **32,419 bytes**, SHA-256 `a6ed36116372738946ca2138e4ee362081924d8bcb1440f61c1d09d5aaa0f7e6`; APK archive CRC checked successfully and includes `AndroidManifest.xml`, `classes.dex`, `classes2.dex`, `classes3.dex` and `resources.arsc`.
- Application ID `com.visualbook.player.slice`, name `VisualBook First Story`. No INTERNET permission, no bundled art or speech models; art generated procedurally by Android Canvas.

## Behaviors implemented in code, NOT proven on device

- Script parser: `@n`, `@cN`, `@cNm/@cNf`, and `@bg classroom|neutral`; default narrator and generic actor identities; UTF-8 TXT/story.md reader with bounded input.
- `PlaybackState`: utterance queued ≠ speaking, `onStart` toggles active speaker visuals, `onDone` increments cursor once; stale callbacks ignored on pause/stop/error; manual Skip explicit.
- Android screen: responsive procedural classroom, two characters, demo names Juan/Pedro, speaker highlight/mouth movement, optional narrator/c1/c2 installed voice choice, story editor, subtitles, Play/Pause/Stop/Skip controls.
- Pause restarts the current sentence; no true mid-utterance audio seek, media session or prepared-file caching yet.
- Voice engine/platform availability varies. No claim this APK works offline with Filipino on fresh devices or that current device's Google-versus-Samsung engine ambiguity is resolved.

## Phone validation checklist

1. Install APK on Samsung SM-A066B alongside probes. Load preinstalled Juan/Pedro demo and press Play after TTS initialization.
2. Ensure visible scene and subtitle follow the **speaker in audible speech**, not synthesis/preparation.
3. Test distinct installed Filipino voices for narrator/c1/c2, run in airplane mode, and report if any `-7` error appears.
4. Pause mid-utterance, tap Play to restart it; Stop rewinds; Skip advances only by explicit user action.
5. Import arbitrary plain TXT, then an annotated `.story.md` with @c1/@c2; verify default generic actors and clear compiler diagnostics.
6. Rotate portrait/landscape, report clipped controls/text and any lag/unresponsiveness. Collect screenshots and user-observed audio sync.
7. Only promote this implementation from "build verified" to "device verified" with actual test evidence.

See [implementation README](README.md), [Phase Gates](../../knowledge-system/vault/plans/PHASE_GATES.md), and [Current Checkpoint](../../knowledge-system/vault/checkpoints/CURRENT.md).

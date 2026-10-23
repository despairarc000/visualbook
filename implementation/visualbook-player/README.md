# VisualBook First Animated Story — Vertical Slice v0.1

**Implementation branch:** `implementation/first-animated-story`. This is a real Android player **prototype**, not a full production app or finished generic-media library.

### Architecture

`story-core/` contains dependency-free Java script parsing, sentence segmentation and generation-safe logical playback state, with a main-method test suite. `app/` is the Android adapter: TTS runs through a worker, Canvas draws the generic classroom/cast, Android UI hosts voice selectors and captions.

### Scope

- Demo Juan/Pedro story preloaded.
- Android text document picker (UTF-8 TXT / .story.md inferred from extension), bounded 200-KB import; no storage permission.\n- User-editable script accepting `@n`, `@c1`, `@c2m`, `@c3f`, `@bg classroom` / `@bg neutral`, or plain TXT toggle.
- Implicit cast: no character manifest required. Speaker-specific generic silhouette fallback including a "guest" actor for `c3+`.
- Three actor voice selectors (n, c1, c2), automatically proposing distinct local Filipino voice candidates if present; optional on-device voice engine, no bundled models.
- Live Android TTS utterance callbacks drive visible speaking animation and progression. No fake timer-based story advancement. Animation frames use a subordinate clock only while speaking.
- Stop, restart, pause (stops TTS; resumes from start of interrupted sentence), manual Skip when failed. Errors display visibly without auto-skipping.
- Caption and voice chooser are Android UI widgets; Canvas uses no external graphical assets or storage permissions.

### Limitations

This is a **narrow tested subset**, not the complete v0.4 spec: no ZIP/storypack import, full import validation, @after expressions, multiple background scene visits, audio clip caching, exact interior audio seek, saved progress, actor-specific custom sprites, soundtrack, screen-off foreground service, rate/pitch controls or offline-engine guarantee. Editor accepts up to 100k characters and 500 utterances; Android UI loads input directly into memory. Parser preserves imported text in the in-memory `Story`, though no persisted state yet.

The old research probe remains unmodified. Android TTS provider identity and installed voices vary; fail visibly if no playable voice. No model/audio/media licensing to redistribute is assumed.

### Build

JDK 17 / Android API 35 / Gradle 8.11.1. GitHub Actions `visualbook-player-build.yml` produces the debug APK and runs pure-core tests.

```sh
# pure core
javac -d /tmp/visualbook-core-test story-core/src/main/java/com/visualbook/core/*.java story-core/src/test/java/com/visualbook/core/StoryCoreTest.java
java -cp /tmp/visualbook-core-test com.visualbook.core.StoryCoreTest

# Android debug APK
gradle --no-daemon assembleDebug
```

Android app ID: `com.visualbook.player.slice` to avoid colliding with any research probe. First phone test should verify synchronization, Tagalog pronunciation, voice switching, screen rotation and 4 GB memory behavior. Audio engine/listening results are unknown until tested.

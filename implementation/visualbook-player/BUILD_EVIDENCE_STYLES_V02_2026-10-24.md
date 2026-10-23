# VisualBook Styles v0.2 — scoped art rendering build evidence

**Status:** VERIFIED-ANDROID-BUILD, NOT YET VERIFIED ON PHYSICAL PHONE.

- Repository: `despairarc000/visualbook`
- [Feature PR #11](https://github.com/despairarc000/visualbook/pull/11), merged as `a3845299a6cb07c9291a3101fbfd80d65d70b7c8`.
- Source commit tested: `481e89e3b740bb01d3d277d664d61ca06c3a9238`.
- [Android CI run 40037026012](https://github.com/despairarc000/visualbook/actions/runs/40037026012): **SUCCESS**; JDK 17, Android SDK 35, AGP 8.9.2, Gradle 8.11.1.
- Java portable StoryCore and PlaybackState suite: **42 assertions PASS** in the Android CI job; includes scoped style nesting, missing end, unknown mode, implicit `@c3`, plain TXT, and original narration guard tests.
- [Knowledge doctor run 40037026020](https://github.com/despairarc000/visualbook/actions/runs/40037026020): **SUCCESS**.
- Action uploaded artifact `visualbook-first-animated-story-debug`, id `12537691169` (expires 2026-11-06).
- ZIP extracted debug APK `app-debug.apk`: **35,067 bytes**. SHA-256: **`cb1d5408afd8ea1ac7e04b2afb8f639537d2b333d3bead969d8cceff4996efbe`**. APK archive integrity/CRC verified.
- Android application ID: `com.visualbook.player.styles.v02`, label `VisualBook Styles v0.2`; side-by-side with original first story app.

## What's actually implemented

The script accepts `@chibi`/`@end` or `@style chibi|normal|cartoon`/`@end` with a nesting stack and deterministic restoration. Every speech segment contains its authored effective `artStyle`. Markers don't synthesize speech. Android TTS onStart applies the style, and valid audio completion is still authoritative for segment advancement.

The Canvas renderer has a materially different chibi character branch: larger head, larger eyes, shorter legs/torso, blush/mouth, active sparkle, pastel aura; original normal mode remains. The built-in Juan/Pedro story demonstrates a chibi comedic reaction and returns to normal. No assets downloaded, no network permission.

## Not yet demonstrated

- User-confirmed correct visual switching on Samsung SM-A066B (unlike first story v0.1 which **was** user-confirmed working).
- Exact acoustic/visual onset synchrony, high-FPS rendering under load, screen rotation, numerous styles, custom image asset packs or character-specific chibi (v0.2 is global).
- Compatibility with non-current Android TTS models.
- End-to-end physical tests for nested scope and `@c3` within chibi.

## Next validation on device

1. Install separate `VisualBook Styles v0.2`; launch demo.
2. Note the scene first looks normal, becomes exaggerated chibi for the joke, then reverts after `@end`. It should switch when audible utterance starts, not when the TTS operation merely queues.
3. Test edited source: normal speech, `@chibi`, `@c3`, `@end`, normal speech.
4. Check Pause/Play during chibi, Stop to normal, manual Skip, voice fallback, and small-screen readability.
5. Send a screenshot and report whether proportions visibly change and restore. Do not call this device-verified until then.

See [scoped style contract](../../knowledge-system/vault/design/SCOPED_ART_STYLES_V02.md) and [prototype README](README.md).

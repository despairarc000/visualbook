# VisualBook v0.5 — animated shape parts build evidence

**Status: VERIFIED Android CI + artifact, but NOT YET real-device v0.5 animation verified.**

- [PR #14](https://github.com/despairarc000/visualbook/pull/14): merged as `13f9d429a933b7ff70225b8fa00d06937fbc9add`.
- Code SHA `9813e8b0d57f4a2e3997f46c5dbdd0cea45c96d3`; final feature branch tip `bd232fd1a0110cf8dadcaafb84f251adc48ab6b3` also added documentation only.
- [Android Actions run 40047828597](https://github.com/despairarc000/visualbook/actions/runs/40047828597): **SUCCESS**; `StoryCoreTest` 79 assertions, `VectorFigureTest` 85 assertions, total **164 passing Java assertions**; Gradle **BUILD SUCCESSFUL**, debug artifact uploaded. [Knowledge check run 40047919687](https://github.com/despairarc000/visualbook/actions/runs/40047919687) succeeded on documentation tip.
- Artifact ID `12541219180`, `visualbook-first-animated-story-debug`; expires **Nov 7, 2026**. Downloaded ZIP `VisualBook_Motion_v05_artifact.zip` verified CRC; extracted `app-debug.apk` verified all nested archive entries.
- APK actual size **52,891 bytes**; SHA-256 **`60c245b04bdab139856dfeb4c172cf2d5dbda4203b842f3fa4bf634e0d03e369`**.
- Separate Android app ID `com.visualbook.player.motion.v05` and display label **VisualBook Motion v0.5**, designed to coexist with installed v0.4.

## Demonstrated in build, not yet phone

Named root group instances `@use wing ... id:leftwing`, scoped `@animate figure instance swing:24 period:850`, `@still` stop tags, bound input validation and per-audible-utterance motion snapshots compile as Java data-only control. Stage draws bounded sine-wave rotation around authored part pivots *only between live validated TTS audible start and stop/done*. Motion does not advance narration and is disabled by stage reducedMotion flag (no exposed reduced-motion UI toggle yet). Built-in sample has two wings with simple time-dependent rotation.

**Not yet tested on hardware:** visual smoothness, clipping, perceived artistic quality, long utterances, headset/background audio interaction, orientation/rotation, and accurate audible visual onset. Code uses `postInvalidateDelayed(48ms)` only while speech is active for animated parts—this is not a device frame rate benchmark.

## Acceptance checklist

1. Install v0.5 without removing v0.4, launch built-in demo and Play.
2. Watch the upper-center winged creature **while the narrator is audibly speaking**. Both wings should move gently rather than remain rigid.
3. Pause during speech. Wings should stop and settle at base rotation. Play replays the sentence from start and starts wing motion again; Stop returns to base.
4. Edit script: switch `swing:24` to `swing:8`, Apply, Play and compare; `@still` or removing `@animate` should make wings stationary.
5. Report if the wings are clipped oddly, interfere with characters or are difficult to notice. Screenshot or short screen recording is useful for visual diagnosis.

See [design](../../knowledge-system/vault/design/AUDIBLE_PART_MOTION_V05.md) and [application source](app/src/main/java/com/visualbook/player/StoryStageView.java).

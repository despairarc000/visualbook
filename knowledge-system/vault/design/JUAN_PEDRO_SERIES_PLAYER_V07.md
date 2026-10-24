# VisualBook v0.7 — Juan and Pedro Series Player

**Implementation status:** Android app built on [PR #17](https://github.com/despairarc000/visualbook/pull/17). Device playback timing and long-session behavior remain unverified. Scope is intentionally universal enough for later stories.

## Product and audience
The v0.6 player required manually importing the ~24-minute Episode 1 story; no chapter indexes, saved checkpoints, named guest actors or directed scenery existed. v0.7 ships `Juan at Pedro: Mga Kuwento ng Bayan ng Maligaya` Season 1 Episode 1 automatically as an installable, **separate application ID** `com.visualbook.player.series.v07`. Its full source and continuity bible remain under `stories/juan-pedro/`. The season library lists E2–E8 **Coming soon**, not as playable content.

## Directed source and compatibility
- **Unmodified original** v0.6-compatible script: `stories/juan-pedro/season-01/E01_Ang_Kampanang_Nawala.story.md`.
- **Directed v0.7 edition**: `stories/juan-pedro/season-01/E01_Directed_v07.story.md`.
- **Bundled Android asset**: `implementation/visualbook-player/app/src/main/assets/juan_pedro_e01.story.md`.

v0.7 extends prose with silent metadata only:

```text
@bg neutral
@name c1 Juan
@name c2 Pedro
@name c3 Maria
@chapter simula Ang Umagang Walang Kampana
@scene guava
@n
...
@scene plaza
...
@chapter tulay Sa Likod ng Lumang Tulay
@scene bridge
```

`SeriesDirector` interprets `@name` (c1–cN display label), `@scene` (bounded supported presets), and `@chapter` (unique stable id, readable title). It replaces lines holding these metadata commands with **blank lines**, then hands a source-line-aligned story to the unchanged Java `StoryCore`. This is the mechanism that guarantees metadata does not become spoken narration or alter the speech-clock contract.

`SeriesDirectorTest` performs **end-to-end source equivalence**: the bundled story must match the directed source exactly and compile into the same ~430 spoken utterances and speaker IDs as the original. Chapters are monotonically increasing indexes. Scene snapshots are associated with spoken segment source-line positions and applied only when valid `onStart` is received. No timer generates autonomous story events.

## Android player and scenes
- Built-in Episode 1 loaded from the Android `assets` folder at launch with UTF-8 decoding. If corrupted, an explicit fallback to the previous demo remains.
- **Series** dialog offers E01 and honest E02–E08 upcoming titles. Choosing E01 reloads the built-in source if an external story was previously opened.
- **Chapters** dialog lists five authored chapters. Manual seek invokes `PlaybackState.seek()` and invalidates stale TTS generation before selecting the new utterance. No fake `audibleDone`.
- `StoryStageView` draws lightweight scene-specific procedural art: outdoor guava tree and fruit, plaza with bell missing or restored, library, smithy, bridge, river, warehouse, forest and Balete. All are local Canvas geometry; no downloaded art packs.
- Persistent cast *identifiers and labels* c1..c9 are available; guest actors presently render generically when they speak. Juan/Pedro remain on stage. **Full cast persistence, individual facial rigs, gesture planning and seamless cinematics are NOT yet implemented.**
- Existing optional v0.2 chibi style, v0.3–0.5 vector shape/motion control, v0.6 preview/inspection, text import, and TTS behavior remain.

## Saved progress and explicit seeking
- Saved checkpoint is a tuple: SHA-256 of the **bundled directed source** + next `PlaybackState.index()`, stored in **private Android SharedPreferences**, after every valid audible `onDone`. Pausing, Stop, manual Next and chapter seeking also record explicit cursor changes.
- On next launch restore only if the fingerprint matches and index is within [0,total]. A changed episode source invalidates old checkpoints automatically.
- Stop resets to segment 0; completing E01 stores the terminal index. Tapping Play on completion resets and replays.
- Resume replays a paused sentence from its beginning; there is no mid-word/time-based seek. No automatic progress storage for imported file content (which isn't automatically retained). Current `allowBackup=false` deliberately keeps saves device-local.
- Android TTS is a device engine dependency. **24 minutes is a script-design target**, not a verified wall-clock duration. Voice sets for c3+ currently use device-default voices, not dedicated generated performances.

## Safety, performance and acceptance
- All new metadata is non-executable and validated; bounded chapter count (16) and name count (20), allowed scene IDs and source limit remain enforced.
- Art remains lightweight and reproducible, with no permissions to download assets or use the internet.
- First device test: install alongside v0.6; Episode 1 opens automatically, shows stage 'plaza', chapter picker has five entries, full Filipino story speaks, location changes correspond to events; Pause and app relaunch resume; Next and chapter selection never manufacture audible completion.
- Long-session acceptance: full 430 utterances, memory/performance, reliable Philippine offline voices, no unexpectedly lost checkpoints, accurate scene timing and user-facing voice names on Galaxy A06 5G.

## Next product steps
Real user testing, actor presence/exit staging, speaker-specific voice controls for entire cast, reusable image assets/background packs, audio-driven expressions and subtitle/story-seeking UI are the next quality gaps. The present v0.7 is a real **series-player milestone**, not a fully produced anime/movie.

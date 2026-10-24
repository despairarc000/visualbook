# VisualBook v0.7 — Juan and Pedro Series Player Build Evidence

**Status: Android CI verified; physical long-session phone playback and voice runtime unverified.**

- [PR #17](https://github.com/despairarc000/visualbook/pull/17) merged as `09c734f066accc8ff5e22e56d5c25b31a053da5a`.
- [Final Android run 40135738209](https://github.com/despairarc000/visualbook/actions/runs/40135738209) **SUCCESS** on feature SHA `9000932317acb25e97fa79e636d73a36fc522983`.
- [Final Knowledge System Doctor 40135738203](https://github.com/despairarc000/visualbook/actions/runs/40135738203) **SUCCESS** on same SHA.
- [Original production code build 40135449540](https://github.com/despairarc000/visualbook/actions/runs/40135449540) **SUCCESS**, same Android code as final documentation-only commits. Tests: StoryCore 79, VectorFigure 85, RenderInspector 22, JuanPedro Episode source 440, and directed SeriesDirector 890 assertions (**1,516 total**); `BUILD SUCCESSFUL`.
- GitHub artifact ID `12575989032`, expires 2026-11-07. Downloaded ZIP CRC PASS; extracted APK CRC PASS with bundled `assets/juan_pedro_e01.story.md` physically present.
- APK **79,275 bytes**, SHA-256 `700ccfd5864379d3a78fe8f54288b4c4bbf442af69d1d50c179dec97174bc523`.
- Android package ID `com.visualbook.player.series.v07` and label `VisualBook Series v0.7`. Prior v0.6 app remains unaffected.

## Functionality built
Full S1E1 opens automatically; five chapters, named c1–c9 cast labels, deterministic scenes (guava, plaza with bell missing/restored, library, bridge, warehouse, river, forest, Balete), chapter seek, SHA-256-bound local checkpoint saving after genuine TTS completion, manual controls, preview and existing script import. Season library explicitly marks other seven episode titles as Coming Soon.

## What is NOT verified
- Actual ~24-minute runtime with chosen device TTS, voice download/offline reliability, or multi-voice cast quality.
- Full playthrough of 430 utterances without network voice failures, lifecycle race conditions or slowdowns.
- Chapter switching, saved/resumed index and scene drawing on physical Samsung Galaxy A06 5G. The procedural scenes are not animation-film assets or complete actor staging.

## Suggested on-phone test
1. Install APK alongside v0.6. Episode 1 should be preloaded and start by tapping Play; no import.
2. Tap Series to see eight entries, only E01 playable; Chapters should show five entries.
3. Enter a later chapter, Play a few sentences, Pause, leave/reopen the app and Play: should resume from the next successfully completed sentence, or repeat the interrupted sentence from its start.
4. Verify early plaza lacks bell, later plaza_restored shows its bell; witness named speakers in subtitles; chibi reactions remain.
5. Try the entire 430-utterance episode and record actual playback time, any voice failures, visual problems and device battery/heat.

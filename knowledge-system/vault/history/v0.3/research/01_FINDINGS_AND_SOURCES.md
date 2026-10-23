# 01 — Research, Investigation, and Evidence Ledger

Date: 2026-10-23. Evidence levels: **documentation verified**, **candidate / needs benchmark**, **author decision**. The sources establish APIs and licenses, NOT that our app works yet.

## Android speech playback

**Documentation verified:** Android TextToSpeech supports `speak`, `synthesizeToFile`, querying `getVoices`, and a max TTS input length. UtteranceProgressListener documents `onStart`, `onDone`, `onError`, `onStop`, plus `onRangeStart` *only if the engine provides timing*. `onStart` is just before audible start in `speak` mode, but refers to file production in synthesize-to-file mode. `onDone` for audible playback indicates audio completed; for synthesis-to-file it indicates output was written. These are categorically distinct events.

Sources:
- https://developer.android.com/reference/android/speech/tts/TextToSpeech
- https://developer.android.com/reference/android/speech/tts/UtteranceProgressListener
- https://developer.android.com/reference/android/speech/tts/Voice

**Resolution:** define `SpeechSynthesisCompleted` separately from `NarrationPlaybackCompleted`, and forbid synthetic file completion from advancing the narrative. Prefer buffered clips played under our own audio player. Live Android TTS utterances remain compatibility-only because audio-clock position and seamless seek are constrained. Re-check voice offline capability per installed voice (`isNetworkConnectionRequired`), and measure real airplane-mode behavior; not all reported locales guarantee offline installation.

## Media3 / background mode

**Documentation verified:** `MediaSessionService` hosts player/session independent of the activity, supports notification/media transport. Media3 playlists support transitions between media items; transitions are documented as seamless, but authoring separate TTS sentences might still affect prosody and gaps in generated audio. No guarantee is made about the quality of independently synthesized pieces.

Sources:
- https://developer.android.com/media/media3/session/background-playback
- https://developer.android.com/media/media3/exoplayer/playlists
- https://developer.android.com/media/media3/exoplayer/listening-to-player-events

**Resolution:** Android service owns the playback engine, core cursor/event reducer; UI observes state. Use a prefetch queue bounded by bytes and latency, not 'generate entire book'. Audio focus, interruptions and process death require own tests. The scheduler consumes *audible playback position*, not event-loop callbacks alone.

## Rendering / responsive composition

**Documentation verified:** libGDX has a scene graph, actor transforms, actions, viewports, and Android `AndroidFragmentApplication.initializeForView()`. `FitViewport` can letterbox; `FillViewport` crops; `ExtendViewport` reveals extra world content. None automatically chooses cinematographically correct character composition or supports accessibility safe areas.

Sources:
- https://libgdx.com/wiki/graphics/2d/scene2d/scene2d
- https://libgdx.com/wiki/graphics/viewports
- https://libgdx.com/wiki/app/starter-classes-and-configuration

**Resolution:** keep semantic scene state in story core, not libGDX actors. Build `StoryRenderer` port; implement thin backend after measuring lifecycle and 4GB-class memory. Camera/world/view/screen layout rule system is engine-owned; Android UI/subtitles overlay stays separate.

## Speech model license, Tagalog feasibility

**Documentation verified:** `facebook/mms-tts-tgl` is an actual Tagalog text-to-speech checkpoint, ~36.3M model parameters, repository shows ~145MB safetensors; CC BY-NC 4.0 limits commercial use. It is distributed for PyTorch Transformers, *not* guaranteed to run directly in sherpa-onnx without conversion and tokenization porting. Kokoro 82M has Apache-2.0 weights but model's published languages do not establish Tagalog support. sherpa-onnx supports several offline TTS model families and Android but is not itself an available Tagalog voice.

Sources:
- https://huggingface.co/facebook/mms-tts-tgl
- https://huggingface.co/facebook/mms-tts-tgl/tree/main
- https://huggingface.co/hexgrad/Kokoro-82M
- https://github.com/k2-fsa/sherpa-onnx
- https://k2-fsa.github.io/sherpa/onnx/tts/pretrained_models/vits.html

**Resolution:** split `synthesis backend capability`, `licensed speaker model`, `language frontend/normalization` into separate requirements. Do not conflate an ONNX file and a ready-to-distribute Filipino voice. Evaluate Tagalog, Taglish, abbreviations, dialogue punctuation, and pronunciation with blind listening ratings and energy/latency traces. A voice with good English and poor Tagalog fails our primary-use acceptance gate.

## EPUB / story language precedent

**Documentation verified:** EPUB Media Overlays (SMIL-based) synchronize audio with text; this supports our segment-boundary idea. It does not supply our full cinematic scene graph, character model or adaptive fallbacks. Keep simple source script and typed canonical IR.

Sources:
- https://www.w3.org/TR/epub-33/
- https://www.w3.org/submissions/2017/SUBM-epub-mediaoverlays-20170125/
- https://json-schema.org/draft/2020-12/

## Assets, licenses, security, memory

**Documentation verified:** Kenney's game-asset page content is publicly described as CC0; specific included license files remain authoritative. Android documents the storage access framework for controlled user file import. Android's bitmap guidance explains decoded image memory expansion; a 4000x3000 32-bit bitmap occupies ~48 MB in decoded pixel memory.

Sources:
- https://kenney.nl/support
- https://developer.android.com/guide/topics/providers/document-provider
- https://developer.android.com/topic/performance/memory/guide/bitmaps

**Resolution:** no random 'royalty-free' resource compilation; store source, author, license file, license version, checksums and restrictions. Ship coherent procedural graphics before large art downloads. Use capped decode dimensions, bounded concurrency, resource refcounts and prefetch eviction. Parse untrusted archives without unsafe path extraction.

## Decision-driving risks (ranked)

| Rank | Risk | Why it can block us | Evidence/action |
|---|---|---|---|
| P0 | Good licensed offline Tagalog speech | Without a practical voice, promise of fully narrated offline stories is untrue | Voice shortlist, license audit, 4GB-class prototype |
| P0 | Audio gaps/unnatural sentence speech | Segment splitting may disrupt prosody | Compare live vs prepared, 1/2/4 sentence chunking, objective gaps + subjective MOS |
| P0 | Incorrect trigger semantics | Scenes out of sync, callback races on seek/interrupt | Prototype deterministic fake audio event source + reducer tests |
| P0 | Corrupt/untrusted packages | Resource exhaustion, traversal, invalid model parsing | Threat model + limits + fuzz/import tests |
| P1 | Android activity/service/rendering mismatch | Losing scene after rotation or screen off | Media3 + libGDX host lifecycle spike |
| P1 | Memory contention | Model inference + textures + audio can exceed low-memory device | bounded budgets, actual heap/GPU/native profiling |
| P1 | Generic art inconsistency | No amount of correct code makes heterogeneous assets coherent | one art bible and first production slice |
| P1 | Script semantics drift | AI-produced stories stop compiling after updates | formal grammar, version conformance fixtures |

## Non-conclusions: do not mistake for validated claims

- Not established: a redistributable, high-quality, offline Tagalog model that runs at acceptable performance on our target phone.
- Not established: Android system TTS on any particular device provides Filipino fully offline.
- Not established: libGDX+Compose is faster than Canvas on this workload.
- Not established: sentence-boundary callback latency is sample-perfect or that clips sound fluent together.
- Not established: all 101 catalog entries have licensed artwork/media; they are only logical planned IDs.

## Experiments with pass/fail questions

- TTS-01: Offline voices enumerated in airplane mode; is any acceptable fil-PH/tl-PH voice local? Record package/voice ID, locale, network requirement and exact error.
- TTS-02: For 100, 200, 350-char Tagalog/Taglish test lines, measure synthesis seconds per audio-second, peak RSS, steady CPU, gaps and artifacts.
- SYNC-01: Rapid next/back/seek while TTS work is pending; can stale responses ever move story forward? **Must be NO**.
- SYNC-02: Apply cue at speech-block end after 2+ utterances, paused/seeked or regenerated; action must occur exactly once per linear traversal.
- RENDER-01: portrait 9:16, landscape 16:9, extreme aspect ratios; character faces not covered, no stretched assets.
- RENDER-02: rotate/activity recreate while Media3 audio continues; cursor remains aligned and scene rebuilt.
- SECURITY-01: zip bomb/traversal/unsupported voice, corrupt oversized texture all rejected without crash.
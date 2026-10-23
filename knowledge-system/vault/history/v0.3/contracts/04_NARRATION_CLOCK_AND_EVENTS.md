# 04 — Narration-Driven Controller and Event Semantics

## Source of truth

The authoritative *logical* progress is `(storyVersion, visitId, blockId, utteranceId, audiblePositionMs?, playbackState, generation)`. An utterance is a chunk selected for synthesis. A block is the authored speaker turn/paragraph. **Narration progresses only from audible playback**, not from synthesis completion, animation completion or background load.

### Two adapters, one contract

- **Preferred prepared audio**: synthesize `utteranceId -> audio sample/duration` ahead of playback; Media3 or equivalent emits position/current clip/end. Maps `[audio_position] -> [block, utterance]`. Appropriate for seeking/playlist control. Synthesis-to-file `onDone` only means output is ready, NEVER that listeners heard it.
- **Live system TTS**: queue utterances with unique IDs; `onStart`/`onDone` report coarse audible progress, `onError`/`onStop` interruptions. No guaranteed sub-utterance seeking. When paused, stop-and-resynthesize or resume from a *defined logical boundary*, and explain reduced precision if needed.
- **Pre-recorded story voice audio** is a third producer behind same audio-player contract; it bypasses synthesis but still needs segment-to-text mapping.

## Deterministic event model

```
NarrationEvent =
  Prepared(story, visit, utterance, generation)
  | PlaybackStarted(...)
  | PositionAdvanced(..., positionMs)
  | PlaybackEnded(...)
  | PlaybackFailed(..., errorCode)
  | SeekRequested(cursor)
  | PauseRequested | ResumeRequested
  | SceneAdmitted(sceneInstance)
  | UserOverrideChanged(profileFingerprint)
  | ResourceReady(resourceId)
```

Every async audio/voice/resource callback carries **active generation token** and logical ID; stale events are ignored after seek, voice switch, scene exit, cancellation or process recovery. Events must be delivered to one serialized reducer, never mutate scene state from arbitrary callbacks.

## State machine

```
UNLOADED -> IMPORTED -> PREPARING -> READY -> PLAYING -> ENDED
                             |           |        |
                             v           v        v
                         ERROR        PAUSED    BUFFERING
                             ^           |        |
                             +-----------+--------+
```

**Transitions:** load/validate is separate from scene prep; `READY -> PLAYING` only after audio playable and minimal scene visual state ready. Pause freezes narration-anchored animations and does not unpause while focus lost. `BUFFERING` must preserve cursor. `SEEK` invalidates generation, cancels/ignores stale workers, reconstructs scene state and restarts in `PREPARING/READY`. `ERROR` should expose retry/switch voice/text-only options.

## Trigger ordering

For each authored speech block:

1. **Pre-block**: scene admission + current state rebuild; prepare background/visible cast. Next speech may not start until minimal fallback visual state resolved.
2. **Block start**: apply `before` cues; activate speaker/highlighting; present subtitle block or active utterance according to user preference.
3. **Utterance start**: begin/confirm actual audible clip; mouth animation follows the audible clip. Timed animations derive local `clipPosition` and pause together.
4. **Utterance end**: advance to following utterance within block; do *not* fire block-end cues before final utterance.
5. **Block end**: apply authored `after` cues in declaration order, stop mouth animation; schedule the next block.
6. **Scene exit**: defer/finish scene transitions according to declared policy, cancel scene-scoped effects, preserve story-independent audio only if allowed.

If multiple cues share the same boundary, deterministic ordered phases: scene base/environment changes -> cast spawn/remove -> appearance/expression -> camera -> audio one-shots -> presentation/subtitles. Within each phase use authored order; conflicting writes to the *same* property are a validation error unless explicitly declared replace/priority semantics. Define a stable tie-breaker and expose it to the linter.

## Time and animation policies

- Audio clip position is the reference for narration-relative visual cue offsets where timing exists. Do not infer a word boundary from estimated words/minute.
- Visual animation has local elapsed time only while corresponding narration is PLAYING; on pause it freezes, on seek state rebuild computes interpolation from stable anchor time, not a slow per-frame replay.
- Silent dramatic holds may be inserted as **explicit presentation segments** anchored between narration blocks, with their own finite duration and controller-owned advancement. Silence is content; it must not create an autonomous story playhead.
- Background ambiance and music may have independent continuous loops, but the controller governs starting/stopping/ducking; looping a sound does not advance narration.
- If media position is discontinuous, do not replay one-shot effects on reconstruct. `desired state` and `emission of ephemeral side effects` must be separate.
- Resuming an interrupted, segmented live TTS utterance can replay from a boundary unless richer support exists; prepared-audio seek is the preferred path.

## Example with two speech chunks in one block

```
@c1m
Magandang umaga, Pedro. Nakahanda ka na ba?
@after c2 expression=surprised
```

Block `b17` compiles to `u17.1` and `u17.2` (example split). `c2=surprised` fires at **`b17:end`** only after `u17.2` playback completes. It does not fire if synthesis finishes ahead or when playback reaches `u17.1:end`. If seek jumps into `u17.2`, reducer reconstructs `c2` as pre-surprise until crossing `b17:end`.

## Failure cases to handle before release

- Callback arrives out of order due to TTS queue flush: generation guard + serial reduction.
- Pause while synthesis completes: prepared clip enters cache, never auto-plays.
- Device speaker disconnected/audio focus lost: pause audible pointer, freeze synchronized animations.
- Audio playback ends but underlying provider misses a callback: media player state drives completion when app owns playback; live TTS needs bounded recovery/error path, not a blind guessed timer.
- Voice switch while speaking: change starts at next safe block boundary by default; old clip IDs invalidated at intentional restart.
- Scene background missing: insert neutral gradient by typed fallback before speech begins, no deadlock.
- Player process killed: restore checkpoint and seek; do not assume native model instance survives.

## Baseline acceptance thresholds (PROPOSED, must be calibrated)

- 100% block-end cue correctness in deterministic test harness, including 1,000 randomized seek/pause/flush sequences.
- No duplicate one-shot SFX or scene transitions after seeking backward/forward.
- Narration fidelity: sentence order, character attribution and spoken text integrity always maintained.
- Voice transition gaps measured and reported (do not declare a universal pass threshold before testing real listening quality); capture median and p95.
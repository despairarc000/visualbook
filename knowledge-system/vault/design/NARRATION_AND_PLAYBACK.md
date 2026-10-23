# Narration-led Playback

**Status:** ACCEPTED semantics, adapter/performance PROVISIONAL.

## Source of truth

Narrative position is `storyId + sceneVisitId + authoredBlockId + utteranceId + audiblePositionMs? + playerState + generationToken`. Generated speech is not equal to heard speech; `synthesizeToFile` completion means media prepared. Prefer player-owned synthesized or prerecorded audio clips as measurable position. Live system TTS start/end callbacks supply coarser boundaries.

## Reducer event inputs

`Prepared`, `PlaybackStarted`, `PositionAdvanced`, `PlaybackEnded`, `PlaybackFailed`, `SeekRequested`, `PauseRequested`, `ResumeRequested`, `UserOverrideChanged`, `SceneAdmitted`, `ResourceReady`.

The serialized reducer accepts events only for the active generation and identifiers. Obsolete callbacks after voice switching, scene exits or seeks are dropped.

## Deterministic event phases

For a scene's block: ensure minimum available visual fallback -> apply pre-block cues -> start audible utterance -> synchronize speaker highlight/captions and optional talk animation -> finish any additional utterances -> execute block-end cues exactly once per forward crossing -> next block. Seek restores immutable scene snapshot plus deterministic event state; it must not replay one-shot sound effects when reconstructing state.

Order simultaneous events by semantic phase: environment/scene base; cast; appearance; camera; sounds; text overlay; within phase use authored order. Conflicting same-property writes require explicit rules or validation error.

## Pause/seek/slow voices

Pause freezes narration-based animations and cannot silently continue after audio focus loss; prepared audio may finish buffering without advancing pointer. Seek invalidates old generation, restores logical state and resumes only after resources ready. On live TTS without exact seek, jump to nearest reproducible utterance boundary rather than inventing an exact word position. Voice changes apply at next safe segment boundary unless explicitly requested otherwise.

## Segmenter

Natural punctuation and dialogue boundaries by default. Very short fragments may be combined; clauses may split when exact reaction points are needed. Do not split every word or inject long gaps; test Tagalog prosody, abbreviations and numeric reading with actual engines.

## Failure invariants

Missing background never stalls speech; no voice leads to explicit text-only/retry install state; package unsupported capability halts appropriate chapter with actionable error; no stale completion callback may advance narration. Background listening lifecycle belongs to Android media service, not visual Activity.

See preserved [v0.3 detailed event contracts](../history/v0.3/contracts/04_NARRATION_CLOCK_AND_EVENTS.md).

# 03 — Baseline Authoring Grammar and Compiler Specification

Status: **baseline proof-of-concept grammar**; advanced syntax not yet implemented. Terminology: **speech block** = a paragraph from one speaker; **utterance** = one TTS chunk; **cue** = visual/audio instruction anchored to a speech block boundary.

## File tiers

- `.txt`: literal prose; no script directives interpreted. Multiple paragraphs spoken by narrator. A line that says `@c1m` inside `.txt` is spoken as text, **not parsed**.
- `.story.md`: line-based directives at start of line and all other text as prose; add metadata when desired. All normal text before a speaker declaration belongs to narrator.
- `.storypack`: container storing source, validated canonical IR and optional media/voice dependencies; no new narrative semantics.

## Baseline grammar (implemented by prototype)

```
SpeakerMarker   := '@n' | '@c' PositiveInteger [ 'm' | 'f' ]
SpeakerLine     := SpeakerMarker [ SPACE SpokenContent ]
Scene           := '@scene ' SceneId
Background      := '@bg ' BackgroundId
Character       := '@character ' ('c' PositiveInteger) [SPACE name=QuotedString] [SPACE sex=(m|f)]
AfterCue        := '@after ' ('c' PositiveInteger) ' expression=' KnownExpression
BeforeCue       := '@before ' ('c' PositiveInteger) ' expression=' KnownExpression
EscapedLiteral  := '@@' at start of prose line -> one literal '@'
Text            := any other non-directive line
```

Character speaker declarations persist across future blocks. `@c1m` means `speaker=c1, declaredSex=m`, while `@c1` still means that same character. `@n` is the implicit narrator, not an instance of `c0`. Name and sex are optional. Characters are registered implicitly, including when a cue refers to one; the compiler may warn if a cue-only character never speaks.

## Speech blocks and trigger semantics

- Contiguous nonblank prose lines from a single speaker = one block. Blank lines close the block. Switching speakers, entering a scene or any directive also closes a block.
- Wrapped lines stay one block; the compiler retains source text and creates normalized TTS text separately.
- Speech block segmentation is an internal service. Paragraph containing 3 sentences can be cut into 3 utterances without changing block ID or cue anchors.
- `@after c1 expression=confused` targets the *last completed authored block in the current scene*, whether narrator/c1/c2 spoke it, and changes c1's expression after its **last audible utterance** ends.
- `@before` targets the *next authored block* start in the current scene. At end-of-file or across an explicit scene boundary it is an error.
- A cue with missing/ambiguous anchor is an error. Unknown directives are errors. `@@` escapes literal leading `@`.
- Scene names may recur as author logical IDs but each visit must have a distinct runtime instance. Narrative state and checkpoints must include visit ID.
- `@bg` default is `bg.neutral`, and `@bg school.classroom` means logical `bg.school.classroom`. The proof-of-concept rejects `@bg` after the first spoken block of a scene (`SCENE002`) to prevent retroactive visual changes; future advanced syntax will offer explicit timed background-change cues.

## Hard requirements for eventual production parser

1. Source-spans: starting/ending UTF-8 byte and Unicode scalar offsets, original line/column; diagnostic must point to exact text.
2. Named-ID preservation through source edits via editor metadata or explicit anchors; prototype sequential block IDs are **not stable after inserted previous content**.
3. Unicode normalization for lookups only; do not silently alter subtitle original words, case or punctuation.
4. Proper Tagalog/Taglish segmentation: abbreviations (Gng., Dr.), ellipses, decimals, quotations, abbreviations with periods, emotive interjections; per-provider character limits; fall back to safe word boundaries when necessary.
5. Prevent command lookalikes buried in quoted story prose from being parsed. Escape semantics must be explicit and tested.
6. Deterministic compiler passes: lex/parse → identities → source blocks → utterance suggestions → cue references → default resolution → semantic validation → canonical IR → package.
7. Versioned grammar extension discovery, no arbitrary scripting/eval.

## Advanced features reserved but **not implemented**

- Named blocks/anchors: `@line id=juan_question speaker=c1`, `@at juan_question:end ...` (syntax provisional).
- Speech word/phrase alignment marks with required capability or automatically downgraded coarse trigger contract.
- `@show`, `@hide`, `@camera`, `@animate`, `@sfx`, `@ambience`, `@transition`, `@hold`, custom character asset/voice refs.
- Named simultaneous tracks and branching choices (not required for MVP).
- Multi-lingual pronunciation overrides and speech rate preferences.

## Compiler output sketch

```
Story {
 formatVersion; sourceFingerprint; chapters[];
 characterRegistry[];
 scenes[] {
   logicalId, instanceId, backgroundRef,
   blocks[] { blockId, speakerId, originalText, normalizedSpeechText,
              utterances[] { utteranceId, normalizedText, constraints } },
   cues[] { cueId, anchor:{ blockId, edge:start|end }, action, target, value }
 }
}
```

No OS file paths or GL textures appear inside canonical IR.

## Conformance example

```
@scene classroom
@bg school.classroom
@character c1 name="Juan" sex=m
@character c2 name="Pedro" sex=m
@n
Pumasok si Juan sa klase. Nakita niya si Pedro.
@c1
Pedro, gising!
@c2
Bakit, Juan?
@after c1 expression=confused
```

The final cue must trigger after `c2`'s **speech** finishes and change `c1` to `confused`. The API must not incorrectly apply it to the last sentence emitted by the segmenter from the previous narration block.

## AI authoring contract

AI should prefer `@n`, `@c1m`, `@c2f`, uncomplicated prose and optional scene markers. It must not invent media filenames, exact voice IDs, or unsupported directives. After generating a chapter, run `storytool` and repair syntax/semantic diagnostics while preserving dialogue text. Unknown content should be left generic, not fabricated.
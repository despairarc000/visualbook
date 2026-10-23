# Script & Compiler Contract — current design

**Status:** ACCEPTED minimal UX; detailed grammar PROVISIONAL. The [v0.3 grammar research](../history/v0.3/contracts/03_SCRIPT_LANGUAGE_V0_3.md) is preserved, not automatically frozen production syntax.

## Format levels

1. `.txt` literal story: narrator by default. The bytes `@c1` are literal text unless user explicitly chooses to interpret script syntax. No silent ambiguous parsing.
2. `.story.md` easy structured text with speaker markers; supports narration paragraphs, `@scene`, `@bg`, named character overrides and speaker-relative `@before/@after` cues.
3. `.storypack` versioned archive with source, compiled IR, resources and capability manifests. Does not introduce a different story engine.

Example:

```text
@n
Isang araw, dumating sina Juan at Pedro.

@c1m
Pedro, may assignment ka ba?

@c2m
Meron, sa panaginip ko!

@after c1 expression=confused
```

## Semantic rules

- First `@cN[m|f]` creates ID `cN` with optional metadata; later `@cN` refers to same identity.
- A marker persists until a new speaker marker. Inline marker and block marker forms are allowed by design; exact whitespace/escaping tests must be specified.
- `@n` is implicit narrator, not `c0`. Text before first speaker is narrator.
- Character sex metadata must be consistent; conflicting changes report a warning/error with source location, not silent mutation.
- Wraps within a spoken paragraph do not create TTS clips by themselves. Preserve the original authored text separately from normalized speech form.
- A speech **block** is an authored speaker turn; one block may have multiple adaptive synthesized **utterances**.
- `@before` resolves to the next speech block start; `@after` to the immediately preceding block's *last audible utterance end*. Invalid anchor cannot be guessed.
- Every cue is typed, with compatibility and fallback behavior declared in IR.
- Missing character/background description activates coherent fallback, not a fabricated author assertion.

## Compiler passes (planned)

Tokenize with source spans -> parse and preserve literals -> build implicit symbols -> normalize scene boundaries -> resolve character/scene/resource registry -> convert text blocks to utterance plans -> bind cues to stable logical anchors -> validate resource types/capabilities -> emit versioned canonical IR + structured diagnostics.

Never permit general-purpose embedded code execution. Compiler should be deterministic given source, catalog and config; AI-assisted creative direction belongs before compiler, not inside its deterministic passes.

## Explicit OPEN issues

Exact stable identity across editing, quote/abbreviation-aware Tagalog segmentation, literal/directive opt-in for .txt, forward references, advanced triggers, scene revisit identity, cross-chapter ref validation and formal schema/version migration.

## Acceptance fixtures

Bare text, two speakers with m/f metadata, wrapped paragraph, inline marker, unknown directives, escaped `@@`, wrong character reference, invalid after/before anchors, cue on multi-utterance block, emoji/Unicode text, no voice fallback, long document.

Historical Python prototype reportedly passed tests as of v0.3 research; archived evidence must remain labeled *simulated prototype only*.

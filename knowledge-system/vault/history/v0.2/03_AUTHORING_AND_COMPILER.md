# 03 — Authoring Grammar, AI-First Compilation, and Story Packages

All syntax below is **DRAFT** and exists to resolve architecture, not to claim a working interpreter.

## A. Progressive modes, single canonical IR
1. `.txt`: 100% ordinary story prose; imported as narrator by default. Heuristic scenes optional and conservative.
2. `.story.md` Basic: Markdown-like blocks with `@n`, `@c1m`, `@c2`, `@scene`, `@bg`, `@show`, `@after`. Auto TTS segmentation and visuals.
3. `.story.md` Advanced: named cues, named utterances, conditional branches, parallel effects, metadata declarations, explicitly authored scene/layout/voice settings.
4. `.storypack`: ZIP-compatible distributable bundle of source/compiled data + optional assets/voice packs + checksums, licenses, version/capabilities.

No separate renderers per mode. Source text remains the canonical subtitle content, with optional pronunciation transformations stored independently.

## B. Proposed basic grammar
```
@n             // narrator
@cN[suffix]?   // @c1, @c2m, @c3f; active speaker until changed
@c1 Kumusta?   // inline single-line alternative
@scene classroom
@bg classroom
@show c1 left
@hide c1
@after c1 expression=surprised
@before c2 expression=neutral
@transition fade
```
- `@n` is a reserved narrator ID. `cN` positive numeric speaker identities, persisted across chapters.
- `m` or `f` on *first reference* supplies optional default sex metadata, not the voice ID, display name, or separate character identity.
- `@c1` after `@c1m` references same person. Any conflicting `@c1f` declaration later emits conflict diagnostic; no silent overwrite.
- A speaker marker on its own line governs subsequent paragraphs until the next marker; text on same line is one new speech block.
- Blank lines separate blocks but not characters; wrapped lines within a paragraph remain the same speech block.
- Text beginning with `@` in narrative must have an escaping rule (proposed `@@` literal) and require a line-start recognized directive for parsing. Unknown `@` commands are syntax errors, not silently spoken.
- Prose preceding the first marker defaults to narrator (optional warning in annotated scripts). A purely unannotated `.txt` always narrates via `@n`.
- Scene is optional; `default_scene` is neutral, plus conservative compatible background choice if explicit tags or reliable cues exist.
- `@after` following a speech block attaches to **end of the most recent authored speech block**, not to the end of whichever sentence segmentation currently creates. `@before` ahead of a speech block attaches to **start of next authored block**. A directive with no unambiguous anchor is a validation error.
- `@show` character token must be `c1` etc, while display names remain metadata. No `@show Juan` without a declared alias mapping.
- Require explicit scene changes or headings when an AI wants guaranteed scene cuts; heuristic scene detection is a non-binding authoring suggestion, not runtime truth.

## C. Canonical examples
```
@n
Isang araw, nagkita sina Juan at Pedro sa paaralan.

@c1m
Pedro, bakit hindi ka pumasok kahapon?

@c2m
Kasi Linggo kahapon!

@after c1 expression=confused

@n
Napakamot si Juan sa ulo.
```
Auto-created `c1`,`c2`, narrator; labels generic unless optional `@character c1 name=Juan` / `@character c2 name=Pedro` metadata supplied. Do not guess mapping from nearby prose unless AI authoring explicitly commits it to source.

**Optional advanced scene**
```
@scene classroom
@bg school.classroom
@character c1 name=Juan
@character c2 name=Pedro
@show c1 at=left
@show c2 at=right

@line id=juan_q speaker=c1
Pedro, bakit hindi ka pumasok kahapon?

@at juan_q:end c2 expression=nervous
@line id=pedro_a speaker=c2
Kasi Linggo kahapon!
@at pedro_a:end c1 expression=confused
```
`@line` and `@at` illustrate the intended semantic model, not final grammar. Names and references must be formalized before compiler implementation.

## D. Parser/validator/compiler passes
1. `Detect & decode`: UTF-8/BOM, sane size, TXT vs annotated vs archive, newline normalization preserving original text offsets.
2. `Parse syntax`: line/token-based grammar -> typed AST with precise source spans. Do not run regular-expression substitution as a parser.
3. `Register identities`: collect all character/scene IDs, assign implicit characters, resolve annotations; reject conflicting declarations.
4. `Build speech blocks`: speaker attribution, authored paragraph boundaries, preserved punctuation and dialogue.
5. `Semantic segmenter`: split by natural sentence/clause based on abbreviations/quotes/language, preserve parent block ID; limit chunk by backend `maxSpeechInputLength` and synthesis suitability.
6. `Create cues`: attach `@after`, `@before` to stable block anchors; validate explicit advanced references, ordering, unresolved targets and supported capabilities.
7. `Resolve defaults`: generic background, character style, speaker voice *intent*, subtitle styling, transitions, constraints. Enforce semantic no-fabrication rule.
8. `Validate assets`: expected type, existence, legal dependencies, fallback target, license metadata. Distinguish optional vs required failures.
9. `Produce canonical IR`: deterministic serialization with stable IDs or ID map migration. Include source map for AI repair/editor navigation.
10. `Package`: verify graph and hashes, write atomically. Optional pre-synthesized segments as derived/cache data, not necessarily packed.

**Determinism**: Running compiler twice on exactly the same sources + catalog + compiler config produces the same canonical IR. AI inference is a separate optional, reviewable *authoring transform*; not hidden nondeterminism within runtime compilation.

## E. AI workflow
```
User request -> Outline -> Cast registry -> Chapter drafts
       -> Scene/art direction suggestions -> Script text
       -> Local compiler -> machine-readable diagnostics
       -> AI assisted bounded repair -> preview -> package
```
- Provide tool-call/schema contract and fixed dictionary of IDs/verbs.
- Return diagnostics with code, severity, sourceSpan, explanation, failed constraint, safe suggested fix.
- AI repairs keep dialogue/story text unchanged unless explicitly permitted. Never invent assets present on disk merely because a prompt described them.
- Linter should flag untagged dialogue, giant speech blocks, inconsistent c1/c2 metadata, dead cues, deep fallbacks, problematic aspect ratios, unavailable languages and missing rig profiles.
- Export validation results even when the package uses all fallbacks; grade completeness independently from validity.
- Example commands for CLI eventually: `storytool lint sample.story.md --json`, `storytool compile ...`, `storytool preview ...`, `storytool pack ...`, `storytool audit-assets ...` (not implemented).

## F. Two kinds of completeness
- **Narrative completeness**: all text is attributed, ordered, and playable as spoken segments or exposed as text-only if speech is unavailable; branching is coherent.
- **Production completeness**: desired assets, effects and voices present. A source can be narratively complete but aesthetically basic.

Warnings such as "used generic background for 5 scenes" must not be classified as errors. A missing branch or unknown directive must be an error.

## G. Package design notes
- Bundled full package and app-dependent lightweight reference package both supported; declare offline readiness explicitly.
- JSON manifest with language, title, creator, formatVersion, requires[], optional[], resource manifest, content hash and declared license terms.
- Resources can be `story://assets/foo`, `builtin://...`, `pack://...` logical URIs as a PROPOSAL; no access to arbitrary filesystem or remote URI in scripts.
- Voice model bundles require compatibility manifest, integrity checks, license, model family, token/config dependencies and resource policy. Do not import arbitrary model code.
- Story permissions should be zero by default; only trusted app code may access storage, network or sensors.

## H. Minimal prompts for an AI creator
```
Produce a valid .story.md for a Tagalog funny story.
Use only @n, @c1m, @c2m, @scene and @after directives unless more is required.
Do not specify timings in seconds. Every narrator/dialogue paragraph must belong to exactly one speaker.
Only refer to characters that have appeared or are implicitly created by @cN.
Keep names optional, preserve text exactly, make visual instructions optional.
Write scenes for easy narration; do not invent asset file paths.
```
Advanced AI agents receive the actual versioned grammar and asset catalog instead of generic prose-only instructions.
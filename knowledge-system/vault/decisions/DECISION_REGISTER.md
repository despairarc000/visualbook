# VisualBook Decision Register

**Rule:** decisions require rationale, alternatives, tests, consequences and status. Product intent is not automatically a settled implementation.

| ID | Decision | Status | Basis / next verification |
|---|---|---|---|
| ADR-001 | Narration-led story progress with audible position | ACCEPTED | User requirement; real audio adapter proof pending |
| ADR-002 | Sentence/adaptive utterance segmentation | ACCEPTED | No word callbacks required for boundaries; real prosody test pending |
| ADR-003 | `@n/@c1/@c2m/@c3f` minimal authoring | ACCEPTED | User preference; formal grammar still provisional |
| ADR-004 | Implicit cast and generic coherent defaults | ACCEPTED | User preference; first visual batch not produced |
| ADR-005 | One canonical IR for TXT/annotated/package tiers | ACCEPTED | Avoid parallel engines; production schema pending |
| ADR-006 | Pure portable compiler and playback reducer | PROVISIONAL | Python proof; Kotlin/module contracts pending |
| ADR-007 | Android Compose + Media3 + libGDX | PROVISIONAL | Must benchmark rendering/rotation/concurrent TTS |
| ADR-008 | Prepared speech clip clock preferred, live TTS adapter retained | PROVISIONAL | Device gap/seek benchmark pending |
| ADR-009 | Storybook-flat-v1 generic visual style | PROVISIONAL | Need actual assets and design review |
| ADR-010 | Voice providers pluggable, no fixed default Tagalog model | ACCEPTED | License, language support and device tests open |
| ADR-011 | Separate main planning truth from implementation branches | ACCEPTED workflow | Avoid unreviewed architecture/code coupling |
| ADR-012 | Typed fallbacks; no arbitrary script execution | ACCEPTED | Safety/reproducibility |
| ADR-013 | Plain .txt does not auto-execute script markers | PROVISIONAL | Offer explicit import choice/extension if needed |
| ADR-014 | All missing creative detail has neutral fallback, not invented plot | ACCEPTED | User requirement interpreted conservatively |

**Disagreement protocol:** propose new ADR, cite current authority/evidence, list migration and behavior impact; do not erase old decision without historical trail.

| ADR-015 | Introduce a versioned, strict canonical IR boundary; validate schema and cross-references, but keep 0.4-draft provisional | PROVISIONAL | [IR research](../design/CANONICAL_IR_V04.md); pure Python test evidence is not Android evidence |
| ADR-016 | IDs reproducible for unchanged source but not stable across edits; bookmarks must invalidate or migrate explicitly | PROVISIONAL | Provisional ordinal IDs; stable content identity and edit migration are open |

| ADR-017 | Test first Android vertical slice with platform Canvas + procedural generic assets, without locking final renderer | PROVISIONAL | [Runnable story player source](../../../implementation/visualbook-player/README.md); real-device readability/performance to measure |
| ADR-018 | Implement a narrow portable script model and sentence-clock state machine before full canonical IR Kotlin port | PROVISIONAL | Distinguish explicit prototype grammar from production 0.4 schema; future migration tests required |

| ADR-019 | Allow authored visual presentation styles to switch mid-story through scoped start/end markers (`@chibi`/`@end`), independent of speaker identity and audible narration cursor | ACCEPTED (PROTOTYPE) | [Scoped style contract](../design/SCOPED_ART_STYLES_V02.md); only normal and chibi implemented |

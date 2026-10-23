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

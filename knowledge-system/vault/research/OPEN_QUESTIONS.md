# Open Questions / Investigation Backlog

**Status:** OPEN unless otherwise tagged. Reassess at each checkpoint.

| Priority | Question | Blocking | Evidence needed |
|---|---|---|---|
| P0 | Which legally distributable offline Tagalog voice meets quality/performance? | Audible offline first-run | Source license, supported phones, subjective intelligibility, objective memory/latency |
| P0 | Does sentence-by-sentence TTS create audible discontinuities? | Narration quality | Sample recordings, p50/p95 gaps, punctuation and voices |
| P0 | How to handle audio focus/background service/rotation on target Android? | Usable Android player | Device instrumentation incl screen-off |
| P0 | What is frozen canonical IR and stable block ID policy? | Authoring and checkpoints | Golden fixtures, edit/migration tests |
| P0 | How to handle silence or visual-only scenes with narration clock? | Event semantics | Typed silent beat and resumption/seek tests |
| P0 | Distinguish absolute script vs user overrides when asset/voice unavailable | Trust / compatibility | Precedence table and conflicting settings tests |
| P1 | Is libGDX best for Android renderer? | Renderer selection | Frame/memory comparisons vs Compose Canvas |
| P1 | Exact resource import/model RAM quotas on 4GB phone? | Security/performance | Worst-case fuzz and device measurements |
| P1 | First generic character rig vs whole-sprite implementation? | Content production | Two-character prototyping quality/performance |
| P1 | How to map optional auto-inferred context without fabricating scenes? | Author UX | Confidence rules and neutral fallback tests |
| P1 | Which packaged model families are safe/realistic? | Voice pack extensions | Decoder/API/rights/memory benchmark |
| P2 | Interactive branches and choices, advanced camera, parallax? | Advanced stories | Explicit scope/graph interpretation |

Research results belong in dedicated evidence reports with source, access date, device/build, commands and outcome. Don't close questions through reasoning alone.

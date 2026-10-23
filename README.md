# VisualBook

**Status (2026-10-23): architecture research / prototype, not yet an Android player.**

VisualBook is an **offline-first Android cinematic storytelling engine**: a plain story or tiny `@n / @c1 / @c2m / @c3f` speaker markers should compile into a complete, potentially generic illustrated and captioned experience. More detailed scripts may add backgrounds, visual assets, voice assignments, scene transitions and audio-anchored effects without changing the engine.

Narration's **audible position** drives story progress. Sentence/utterance boundaries work when word callbacks are absent. Unspecified visual production details are filled by typed, coherent built-in defaults. A high-quality, redistributable offline Tagalog TTS model has **not** been validated.

## Start here

1. [NAVIGATION.md](NAVIGATION.md) — whole-project clickable index and reading routes.
2. [IDEA_PLAN.md](IDEA_PLAN.md) — product intent, requirements, boundaries and quality bar.
3. [IMPLEMENTATION_PLAN.md](IMPLEMENTATION_PLAN.md) — phased architecture, dependencies, gates and tests.
4. [AGENTS.md](AGENTS.md) — mandatory agent/assistant operating procedures.
5. [Current checkpoint](knowledge-system/vault/checkpoints/CURRENT.md) — verified state, blockers, next steps.

### Knowledge and automation

- [Markdown vault](knowledge-system/vault/INDEX.md) — design decisions, research, contracts, evidence and historical v0.2/v0.3 docs.
- [ICM context routing](knowledge-system/icm/README.md) — focused reading routes and doctor commands.
- [Graphify dependency maps](knowledge-system/graphify/README.md) — conceptual nodes and validated relationships.
- [Validation](tools/README.md) — link/graph/routing integrity.
- [Legacy v0.3 research contracts](knowledge-system/vault/history/v0.3/00_START_HERE.md) — preserved original documents.
- [Legacy v0.2 research and 101 planned asset IDs](knowledge-system/vault/history/v0.2/00_START_HERE.md) — historical only.

No APK, downloaded/redistributable neural voice, finished illustrated asset set or device benchmark is claimed. The archived v0.3 Python parser and fake narration reducer are committed and reproduced by GitHub CI (29 historical tests). Original sources were audited against source SHA-256. This does not mean an Android app exists.

## Samples and quality

- [Tagalog anthology input sample](samples/tagalog_tts_anthology.txt) — long-form narrator stress input.
- [Historical v0.3 parser and reducer](knowledge-system/vault/history/v0.3/prototype/) — pure-Python research only.
- [Knowledge CI](.github/workflows/knowledge-check.yml) — validation of links, ICM routes, Graphify, golden fixtures and the archived experimental tests.

# VisualBook — AGENTS.md

Applies to the entire repo. Read before editing anything; scoped `AGENTS.md` adds narrower guidance.

## Mission

An offline-first Android narrative-led animated story player. Plain text is usable; `@n`, `@c1`, `@c2m`, `@c3f` are sufficient to create implicit participants and differentiate speech. **Audibly played** speech (not synthesized text production) drives visual events and story progress. A coherent generic audiovisual presentation is the default; deep scripting enhances it.

## Startup / ICM routing

1. Read `README.md` → `NAVIGATION.md` → `IDEA_PLAN.md` → `knowledge-system/vault/checkpoints/CURRENT.md`.
2. Read `IMPLEMENTATION_PLAN.md` and route current task via `python3 knowledge-system/icm/icm.py route <task>` when Python is available; or consult `knowledge-system/icm/routes.json`.
3. Inspect `knowledge-system/graphify/graph.json` and design docs for touched module dependencies.
4. For evidence-sensitive statements, inspect latest relevant research and historical sources; don't confuse historical planned behavior with working code.

## Priority and status

Precedence: confirmed user intent and current source-code/test evidence > canonical current requirements & accepted ADRs > current provisional plans > older historical docs. Labels: CONFIRMED / ACCEPTED / PROVISIONAL / OPEN / DEFERRED / HISTORICAL / VERIFIED-TEST. An implementation claim requires command/device evidence.

## Work protocol

- Explore and plan before editing; identify interfaces and downstream edges; avoid duplicating work or changing unrelated files.
- Keep simple script marker semantics stable, preserve original story text, treat gender/sex labels as optional metadata independent of voice.
- No unconditional promise of offline Tagalog voice: model/license/performance gate remains open.
- Keep compiler and state reducer portable, independent of Android, rendering and concrete TTS.
- No untrusted script execution; asset fallback must be typed; preserve transparent source/voice licensing.
- For changes: revise tests, graph edges where architecture changed, decision log and checkpoint. If a change contradicts canon, document ADR and migration plan.
- Never overwrite/vaporize v0.2/v0.3 snapshots to make the story look more complete.
- For user-facing updates distinguish spec, prototype, simulated tests, real-device tests and app builds.
- Handoff with `knowledge-system/vault/templates/HANDOFF.md` including refs, changes, evidence, issues and next action.

## Mandatory checks before merging

```bash
python3 tools/doctor.py
python3 knowledge-system/icm/icm.py doctor
python3 knowledge-system/graphify/graphify.py validate
```

Run production tests on relevant implementation branches; never claim those tests ran if that branch/code isn't present. Update `knowledge-system/vault/checkpoints/CURRENT.md` after substantive changes.

## Branching

`main` should remain implementation-neutral unless task explicitly asks otherwise; experimental implementation on branches with PRs. Preserve user's work; no force pushes or destructive cleanups.

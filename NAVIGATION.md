# VisualBook Navigation — repository knowledge map

**Read this after [AGENTS.md](AGENTS.md); the links below are entrypoints, not priority overrides.**

## Quick routes

| Goal | Read these in order |
|---|---|
| Understand project | [Idea plan](IDEA_PLAN.md) → [Requirements](knowledge-system/vault/requirements/PRODUCT_REQUIREMENTS.md) → [Current checkpoint](knowledge-system/vault/checkpoints/CURRENT.md) |
| Plan next implementation step | [Implementation plan](IMPLEMENTATION_PLAN.md) → [Decision register](knowledge-system/vault/decisions/DECISION_REGISTER.md) → [Phase gates](knowledge-system/vault/plans/PHASE_GATES.md) |
| Understand authoring | [Authoring design](knowledge-system/vault/design/SCRIPT_AND_COMPILER.md) → [historical v0.3 grammar](knowledge-system/vault/history/v0.3/contracts/03_SCRIPT_LANGUAGE_V0_3.md) |
| Canonical IR v0.4 research | [IR contract](knowledge-system/vault/design/CANONICAL_IR_V04.md) → [Schema](schemas/story-ir-v0.4-draft.schema.json) → [Bridge](research/prototypes/v04/story_ir.py) → [Tests](research/prototypes/v04/tests/test_story_ir.py) |\n| Android speech experiment | [Experiment protocol](research/experiments/android-audio/EXPERIMENT.md) → [voice risks](knowledge-system/vault/research/TTS_AND_ANDROID_RISKS.md) |\n| Fix timing or playback | [Narration model](knowledge-system/vault/design/NARRATION_AND_PLAYBACK.md) → [historical event contract](knowledge-system/vault/history/v0.3/contracts/04_NARRATION_CLOCK_AND_EVENTS.md) |
| Plan art and effects | [Content design](knowledge-system/vault/design/BUILTIN_CONTENT_PLAN.md) → [101-ID seed](knowledge-system/vault/history/v0.2/builtin_asset_catalog_seed.json) |
| Explore long Tagalog sample | [Anthology sample](samples/tagalog_tts_anthology.txt) → [Story sample guide](samples/README.md) → [Parser prototype](knowledge-system/vault/history/v0.3/prototype/storytool.py) |
| Samsung voice-settings screenshots | [Observed voices and next engine test](research/experiments/android-audio/SAMSUNG_TTS_SETTINGS_SM-A066B_2026-10-24.md) → [Filipino timeout log](research/experiments/android-audio/DEVICE_TEST_SM-A066B_V03_TIMEOUT_2026-10-24.md) |\n| Filipino voice timeout (-7) | [Samsung v0.3 device diagnosis](research/experiments/android-audio/DEVICE_TEST_SM-A066B_V03_TIMEOUT_2026-10-24.md) → [Voice research](knowledge-system/vault/research/TTS_AND_ANDROID_RISKS.md) |\n| Offline Filipino probe v0.3 | [Build and device-test instructions](research/experiments/android-audio/BUILD_EVIDENCE_V03_2026-10-24.md) → [App controls](research/experiments/android-audio/probe/README.md) |\n| SM-A066B v0.2 observations | [Successful user device stress test](research/experiments/android-audio/DEVICE_TEST_SM-A066B_V02_2026-10-24.md) → [v0.3 voice comparison](research/experiments/android-audio/probe/README.md) |\n| Android TTS probe | [How to run](research/experiments/android-audio/probe/README.md) → [Verified build evidence](research/experiments/android-audio/BUILD_EVIDENCE_2026-10-24.md) → [Experimental protocol](research/experiments/android-audio/EXPERIMENT.md) |\n| Evaluate Tagalog voice | [TTS research](knowledge-system/vault/research/TTS_AND_ANDROID_RISKS.md) → [old benchmark plan](knowledge-system/vault/history/v0.3/contracts/06_VOICES_AND_PERFORMANCE.md) |
| Inspect real code relationships | [AST Graphify](knowledge-system/graphify/scan_code.py) → [Graphify docs](knowledge-system/graphify/README.md) |
| Inspect dependencies | [Graphify index](knowledge-system/graphify/README.md) → [Graph data](knowledge-system/graphify/graph.json) |
| Agent task context | [ICM router](knowledge-system/icm/README.md) → [routes](knowledge-system/icm/routes.json) → [checkpoint](knowledge-system/vault/checkpoints/CURRENT.md) |
| Provenance / historical claims | [Evidence register](knowledge-system/vault/evidence/EVIDENCE_REGISTER.md) → [v0.3 research](knowledge-system/vault/history/v0.3/research/01_FINDINGS_AND_SOURCES.md) |
| New session handoff | [AGENTS.md](AGENTS.md) → [current checkpoint](knowledge-system/vault/checkpoints/CURRENT.md) → [handoff template](knowledge-system/vault/templates/HANDOFF.md) |

## Canonical / historical boundary

- **Canonical live**: `IDEA_PLAN.md`, `IMPLEMENTATION_PLAN.md`, `knowledge-system/vault/{requirements,design,decisions,plans,checkpoints}`.
- **Historical evidence**: `knowledge-system/vault/history/v0.2` and `v0.3` are *snapshots*; they are not silently promoted to current contracts.
- **Implementation**: future Android code belongs on a dedicated implementation branch; do not confuse planning prototypes with production-ready functionality.

## Commands

```bash
python3 knowledge-system/icm/icm.py route authoring
python3 knowledge-system/icm/icm.py route narration
python3 knowledge-system/icm/icm.py doctor
python3 knowledge-system/graphify/graphify.py validate
python3 tools/doctor.py
```

If a command is not available in a partial checkout, record that as an environmental limitation.

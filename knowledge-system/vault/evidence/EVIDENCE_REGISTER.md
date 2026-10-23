# VisualBook — Evidence and Source Archive

**Status:** updated 2026-10-23. Archived work is documentation and Python experiments, not an Android application.

| ID | Artifact | Evidence scope |
|---|---|---|
| EV-001 | [Original v0.2 planning](../history/v0.2/00_START_HERE.md) | Early architectural ideas and 101 planned logical content entries, no binary media |
| EV-002 | [Original v0.3 research overview](../history/v0.3/00_START_HERE.md) | Prototype/compiler/reducer concept and research dates |
| EV-003 | [V0.3 grammar](../history/v0.3/contracts/03_SCRIPT_LANGUAGE_V0_3.md) | Explicit minimal marker and cue semantics of Python proof |
| EV-004 | [Original storytool](../history/v0.3/prototype/storytool.py), [fake playback](../history/v0.3/prototype/fake_playback.py) | Historical proof source, Python standard library, no TTS/Android |
| EV-005 | [Archived parser/playback tests](../history/v0.3/tests/test_storytool.py), [playback tests](../history/v0.3/tests/test_fake_playback.py) | 29 simulated tests reproduced by GitHub CI |
| EV-006 | [Sample script](../history/v0.3/examples/minimal.story.md) / [compiled IR](../history/v0.3/examples/minimal.compiled.json) | Golden compiler fixtures, not production IR |
| EV-007 | [Tagalog TTS test corpus](../history/v0.3/research/TAGALOG_TTS_TEST_CORPUS.tsv) | 30 phrases for future listening/latency tests |
| EV-008 | [100+ planned assets](../history/v0.2/builtin_asset_catalog_seed.json) and [original audit](../history/v0.3/research/ASSET_CATALOG_AUDIT.json) | 101 logical assets / 0 shipped binaries |
| EV-009 | [Long Tagalog anthology](../../../samples/tagalog_tts_anthology.txt) | Large plain-text stress input, not reviewed TTS output |
| EV-010 | [Original v0.3 hashes](../history/v0.3/CHECKSUMS_AND_RESULTS.json) | Recorded baseline source checksums; 7 tested byte-identical by CI |
| EV-011 | [Knowledge CI](../../../.github/workflows/knowledge-check.yml) | Executable ICM/Graphify/link integrity and Python tests |

## Provenance safeguards

- Original v0.2/v0.3 docs are preserved as historical snapshots; new live contracts are in `vault/design`, `requirements`, and `decisions`.
- The former experimental scripts and fixtures are now in GitHub, with a SHA-256 audit for seven source files. The original complete ZIP from the earlier chat was separately available to the project initiator; GitHub stores its unpacked research contents rather than a ZIP binary.
- Do not assert usable voices, finished visual art or real Android device tests were performed. A diagnostic APK **build** was verified on 2026-10-24; its runtime behavior remains untested.
- For each future experiment record exact commit, device, OS build, tool versions, command and reproducible result.

## Verified CI reference (before this evidence update)

[GitHub Actions run 39965364380](https://github.com/despairarc000/visualbook/actions/runs/39965364380): 10 knowledge/fixture tests and 29 preserved Python prototype tests pass; 9 valid ICM routes, 32 conceptual Graphify nodes/40 edges, 28 live Markdown files with no missing links, seven original sources SHA-256-identical. The new AST code scanner reports 58 nodes / 104 relationships, no parse errors. This is a reproducible source-structure observation, not a phone performance measurement.

## Android diagnostic build evidence (2026-10-24)

- **EV-012:** [Android TTS probe build evidence](../../../research/experiments/android-audio/BUILD_EVIDENCE_2026-10-24.md). GitHub Actions run `40019414406` succeeded, source SHA `21aa89e8d5570c73d7b12054f4c5ea03489863a4`; experimental APK archived as a short-lived GitHub Actions artifact. Not tested on an actual phone.
- **EV-013:** [PR #1 merged](https://github.com/despairarc000/visualbook/pull/1) and [knowledge CI success](https://github.com/despairarc000/visualbook/actions/runs/39968958633). Canonical IR/schema checks pass in CI but production format and real-world timing remain provisional.

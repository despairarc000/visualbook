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

- **EV-014:** [First Samsung Galaxy A06 5G device log analysis](../../../research/experiments/android-audio/DEVICE_TEST_SM-A066B_2026-10-24.md). Actual live 3x2 completion and prepared-file 2x playback callbacks, user-reported UI switch freeze; no root cause trace or offline Tagalog voice verdict. Experimental v0.2 fix requires real-device validation.

- **EV-015:** [v0.2 TTS freeze-mitigation APK CI/build evidence](../../../research/experiments/android-audio/BUILD_EVIDENCE_V02_2026-10-24.md), [merged PR #3](https://github.com/despairarc000/visualbook/pull/3). Compiles, produces APK; NOT real-device tested for freeze correction.

- **EV-016:** [Samsung SM-A066B TTS Probe v0.2 successful stress test](../../../research/experiments/android-audio/DEVICE_TEST_SM-A066B_V02_2026-10-24.md), user states "Smooth" after repeated live/prepared/stop switching. One startup UI gap; no recurring gap under stress. No proof of voice quality or root-cause isolation.

- **EV-017:** [v0.3 Android Filipino voice probe compiled](../../../research/experiments/android-audio/BUILD_EVIDENCE_V03_2026-10-24.md), [CI build](https://github.com/despairarc000/visualbook/actions/runs/40027370638). APK archive inspected; no phone-based v0.3 result yet.

- **EV-018:** [SM-A066B TTS Probe v0.3 Filipino synthesis failures](../../../research/experiments/android-audio/DEVICE_TEST_SM-A066B_V03_TIMEOUT_2026-10-24.md): actual Samsung TTS engine `com.samsung.SMT`, two confirmed local `fil-PH` voice selections, live/prepared errors -7 approximately two seconds after queue; English baseline works. Root cause of timeouts and true offline voice availability OPEN.

- **EV-019:** [SM-A066B Samsung TTS Settings screenshots distilled](../../../research/experiments/android-audio/SAMSUNG_TTS_SETTINGS_SM-A066B_2026-10-24.md). Visible installed/configurable voice list only English (India), English (US), Hindi (India), Samsung TTS v3.5.02.16; no Filipino entry or Install voice data on that screenshot. No original screenshot binary committed to avoid unrelated app-name disclosure.

- **EV-020:** [SM-A066B Filipino 5-profile synthesis success](../../../research/experiments/android-audio/SM-A066B_FIVE_FILIPINO_VOICES_2026-10-24.md). Three full LIVE runs across `fic/fid/fie`, one PREP clip `cfc`, generic `fil-PH-language` partial callbacks; earlier failures `-7` now absent from this excerpt. Unknown actual TTS provider, offline mode, human-perceived voice differences/quality.

- **EV-021:** [Qualitative voice-distinctness feedback](../../../research/experiments/android-audio/SM-A066B_FIVE_FILIPINO_VOICES_2026-10-24.md): user says `cfc/fic/fid/fie` voices are "Kinda different, no same"; perceptible difference only; no scored quality, gender classification or guaranteed provider/offline claim.

- **EV-022:** [First actual story player prototype source](../../../implementation/visualbook-player/README.md), [Android CI workflow](../../../.github/workflows/visualbook-player-build.yml). Pure Java story compiler and playback cursor have executable tests; Android Java UI and procedural stage implemented on feature branch. Compile/runtime status must be reported from CI; **no physical-device or audio-visual synchronization measurement yet**.

- **EV-023:** [First VisualBook animated story Android build and binary checksum](../../../implementation/visualbook-player/BUILD_EVIDENCE_V01_2026-10-24.md). PR #9 merged; 26 pure Java assertions, Android Gradle build and knowledge validation all passed. **No phone install/visual-audio behavior tested yet**.

- **EV-024:** [First VisualBook player installed and exercised by user](../../../implementation/visualbook-player/DEVICE_TEST_V01_FIRST_SUCCESS_2026-10-24.md). User reports "Nice, it works" and `@c3` implicitly appears only during its spoken turn, then disappears. Real device visual success for dynamic guest, not full audio-sync or performance validation.

- **EV-025:** [Scoped art style grammar and implementation](../design/SCOPED_ART_STYLES_V02.md), [app source](../../../implementation/visualbook-player/README.md). User request for normal→chibi→normal expressive story transitions implemented on v0.2 feature branch; final CI and phone tests required before claiming working in practice.

- **EV-026:** [Scoped normal↔chibi art-style APK build evidence](../../../implementation/visualbook-player/BUILD_EVIDENCE_STYLES_V02_2026-10-24.md): merged PR #11, 42 pure Java checks, verified Android APK SHA-256. Device visual behavior remains unverified.

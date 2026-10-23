# Knowledge Validation Tools

No third-party Python dependencies. Run from repository root:

```sh
python3 tools/doctor.py
python3 knowledge-system/icm/icm.py doctor
python3 knowledge-system/graphify/graphify.py validate
python3 -m unittest discover -s tools/tests -v
python3 -m unittest discover -s knowledge-system/vault/history/v0.3/tests -v
python3 tools/archive_integrity.py --strict
```

`doctor.py` checks Markdown link targets in **live** docs (not intentionally frozen historical snapshots), ICM routes and graph data via subprocess, and basic provenance/core-path invariants. GitHub Actions runs the same checks on pushes and pull requests, but do not report CI passed until actual workflow result is inspected.

The original historical v0.3 parser/reducer tests are committed and run under `knowledge-system/vault/history/v0.3/tests`; they test a Python simulator, not Android TTS or real audiovisual output. SHA-256 of seven preserved research sources is enforced by CI.

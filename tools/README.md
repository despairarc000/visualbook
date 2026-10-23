# Knowledge Validation Tools

No third-party Python dependencies. Run from repository root:

```sh
python3 tools/doctor.py
python3 knowledge-system/icm/icm.py doctor
python3 knowledge-system/graphify/graphify.py validate
python3 -m unittest discover -s tools/tests -v
```

`doctor.py` checks Markdown link targets in **live** docs (not intentionally frozen historical snapshots), ICM routes and graph data via subprocess, and basic provenance/core-path invariants. GitHub Actions runs the same checks on pushes and pull requests, but do not report CI passed until actual workflow result is inspected.

When historic ZIP contents are fully restored, add their original parser/reducer tests to separate `research/prototypes` validation and record exact command/evidence.

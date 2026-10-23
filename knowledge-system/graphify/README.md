# Graphify — VisualBook architecture/navigation graph

Graphify represents **explicit** module relationships, evidence-to-decision associations and document locations, avoiding full-project rereads.

## Commands

```bash
python3 knowledge-system/graphify/graphify.py validate
python3 knowledge-system/graphify/graphify.py neighbors narration-cursor
python3 knowledge-system/graphify/graphify.py impact voice-resolver
python3 knowledge-system/graphify/graphify.py mermaid
```

[graph.json](graph.json) is the machine-readable source. [GRAPH.md](GRAPH.md) is an explanatory map. Nodes represent **design concepts and documents**, not automatically discovered classes/functions. Only map actual source code after code exists, with repository path and commit evidence. Graphify validation checks uniqueness, edges and pointed-to document paths.

## Graph semantics

- `depends_on`: module A requires B contract/service
- `controls`: A is authoritative for B's progression/state
- `feeds`: A supplies data to B
- `resolves`: fallback/content resolver path
- `documents`: document defines/intentionally describes concept
- `tests`: test/gate validates behavior
- `blocks`: open question blocks milestone

Update nodes/edges together with relevant ADR and ICM route. Missing evidence must be marked PROVISIONAL, never disguised as implemented code.

## Source-level Graphify (AST)

The hand-maintained architecture graph is complemented by an on-demand **real Python code scanner**:

```sh
python3 knowledge-system/graphify/scan_code.py --summary
python3 knowledge-system/graphify/scan_code.py --focus storytool
python3 knowledge-system/graphify/scan_code.py --json
```

It parses Python source under the configured folders, outputs file/function/class/import nodes and conservative local function-call relations. This is observational evidence from actual committed files, unlike proposed conceptual nodes. It deliberately does not claim Kotlin/Java AST support yet; when Android code exists, add a proper parser for that language rather than guessing by filename.

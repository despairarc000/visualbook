# VisualBook IR 0.4 — Research Prototype

**Research-only Python adapter**, not an Android application or production Kotlin model. [Canonical contract](../../../knowledge-system/vault/design/CANONICAL_IR_V04.md) and [JSON Schema](../../../schemas/story-ir-v0.4-draft.schema.json).

```bash
python3 research/prototypes/v04/story_ir.py knowledge-system/vault/history/v0.3/examples/juan_pedro.story.md
python3 -m unittest discover -s research/prototypes/v04/tests -v
```

The original v0.3 parser is loaded read-only as a separate module and lowered into typed versioned representation. Failing a legacy error or semantic validation does not produce a partial accepted story. Generated scenes/voices are logical defaults; there is no Android audio or rendered image.

Next: independent JSON-Schema standard validation, golden fixture versioning, stable edit IDs, Kotlin interface alignment and real-device audible-clock tests.

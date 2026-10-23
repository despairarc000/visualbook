# ICM — Intelligent Context Mapping

GitHub-native **targeted context routing**. This is a deterministic local Python helper, not an LLM memory database. It takes a topic, identifies relevant canonical documents and prints a focused reading route with checkpoint included.

## Usage

```sh
python3 knowledge-system/icm/icm.py route authoring
python3 knowledge-system/icm/icm.py route "offline tagalog voice"
python3 knowledge-system/icm/icm.py route rendering --json
python3 knowledge-system/icm/icm.py topics
python3 knowledge-system/icm/icm.py doctor
```

Definitions live in [routes.json](routes.json). Routes should reference **existing repository files** and prefer canonical current docs. Each task has trigger terms, ordered read paths, and a scope note. Root [AGENTS.md](../../AGENTS.md) and current [checkpoint](../vault/checkpoints/CURRENT.md) are always the first route content.

### Update protocol

When a document moves or architecture changes, update route map, node graph and checkpoint. If no route matches, use the `overview` route and search the [vault index](../vault/INDEX.md); do not dump the entire vault into the model's context. Branch-specific code routes may be added when working code exists. ICM doesn't fabricate evidence, silently ignore missing paths or overwrite context.

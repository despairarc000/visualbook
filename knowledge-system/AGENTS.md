# Knowledge-System Agent Scope

Root [AGENTS.md](../AGENTS.md) remains mandatory.

- The Markdown vault is the narrative source of knowledge. Treat the historical `history/v0.2` and `history/v0.3` directories as immutable research snapshots; fix canon through new files/ADRs, not retroactive edits.
- ICM is a **context-selection and routing layer**, not persistent assistant memory or secret embedding service. Update routes when documents move; doctor must ensure references exist.
- Graphify is an **explicit dependency/evidence graph**, not fabricated AST analysis. Only label code/test nodes as IMPLEMENTED when backed by committed working code.
- Vault documents must include status and evidence; hyperlink canonical paths, not chat-specific sandbox artifacts.
- After architectural changes, update `vault/checkpoints/CURRENT.md`, `vault/decisions/DECISION_REGISTER.md`, ICM routes and relevant graph edges.
- Keep script parser, narration clock and default asset contracts independent and separately testable.
- Run `python3 tools/doctor.py`, ICM doctor and Graphify validate before a knowledge PR.

# Cinematic Story Engine — Phase 1 Architecture Pack (Draft 0.2)

Date: 2026-10-23. Status: **planning draft, not an implemented player or finished file-format specification**.

## Vision
An offline-first Android narrative player that turns simple text or rich scripted packages into synchronized narration, subtitles, characters, backgrounds, motion, and sound. Narration position governs story progression. Authors need only text and, for multi-speaker dialogue, simple markers like `@n`, `@c1`, `@c2m`. Missing *optional* production details resolve to coherent built-in defaults. Advanced creators can override every layer without needing a separate player.

## Documents
- `01_SYSTEM_ARCHITECTURE.md` — reviewed decisions, runtime boundaries, narration synchronization, layout, files, character/voice systems, fallbacks, performance/security, decisions still open.
- `02_BUILTIN_CONTENT_LIBRARY.md` — practical generic backgrounds, characters, props, animation, effects, audio, look-and-feel and composition specifications.
- `03_AUTHORING_AND_COMPILER.md` — proposed progressive authoring grammar and canonical compiler contracts, examples, diagnostics, AI workflows.
- `04_TESTS_AND_PHASE_GATES.md` — validation matrix, architectural experiments and criteria for Phase 1 completion.
- `builtin_asset_catalog_seed.json` — initial *logical asset identifiers*, **not actual media files**.
- `examples/juan_pedro.story.md` — proposed simple-level authoring specimen; **not executable yet**.

## Conventions
- **LOCKED PRINCIPLE** means an intentional architectural contract agreed in discussion, still subject to implementation validation.
- **PROPOSED** means a concrete recommendation for review.
- **UNRESOLVED** means benchmarking or additional design is necessary.
- All `@` directives shown are **draft grammar** and do not claim to be executable.
- `m`/`f` on speaker markers are optional declared sex metadata used only for defaults; they are not the immutable character identity or guarantees of a voice's capabilities.

## Primary research and technical references
- Android TTS API: https://developer.android.com/reference/android/speech/tts/UtteranceProgressListener
- Android TTS synthesis: https://developer.android.com/reference/android/speech/tts/TextToSpeech
- Android Media3 background playback: https://developer.android.com/media/media3/session/background-playback
- libGDX scene2d and viewports: https://libgdx.com/wiki/graphics/2d/scene2d/scene2d ; https://libgdx.com/wiki/graphics/viewports
- Ren'Py images/layers and compositing: https://www.renpy.org/doc/html/displaying_images.html ; https://www.renpy.org/doc/html/layeredimage.html
- EPUB media overlays: https://www.w3.org/TR/epub-33/
- Sherpa-ONNX: https://github.com/k2-fsa/sherpa-onnx
- MMS Tagalog checkpoint and license (CC BY-NC 4.0): https://huggingface.co/facebook/mms-tts-tgl
- Kenney CC0 content and per-pack license confirmation: https://kenney.nl/support
- JSON Schema 2020-12: https://json-schema.org/draft/2020-12/
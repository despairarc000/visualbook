# 07 — Story Package Contract, Compatibility and Trust Boundaries

## Portable package (PROPOSED)

`.storypack` = ZIP-compatible container with `manifest.json`, `source/main.story.md`, `ir/story.json` (optional derived), `assets/*`, `licenses/*`, optionally `voices/*`. One package can reference explicitly installed shared asset/voice packs, but an external dependency makes it *not fully standalone*. A fully self-contained package must include all required licensed media and decoder-compatible voice dependencies, or target a guaranteed bundled app resource.

```
manifest.json
source/main.story.md
ir/story.json
assets/backgrounds/classroom.webp
assets/characters/juan.webp
assets/audio/door.ogg
licenses/asset_sources.json
voices/voice_manifest.json     # optional, data only
```

`manifest`: `format`, `formatVersion`, `id`, `title`, `language`, `requiredCapabilities[]`, `optionalCapabilities[]`, `entrypoint`, `resourceIndex[]`, `hashes`, `license/provenance`, optional `voiceModelRequirements`. Capability registry is versioned; unknown required capabilities reject package before play, unknown optional degrade by explicit policy.

## Logical resource references

Use stable `builtin://bg.school.classroom`, `story://assets/...`, `shared://voice/...` logical namespaces. No arbitrary `file://`, `content://`, `http(s)://` in untrusted script instructions; importing references is a separate user-authorized resolver step. Assets have type, role, dimensions and fallback IDs so missing items cannot be silently substituted across incompatible categories.

## Trust model

- **Untrusted:** downloaded/imported package prose, JSON, textures, audio, model weights, archive entry names, external URIs, declared model formats, embedded metadata. They cannot execute JS/Python/native plugins or request arbitrary filesystem/network/sensor privileges.
- **Trusted:** installed application code, bundled audited model runners, the runtime parser, the permission broker and importer.
- **Sanitization at boundaries:** compile story to constrained IR; decode external images/audio with size and codec caps; test model graph compatibility in restricted trusted runner and refuse unsupported model formats.

## ZIP risk limits (engineering proposals, not final numbers)

- Exact normalized archive entry paths only; reject absolute paths, `..`, symlink entries and case/unicode-confusable duplicate normalized targets. Never extract before validation.
- Cap entry count, nesting depth, single file size, total uncompressed bytes and compression ratio. Initial suggested limits: 1,000 entries, 100MB per decoded media file, 250MB cumulative uncompressed archive and 100:1 ratio; revise after testing large optional voice packs separately. Voice models require a distinct installation budget outside ordinary story archive limits.
- Reject encrypted/unexpected unsupported archives; checksum individual resources before trust and always verify the manifest's format.
- Cap decoder pixels (e.g. 8192 x 8192 absolute and smaller working texture budget), frame count and concurrent allocation; cap audio duration/channel count/samples.
- Unknown required directive/schema property is a semantic error, not an escape hatch. Keep an explicit version-migration tool and fixture tests.
- Use SAF import, copy to app-controlled staging area; validation preflight then **atomic commit** to content library. Never partially register imported package on failure.

## Compatibility behavior

- Two version dimensions: `sourceGrammarVersion` and `runtimeCapabilityVersion`. App upgrade must not silently reinterpret old `@after` semantics.
- Compiler emits canonical IR plus source and version fingerprints; app may recompile from archived source using pinned compatible compiler or deterministic migration.
- Maintain compatibility fixtures for older versions. When requiring a new advanced effect, package declares `requiredCapabilities`; otherwise optional effect falls back by documented behavior.
- Package ownership/license metadata travels with content; do not merge proprietary or unclear binaries into built-in app assets.

## Source and references

- https://developer.android.com/guide/topics/providers/document-provider
- https://developer.android.com/topic/performance/memory/guide/bitmaps
- https://json-schema.org/draft/2020-12/

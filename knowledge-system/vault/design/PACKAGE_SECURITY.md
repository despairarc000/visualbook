# Package Security and Compatibility

**Status:** ACCEPTED safety constraints; exact quotas remain OPEN until benchmarks.

ZIP-compatible `.storypack` may contain manifest, source, compiled canonical representation, optional artwork/audio, separate voice manifests, attribution and hashes. Story packages are untrusted input, not executable add-ons.

## Import contract

- Explicit package version, supported required/optional capabilities, entry chapter and declared resources.
- Normalize archive paths and reject traversal (`../`, rooted names, illegal separators), symlinks, duplicates, archive bombs, suspicious nesting, overly many files or unreasonable expansion ratios.
- Reject or quarantine unsupported formats; bound decoded image resolution, raw PCM duration, total on-disk size, animation event counts, model load RAM/time.
- No user files or network accesses from story directives; no embedded native code/plugins.
- Validate cryptographic hashes when declared. Do not infer license/ownership from media name.
- Keep original package read-only; put caches, progress and user override profiles outside it.
- Distinguish `REQUIRED_UNSUPPORTED` vs `OPTIONAL_UNAVAILABLE` for safe fallback.
- Protect subtitle content and user-provided stories from unconsented upload/telemetry.

## File format and revision

`format_version`, `compiler_version`, `content_fingerprint`, `requires`, `optional`, resources, scripts, voice references. Add migrations; never silently reinterpret old directives.

## Known future questions

Exact maximum archive/model budgets, trusted local voice installation flow, accessible content provider streaming, ZIP encryption policy, provenance verification and final binary types.

See [historical v0.3 package contract](../history/v0.3/00_START_HERE.md) for earlier summary.

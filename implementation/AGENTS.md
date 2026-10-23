# Scoped Instructions — Future Android Implementation

Parent [AGENTS.md](../AGENTS.md) applies first. No production Android app exists yet.

- Implement architecture only after relevant Phase 1 contract and Phase 2 experimental gate is satisfied or the branch is explicitly labeled as a disposable spike.
- Preserve pure `story-core` compiler/reducer independent of Android, libGDX, speech providers and UI; write conformance fixtures before wiring adapters.
- Do not treat historical Python proof as production Kotlin functionality.
- Media playback end, not synthesis finish, changes the narration cursor.
- For any graphics change review stage coordinate spaces, aspect-preserving layout, protected subtitles and fallback screen.
- Reproduce issues with test/trace. Measure TTS discontinuity, memory, CPU and frame budget on real low-memory Android hardware before declaring quality.
- Update graph, decision register, checkpoint and tests; submit isolated PR to main where appropriate.

# Shape Engine v0.4 — Android build and rig tests

**Status: VERIFIED-CI-BUILD, PHYSICAL PHONE PLAYBACK UNVERIFIED.**

- [Feature PR #13](https://github.com/despairarc000/visualbook/pull/13): merged as `8d79381eca3120bcc36c9f51e024559260cf0395`.
- Final feature SHA `9b0b05092fc755d53344ee9f2ab48b8fc2bdaadb` contains v0.4 Android code and docs. Only the documentation changed between passing code commit `3fdb244dccab03500410cdf321d3fdcfb9d9ff0a` and feature head.
- [Android CI run 40044298517](https://github.com/despairarc000/visualbook/actions/runs/40044298517), **SUCCESS**: `StoryCoreTest` 66 assertions passed; `VectorFigureTest` 76 assertions passed (**142 total**); Android `assembleDebug` reported **BUILD SUCCESSFUL**. [Final feature run 40044410923](https://github.com/despairarc000/visualbook/actions/runs/40044410923) also **SUCCESS**, including APK upload.
- [Knowledge System Doctor 40044410950](https://github.com/despairarc000/visualbook/actions/runs/40044410950): **SUCCESS**.
- GitHub Actions artifact ID `12540851798` `visualbook-first-animated-story-debug` from code SHA `3fdb244dccab03500410cdf321d3fdcfb9d9ff0a`, expires Nov 7 2026. Downloaded ZIP CRC checked; extracted Android APK also passed CRC.
- Extracted debug APK **50,639 bytes**, SHA-256 **`f305e3770a9bd3d4f7d45a7e6853a7770cb56c407c3683eb1ab372192842e9c3`**.
- Android package ID `com.visualbook.player.rig.v04` installs separately from prior VisualBook prototypes; label **VisualBook Shape Rig v0.4**.

## What was actually compiled

A pure Java vector builder supports `@part id WxH pivot:x,y`, `@endpart`, nested prior-defined `@use` with rotation, uniform scale and horizontal mirror. Android Canvas recursively renders a bounded DAG of parts, each in its own local coordinate system and with a pivot anchor. Built-in story includes an illustrative mystical creature with reusable eyes/horns/wings and assembled head. `StoryCore` still drives visual changes using actual TTS `onStart`, keeps prior `@show`/`@hide` and `@chibi` semantics. No downloadable art, 3D, shader-code execution or network permissions.

Tests cover old primitive vector controls, local and parent mapping, rotation/mirror math, validity and limits (12 reusable parts, 64 nodes, 256 expanded drawing operations, four levels), input sanitization, invalid part references, and story state.

## Still to test

On-device rendering and smoothness, actual image composition, clipping of nested parts, orientation changes, real timing and interactive author corrections are **not verified by CI**. In particular, the drawing has no authored rotation-animation over time; authored angle is static. Quality of generated art depends on the authored geometry, not a general-purpose text-to-image system.

## User test

Install the v0.4 APK, run the built-in story (the figure appears near upper center and later hides); enable **Show 0..100 coordinate grid** under Voices to check stage placement, then inspect the wings, eyes and horns. Edit a `@use wing` rotation or one eye's scale, Apply Script, replay, and compare. Validate `@chibi` still changes the built-in human characters without deforming the custom rig. Record phone screenshots for future fixes.

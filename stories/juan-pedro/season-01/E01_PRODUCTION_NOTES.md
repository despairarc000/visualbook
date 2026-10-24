# Episode 1 production notes — VisualBook

**Title:** Ang Kampanang Nawala
**Series:** Juan at Pedro: Mga Kuwento ng Bayan ng Maligaya
**Final source:** `E01_Ang_Kampanang_Nawala.story.md`
**Narration goal:** ~24 minutes of spoken Filipino content (see timing notes and local validation).

## Important version compatibility

The source uses only the existing VisualBook v0.6 script directives: `@bg neutral` (global), `@n`, `@c1m`, `@c2m`, `@c3f`, ..., `@c9m`, `@chibi`, and matching `@end`. There are **no invented `@place`, `@enter`, `@scene` or image directives** in this playable file. Guest actors c3 and above are accepted as implicit cast but may not persist visually between their turns in the current experimental Android stage. Voice selectors for the current Android UI emphasize narrator, c1 and c2; other characters can inherit engine/fallback voices. Longer paragraphs may cause unnatural segmentation/gaps on some installed speech engines. This is a **playable text-first script**, not a claim of 24 minutes of unique detailed animation.

## Cast voice mapping

- `@n` narrator, calm energetic Filipino delivery.
- `@c1m` Juan: relaxed, slightly playful, sharp in serious moments.
- `@c2m` Pedro: brisk, earnest, grounded, occasionally dry.
- `@c3f` Maria: articulate, curious and decisive.
- `@c4f` Aling Sela: measured, warm with dry authority.
- `@c5f` Kapitana Pilar: practical leader, composed.
- `@c6m` Mang Tino: gruff craftsperson, well-meaning.
- `@c7m` Don Esteban: controlled, urbane, persuasive; not cartoonish villain voice.
- `@c8f` Nena: younger, fast and perceptive; avoid extreme squeaky voice.
- `@c9m` Elias / unknown assistant: muted and worried; identity not spoken in episode.

## Story-to-visual plan for future renderer upgrades

| Beat | Visual stage | Key props or movement | Existing v0.6 can do? |
|---|---|---|---|
| Guava morning | shaded tree and houses, Juan lounging, Pedro carries water | falling fruit and chibi reaction | Chibi changes built-in shapes; not yet true tree environment |
| Empty bell tower | crowded village square and rope | missing bronze bell silhouette | Would need custom `@figure` or imported art |
| Archive | small shelves, map and ledger | torn page with half-leaf wax stamp | Would need custom geometry or assets |
| Workshop | anvils, rope texture and tools | white-fiber clue | Needs asset pack |
| River trail | rain, bridge, wagon grooves | footprints, hiding under bamboo | Needs layered scene generation |
| Bell recovery | wooden crate, heavy bell at rising water | coordinated pulling and gestures | Needs multi-actor staging/motion |
| Seven strikes | civic bell on repaired platform, gathered people | safe bell strikes, anticipation | Narration can speak sounds; sound FX not yet authored as data |
| Night cliffhanger | dark river warehouse, red seal and leaf key | shadow figure opens metal box | Requires scene pack / new rendering directives |

This plan describes **desired future cinematic rendering**, not commands implemented in the current APK.

## Timing and listening QA

The script has a measured word count in `E01_VALIDATION.json`. A target of 140 Filipino spoken words/minute implies about 24 minutes *before* TTS engine-specific pauses, sentence gaps and synthesis quirks; faster/slower engines may differ significantly. Do not claim exact runtime until measuring a complete playback on the target device. The `@chibi` markers are silent instructions, not timing cues. The 500-utterance hard limit and 100,000-source-character ceiling of the prototype need checking when compiling.

## Recommended workflow

1. Import the `.story.md` file in VisualBook v0.6 using Import TXT and ensure the **Plain TXT checkbox is unchecked** (the app sets script mode for `.story.md`).
2. Verify it compiles with no line-numbered errors. Before pressing Play, test the installed system Filipino voice offline in airplane mode.
3. Listen to a full rendition and note start/end timestamps. Time the voice, not simply the text word count.
4. If necessary, adjust engine speech rate or trim/add 100–200 words while preserving continuity, never inventing facts about the folklore.
5. On future iterations, author supported vector artboards or import packs alongside the story rather than injecting unsupported tags into its spoken text.

## Future production TODO

- Persistent and named supporting actors in a scene, consistent voices, and person-specific costumes and facing.
- Per-scene visual-state directives beyond prototype's `@bg neutral`/`classroom` initial global choice.
- Ambient sounds, bell audio, night/rain/weather effects and accurate non-speaking cue sequencing.
- Portrait/landscape camera staging and use of fallbacks for absent assets.
- Storypack packaging with compatible/licensed images and voice models, plus export of preview video if ever supported.
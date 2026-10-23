# Samsung A06 5G — system voice settings screenshots after Filipino timeout

**Evidence type:** screenshot visually observed in user conversation on 2026-10-24. **Device:** Samsung SM-A066B (Galaxy A06 5G), Android 15, One UI 7 according to previous device reports. **Not an APK/ADB inventory, not a controlled offline voice test.** Binary screenshots deliberately not committed, since one screenshot also displays unrelated installed app names.

## Screenshot 1: Samsung TTS settings

- Heading: `Samsung TTS settings`.
- Version: `3.5.02.16`, described by settings as `Latest version`.
- Under `Languages and voices`, only **English (India)** (Default voice 1), **English (United States)** (Default voice 1; selected), and **Hindi (India)** (Voice 1) are visible.
- No `Filipino` voice and no `Install voice data` control are visible on this page.
- Do not claim this proves a Filipino pack does not exist in the Galaxy Store or in a separate download screen: it only proves it is not shown on the captured local installed-voice settings page.

## Screenshot 2: Settings search

- A search for `text-to` returns a `Text-to-speech` entry under `Accessibility → TalkBack → TalkBack settings`.
- The entry is a potential navigation path, not proof that the phone lacks another system-wide Text-to-speech menu or Google engine.
- Avoid asking the user to enable TalkBack merely to find TTS configuration; screen reader activation is unnecessary and can change navigation controls.

## Combined interpretation with v0.3 voice trace

- TTS engine `com.samsung.SMT` enumerated 473 voice IDs, including multiple `fil-PH` candidates marked `requiresNetwork=false`.
- Two explicitly selected `fil-PH` IDs returned `setVoice=SUCCESS` and matching active ID, **but** `speak` and `synthesizeToFile` produced Android `-7 ERROR_NETWORK_TIMEOUT` after approximately 2 seconds.
- Screen shows only English and Hindi installed/configurable voices. This **strengthens** the hypothesis of unavailable/not-fully-ready Filipino voice resources, but the actual `-7` error remains network timeout, not `-9` missing data. Root cause is OPEN.

## External official references

- [Samsung TTS help (2026)](https://www.samsung.com/in/support/mobile-devices/how-to-use-the-text-to-speech-feature-on-your-galaxy-phone-or-tablet/) documents preferred-engine gear and an `Install voice data` action **on some model/UI configurations**. This screen does not show such a control; do not insist that it exists there.
- [Google: supported TalkBack spoken languages](https://support.google.com/accessibility/android/answer/11101402?hl=en_) lists Filipino (Philippines) under Google TTS language support; on-device availability/offline download remain to be proven.
- [Google Speech Recognition & Synthesis Play Store](https://play.google.com/store/apps/details?id=com.google.android.tts) offers an alternate Android TTS provider if compatible and installed.
- [Android TextToSpeech API](https://developer.android.com/reference/android/speech/tts/TextToSpeech) defines `ERROR_NETWORK_TIMEOUT=-7` and `ERROR_NOT_INSTALLED_YET=-9`.

## Next low-risk steps (do in this order)

1. On phone open **Settings → General management → Text-to-speech** (if absent, **Settings → Accessibility → Text-to-speech output**, device variant). Look at the *system-wide output screen* for a `Preferred engine` selector, as distinct from already-open `Samsung TTS settings`.
2. If the preferred-engine screen has a settings gear with `Install voice data`, inspect there; do not assume the option exists. Check for Filipino without resetting/deleting Samsung TTS data.
3. If Google Speech Recognition & Synthesis is not an available preferred engine, install/update it from the official Google Play app page, then return and select it. Its Google TTS has Filipino in the documented language family; need still install required language data.
4. Download Filipino voice data if available while online; afterward test in airplane mode. Restart VisualBook's TTS Probe v0.3 and press **List voices** to verify `default_engine` changed and new actual selected voice IDs are available. Use corresponding live/prepared buttons; report audible quality and any errors.
5. If Google is unavailable or does not offer usable Filipino offline, keep a text-only fallback and continue independent research into redistributable compatible offline voice packs. Avoid unsafe sideloading and modifying system packages.

## Architectural implications

This is a concrete example of **advertised ≠ installed ≠ selected ≠ capable ≠ offline playable**. VoiceResolver should register probe outcomes per `engine package + version + voice ID + locale`, invalidate after voice-pack installs/engine changes and fail safely without narratively advancing the audible cursor. Built-in generic visuals can proceed in parallel; user need not block all progress on Samsung voice support.

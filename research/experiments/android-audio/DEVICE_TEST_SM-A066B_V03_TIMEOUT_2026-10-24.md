# SM-A066B TTS Probe v0.3 — selected Filipino voices fail with Android error -7

**Status:** USER-SUPPLIED REAL-DEVICE CALLBACK EVIDENCE. Android 15 SDK 35, Samsung SM-A066B. Observed 2026-10-24. Airplane mode status and audio audition outcome were **not specified**.

## Device evidence — supplied selected log

- 91143934: App starts v0.3 on Samsung SM-A066B.
- 91145243: One UI heartbeat approximately 501ms late during startup, BEFORE TTS ready.
- 91147539: TTS initialized successfully; listener installed on background audio thread.
- 91150986: default TTS engine **com.samsung.SMT**, active voice **en-US-language**, inventory **473**, \`fil-PH support=1\` and \`tl-PH support=-2\`.
- Listed Filipino voices include \`fil-PH-language\`, \`fil-ph-x-cfc-local\`, \`fil-ph-x-fic-local\`, \`fil-ph-x-fid-local\`, \`fil-ph-x-fie-local\`, plus named network variants. All report generic quality=400, latency=200. These fields are engine metadata, NOT measured quality or actual installed weights.
- 91156089: baseline LIVE selects active en-US-language and starts successfully (91156715 audible start).
- 91159853: baseline PREP active en-US-language, synthesized file successfully (91160162) and MediaPlayer starts (91160505).
- 91162627: \`setVoice(fil-PH-language)\` returns **0** (SUCCESS), effective voice confirmed \`fil-PH-language\`; networkRequired=false. Two live speech requests fail: \`L:3:0 ERROR -7\` at 91164839 and \`L:3:1 ERROR -7\` at 91166845. First queued at 91162813 → **2026ms** until error; second queued 91162815 → **4030ms**, reflecting sequential failed utterances.
- 91168167: \`setVoice(fil-ph-x-cfc-local)\` returns **0**, effective voice confirmed \`fil-ph-x-cfc-local\`; networkRequired=false.
- 91168288: \`synthesizeToFile\` enqueued successfully; 91170294: \`P:4 ERROR -7\` → **2006ms** after enqueue. No media playback from this Filipino synthesis.
- No device-side evidence yet for the remaining named local voice variants or a Google TTS engine test.

## Correct interpretation of Android -7

According to [Android TextToSpeech API](https://developer.android.com/reference/android/speech/tts/TextToSpeech), \`ERROR_NETWORK_TIMEOUT=-7\`, meaning network timeout. **It is NOT** \`ERROR_NOT_INSTALLED_YET=-9\` (unfinished voice-data download).

Android's [KEY_FEATURE_NOT_INSTALLED](https://developer.android.com/reference/android/speech/tts/TextToSpeech.Engine#KEY_FEATURE_NOT_INSTALLED) notes voices may be listed while additional data is not fully installed, and requesting such a voice may start a download. However, this log's exact error is -7, so **missing data is only a hypothesis**, not a confirmed diagnosis.

Voice \`isNetworkConnectionRequired=false\` is a claim about intended voice behavior, not proof the engine will never try network for downloading data or that the required local data already exists. **Successful setVoice/getVoice checks also do not establish successful synthesis.**

## Hypotheses and follow-up (do not conflate)

1. **Uninstalled/partially installed voice data**: Samsung engine advertises voice without complete resources; inspect device Install Voice Data screen and download Filipino if offered.
2. **Samsung TTS implementation/backend issue**: offline voice incorrectly uses network/download path or times out; compare remaining three local voices and check voice features.
3. **Phone/provider network configuration or TTS data service unavailable**: repeat with/without airplane mode AFTER voice pack install, noting the actual setting.
4. **Engine-specific compatibility**: choose Google Speech Recognition & Synthesis as Android's preferred TTS engine if installed, restart probe, list voices and test a true local Filipino voice.

Samsung's official guide: [Download new TTS voices](https://www.samsung.com/us/support/answer/ANS10003701/). Suggested settings route: Settings → General management → Text-to-speech → engine gear → Install voice data. Menu and download availability vary by device, region and installed software.

**Do not diagnose Android UI freezing or resource exhaustion from -7.** The earlier v0.2 stop/switch stress test was smooth; the 501ms startup UI gap occurred before TTS readiness. Missing synthesized Filipino speech is a separate problem.

## Result for VisualBook architecture

- \`VoiceResolver\` must not accept a candidate solely because \`getVoices\` lists it, \`isNetworkConnectionRequired=false\` or \`setVoice()==SUCCESS\`.
- Distinguish capabilities: \`ADVERTISED\`, \`SELECTED\`, \`SYNTHESIS_TESTED\`, \`OFFLINE_TESTED\`, \`PLAYABLE\` and \`FAILED\`. Selection is only a provisional intermediate state; mark offline only after real airplane-mode synthesis.
- Handle -7 as a timed backend failure, not generic "unsupported language", and -9 as unavailable/download pending. Fail over to another **actually tested** suitable voice/provider or to text-only presentation with clear UI feedback; don't silently substitute English for Filipino.
- Abort or skip remaining queued utterances on repeated errors and protect the narration pointer from advancing on failures.
- Maintain separation between authoritative audible playback and TTS synthesis, even when synthesis succeeds instantly but played audio is later.
- Consider per-language/voice/device cache, revalidation after pack installs, voice backend updates and appropriate user consent for network-dependent downloads.

## Evidence quality and next gate

The device recorded \`-7\` errors from two advertised local Filipino voices using different audio paths. **No recorded speech, no proof of true offline execution, no diagnosis of why Samsung engine tried the network path.** Do not choose a bundled neural model or declare Samsung permanently unusable on this phone without checking installed voice data and a second engine.

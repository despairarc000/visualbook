# Samsung Galaxy A06 5G — TTS Probe v0.2 stress test

**Status: OBSERVED-DEVICE-TEST (user-supplied log), 2026-10-24.** This is the first positive regression of the stop/switch freeze scenario. The user described test experience as **"Smooth."** No voice audition/airplane-mode proof or on-device ANR trace supplied.

## Device and version

- Samsung SM-A066B, Android 15 SDK 35
- VisualBook TTS Probe v0.2, distinct app ID \`com.visualbook.ttsprobe.v2\`
- Log timestamps are uptime/elapsed realtime monotonic milliseconds, **not wall-clock Unix times**.
- Test mode: live TTS, generated-audio MediaPlayer, voice inventory, Stop/Restart, repeated rapid switching.

## Verifiable events

| Observation | Timestamp(s) / exact values | Assessment |
|---|---|---|
| TTS initializes | \`90514461\` init status 0; \`90514462\` listener ready | Engine initialized successfully; premature voice button tap earlier correctly rejected |
| Startup UI gap | \`90511696\`, roughly 514 ms late | One UI heartbeat delay **before** TTS ready; no further \`UI-MAIN gap\` during rapid switching |
| LIVE 1 first sentence | start \`90521083\` / end \`90526306\` | 5223 ms callback elapsed, word-range events received |
| LIVE 1 second sentence | start \`90526308\` / end \`90529382\` | 3074 ms, boundary callback difference 2 ms |
| LIVE 1 third sentence | start \`90529383\` / end \`90530949\` | 1566 ms, boundary callback difference 1 ms |
| Prepared audio | \`90532664\` synthesis file done → \`90533057\` media start → \`90538169\` media end | Distinguishes synthesis from actual playback; 5112-ms wall-clock playback interval |
| Rapid repeated LIVE | generation 5 through 13; \`tts.stop\` reported mostly 2–4 ms, successful new utterance starts | UI continued scheduling commands and sound while old callbacks were rejected |
| Rapid PREP switching | generations 14–17, media release around 49–53 ms | Background-thread cleanup completed even during ongoing playback |
| Voice inventory | 473 voices; active **\`en-US-language\`**; \`fil-PH=1\`, \`tl-PH=-2\` | Filipino locale is supported by engine but the test did **not** explicitly select Filipino voice |
| Candidate examples | \`fil-PH-language\`, \`fil-ph-x-cfc-local\`, \`fil-ph-x-fic-local\`, \`fil-ph-x-fid-local\`, \`fil-ph-x-fie-local\` | Voice engine advertises non-network Filipino voices; presence/quality offline not verified |
| Memory | PSS snapshots 81,514–93,625 KB in submitted excerpt | 79.6–91.4 MiB app PSS; insufficient duration/samples for leak claims |
| Prepared range callback anomaly | \`P:2 start=360 end=0 frame=5\` | File-synthesis callback violates normal text offset ordering; v0.2 rejects as invalid |

## Interpreting UI responsiveness carefully

- The user reports smoother stop/switch behavior than v0.1. No new \`UI-MAIN gap\` during rapid switching in the supplied log; audio commands continue to finish in tens of milliseconds (roughly 36–106 ms).
- Main-thread blocking by \`tts.stop()\`/MediaPlayer release and per-callback full TextView replacement were plausible v0.1 causes. Moving commands to worker and throttling log redraw correlates with improvement; **the individual causal contribution is not established**.
- Live callbacks indicate logical utterance boundary completion; 1–2 ms boundary gap in this run is callback scheduling, not acoustic silence measurement.
- Generation guard works visibly: \`onStop ... active_generation=false\` for obsolete live runs. Some canceled queued utterances report non-interrupted, the active utterance interrupted; expected distinction.
- Although voice metadata reports "requiresNetwork=false", proving full offline synthesis requires selecting a concrete voice and testing in airplane mode.

## Next experiment: Filipino voice comparison

[Probe v0.3 protocol](probe/README.md): explicitly cycle local Filipino voice candidates using \`setVoice(Voice)\`, verify \`getVoice()\` returns selected ID and \`setVoice()==SUCCESS\`, then evaluate live and prepared speech with airplane mode enabled. Compare pronunciation, prosody, clarity, actual availability and source-cache behavior. Do **not** assume that a 473-voice inventory means 473 installed acoustic models.

## Evidence limitations

Full user log is in conversation; this repo note preserves representative values, findings and test conditions. No raw audio recording, thread capture, hardware memory profile, media-player timeline or independent voice quality rating yet. Do not treat this as a complete performance benchmark.

# Samsung SM-A066B real-device TTS observation — v0.1

**Status:** USER-SUPPLIED REAL-DEVICE LOG, exact root cause NOT proven. 2026-10-24, Android 15 (SDK 35). Reported behavior: tapping **Stop / invalidate callbacks** and switching to another test intermittently makes UI freeze/unresponsive. No ANR trace, Perfetto trace, or stack capture supplied.

## What the user actually observed (monotonic log milliseconds)

| Event | Evidence | Interpretation |
|---|---|---|
| TTS initialization | `TTS initialized=true result=0` | TTS service initialized; first earlier tap at 89676066 ms preceded readiness |
| Live run #1 | `L:1:0` start 89684990 / end 89690130; `L:1:1` 89690143–89693197; `L:1:2` 89693210–89694772 | 3/3 live utterances delivered successful callbacks |
| Live run #2 | `L:2:0` 89699572–89704426; `L:2:1` 89704442–89707520; `L:2:2` 89707526–89709094 | 3/3 live utterances delivered successful callbacks |
| Boundary gaps | 13 ms, 13 ms; 16 ms, 6 ms | **Callback-to-callback intervals**, not measured silence in audio waveform |
| Word-range | live `[0,5] frame=360`, `[42,50] frame=82680` | This installed engine supplies optional text-range callbacks for live speech; actual audibility not independently verified |
| Prepared clip #1 | synthesis start 89712658, file done 89712719, media start 89713039, end 89718154 | File synthesis completes well before actual playback. Audible-player interval 5115 ms (callback clock) |
| Prepared clip #2 | synthesis start 89720066, file done 89720138, media start 89720170, end 89725183 | Same distinction; playback callback clock interval 5013 ms |
| Prepared range anomaly | `P:3 [360,0] frame=5`, `[10559,6] frame=10`; `P:4` repeats | Engine reports suspiciously permuted fields for synthesizeToFile. Reject invalid ranges, do not trust for precise alignment |
| App PSS snapshots | 68535, 76481, 83645, 75904 KB | Roughly 67–82 MiB PSS sampled at different stages; no long-term leak conclusion |
| Unresponsiveness | User reports after Stop then another mode | **Not proven ANR**, may be synchronous binder/MediaPlayer calls, repeated whole TextView redraw, or TTS service scheduling |

## Evidence-based hypothesis

v0.1 executes `tts.stop()`, synchronous MediaPlayer cleanup, TTS queuing and voice inventory on Android main thread, and calls `results.setText(report.toString())` **on every callback**. Heavy binder/service calls and repeated layout can block the UI. Real-device stack/trace required to attribute the freeze. See [Android ANR documentation](https://developer.android.com/topic/performance/issues/anr) and [UtteranceProgressListener contract](https://developer.android.com/reference/android/speech/tts/UtteranceProgressListener).

## v0.2 changes to test

- Move stop/query/speak/synthesize and MediaPlayer preparation/release to one `HandlerThread`.
- Assign generation before queueing; stale callbacks cannot initiate old-mode prepared media.
- Throttle diagnostic TextView to latest 9K characters at most once per 200 ms; preserve bounded full shareable log separately.
- Add 250-ms main-thread heartbeat, audio command begin/end duration including stop() duration, and report audio command stuck beyond 1.5s.
- Only consider range callbacks when text offsets are plausible; prepared-file range timestamps are NOT audible progress.
- Increment version code; replace previous installed app without changing package ID.

## Tests required on phone

1. Install version 0.2 over 0.1. Tap Voice List after TTS ready.
2. Run live, tap **Stop while speaking**, immediately run prepared-file, then repeat in reverse.
3. Tap each mode several times quickly; verify latest requested mode wins with no stuck UI.
4. Confirm logs include `AUDIO COMMAND begin/finished`, `tts.stop duration_ms` and `UI-MAIN gap` only if main thread stalls.
5. Confirm prepared-file `onDone` does **not** advance the story pointer, only its own MediaPlayer end does.
6. Share the entire report and any ANR dialog details; if unresponsive despite worker serialization, inspect Perfetto (main thread, Binder and text rendering).

**No voice quality verdict:** user did not provide subjective Filipino pronunciation/offline-airplane-mode observations or full voice inventory. No external neural model was tested.

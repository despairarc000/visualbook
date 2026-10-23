package com.visualbook.ttsprobe;

import android.app.Activity;
import android.content.Intent;
import android.media.AudioAttributes;
import android.media.MediaPlayer;
import android.os.Build;
import android.os.Bundle;
import android.os.Debug;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.SystemClock;
import android.speech.tts.TextToSpeech;
import android.speech.tts.UtteranceProgressListener;
import android.speech.tts.Voice;
import android.view.WindowInsets;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Locale;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Android speech research probe (not the VisualBook app).
 *
 * IMPORTANT: UI clicks MUST NOT execute TTS.stop(), MediaPlayer cleanup, synthesize
 * requests, or voice enumeration on the UI thread.
 * One HandlerThread serializes TTS and MediaPlayer operations.
 */
public final class MainActivity extends Activity {
    private static final String[] CORPUS = {
            "Isang araw, nagkita sina Juan at Pedro sa paaralan.",
            "Pedro, bakit hindi ka pumasok kahapon?",
            "Kasi Linggo kahapon!"
    };
    private static final long UI_FLUSH_MS = 200L;
    private static final int VISIBLE_LOG_CHARS = 9000;
    private static final int MAX_REPORT_CHARS = 85000;

    private final Handler main = new Handler(Looper.getMainLooper());
    private final AtomicInteger generation = new AtomicInteger(0);
    private final ConcurrentHashMap<String, Long> utteranceStarts = new ConcurrentHashMap<>();
    private final Set<String> invalidRangeWarned = ConcurrentHashMap.newKeySet();
    private final StringBuilder report = new StringBuilder();
    private HandlerThread audioThread;
    private Handler audio;
    private TextView results;
    private TextToSpeech tts;
    private MediaPlayer media; // owned exclusively by audioThread
    private volatile boolean ready = false;
    private volatile boolean destroyed = false;
    private volatile long commandBeganMs = 0;
    private volatile String activeCommand = "";
    private long lastUiBeatMs = 0;
    private boolean uiFlushPending = false;

    @Override
    public void onCreate(Bundle state) {
        super.onCreate(state);
        audioThread = new HandlerThread("VisualBook-Probe-Audio");
        audioThread.start();
        audio = new Handler(audioThread.getLooper());

        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        int pad = dp(12);
        root.setPadding(pad, pad, pad, pad);
        if (Build.VERSION.SDK_INT >= 35) {
            root.setOnApplyWindowInsetsListener((view, insets) -> {
                android.graphics.Insets safe = insets.getInsets(
                        WindowInsets.Type.systemBars() | WindowInsets.Type.displayCutout());
                view.setPadding(pad + safe.left, pad + safe.top,
                        pad + safe.right, pad + safe.bottom);
                return insets;
            });
        }

        TextView title = new TextView(this);
        title.setText("VisualBook TTS Probe v0.2 — responsive switching diagnostic");
        title.setTextSize(18);
        root.addView(title);

        addButton(root, "1. List installed voices", this::listVoices);
        addButton(root, "2. Speak 3 live sentences", () -> requestMode("LIVE"));
        addButton(root, "3. Synthesize + play file", () -> requestMode("PREP"));
        addButton(root, "Stop / invalidate callbacks", () -> requestMode("STOP"));
        addButton(root, "Share diagnostic text", this::shareReport);

        ScrollView scroll = new ScrollView(this);
        results = new TextView(this);
        results.setTextIsSelectable(true);
        results.setTextSize(12);
        scroll.addView(results);
        root.addView(scroll, new LinearLayout.LayoutParams(-1, 0, 1));
        setContentView(root);
        log("Device " + Build.MANUFACTURER + " " + Build.MODEL +
                " Android " + Build.VERSION.RELEASE + " SDK " + Build.VERSION.SDK_INT);
        log("Probe v0.2: TTS stop, speak, file synthesis, voice queries and MediaPlayer " +
                "operations run on a serial background thread. No INTERNET permission.");

        lastUiBeatMs = SystemClock.elapsedRealtime();
        main.postDelayed(uiHeartbeat, 250L);

        tts = new TextToSpeech(getApplicationContext(), status -> {
            if (destroyed) return;
            log("TTS init callback status=" + status);
            if (status != TextToSpeech.SUCCESS) return;
            audio.post(() -> {
                if (destroyed || tts == null) return;
                tts.setOnUtteranceProgressListener(listener);
                ready = true;
                log("TTS ready; listener installed on audio command thread.");
            });
        });
    }

    private final Runnable uiHeartbeat = new Runnable() {
        @Override public void run() {
            if (destroyed) return;
            long now = SystemClock.elapsedRealtime();
            long delay = now - lastUiBeatMs - 250L;
            if (delay > 400L) {
                log("UI-MAIN gap detected: approximately " + delay + "ms late. " +
                        "Look for main-thread stall or long layout.");
            }
            lastUiBeatMs = now;
            if (commandBeganMs != 0L && now - commandBeganMs > 1500L) {
                // Report periodically, even when audio commands are stuck but UI still works.
                if ((now / 2000L) != ((now - 250L) / 2000L)) {
                    log("AUDIO COMMAND pending: " + activeCommand + " " +
                            (now - commandBeganMs) + "ms; UI heartbeat still active");
                }
            }
            main.postDelayed(this, 250L);
        }
    };

    private final UtteranceProgressListener listener = new UtteranceProgressListener() {
        @Override public void onStart(String id) {
            long at = SystemClock.elapsedRealtime();
            if (!valid(id)) return;
            utteranceStarts.put(id, at);
            logAt(at, id.startsWith("L:") ?
                    "LIVE audible-boundary start " + id :
                    "PREP synthesis start (NOT audible) " + id);
        }

        @Override public void onDone(String id) {
            long at = SystemClock.elapsedRealtime();
            if (!valid(id)) return;
            Long start = utteranceStarts.remove(id);
            long elapsed = start == null ? -1 : at - start;
            if (id.startsWith("L:")) {
                logAt(at, "LIVE audible completion " + id + " elapsed_ms=" + elapsed);
            } else if (id.startsWith("P:")) {
                logAt(at, "PREP file writing completed (NOT audible) " + id +
                        " elapsed_ms=" + elapsed);
                audio.post(() -> {
                    if (valid(id)) playPreparedAudio(id);
                });
            }
        }

        @Override public void onError(String id) {
            if (valid(id)) log("TTS onError " + id);
        }

        @Override public void onError(String id, int code) {
            if (valid(id)) log("TTS onError " + id + " error_code=" + code);
        }

        @Override public void onStop(String id, boolean interrupted) {
            log("TTS onStop " + id + " interrupted=" + interrupted +
                    " active_generation=" + valid(id));
        }

        @Override public void onRangeStart(String id, int start, int end, int frame) {
            if (!valid(id)) return;
            // AOSP contract: start/end are text offsets, frame is audio frame offset.
            // The user's system engine appears to permute these during file synthesis.
            int expectedLen = id.startsWith("P:") ? CORPUS[0].length() :
                    liveUtteranceLength(id);
            boolean sane = start >= 0 && end > start && end <= expectedLen && frame >= 0;
            if (!sane) {
                if (invalidRangeWarned.add(id)) {
                    log("ENGINE RANGE ANOMALY " + id + " example start=" + start +
                            " end=" + end + " frame=" + frame +
                            "; ignore file-mode ranges pending verification");
                }
                return;
            }
            // Only LIVE onRangeStart is usable as a provisional audible-time signal.
            if (id.startsWith("L:")) {
                log("LIVE range " + id + " [" + start + "," + end +
                        "] frame=" + frame);
            }
        }
    };

    private static int liveUtteranceLength(String id) {
        try {
            String[] parts = id.split(":");
            int index = Integer.parseInt(parts[2]);
            return index >= 0 && index < CORPUS.length ? CORPUS[index].length() : 0;
        } catch (RuntimeException ignored) {
            return 0;
        }
    }

    private int dp(int px) {
        return Math.round(px * getResources().getDisplayMetrics().density);
    }

    private void addButton(LinearLayout root, String title, Runnable callback) {
        Button button = new Button(this);
        button.setText(title);
        button.setAllCaps(false);
        button.setOnClickListener(v -> callback.run());
        root.addView(button);
    }

    private boolean valid(String id) {
        if (destroyed || id == null) return false;
        String[] p = id.split(":");
        if (p.length < 2) return false;
        try { return Integer.parseInt(p[1]) == generation.get(); }
        catch (NumberFormatException ignored) { return false; }
    }

    private void log(String message) { logAt(SystemClock.elapsedRealtime(), message); }

    private void logAt(long at, String message) {
        if (destroyed) return;
        if (Looper.myLooper() != Looper.getMainLooper()) {
            main.post(() -> logAt(at, message));
            return;
        }
        report.append(at).append(" ms | ").append(message).append('\n');
        if (report.length() > MAX_REPORT_CHARS)
            report.delete(0, report.length() - 60000);
        if (!uiFlushPending) {
            uiFlushPending = true;
            main.postDelayed(() -> {
                uiFlushPending = false;
                if (destroyed || results == null) return;
                int from = Math.max(0, report.length() - VISIBLE_LOG_CHARS);
                results.setText(report.substring(from));
            }, UI_FLUSH_MS);
        }
    }

    private void listVoices() {
        if (!ready) {
            log("TTS still initializing; try List installed voices again.");
            return;
        }
        log("Voice inventory requested; querying on audio worker.");
        audio.post(() -> {
            beginCommand("listVoices");
            try {
                log("fil-PH support=" + tts.isLanguageAvailable(Locale.forLanguageTag("fil-PH")));
                log("tl-PH support=" + tts.isLanguageAvailable(Locale.forLanguageTag("tl-PH")));
                Set<Voice> raw = tts.getVoices();
                if (raw == null) {
                    log("Voice enumeration unavailable.");
                    return;
                }
                ArrayList<Voice> voices = new ArrayList<>(raw);
                voices.sort(Comparator.comparing(Voice::getName));
                Voice active = tts.getVoice();
                log("Voice inventory count=" + voices.size() + " active=" +
                        (active == null ? "<none>" : active.getName()));
                int printedOther = 0;
                for (Voice voice : voices) {
                    String language = voice.getLocale().getLanguage();
                    boolean tagalog = "fil".equals(language) || "tl".equals(language);
                    if (tagalog || printedOther++ < 12) {
                        log((tagalog ? "TAGALOG CANDIDATE " : "") + voice.getName() +
                                " locale=" + voice.getLocale().toLanguageTag() +
                                " requiresNetwork=" + voice.isNetworkConnectionRequired());
                    }
                }
                log("Voice listing does not change voice mid-utterance.");
            } catch (RuntimeException problem) {
                log("Voice query error " + problem);
            } finally {
                endCommand();
            }
        });
    }

    private void requestMode(String mode) {
        if (!ready && !"STOP".equals(mode)) {
            log("TTS not yet ready; request rejected without blocking UI.");
            return;
        }
        int desired = generation.incrementAndGet();
        log("UI REQUEST " + mode + " generation=" + desired +
                " queued in background; button remains responsive.");
        audio.post(() -> {
            if (generation.get() != desired || destroyed) {
                log("Discarded superseded queued command " + mode + " generation=" + desired);
                return;
            }
            beginCommand(mode);
            try {
                stopCore();
                if (generation.get() != desired || destroyed) {
                    log("Command superseded after cleanup: " + mode);
                    return;
                }
                if ("LIVE".equals(mode)) {
                    startLive(desired);
                } else if ("PREP".equals(mode)) {
                    prepareFile(desired);
                } else {
                    log("STOP completed; generation=" + desired);
                }
                recordMemory();
            } catch (RuntimeException unexpected) {
                log("COMMAND ERROR " + mode + ": " + unexpected);
            } finally {
                endCommand();
            }
        });
    }

    private void beginCommand(String name) {
        commandBeganMs = SystemClock.elapsedRealtime();
        activeCommand = name;
        log("AUDIO COMMAND begin " + name + " generation=" + generation.get());
    }

    private void endCommand() {
        long elapsed = SystemClock.elapsedRealtime() - commandBeganMs;
        log("AUDIO COMMAND finished " + activeCommand + " elapsed_ms=" + elapsed);
        activeCommand = "";
        commandBeganMs = 0L;
    }

    private void stopCore() {
        long begin = SystemClock.elapsedRealtime();
        if (tts != null) {
            int result = tts.stop();
            log("tts.stop() returned=" + result + " duration_ms=" +
                    (SystemClock.elapsedRealtime() - begin));
        }
        MediaPlayer previous = media;
        media = null;
        if (previous != null) {
            long releaseStart = SystemClock.elapsedRealtime();
            previous.setOnCompletionListener(null);
            previous.setOnPreparedListener(null);
            previous.setOnErrorListener(null);
            try { previous.stop(); } catch (IllegalStateException ignored) { }
            previous.release();
            log("MediaPlayer cleanup duration_ms=" +
                    (SystemClock.elapsedRealtime() - releaseStart));
        }
        utteranceStarts.clear();
        invalidRangeWarned.clear();
    }

    private void startLive(int desired) {
        for (int i = 0; i < CORPUS.length; i++) {
            if (generation.get() != desired) return;
            String id = "L:" + desired + ":" + i;
            long start = SystemClock.elapsedRealtime();
            int result = tts.speak(CORPUS[i],
                    i == 0 ? TextToSpeech.QUEUE_FLUSH : TextToSpeech.QUEUE_ADD,
                    null, id);
            log("LIVE enqueue " + id + " status=" + result +
                    " call_duration_ms=" + (SystemClock.elapsedRealtime() - start));
        }
    }

    private void prepareFile(int desired) {
        File file = new File(getCacheDir(), "probe-" + desired + ".wav");
        // Very small example; production needs bounded, atomically written cache management.
        int result = tts.synthesizeToFile(CORPUS[0], null, file, "P:" + desired);
        log("PREP synthesizeToFile enqueue returned=" + result + " file=" + file.getName() +
                " (NOT audible progress)");
    }

    private void playPreparedAudio(String id) {
        if (!valid(id)) return;
        File file = new File(getCacheDir(), "probe-" + generation.get() + ".wav");
        if (!file.isFile() || file.length() <= 0) {
            log("Prepared audio missing or empty; will not play.");
            return;
        }
        MediaPlayer player = null;
        try {
            player = new MediaPlayer(); // constructed on HandlerThread with Looper
            media = player;
            player.setAudioAttributes(new AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_MEDIA)
                    .setContentType(AudioAttributes.CONTENT_TYPE_SPEECH)
                    .build());
            player.setDataSource(file.getAbsolutePath());
            final int clipGeneration = generation.get();
            final MediaPlayer activePlayer = player;
            player.setOnPreparedListener(p -> {
                if (clipGeneration != generation.get() || p != media) return;
                long at = SystemClock.elapsedRealtime();
                p.start();
                utteranceStarts.put("AUDIBLE:" + clipGeneration, at);
                log("PREP AUDIO START own MediaPlayer; size=" + file.length());
            });
            player.setOnCompletionListener(p -> {
                if (clipGeneration != generation.get() || p != media) return;
                Long started = utteranceStarts.remove("AUDIBLE:" + clipGeneration);
                log("PREP AUDIO END own MediaPlayer elapsed_ms=" +
                        (started == null ? -1 : SystemClock.elapsedRealtime() - started));
                media = null;
                p.release();
            });
            player.setOnErrorListener((p, what, extra) -> {
                log("PREP MediaPlayer error what=" + what + " extra=" + extra);
                if (media == p) media = null;
                p.release();
                return true;
            });
            player.prepareAsync();
        } catch (IOException | IllegalStateException exc) {
            if (media == player) media = null;
            if (player != null) player.release();
            log("PREP MediaPlayer error " + exc);
        }
    }

    private void recordMemory() {
        Debug.MemoryInfo data = new Debug.MemoryInfo();
        Debug.getMemoryInfo(data);
        log("APP Memory totalPss_kb=" + data.getTotalPss());
    }

    private void shareReport() {
        Intent send = new Intent(Intent.ACTION_SEND);
        send.setType("text/plain");
        send.putExtra(Intent.EXTRA_SUBJECT, "VisualBook TTS Probe v0.2 diagnostics");
        send.putExtra(Intent.EXTRA_TEXT, report.toString());
        startActivity(Intent.createChooser(send, "Share probe diagnostics"));
    }

    @Override protected void onDestroy() {
        destroyed = true;
        generation.incrementAndGet();
        main.removeCallbacks(uiHeartbeat);
        final TextToSpeech old = tts;
        if (audio != null) {
            audio.post(() -> {
                try {
                    stopCore();
                    if (old != null) old.shutdown();
                } finally {
                    audioThread.quitSafely();
                }
            });
        }
        super.onDestroy();
    }
}

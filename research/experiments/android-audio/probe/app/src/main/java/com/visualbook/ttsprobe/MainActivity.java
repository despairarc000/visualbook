package com.visualbook.ttsprobe;

import android.app.Activity;
import android.content.Intent;
import android.media.AudioAttributes;
import android.media.MediaPlayer;
import android.os.Build;
import android.os.Bundle;
import android.os.Debug;
import android.os.Looper;
import android.os.SystemClock;
import android.speech.tts.TextToSpeech;
import android.speech.tts.UtteranceProgressListener;
import android.speech.tts.Voice;
import android.view.View;
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

/**
 * Disposable research app. No INTERNET permission; no story inference or model installation.
 * Android TTS onStart/onDone must NOT be conflated with prepared-file audible playback.
 */
public final class MainActivity extends Activity {
    private final String[] corpus = new String[] {
            "Isang araw, nagkita sina Juan at Pedro sa paaralan.",
            "Pedro, bakit hindi ka pumasok kahapon?",
            "Kasi Linggo kahapon!"
    };
    private final StringBuilder report = new StringBuilder();
    private TextToSpeech tts;
    private MediaPlayer media;
    private TextView results;
    private int generation = 0;
    private long liveStartMs = 0;
    private long preparedStartMs = 0;
    private boolean ready = false;

    @Override
    public void onCreate(Bundle state) {
        super.onCreate(state);
        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        int pad = dp(12);
        root.setPadding(pad, pad, pad, pad);
        if (Build.VERSION.SDK_INT >= 35) {
            root.setOnApplyWindowInsetsListener((view, insets) -> {
                android.graphics.Insets safe =
                        insets.getInsets(WindowInsets.Type.systemBars() | WindowInsets.Type.displayCutout());
                view.setPadding(pad + safe.left, pad + safe.top, pad + safe.right, pad + safe.bottom);
                return insets;
            });
        }
        TextView title = new TextView(this);
        title.setText("VisualBook — TTS Feasibility Probe\nResearch only; not the story player.");
        title.setTextSize(18);
        root.addView(title);
        addButton(root, "1. List installed voices", this::listVoices);
        addButton(root, "2. Speak 3 live sentences", this::playLive);
        addButton(root, "3. Synthesize + play file", this::prepareFile);
        addButton(root, "Stop / invalidate callbacks", this::stopAll);
        addButton(root, "Share diagnostic text", this::shareReport);
        ScrollView scroll = new ScrollView(this);
        results = new TextView(this);
        results.setTextIsSelectable(true);
        results.setTextSize(12);
        scroll.addView(results);
        root.addView(scroll, new LinearLayout.LayoutParams(-1, 0, 1));
        setContentView(root);
        append("Device: " + Build.MANUFACTURER + " " + Build.MODEL
                + " | Android " + Build.VERSION.RELEASE + " SDK " + Build.VERSION.SDK_INT);
        append("No INTERNET permission. Do not assume the installed voice supports offline Tagalog.");
        tts = new TextToSpeech(getApplicationContext(), status -> {
            ready = status == TextToSpeech.SUCCESS;
            append("TTS initialized=" + ready + " result=" + status);
            if (ready) {
                tts.setOnUtteranceProgressListener(new UtteranceProgressListener() {
                    @Override public void onStart(String id) {
                        runOnUiThread(() -> {
                            if (!valid(id)) return;
                            if (id.startsWith("L:")) {
                                liveStartMs = SystemClock.elapsedRealtime();
                                append("LIVE audible-boundary start: " + id);
                            } else {
                                append("PREP synthesis start, NOT audible start: " + id);
                            }
                        });
                    }
                    @Override public void onDone(String id) {
                        runOnUiThread(() -> {
                            if (!valid(id)) return;
                            if (id.startsWith("L:")) {
                                append("LIVE audible completion: " + id +
                                        " elapsed_ms=" + (SystemClock.elapsedRealtime() - liveStartMs));
                            } else {
                                append("PREP file writing completed (NOT audible): " + id);
                                playPreparedAudio(id);
                            }
                        });
                    }
                    @Override public void onError(String id) {
                        runOnUiThread(() -> { if (valid(id)) append("TTS onError: " + id); });
                    }
                    @Override public void onError(String id, int code) {
                        runOnUiThread(() -> { if (valid(id)) append("TTS onError: " + id + " code=" + code); });
                    }
                    @Override public void onStop(String id, boolean interrupted) {
                        runOnUiThread(() -> append("TTS stopped: " + id + " interrupted=" + interrupted));
                    }
                    @Override public void onRangeStart(String id, int start, int end, int frame) {
                        runOnUiThread(() -> {
                            if (valid(id)) append("optional range callback " + id +
                                    " [" + start + "," + end + "] frame=" + frame);
                        });
                    }
                });
            }
        });
    }

    private int dp(int value) {
        return Math.round(value * getResources().getDisplayMetrics().density);
    }

    private void addButton(LinearLayout root, String title, Runnable action) {
        Button button = new Button(this);
        button.setText(title);
        button.setAllCaps(false);
        button.setOnClickListener(view -> action.run());
        root.addView(button);
    }

    private boolean valid(String id) {
        String[] tokens = id.split(":");
        if (tokens.length < 2) return false;
        try {
            return Integer.parseInt(tokens[1]) == generation;
        } catch (NumberFormatException err) {
            return false;
        }
    }

    private void append(String msg) {
        if (Looper.myLooper() != Looper.getMainLooper()) {
            runOnUiThread(() -> append(msg));
            return;
        }
        String line = SystemClock.elapsedRealtime() + " ms | " + msg + "\n";
        report.append(line);
        if (report.length() > 60000) {
            report.delete(0, report.length() - 45000);
        }
        results.setText(report.toString());
    }

    private boolean requireTts() {
        if (ready && tts != null) return true;
        append("TTS unavailable: cannot run speech test.");
        return false;
    }

    private void listVoices() {
        if (!requireTts()) return;
        append("fil-PH support code=" +
                tts.isLanguageAvailable(Locale.forLanguageTag("fil-PH")));
        append("tl-PH support code=" +
                tts.isLanguageAvailable(Locale.forLanguageTag("tl-PH")));
        Set<Voice> raw = tts.getVoices();
        if (raw == null) {
            append("Voice inventory unavailable (null).");
            return;
        }
        ArrayList<Voice> voices = new ArrayList<>(raw);
        voices.sort(Comparator.comparing(Voice::getName));
        append("Voice inventory count=" + voices.size());
        Voice candidate = null;
        for (Voice voice : voices) {
            String language = voice.getLocale().getLanguage();
            boolean desired = "fil".equals(language) || "tl".equals(language);
            if (desired && !voice.isNetworkConnectionRequired() && candidate == null)
                candidate = voice;
            append((desired ? "TAGALOG-CANDIDATE " : "") +
                    voice.getName() + " locale=" + voice.getLocale().toLanguageTag() +
                    " network=" + voice.isNetworkConnectionRequired());
        }
        if (candidate != null) {
            int result = tts.setVoice(candidate);
            append("Select first advertised offline Filipino candidate " +
                    candidate.getName() + " status=" + result);
        } else {
            Voice existing = tts.getVoice();
            append("No advertised offline fil/tl voice. Retaining selected default: " +
                    (existing == null ? "unknown" : existing.getName()) +
                    ". Playback might be in another language or unavailable.");
        }
    }

    private void stopAll() {
        generation++;
        if (tts != null) tts.stop();
        if (media != null) {
            try { media.stop(); } catch (IllegalStateException ignored) { }
            media.release();
            media = null;
        }
        append("STOP; generation now " + generation + "; old callbacks invalid.");
    }

    private void playLive() {
        if (!requireTts()) return;
        stopAll();
        int g = generation;
        append("LIVE: queueing " + corpus.length + " utterances.");
        for (int i = 0; i < corpus.length; i++) {
            String id = "L:" + g + ":" + i;
            int result = tts.speak(corpus[i],
                    i == 0 ? TextToSpeech.QUEUE_FLUSH : TextToSpeech.QUEUE_ADD,
                    null, id);
            append("enqueue " + id + " status=" + result + " text=" + corpus[i]);
        }
        recordMemory();
    }

    private void prepareFile() {
        if (!requireTts()) return;
        stopAll();
        int g = generation;
        File file = new File(getCacheDir(), "probe-" + g + ".wav");
        append("PREP: preparing one utterance; callback completion must NOT change visual story pointer.");
        int rc = tts.synthesizeToFile(corpus[0], null, file, "P:" + g);
        append("synthesizeToFile status=" + rc + " path=" + file.getName());
        recordMemory();
    }

    private void playPreparedAudio(String id) {
        if (!valid(id)) return;
        File file = new File(getCacheDir(), "probe-" + generation + ".wav");
        if (!file.isFile() || file.length() == 0) {
            append("Prepared file absent/empty; not starting media.");
            return;
        }
        try {
            if (media != null) media.release();
            MediaPlayer player = new MediaPlayer();
            media = player;
            player.setAudioAttributes(new AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_MEDIA)
                    .setContentType(AudioAttributes.CONTENT_TYPE_SPEECH).build());
            player.setDataSource(file.getAbsolutePath());
            final int myGeneration = generation;
            player.setOnPreparedListener(p -> {
                if (myGeneration != generation) return;
                preparedStartMs = SystemClock.elapsedRealtime();
                p.start();
                append("PREP audible playback START (own MediaPlayer), size=" + file.length());
            });
            player.setOnCompletionListener(p -> {
                if (myGeneration != generation) return;
                append("PREP audible playback END, elapsed_ms=" +
                        (SystemClock.elapsedRealtime() - preparedStartMs));
                p.release();
                if (media == p) media = null;
            });
            player.setOnErrorListener((p, what, extra) -> {
                if (myGeneration == generation)
                    append("PREP MediaPlayer ERROR what=" + what + " extra=" + extra);
                return false;
            });
            player.prepareAsync();
        } catch (IOException | IllegalStateException problem) {
            append("Media playback failed: " + problem);
        }
    }

    private void recordMemory() {
        Debug.MemoryInfo memory = new Debug.MemoryInfo();
        Debug.getMemoryInfo(memory);
        append("Memory totalPss_kb=" + memory.getTotalPss());
    }

    private void shareReport() {
        Intent intent = new Intent(Intent.ACTION_SEND);
        intent.setType("text/plain");
        intent.putExtra(Intent.EXTRA_SUBJECT, "VisualBook TTS probe log");
        intent.putExtra(Intent.EXTRA_TEXT, report.toString());
        startActivity(Intent.createChooser(intent, "Share diagnostic log"));
    }

    @Override
    protected void onDestroy() {
        stopAll();
        if (tts != null) {
            tts.shutdown();
            tts = null;
        }
        super.onDestroy();
    }
}

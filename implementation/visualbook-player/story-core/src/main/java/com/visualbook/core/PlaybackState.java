package com.visualbook.core;

import java.util.List;

/** Deterministic logical cursor; no Android or synthesis dependencies. */
public final class PlaybackState {
    private final List<StoryCore.Segment> segments;
    private int index;
    private long generation;
    private String active;
    private boolean speaking, running;

    public PlaybackState(List<StoryCore.Segment> segments) { this.segments = segments; }

    /** Start/restart current segment; does NOT mark it audible. */
    public synchronized String begin() {
        if (index >= segments.size()) return null;
        generation++;
        active = generation + ":" + segments.get(index).id;
        running = true;
        speaking = false;
        return active;
    }

    public synchronized boolean isActive(String id) {
        return running && active != null && active.equals(id);
    }
    public synchronized boolean audibleStart(String id) {
        if (!isActive(id) || speaking) return false;
        speaking = true;
        return true;
    }
    /** True only for successful audible completion of the active utterance. */
    public synchronized boolean audibleDone(String id) {
        if (!isActive(id)) return false;
        index++;
        running = false;
        speaking = false;
        active = null;
        return true;
    }
    /** Audio errors do not imply completion. */
    public synchronized boolean failed(String id) {
        if (!isActive(id)) return false;
        invalidate();
        return true;
    }
    public synchronized void pause() { invalidate(); }
    public synchronized void reset() { invalidate(); index = 0; }
    /** Explicit listener seek (chapters/resume). Invalidate stale TTS generation. */
    public synchronized void seek(int targetIndex) {
        if(targetIndex<0 || targetIndex>segments.size())
            throw new IllegalArgumentException("Seek outside narration");
        invalidate();
        index=targetIndex;
    }
    /** User-specified text-only fallback; never invoked automatically after TTS error. */
    public synchronized void skip() {
        invalidate();
        if (index < segments.size()) index++;
    }
    private void invalidate() { generation++; running = false; speaking = false; active = null; }
    public synchronized int index() { return index; }
    public synchronized boolean isSpeaking() { return speaking; }
    public synchronized boolean running() { return running; }
    public synchronized boolean isFinished() { return index >= segments.size(); }
    public synchronized StoryCore.Segment current() {
        return isFinished() ? null : segments.get(index);
    }
    public synchronized int total() { return segments.size(); }
}

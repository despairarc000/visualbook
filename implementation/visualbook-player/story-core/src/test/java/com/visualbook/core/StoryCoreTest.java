package com.visualbook.core;

public final class StoryCoreTest {
    private static int tests;
    private static void ok(boolean expression, String message) {
        ++tests;
        if (!expression) throw new AssertionError(message);
    }
    private static StoryCore.Story parse(String s, boolean script) {
        try { return StoryCore.compile(s, script); }
        catch (StoryCore.ScriptError e) { throw new AssertionError(e); }
    }
    private static void error(String src) {
        try { StoryCore.compile(src, true); throw new AssertionError("Expected rejection"); }
        catch (StoryCore.ScriptError expected) { ++tests; }
    }
    public static void main(String[] args) {
        StoryCore.Story bare = parse("Isang araw, nagkita sila.", false);
        ok(bare.segments.size() == 1 && bare.segments.get(0).speaker.equals("n"), "plain narration");
        StoryCore.Story literal = parse("@c1m literal text here", false);
        ok(literal.segments.get(0).text.startsWith("@c1m"), "TXT directives remain literal");
        StoryCore.Story cast = parse("@bg classroom\n@n\nIsang araw.\n@c1m\nHello! Kumusta?\n@c2f Mabuti.", true);
        ok(cast.cast.contains("c1") && cast.cast.contains("c2"), "implicit cast");
        ok("m".equals(cast.sexHints.get("c1")) && "f".equals(cast.sexHints.get("c2")),
           "optional speaker sex hints preserved but do not determine voice");
        ok(cast.segments.size() == 4, "sentence segmentation");
        ok(cast.segments.get(1).speaker.equals("c1") && cast.segments.get(2).speaker.equals("c1"), "speaker turn continues");
        ok(cast.segments.get(3).speaker.equals("c2"), "new speaker");
        StoryCore.Story defaultBg = parse("@c1 Hi.", true);
        ok(defaultBg.background.equals("classroom"), "default background");
        StoryCore.Story generic = parse("@c3 Nandito ako.", true);
        ok(generic.cast.contains("c3"), "implicit guest actor");
        error("@unknown do something\nhello");
        error("@bg forest\nHi");
        error("@c0 bad");
        error("@c1m hi\n@c1f bye");
        error("@nm invalid");
        error("@bg neutral\n");
        PlaybackState state = new PlaybackState(cast.segments);
        String first = state.begin();
        ok(state.index() == 0 && !state.isSpeaking(), "queued is not audible");
        ok(!state.audibleDone("wrong"), "reject unrelated callback");
        ok(state.audibleStart(first), "actual start recognized");
        state.pause();
        ok(!state.audibleDone(first) && state.index() == 0, "stale completion ignored after pause");
        String retry = state.begin();
        ok(!retry.equals(first), "new generation on resume");
        ok(state.audibleDone(retry) && state.index() == 1, "audible completion advances once");
        ok(!state.audibleDone(retry), "completion idempotent");
        String failed = state.begin();
        ok(state.failed(failed) && state.index() == 1, "error doesn't skip words");
        ok(!state.audibleDone(failed), "error invalidated callback");
        state.skip();
        ok(state.index() == 2, "manual skip is explicit");
        state.reset();
        ok(state.index() == 0, "reset rewinds");
        System.out.println("VisualBook pure story core tests passed: " + tests);
    }
}

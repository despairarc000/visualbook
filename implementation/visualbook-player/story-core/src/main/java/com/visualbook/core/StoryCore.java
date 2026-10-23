package com.visualbook.core;

import java.text.BreakIterator;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/** Dependency-free, prototype-only grammar. Never evaluate script text as code. */
public final class StoryCore {
    private static final Pattern SPEAKER = Pattern.compile("^@(n|c[1-9][0-9]*)([mf])?(?:\\s+(.*))?$");
    private static final Pattern DIRECTIVE = Pattern.compile("^@([A-Za-z][A-Za-z0-9_-]*)(?:\\s+(.*))?$");
    private StoryCore() { }

    public static final class Segment {
        public final String id, speaker, text;
        public final int line;
        public Segment(String id, String speaker, String text, int line) {
            this.id = id; this.speaker = speaker; this.text = text; this.line = line;
        }
    }

    public static final class Story {
        public final List<Segment> segments;
        public final Set<String> cast;
        public final String background;
        public final String source;
        Story(List<Segment> segments, Set<String> cast, String background, String source) {
            this.segments = Collections.unmodifiableList(new ArrayList<>(segments));
            this.cast = Collections.unmodifiableSet(new LinkedHashSet<>(cast));
            this.background = background;
            this.source = source;
        }
    }

    public static final class ScriptError extends Exception {
        public final int line;
        public ScriptError(int line, String message) {
            super("Line " + line + ": " + message);
            this.line = line;
        }
    }

    /** Plain TXT is literal. Script mode supports @n/@cNm/@cNf plus @bg classroom|neutral. */
    public static Story compile(String source, boolean script) throws ScriptError {
        if (source == null || source.length() > 100_000)
            throw new ScriptError(1, "Story missing or exceeds 100,000 characters");
        String original = source;
        source = source.replace("\r\n", "\n").replace('\r', '\n').replace("\uFEFF", "");
        List<Segment> output = new ArrayList<>();
        Set<String> cast = new LinkedHashSet<>();
        cast.add("n");
        String speaker = "n", background = "classroom";
        StringBuilder block = new StringBuilder();
        int blockLine = 1, lineNum = 0;
        String[] lines = source.split("\n", -1);
        for (String raw : lines) {
            ++lineNum;
            String t = raw.trim();
            if (script && t.startsWith("@")) {
                Matcher match = SPEAKER.matcher(t);
                if (match.matches()) {
                    flush(block, speaker, blockLine, output);
                    speaker = match.group(1);
                    cast.add(speaker);
                    String inline = match.group(3);
                    if (inline != null && !inline.trim().isEmpty()) {
                        blockLine = lineNum;
                        block.append(inline.trim());
                    }
                    continue;
                }
                Matcher directive = DIRECTIVE.matcher(t);
                if (directive.matches() && "bg".equals(directive.group(1))) {
                    flush(block, speaker, blockLine, output);
                    String name = directive.group(2) == null ? "" : directive.group(2).trim();
                    if (!"classroom".equals(name) && !"neutral".equals(name))
                        throw new ScriptError(lineNum, "Only @bg classroom or @bg neutral in this prototype");
                    if (!output.isEmpty())
                        throw new ScriptError(lineNum, "@bg must be before spoken text in this prototype");
                    background = name;
                    continue;
                }
                throw new ScriptError(lineNum, "Unknown directive or malformed speaker marker: " + t);
            }
            if (t.isEmpty()) {
                flush(block, speaker, blockLine, output);
            } else {
                if (block.length() == 0) blockLine = lineNum;
                if (block.length() > 0) block.append(' ');
                block.append(t);
            }
        }
        flush(block, speaker, blockLine, output);
        if (output.isEmpty()) throw new ScriptError(1, "No narration text");
        if (output.size() > 500) throw new ScriptError(1, "More than 500 utterances");
        return new Story(output, cast, background, original);
    }

    private static void flush(StringBuilder block, String speaker, int line, List<Segment> out) {
        String spoken = block.toString().trim();
        block.setLength(0);
        if (spoken.isEmpty()) return;
        // Each sentence becomes a reproducible TTS unit; no word-timing dependency.
        BreakIterator boundary = BreakIterator.getSentenceInstance(Locale.forLanguageTag("fil-PH"));
        boundary.setText(spoken);
        int start = boundary.first();
        int end;
        while ((end = boundary.next()) != BreakIterator.DONE) {
            String sentence = spoken.substring(start, end).trim();
            if (!sentence.isEmpty()) out.add(new Segment("u" + (out.size() + 1), speaker, sentence, line));
            start = end;
        }
    }
}

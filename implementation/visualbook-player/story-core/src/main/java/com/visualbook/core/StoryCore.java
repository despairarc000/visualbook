package com.visualbook.core;

import java.text.BreakIterator;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.LinkedHashMap;
import java.util.Map;
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
        public final String id, speaker, text, artStyle;
        public final int line;
        public Segment(String id, String speaker, String text, String artStyle, int line) {
            this.id = id; this.speaker = speaker; this.text = text;
            this.artStyle = artStyle; this.line = line;
        }
    }

    public static final class Story {
        public final List<Segment> segments;
        public final Set<String> cast;
        public final Map<String, String> sexHints;
        public final String background;
        public final String source;
        Story(List<Segment> segments, Set<String> cast, Map<String,String> sexHints,
              String background, String source) {
            this.segments = Collections.unmodifiableList(new ArrayList<>(segments));
            this.cast = Collections.unmodifiableSet(new LinkedHashSet<>(cast));
            this.sexHints = Collections.unmodifiableMap(new LinkedHashMap<>(sexHints));
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

    /**
     * Plain TXT is literal. Script mode supports the existing dialogue grammar
     * plus scoped, global visual modes: @chibi (or @style chibi), @end.
     * Closing @end restores the prior mode, including in nested style blocks.
     * Art modes are authored state, not spoken dialogue or timed TTS cues.
     */
    public static Story compile(String source, boolean script) throws ScriptError {
        if (source == null || source.length() > 100_000)
            throw new ScriptError(1, "Story missing or exceeds 100,000 characters");
        String original = source;
        source = source.replace("\r\n", "\n").replace('\r', '\n').replace("\uFEFF", "");
        List<Segment> output = new ArrayList<>();
        Set<String> cast = new LinkedHashSet<>();
        cast.add("n");
        Map<String,String> sexHints = new LinkedHashMap<>();
        String speaker = "n", background = "classroom", style = "normal";
        List<String> styleStack = new ArrayList<>();
        List<Integer> styleOpenLines = new ArrayList<>();
        StringBuilder block = new StringBuilder();
        int blockLine = 1, lineNum = 0;
        String[] lines = source.split("\n", -1);
        for (String raw : lines) {
            ++lineNum;
            String t = raw.trim();
            if (script && t.startsWith("@")) {
                Matcher match = SPEAKER.matcher(t);
                if (match.matches()) {
                    flush(block, speaker, style, blockLine, output);
                    speaker = match.group(1);
                    cast.add(speaker);
                    String hint = match.group(2);
                    if (hint != null) {
                        if ("n".equals(speaker)) throw new ScriptError(lineNum,
                                "Narrator cannot have a character sex hint");
                        if (sexHints.containsKey(speaker) && !sexHints.get(speaker).equals(hint))
                            throw new ScriptError(lineNum, "Conflicting sex hint for " + speaker);
                        sexHints.put(speaker, hint);
                    }
                    String inline = match.group(3);
                    if (inline != null && !inline.trim().isEmpty()) {
                        blockLine = lineNum;
                        block.append(inline.trim());
                    }
                    continue;
                }
                Matcher directive = DIRECTIVE.matcher(t);
                if (directive.matches() && "bg".equals(directive.group(1))) {
                    flush(block, speaker, style, blockLine, output);
                    String name = directive.group(2) == null ? "" : directive.group(2).trim();
                    if (!"classroom".equals(name) && !"neutral".equals(name))
                        throw new ScriptError(lineNum, "Only @bg classroom or @bg neutral in this prototype");
                    if (!output.isEmpty())
                        throw new ScriptError(lineNum, "@bg must be before spoken text in this prototype");
                    background = name;
                    continue;
                }
                if (directive.matches()) {
                    String command = directive.group(1);
                    String arg = directive.group(2) == null ? "" : directive.group(2).trim();
                    boolean start = "chibi".equals(command) || "style".equals(command);
                    if (start) {
                        String next;
                        if ("chibi".equals(command)) {
                            if (!arg.isEmpty()) throw new ScriptError(lineNum,
                                    "@chibi takes no arguments");
                            next = "chibi";
                        } else {
                            if (!"chibi".equals(arg) && !"normal".equals(arg) &&
                                    !"cartoon".equals(arg)) {
                                throw new ScriptError(lineNum,
                                        "@style supports chibi, normal or cartoon in v0.2");
                            }
                            next = "cartoon".equals(arg) ? "normal" : arg;
                        }
                        if (styleStack.size() >= 8) throw new ScriptError(lineNum,
                                "At most 8 nested style blocks");
                        flush(block, speaker, style, blockLine, output);
                        styleStack.add(style);
                        styleOpenLines.add(lineNum);
                        style = next;
                        continue;
                    }
                    if ("end".equals(command)) {
                        if (!arg.isEmpty()) throw new ScriptError(lineNum,
                                "@end takes no arguments");
                        if (styleStack.isEmpty()) throw new ScriptError(lineNum,
                                "Unmatched @end without a style opening tag");
                        flush(block, speaker, style, blockLine, output);
                        style = styleStack.remove(styleStack.size()-1);
                        styleOpenLines.remove(styleOpenLines.size()-1);
                        continue;
                    }
                }
                throw new ScriptError(lineNum, "Unknown directive or malformed speaker marker: " + t);
            }
            if (t.isEmpty()) {
                flush(block, speaker, style, blockLine, output);
            } else {
                if (block.length() == 0) blockLine = lineNum;
                if (block.length() > 0) block.append(' ');
                block.append(t);
            }
        }
        flush(block, speaker, style, blockLine, output);
        if (!styleStack.isEmpty()) throw new ScriptError(
                styleOpenLines.get(styleOpenLines.size()-1),
                "Missing @end for this art-style block");
        if (output.isEmpty()) throw new ScriptError(1, "No narration text");
        if (output.size() > 500) throw new ScriptError(1, "More than 500 utterances");
        return new Story(output, cast, sexHints, background, original);
    }

    private static void flush(StringBuilder block, String speaker, String artStyle,
                              int line, List<Segment> out) {
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
            if (!sentence.isEmpty()) out.add(new Segment("u" + (out.size() + 1), speaker, sentence, artStyle, line));
            start = end;
        }
    }
}

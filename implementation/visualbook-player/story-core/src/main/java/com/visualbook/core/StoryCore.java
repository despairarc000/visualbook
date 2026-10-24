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
        public final List<VectorFigure.Placement> visibleFigures;
        public final Map<String,VectorFigure.Motion> motions;
        public final int line;
        public Segment(String id, String speaker, String text, String artStyle,
                       List<VectorFigure.Placement> visibleFigures,
                       Map<String,VectorFigure.Motion> motions,int line) {
            this.id = id; this.speaker = speaker; this.text = text;
            this.artStyle = artStyle; this.line = line;
            this.visibleFigures = Collections.unmodifiableList(new ArrayList<>(visibleFigures));
            this.motions = Collections.unmodifiableMap(new LinkedHashMap<>(motions));
        }
    }

    public static final class Story {
        public final List<Segment> segments;
        public final Set<String> cast;
        public final Map<String, String> sexHints;
        public final Map<String, VectorFigure> figures;
        public final String background;
        public final String source;
        Story(List<Segment> segments, Set<String> cast, Map<String,String> sexHints,
              Map<String,VectorFigure> figures, String background, String source) {
            this.segments = Collections.unmodifiableList(new ArrayList<>(segments));
            this.cast = Collections.unmodifiableSet(new LinkedHashSet<>(cast));
            this.sexHints = Collections.unmodifiableMap(new LinkedHashMap<>(sexHints));
            this.figures = Collections.unmodifiableMap(new LinkedHashMap<>(figures));
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
        Map<String,VectorFigure> figures = new LinkedHashMap<>();
        LinkedHashMap<String,VectorFigure.Placement> shown = new LinkedHashMap<>();
        LinkedHashMap<String,VectorFigure.Motion> motions = new LinkedHashMap<>();
        VectorFigure.Builder figureBuilder = null;
        int figureBeginLine = 0, figureLines = 0;
        String figureId = null;
        String speaker = "n", background = "classroom", style = "normal";
        List<String> styleStack = new ArrayList<>();
        List<Integer> styleOpenLines = new ArrayList<>();
        StringBuilder block = new StringBuilder();
        int blockLine = 1, lineNum = 0;
        String[] lines = source.split("\n", -1);
        for (String raw : lines) {
            ++lineNum;
            String t = raw.trim();
            if (script && figureBuilder != null) {
                if ("@endfigure".equals(t)) {
                    try { figures.put(figureId,figureBuilder.build()); }
                    catch (VectorFigure.GeometryException ex) {
                        throw new ScriptError(lineNum,ex.getMessage());
                    }
                    figureBuilder = null;
                    figureId = null;
                    continue;
                }
                if (!t.isEmpty() && !t.startsWith("#")) {
                    if (++figureLines > 64) throw new ScriptError(lineNum, "Figure exceeds 64 lines");
                    try { figureBuilder.add(t); }
                    catch (VectorFigure.GeometryException ex) {
                        throw new ScriptError(lineNum, ex.getMessage());
                    }
                }
                continue;
            }
            if (script && t.startsWith("@")) {
                if (t.startsWith("@figure ")) {
                    flush(block, speaker, style, blockLine, output, new ArrayList<>(shown.values()), motions);
                    String[] args=t.split("\\s+");
                    if (args.length!=3)throw new ScriptError(lineNum,
                            "@figure requires id and WxH artboard, e.g. @figure spirit 100x100");
                    if (figures.size()>=8)throw new ScriptError(lineNum,"Maximum 8 custom figures per story");
                    if (figures.containsKey(args[1]))throw new ScriptError(lineNum,
                            "Duplicate figure id: "+args[1]);
                    try { figureBuilder = new VectorFigure.Builder(args[1],args[2]); }
                    catch (VectorFigure.GeometryException ex) {
                        throw new ScriptError(lineNum,ex.getMessage());
                    }
                    figureId=args[1];figureBeginLine=lineNum;figureLines=0;
                    continue;
                }
                if (t.startsWith("@show ") || t.startsWith("@hide ")) {
                    flush(block, speaker, style, blockLine, output, new ArrayList<>(shown.values()), motions);
                    String[] args=t.split("\\s+");
                    if ("@show".equals(args[0])) {
                        if(args.length<2||args.length>4)
                            throw new ScriptError(lineNum,
                                "@show id [left|center|right] or @show id at:x,y width:w");
                        if(!figures.containsKey(args[1]))
                            throw new ScriptError(lineNum,"Unknown figure id: "+args[1]);
                        VectorFigure.Placement placement;
                        if(args.length==4 && args[2].startsWith("at:") && args[3].startsWith("width:")) {
                            String[] pos=args[2].substring(3).split(",",-1);
                            if(pos.length!=2)throw new ScriptError(lineNum,"at:x,y needs two numbers");
                            try {
                                float px=Float.parseFloat(pos[0]), py=Float.parseFloat(pos[1]);
                                float pw=Float.parseFloat(args[3].substring(6));
                                if(!Float.isFinite(px)||!Float.isFinite(py)||!Float.isFinite(pw)||
                                        px<0||px>100||py<0||py>100||pw<5||pw>70)
                                    throw new NumberFormatException();
                                placement=new VectorFigure.Placement(args[1],px,py,pw);
                            } catch(NumberFormatException ex) {
                                throw new ScriptError(lineNum,"Use finite at:x,y (0..100) and width:w (5..70)");
                            }
                        } else if(args.length<=3) {
                            String slot=args.length==2?"center":args[2];
                            if(!"left".equals(slot)&&!"center".equals(slot)&&!"right".equals(slot))
                                throw new ScriptError(lineNum,"Use slot left, center or right");
                            placement=new VectorFigure.Placement(args[1],slot);
                        } else throw new ScriptError(lineNum,"Use @show id at:x,y width:w");
                        if(!shown.containsKey(args[1]) && shown.size()>=4)
                            throw new ScriptError(lineNum,"Only 4 simultaneous figures in prototype");
                        shown.put(args[1],placement);
                    } else {
                        if(args.length!=2)throw new ScriptError(lineNum,"@hide requires an id");
                        if(!figures.containsKey(args[1]))throw new ScriptError(lineNum,
                                "Unknown figure id: "+args[1]);
                        shown.remove(args[1]);
                    }
                    continue;
                }
                if(t.startsWith("@animate ") || t.startsWith("@still ")) {
                    flush(block,speaker,style,blockLine,output,
                            new ArrayList<>(shown.values()),motions);
                    String[] args=t.split("\\s+");
                    if("@animate".equals(args[0])) {
                        if(args.length!=5 ||
                                !args[3].startsWith("swing:") ||
                                !args[4].startsWith("period:"))
                            throw new ScriptError(lineNum,
                                    "Use @animate figure instance swing:degrees period:milliseconds");
                        VectorFigure f=figures.get(args[1]);
                        if(f==null || f.rootInstance(args[2])==null)
                            throw new ScriptError(lineNum,
                                    "Unknown figure or named root instance in @animate");
                        float swing;
                        int period;
                        try {
                            swing=Float.parseFloat(args[3].substring(6));
                            period=Integer.parseInt(args[4].substring(7));
                        } catch(NumberFormatException ex){
                            throw new ScriptError(lineNum,"Invalid motion swing or period");
                        }
                        if(!Float.isFinite(swing)||swing<=0||swing>75||
                                period<250||period>3000)
                            throw new ScriptError(lineNum,
                                    "Motion swing must be 0..75 degrees and period 250..3000ms");
                        VectorFigure.Motion motion=new VectorFigure.Motion(args[1],args[2],swing,period);
                        if(!motions.containsKey(motion.key()) && motions.size()>=8)
                            throw new ScriptError(lineNum,"No more than 8 active motions");
                        motions.put(motion.key(),motion);
                    } else {
                        if(args.length!=3)
                            throw new ScriptError(lineNum,"Use @still figure instance");
                        VectorFigure f=figures.get(args[1]);
                        if(f==null || f.rootInstance(args[2])==null)
                            throw new ScriptError(lineNum,"Unknown figure or named instance in @still");
                        motions.remove(args[1]+"/"+args[2]);
                    }
                    continue;
                }
                Matcher match = SPEAKER.matcher(t);
                if (match.matches()) {
                    flush(block, speaker, style, blockLine, output, new ArrayList<>(shown.values()), motions);
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
                    flush(block, speaker, style, blockLine, output, new ArrayList<>(shown.values()), motions);
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
                        flush(block, speaker, style, blockLine, output, new ArrayList<>(shown.values()), motions);
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
                        flush(block, speaker, style, blockLine, output, new ArrayList<>(shown.values()), motions);
                        style = styleStack.remove(styleStack.size()-1);
                        styleOpenLines.remove(styleOpenLines.size()-1);
                        continue;
                    }
                }
                throw new ScriptError(lineNum, "Unknown directive or malformed speaker marker: " + t);
            }
            if (t.isEmpty()) {
                flush(block, speaker, style, blockLine, output, new ArrayList<>(shown.values()), motions);
            } else {
                if (block.length() == 0) blockLine = lineNum;
                if (block.length() > 0) block.append(' ');
                block.append(t);
            }
        }
        if (figureBuilder!=null)
            throw new ScriptError(figureBeginLine,"Missing @endfigure");
        flush(block, speaker, style, blockLine, output, new ArrayList<>(shown.values()), motions);
        if (!styleStack.isEmpty()) throw new ScriptError(
                styleOpenLines.get(styleOpenLines.size()-1),
                "Missing @end for this art-style block");
        if (output.isEmpty()) throw new ScriptError(1, "No narration text");
        if (output.size() > 500) throw new ScriptError(1, "More than 500 utterances");
        return new Story(output, cast, sexHints, figures, background, original);
    }

    private static void flush(StringBuilder block, String speaker, String artStyle,
                              int line, List<Segment> out,
                              List<VectorFigure.Placement> visibleFigures,
                              Map<String,VectorFigure.Motion> motions) {
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
            if (!sentence.isEmpty()) out.add(new Segment("u" + (out.size() + 1), speaker, sentence, artStyle, visibleFigures, motions, line));
            start = end;
        }
    }
}

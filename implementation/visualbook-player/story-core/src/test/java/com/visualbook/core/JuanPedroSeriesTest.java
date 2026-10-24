package com.visualbook.core;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.charset.StandardCharsets;

/** Confirm submitted episodes actually compile with the Android StoryCore. */
public final class JuanPedroSeriesTest {
    private static int assertions;
    private static void ok(boolean condition,String why) {
        assertions++;if(!condition)throw new AssertionError(why);
    }
    public static void main(String[] args) throws Exception {
        String source=Files.readString(
            Path.of("stories/juan-pedro/season-01/E01_Ang_Kampanang_Nawala.story.md"),
            StandardCharsets.UTF_8);
        ok(source.startsWith("@bg neutral\n@n"),"episode starts in supported stage mode");
        ok(source.length()<100_000,"under prototype source character limit");
        StoryCore.Story compiled=StoryCore.compile(source,true);
        ok(compiled.segments.size()>=350 && compiled.segments.size()<=500,
                "episode compiles under the real 500-utterance engine limit");
        ok(compiled.cast.contains("c1") && compiled.cast.contains("c2") &&
           compiled.cast.contains("c3") && compiled.cast.contains("c9"),
                "serial characters and mystery voice retain their actor identity");
        ok("neutral".equals(compiled.background),"using supported renderer background");
        boolean containsChibi=false, containsNormal=false;
        for (StoryCore.Segment segment:compiled.segments) {
            if("chibi".equals(segment.artStyle))containsChibi=true;
            if("normal".equals(segment.artStyle))containsNormal=true;
            ok(!segment.text.trim().isEmpty(),"no empty TTS utterances");
        }
        ok(containsChibi && containsNormal,"comedic style is scoped and later restored");
        ok(compiled.source.equals(source),"story original source preserved");
        ok(compiled.segments.get(0).text.contains("Ang Kampanang Nawala"),
                "episode title is spoken");
        ok(compiled.segments.get(compiled.segments.size()-1).text.contains("Itutuloy"),
                "season continuation is not omitted");
        int countWords=0;
        for(String line:source.split("\\R")){
            if(!line.trim().isEmpty()&&!line.startsWith("@")){
                countWords+=line.trim().split("\\s+").length;
            }
        }
        ok(countWords>=3100 && countWords<=3650,"approx 24-minute word-budget target");
        System.out.println("Juan Pedro Episode 1 story compile PASS: "+
                compiled.segments.size()+" utterances, "+countWords+
                " whitespace-separated spoken words, "+assertions+" checks");
    }
}

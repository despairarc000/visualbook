package com.visualbook.core;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

/** Real distributed Episode 1 + series-directed compatibility acceptance. */
public final class SeriesDirectorTest {
    private static int assertions;
    private static void ok(boolean yes,String what){
        assertions++;if(!yes)throw new AssertionError(what);
    }
    private static void error(String src){
        try {SeriesDirector.compile(src,true);throw new AssertionError("bad metadata accepted");}
        catch(StoryCore.ScriptError expected){assertions++;}
    }
    public static void main(String[] args) throws Exception{
        String packed=Files.readString(Path.of(
            "implementation/visualbook-player/app/src/main/assets/juan_pedro_e01.story.md"),
            StandardCharsets.UTF_8);
        String original=Files.readString(Path.of(
            "stories/juan-pedro/season-01/E01_Ang_Kampanang_Nawala.story.md"),
            StandardCharsets.UTF_8);
        String directedSource=Files.readString(Path.of(
            "stories/juan-pedro/season-01/E01_Directed_v07.story.md"),
            StandardCharsets.UTF_8);
        ok(packed.equals(directedSource),"distributed episode precisely matches author source");
        SeriesDirector.DirectedStory compiled=SeriesDirector.compile(packed,true);
        StoryCore.Story base=StoryCore.compile(original,true);
        ok(compiled.story.segments.size()==base.segments.size(),
            "all spoken sentences retained and nothing injected into TTS");
        ok(compiled.story.segments.size()>400 && compiled.story.segments.size()<=500,
            "full 24-minute authored script within v0.7 engine limit");
        for(int i=0;i<base.segments.size();i++){
            ok(base.segments.get(i).text.equals(compiled.story.segments.get(i).text),
               "spoken words unchanged at segment "+i);
            ok(base.segments.get(i).speaker.equals(compiled.story.segments.get(i).speaker),
               "speaker identity unchanged at segment "+i);
        }
        ok(compiled.chapters.size()==5,"five actual playable chapters");
        ok(compiled.chapters.get(0).segmentIndex==0,"episode starts with chapter one");
        int last=-1;for(SeriesDirector.Chapter c:compiled.chapters){
            ok(c.segmentIndex>last,"chapters strictly ordered");last=c.segmentIndex;
        }
        ok(compiled.names.size()==9,"all supporting voice identities resolved");
        ok("Juan".equals(compiled.names.get("c1"))&&
           "Maria".equals(compiled.names.get("c3"))&&
           "Elias".equals(compiled.names.get("c9")),
           "cast IDs stable across one episode");
        boolean guava=false,plaza=false,bridge=false,warehouse=false,library=false,restored=false;
        for(SeriesDirector.SceneState state:compiled.segmentScenes){
            switch(state.scene){
                case "guava":guava=true;break;
                case "plaza":plaza=true;break;
                case "bridge":bridge=true;break;
                case "warehouse":warehouse=true;break;
                case "library":library=true;break;
                case "plaza_restored":restored=true;break;
            }
        }
        ok(guava&&plaza&&bridge&&warehouse&&library&&restored,
           "key story locations and restored bell are distinct visual scenes");
        ok(compiled.story.cast.contains("c9"),"final mystery voice present");
        ok(compiled.story.background.equals("neutral"),"legacy background directive compatible");
        String hash=SeriesDirector.fingerprint(packed);
        ok(hash.length()==64&&hash.equals(compiled.sourceId),"story fingerprint stable");
        ok(SeriesDirector.restoreIndex(hash,hash,218,430)==218,"valid resume index");
        ok(SeriesDirector.restoreIndex("old",hash,218,430)==0,"updated episode resets stale progress");
        ok(SeriesDirector.restoreIndex(hash,hash,-1,430)==0,"negative corrupted index reset");
        ok(SeriesDirector.restoreIndex(hash,hash,431,430)==0,"overflow index reset");
        ok(SeriesDirector.restoreIndex(hash,hash,430,430)==430,"completed story state preserved");
        PlaybackState playback=new PlaybackState(compiled.story.segments);
        String active=playback.begin();
        ok(playback.audibleStart(active),"ready TTS start recognized");
        playback.seek(compiled.chapters.get(2).segmentIndex);
        ok(!playback.audibleDone(active)&&
           playback.index()==compiled.chapters.get(2).segmentIndex,
           "seek invalidates old TTS generation, does not fabricate completion");
        StoryCore.Story txt=SeriesDirector.compile("@scene nope\nA simple sentence.",false).story;
        ok(txt.segments.get(0).text.contains("@scene nope"),
           "plain text treats all metadata literally");
        error("@scene nowhere\n@n Hi");
        error("@chapter bad\n@n Hi");
        error("@chapter x First\n@n Hi\n@chapter x Again\n@n Hi");
        error("@chapter x No following words\n");
        error("@name c1 Juan\n@name c1 Juan Again\n@n Hi");
        error("@name narrator Joe\n@n Hi");
        System.out.println("VisualBook v0.7 series tests PASS: "+
                compiled.story.segments.size()+" utterances, "+compiled.chapters.size()+
                " chapters, "+assertions+" assertions");
    }
}

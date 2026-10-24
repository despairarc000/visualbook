package com.visualbook.core;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * v0.7 episode direction layer. Metadata directives are silent and deliberately
 * removed as BLANK LINES before passing to the stable v0.6 StoryCore compiler.
 * This preserves source-line alignment, sentence segmentation and TTS authority.
 */
public final class SeriesDirector {
    private SeriesDirector() {}

    public static final class SceneState {
        public final String scene, chapterId;
        public SceneState(String scene,String chapterId){
            this.scene=scene;this.chapterId=chapterId;
        }
    }
    public static final class Chapter {
        public final String id, title;
        public final int segmentIndex, line;
        public Chapter(String id,String title,int segmentIndex,int line){
            this.id=id;this.title=title;this.segmentIndex=segmentIndex;this.line=line;
        }
    }
    private static final class Mark {
        final int line;
        final String scene,chapterId;
        Mark(int line,String scene,String chapterId){
            this.line=line;this.scene=scene;this.chapterId=chapterId;
        }
    }
    private static final class ChapterOpen {
        final String id,title; final int line;
        ChapterOpen(String id,String title,int line){
            this.id=id;this.title=title;this.line=line;
        }
    }
    public static final class DirectedStory {
        public final StoryCore.Story story;
        public final List<SceneState> segmentScenes;
        public final List<Chapter> chapters;
        public final Map<String,String> names;
        public final String sourceId, source;
        DirectedStory(StoryCore.Story story,List<SceneState> segmentScenes,
                      List<Chapter> chapters,Map<String,String> names,
                      String sourceId,String source){
            this.story=story;
            this.segmentScenes=Collections.unmodifiableList(new ArrayList<>(segmentScenes));
            this.chapters=Collections.unmodifiableList(new ArrayList<>(chapters));
            this.names=Collections.unmodifiableMap(new LinkedHashMap<>(names));
            this.sourceId=sourceId;this.source=source;
        }
    }
    private static boolean isScene(String n){
        switch(n) {
            case "neutral": case "classroom": case "guava": case "plaza":
            case "library": case "smithy": case "bridge": case "warehouse":
            case "river": case "forest": case "balete": return true;
            default:return false;
        }
    }
    private static boolean validId(String n) {return n.matches("[a-z][a-z0-9_-]{0,31}");}
    public static String fingerprint(String source) {
        try {
            byte[] digest=MessageDigest.getInstance("SHA-256").digest(
                    source.getBytes(StandardCharsets.UTF_8));
            StringBuilder hex=new StringBuilder();
            for(byte b:digest)hex.append(String.format(Locale.ROOT,"%02x",b&255));
            return hex.toString();
        } catch(NoSuchAlgorithmException e){throw new IllegalStateException(e);}
    }
    public static int restoreIndex(String savedHash,String expectedHash,int saved,int total) {
        if(expectedHash==null || !expectedHash.equals(savedHash) || total<1)return 0;
        return saved>=0 && saved<=total ? saved : 0;
    }

    public static DirectedStory compile(String source,boolean script)
            throws StoryCore.ScriptError {
        if(source==null || source.length()>100_000)
            throw new StoryCore.ScriptError(1,"Story too large");
        String id=fingerprint(source);
        if(!script){
            StoryCore.Story story=StoryCore.compile(source,false);
            List<SceneState> cues=new ArrayList<>();
            for(int i=0;i<story.segments.size();i++)
                cues.add(new SceneState("neutral","main"));
            return new DirectedStory(story,cues,
                    Collections.singletonList(new Chapter("main","Story",0,1)),
                    Collections.emptyMap(),id,source);
        }
        String normalized=source.replace("\r\n","\n").replace('\r','\n')
                .replace("\uFEFF","");
        String[] lines=normalized.split("\n",-1);
        StringBuilder stripped=new StringBuilder(normalized.length());
        List<Mark> marks=new ArrayList<>();
        List<ChapterOpen> requested=new ArrayList<>();
        Map<String,String> cast=new LinkedHashMap<>();
        String scene="neutral",chapterId="main";
        for(int i=0;i<lines.length;i++){
            int line=i+1;
            String t=lines[i].trim();
            if(t.startsWith("@scene ") || t.equals("@scene")) {
                String name=t.length()>7?t.substring(7).trim():"";
                if(!isScene(name))throw new StoryCore.ScriptError(line,
                        "Unknown scene. Use guava/plaza/library/bridge/forest/river/warehouse/balete etc.");
                scene=name;
                marks.add(new Mark(line,scene,chapterId));
            } else if(t.startsWith("@chapter ") || t.equals("@chapter")) {
                String[] values=t.split("\\s+",3);
                if(values.length!=3 || !validId(values[1]) ||
                        values[2].trim().isEmpty() || values[2].length()>60)
                    throw new StoryCore.ScriptError(line,"Use @chapter id Title (<=60 characters)");
                for(ChapterOpen open:requested)
                    if(open.id.equals(values[1]))
                        throw new StoryCore.ScriptError(line,"Duplicate chapter ID");
                if(requested.size()>=16)
                    throw new StoryCore.ScriptError(line,"At most 16 chapters");
                chapterId=values[1];requested.add(new ChapterOpen(chapterId,values[2],line));
                marks.add(new Mark(line,scene,chapterId));
            } else if(t.startsWith("@name ") || t.equals("@name")) {
                String[] values=t.split("\\s+",3);
                if(values.length!=3 || !values[1].matches("c[1-9][0-9]*") ||
                        values[2].trim().isEmpty() || values[2].length()>32 ||
                        cast.containsKey(values[1]) || cast.size()>=20)
                    throw new StoryCore.ScriptError(line,"Use unique @name cN Display Name (<=32 chars)");
                cast.put(values[1],values[2].trim());
            } else {
                stripped.append(lines[i]);
            }
            if(i<lines.length-1)stripped.append('\n');
        }
        StoryCore.Story story=StoryCore.compile(stripped.toString(),true);
        List<SceneState> cues=new ArrayList<>();
        for(StoryCore.Segment s:story.segments) {
            String currentScene="neutral",currentChapter="main";
            for(Mark m:marks) {
                if(m.line>s.line)break;
                currentScene=m.scene; currentChapter=m.chapterId;
            }
            cues.add(new SceneState(currentScene,currentChapter));
        }
        List<Chapter> chapters=new ArrayList<>();
        if(requested.isEmpty())
            chapters.add(new Chapter("main","Story",0,1));
        else {
            for(ChapterOpen c:requested){
                int offset=0;
                while(offset<story.segments.size() &&
                      story.segments.get(offset).line<=c.line)offset++;
                if(offset>=story.segments.size())
                    throw new StoryCore.ScriptError(c.line,"Chapter has no subsequent narration");
                if(!chapters.isEmpty() && offset<=chapters.get(chapters.size()-1).segmentIndex)
                    throw new StoryCore.ScriptError(c.line,"Chapter needs a new narrated segment");
                chapters.add(new Chapter(c.id,c.title,offset,c.line));
            }
        }
        return new DirectedStory(story,cues,chapters,cast,id,source);
    }
    public static Chapter chapterAt(DirectedStory story,int segment){
        Chapter found=story.chapters.get(0);
        for(Chapter c:story.chapters)if(c.segmentIndex<=segment)found=c;
        return found;
    }
}

package com.visualbook.core;

import java.util.List;

/** Pure deterministic smoke/regression tests of preview scene coordinate math. */
public final class RenderInspectorTest {
    private static int count=0;
    private static void check(boolean good,String why){
        count++; if(!good)throw new AssertionError(why);
    }
    private static void invalid(Runnable r,String why){
        try {r.run();throw new AssertionError(why);}
        catch(IllegalArgumentException expected){count++;}
    }

    public static void main(String[] args) throws Exception {
        String script="@bg neutral\n"+
              "@figure spirit 100x100\n"+
              "circle 77 56 11 fill:#FF9900\n"+
              "@endfigure\n"+
              "@show spirit at:77,56 width:40\n"+
              "@n A creature appears. And moves.\n"+
              "@chibi\n@c3 Hello!\n@end\n"+
              "@hide spirit\n@n Goodbye.";
        StoryCore.Story story=StoryCore.compile(script,true);
        check(story.segments.size()==4,"preview can inspect all speaker/scene segments");
        check(story.segments.get(0).visibleFigures.size()==1,
                "scene snapshot captures visible figure");
        check(story.segments.get(2).artStyle.equals("chibi"),
                "preview snapshot preserves effective mid-story style");
        check(story.segments.get(3).visibleFigures.isEmpty(),
                "hide is visible in later snapshot");
        List<VectorFigure.Placement> visible=story.segments.get(0).visibleFigures;
        RenderInspector.Hit center=RenderInspector.inspect(story.figures,visible,
                1000f,600f,770f,336f);
        check(center.matched&&"spirit".equals(center.figureId),
                "stage coordinates select the rendered figure");
        check(Math.abs(center.stageX-77f)<.001&&Math.abs(center.stageY-56f)<.001,
                "stage selection uses normalized 100x100 stage coordinates");
        check(Math.abs(center.localX-50f)<.001&&Math.abs(center.localY-50f)<.001,
                "center maps to figure-local midpoint after uniform fit");
        RenderInspector.Hit origin=RenderInspector.inspect(story.figures,visible,
                1000f,600f,641f,207f);
        check(origin.matched && origin.localX==0f && origin.localY==0f,
                "artboard top-left maps to local origin");
        RenderInspector.Hit end=RenderInspector.inspect(story.figures,visible,
                1000f,600f,899f,465f);
        check(end.matched&&Math.abs(end.localX-100f)<.001,
                "artboard bottom-right respects fitted rectangle");
        RenderInspector.Hit outside=RenderInspector.inspect(story.figures,visible,
                1000f,600f,10f,10f);
        check(!outside.matched&&outside.figureId==null,
                "tap outside artboard is an explicit no-hit");
        check(!RenderInspector.inspect(story.figures,story.segments.get(3).visibleFigures,
                1000f,600f,770f,336f).matched,
                "hidden figures cannot be selected");
        String report=RenderInspector.report(story,0,center);
        check(report.contains("Segment 1/4") && report.contains("source line"),
                "report preserves source context for future correction");
        check(report.contains("spirit artboard 100x100"),
                "report includes figure ID and artboard dimensions");
        check(report.contains("local (50.0,50.0)"),
                "report captures exact geometry selection");
        check(report.contains("Background: neutral")&&report.contains("Speaker: n"),
                "report captures presentation state");
        check(RenderInspector.report(story,3,outside).contains("Visible figures: 0"),
                "report accurately describes scene without assets");
        check(RenderInspector.report(story,2,null).contains("style: chibi"),
                "report includes mid-story style");
        invalid(()->RenderInspector.inspect(story.figures,visible,0,600,100,100),
                "zero viewport rejected");
        invalid(()->RenderInspector.inspect(story.figures,visible,1000,600,
                Float.NaN,80),"nonfinite tap rejected");
        invalid(()->RenderInspector.report(story,-1,null),
                "negative preview frame rejected");
        invalid(()->RenderInspector.report(story,99,null),
                "out of range preview frame rejected");

        StoryCore.Story normal=StoryCore.compile("@n One. Two.",false);
        check(normal.figures.isEmpty() && normal.segments.size()==2,
                "plain TXT remains supported without preview directives");
        System.out.println("VisualBook render inspection tests passed: "+count);
    }
}

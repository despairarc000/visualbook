package com.visualbook.core;

import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * Android-independent rendered-state inspection. Shares StageView's v0.3+ sizing
 * contract so a stage tap can be reported as precise figure-local coordinates.
 * Inspects bounding rectangles, not per-path hit-testing.
 */
public final class RenderInspector {
    private RenderInspector(){}

    public static final class Hit {
        public final float stageX,stageY;
        public final String figureId; // null means point isn't inside a visible figure's artboard
        public final float localX,localY;
        public final boolean matched;
        private Hit(float stageX,float stageY,String figureId,float localX,float localY,boolean matched){
            this.stageX=stageX;this.stageY=stageY;this.figureId=figureId;
            this.localX=localX;this.localY=localY;this.matched=matched;
        }
    }

    public static Hit inspect(Map<String,VectorFigure> figures,
                              List<VectorFigure.Placement> visible,
                              float width,float height,float pixelX,float pixelY) {
        if(!Float.isFinite(width)||!Float.isFinite(height)||width<=0||height<=0||
           !Float.isFinite(pixelX)||!Float.isFinite(pixelY))
            throw new IllegalArgumentException("Invalid stage view or tap coordinates");
        float stageX=pixelX/width*100f,stageY=pixelY/height*100f;
        // Hit-test the most recently painted artboard first. More advanced
        // shape-specific picking can later refine this selection.
        for(int i=visible.size()-1;i>=0;i--){
            VectorFigure.Placement p=visible.get(i);
            VectorFigure f=figures.get(p.figureId);
            if(f==null)continue;
            float scale=VectorFigure.fitScale(f.width,f.height,
                    width*p.widthPercent/100f,height*.43f);
            float drawnW=f.width*scale,drawnH=f.height*scale;
            float left=width*p.stageX/100f-drawnW*.5f;
            float top=height*p.stageY/100f-drawnH*.5f;
            // Bounding box maps to figure-local artboard, uniform fit, never
            // the enclosing stage or UI captions.
            if(pixelX>=left && pixelX<=left+drawnW &&
               pixelY>=top && pixelY<=top+drawnH)
                return new Hit(stageX,stageY,p.figureId,
                        (pixelX-left)/scale,(pixelY-top)/scale,true);
        }
        return new Hit(stageX,stageY,null,0f,0f,false);
    }

    private static String one(float number) {
        return String.format(Locale.ROOT,"%.1f",number);
    }

    public static String report(StoryCore.Story story,int index,Hit hit) {
        if(story==null||index<0||index>=story.segments.size())
            throw new IllegalArgumentException("No preview segment");
        StoryCore.Segment seg=story.segments.get(index);
        StringBuilder out=new StringBuilder();
        out.append("VisualBook render inspection\n");
        out.append("Segment ").append(index+1).append("/").append(story.segments.size())
           .append(" | source line ").append(seg.line).append("\n");
        out.append("Speaker: ").append(seg.speaker).append(" | style: ")
           .append(seg.artStyle).append("\n");
        out.append("Narration: ").append(seg.text).append("\n");
        out.append("Background: ").append(story.background).append("\n");
        out.append("Visible figures: ").append(seg.visibleFigures.size()).append("\n");
        for(VectorFigure.Placement place:seg.visibleFigures){
            VectorFigure figure=story.figures.get(place.figureId);
            if(figure==null)continue;
            out.append(" - ").append(figure.id).append(" artboard ")
               .append(figure.width).append("x").append(figure.height)
               .append(", stage center (").append(one(place.stageX))
               .append(",").append(one(place.stageY)).append("), width ")
               .append(one(place.widthPercent)).append("%")
               .append(", ").append(figure.groups.size()).append(" reusable parts, ")
               .append(figure.elements.size()).append(" root nodes\n");
        }
        out.append("Motion tracks: ").append(seg.motions.size()).append("\n");
        for(VectorFigure.Motion motion:seg.motions.values()){
            out.append(" - ").append(motion.key()).append(" swing ")
               .append(one(motion.swingDegrees)).append(" deg / ")
               .append(motion.periodMs).append("ms\n");
        }
        if(hit!=null){
            out.append("Tap stage (").append(one(hit.stageX)).append(",")
               .append(one(hit.stageY)).append(")");
            if(hit.matched){
                out.append(" -> figure ").append(hit.figureId)
                   .append(" local (").append(one(hit.localX))
                   .append(",").append(one(hit.localY)).append(")");
            } else out.append(" -> no visible figure bounding rectangle");
            out.append("\n");
        }
        out.append("Use this report to revise artboard paths, part pivots, ")
           .append("@use transforms or @show placement.\n");
        return out.toString();
    }
}

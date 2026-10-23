package com.visualbook.player;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;
import android.graphics.Shader;
import android.os.SystemClock;
import android.view.View;

/**
 * Deliberately procedural first content kit:
 * no image assets to download, stretch or run out of memory loading.
 * Visual story position arrives from TTS callbacks; time controls idle motion only.
 */
public final class StoryStageView extends View {
    private final Paint p = new Paint(Paint.ANTI_ALIAS_FLAG);
    private String background = "classroom", active = "n", visualStyle = "normal";
    private String name1 = "C1", name2 = "C2";
    private boolean speaking = false;
    private boolean reducedMotion = false;

    public StoryStageView(Context context) { super(context); setLayerType(View.LAYER_TYPE_SOFTWARE, null); }

    public void setStoryBackground(String id) {
        background = "neutral".equals(id) ? "neutral" : "classroom";
        invalidate();
    }
    public void show(String speaker, boolean isSpeaking) {
        active = speaker == null ? "n" : speaker;
        speaking = isSpeaking;
        invalidate();
    }
    /** Pure rendering state: narration pointers remain owned by PlaybackState. */
    public void setVisualStyle(String style) {
        String normalized = "chibi".equals(style) ? "chibi" : "normal";
        if (!normalized.equals(visualStyle)) { visualStyle = normalized; invalidate(); }
    }
    public void reduceMotion(boolean reduce) { reducedMotion = reduce; invalidate(); }
    public void setActorLabels(String c1, String c2) {
        name1 = c1 == null ? "C1" : c1;
        name2 = c2 == null ? "C2" : c2;
        invalidate();
    }

    @Override protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        float w = getWidth(), h = getHeight();
        if (w <= 0 || h <= 0) return;
        p.setShader(new LinearGradient(0, 0, 0, h,
                new int[]{Color.rgb(245, 232, 215), Color.rgb(223, 210, 194)}, null,
                Shader.TileMode.CLAMP));
        canvas.drawRect(0, 0, w, h, p); p.setShader(null);
        if ("classroom".equals(background)) classroom(canvas, w, h);
        else neutral(canvas, w, h);
        if ("chibi".equals(visualStyle)) chibiAccent(canvas, w, h);

        float size = Math.min(w * .115f, h * .18f);
        size = Math.max(16f, size);
        float foot = h * .84f;
        drawActor(canvas, w * .29f, foot, size, "c1", name1,
                Color.rgb(59, 117, 157), Color.rgb(238, 190, 141));
        drawActor(canvas, w * .72f, foot, size, "c2", name2,
                Color.rgb(154, 103, 117), Color.rgb(195, 144, 110));
        if (!"n".equals(active) && !"c1".equals(active) && !"c2".equals(active)) {
            drawActor(canvas, w * .50f, foot - size * .18f, size * .80f,
                    active, active.toUpperCase(), Color.rgb(90, 120, 105), Color.rgb(210, 170, 135));
        }
        if ("n".equals(active) && speaking) {
            p.setColor(Color.rgb(89, 102, 129));
            p.setTextSize(Math.max(12f, Math.min(w, h) * .035f));
            p.setFakeBoldText(true);
            canvas.drawText("NARRATOR", w * .06f, h * .10f, p);
            p.setFakeBoldText(false);
        }
        if (speaking && !reducedMotion) postInvalidateDelayed(80L);
    }

    private void classroom(Canvas c, float w, float h) {
        // Wall molding and tiles, generated in viewport units.
        rect(c, 0, h * .72f, w, h, Color.rgb(195, 169, 141));
        p.setColor(0x30FFFFFF);
        p.setStrokeWidth(Math.max(1f, w * .002f));
        for (int i = 1; i <= 8; i++) {
            float x = i * w / 9f;
            c.drawLine(x, h * .73f, x, h, p);
        }
        rect(c, 0, h * .70f, w, h * .73f, Color.rgb(139, 106, 91));
        rounded(c, w * .17f, h * .115f, w * .85f, h * .53f, 15f,
                Color.rgb(138, 91, 61));
        rounded(c, w * .19f, h * .13f, w * .83f, h * .51f, 9f,
                Color.rgb(47, 92, 81));
        p.setColor(0xBCE5E7CF);
        p.setTextSize(Math.max(15f, Math.min(w, h) * .050f));
        p.setFakeBoldText(true);
        c.drawText("ARAW NG KUWENTO", w * .245f, h * .25f, p);
        p.setFakeBoldText(false);
        p.setStrokeWidth(Math.max(2f, w * .005f));
        c.drawLine(w * .25f, h * .30f, w * .74f, h * .30f, p);
        window(c, w*.02f, h*.17f, w*.15f, h*.47f);
        // Small plant for visual texture; scene meaningful at person scale.
        rect(c, w*.925f,h*.64f,w*.96f,h*.73f,Color.rgb(148,99,72));
        p.setColor(Color.rgb(83,130,94));
        c.drawOval(new RectF(w*.91f,h*.48f,w*.968f,h*.66f),p);
        c.drawOval(new RectF(w*.937f,h*.50f,w*.99f,h*.65f),p);
    }

    private void window(Canvas c, float l, float t, float r, float b) {
        rounded(c,l,t,r,b,7f,Color.rgb(139,96,78));
        rect(c,l+5,t+5,r-5,b-5,Color.rgb(155,202,211));
        rect(c,(l+r)*.5f-2,t+5,(l+r)*.5f+2,b-5,Color.rgb(233,220,193));
        rect(c,l+5,(t+b)*.5f-2,r-5,(t+b)*.5f+2,Color.rgb(233,220,193));
    }

    private void neutral(Canvas c, float w, float h) {
        p.setColor(Color.rgb(240,226,204));
        c.drawCircle(w*.78f,h*.20f,Math.min(w,h)*.12f,p);
        p.setColor(0x228A99A3);
        c.drawOval(new RectF(-w*.2f,h*.55f,w*.6f,h*1.08f),p);
        c.drawOval(new RectF(w*.35f,h*.45f,w*1.2f,h*1.2f),p);
    }

    private void drawActor(Canvas c, float x, float feet, float r,
                           String id, String label, int shirt, int skin) {
        if ("chibi".equals(visualStyle)) {
            drawChibiActor(c, x, feet, r, id, label, shirt, skin);
            return;
        }
        boolean talk = speaking && active.equals(id);
        float bounce = talk && !reducedMotion ?
                (float)Math.sin(SystemClock.uptimeMillis() / 125.0) * r * .05f : 0f;
        float cy = feet - r * 2.3f + bounce;
        // Soft ground shadow.
        p.setColor(0x44383942);
        c.drawOval(new RectF(x-r*.75f, feet-r*.08f, x+r*.75f, feet+r*.13f),p);
        p.setColor(talk ? Color.rgb(254,238,161) : 0x33848E95);
        c.drawCircle(x,cy-r*.35f,r*1.28f,p);
        // Legs.
        rect(c,x-r*.54f,feet-r*.83f,x-r*.10f,feet,Color.rgb(59,66,83));
        rect(c,x+r*.10f,feet-r*.83f,x+r*.54f,feet,Color.rgb(59,66,83));
        // Torso and arms.
        rounded(c,x-r*.80f,cy+r*.65f,x+r*.80f,feet-r*.66f,
                r*.40f,shirt);
        rounded(c,x-r*1.03f,cy+r*.90f,x-r*.70f,feet-r*.95f,r*.16f,skin);
        rounded(c,x+r*.70f,cy+r*.90f,x+r*1.03f,feet-r*.95f,r*.16f,skin);
        // Head, hair and simple expressive features.
        p.setColor(skin);
        c.drawCircle(x,cy,r,p);
        p.setColor(Color.rgb(62,52,56));
        c.drawArc(new RectF(x-r*1.04f,cy-r*1.02f,x+r*1.04f,cy+r*.58f),
                186f,171f,true,p);
        // hairline cover ears without overpowering face
        p.setColor(skin);
        c.drawOval(new RectF(x-r*.84f,cy-r*.52f,x+r*.84f,cy+r*.94f),p);
        p.setColor(Color.rgb(57,55,57));
        c.drawCircle(x-r*.33f,cy+r*.03f,r*.072f,p);
        c.drawCircle(x+r*.33f,cy+r*.03f,r*.072f,p);
        p.setStyle(Paint.Style.STROKE);
        p.setStrokeWidth(Math.max(2f,r*.055f));
        p.setColor(Color.rgb(106,65,64));
        if(talk) {
            p.setStyle(Paint.Style.FILL);
            c.drawOval(new RectF(x-r*.15f,cy+r*.39f,x+r*.15f,cy+r*.72f),p);
        } else {
            c.drawArc(new RectF(x-r*.17f,cy+r*.34f,x+r*.17f,cy+r*.62f),
                    10f,160f,false,p);
        }
        p.setStyle(Paint.Style.FILL);
        // Name plate and speaker indicator.
        float pad = Math.max(4f,r*.10f);
        float txt = Math.max(11f,r*.36f);
        p.setTextSize(txt); p.setFakeBoldText(true);
        float tw = p.measureText(label);
        float top = feet+r*.17f;
        rounded(c,x-tw*.5f-pad,top,x+tw*.5f+pad,top+txt+pad*1.6f,
                pad, talk?Color.rgb(51,80,94):Color.rgb(98,90,89));
        p.setColor(Color.WHITE);
        c.drawText(label,x-tw*.5f,top+txt+pad*.27f,p);
        p.setFakeBoldText(false);
    }


    /** Real chibi shape transformation: larger head/eyes, tiny limbs and body, pastel aura.
     * No bitmap scaling or outside packs; consistent colors and actor identity.
     */
    private void drawChibiActor(Canvas c, float x, float feet, float r,
                                String id, String label, int shirt, int skin) {
        boolean talk = speaking && active.equals(id);
        float bounce = talk && !reducedMotion ?
                (float)Math.sin(SystemClock.uptimeMillis() / 125.0) * r * .065f : 0f;
        float headR = r * 1.17f;
        float centerY = feet - r * 1.73f + bounce;
        p.setColor(0x444D4D59);
        c.drawOval(new RectF(x - r*.83f,feet-r*.05f,x+r*.83f,feet+r*.16f),p);
        if (talk) {
            p.setColor(0xAAFFDC89);
            c.drawCircle(x,centerY,headR*1.29f,p);
        }
        // Short legs and pill-shaped torso, deliberately distinct from normal proportions.
        rounded(c,x-r*.45f,feet-r*.43f,x-r*.08f,feet, r*.12f,
                Color.rgb(70,72,92));
        rounded(c,x+r*.08f,feet-r*.43f,x+r*.45f,feet, r*.12f,
                Color.rgb(70,72,92));
        rounded(c,x-r*.60f,feet-r*1.15f,x+r*.60f,feet-r*.36f,
                r*.30f,shirt);
        rounded(c,x-r*.80f,feet-r*.99f,x-r*.53f,feet-r*.55f,r*.15f,skin);
        rounded(c,x+r*.53f,feet-r*.99f,x+r*.80f,feet-r*.55f,r*.15f,skin);
        // Oversized head; bangs and large expressive eyes.
        p.setColor(Color.rgb(62,50,56));
        c.drawCircle(x,centerY-r*.12f,headR*1.045f,p);
        p.setColor(skin);
        c.drawCircle(x,centerY,headR,p);
        p.setColor(Color.rgb(66,51,62));
        c.drawArc(new RectF(x-headR,centerY-headR*1.08f,
                x+headR,centerY+headR*.55f),180,171,true,p);
        p.setColor(skin);
        c.drawOval(new RectF(x-headR*.87f,centerY-headR*.46f,
                x+headR*.87f,centerY+headR*.93f),p);
        // Eye whites and colored pupils. Smaller elements remain proportionally legible.
        p.setColor(Color.WHITE);
        float eyeX=headR*.39f, eyeY=headR*.07f;
        c.drawOval(new RectF(x-eyeX-headR*.21f,centerY-eyeY-headR*.18f,
                x-eyeX+headR*.21f,centerY-eyeY+headR*.25f),p);
        c.drawOval(new RectF(x+eyeX-headR*.21f,centerY-eyeY-headR*.18f,
                x+eyeX+headR*.21f,centerY-eyeY+headR*.25f),p);
        p.setColor(Color.rgb(68,61,82));
        c.drawCircle(x-eyeX,centerY-eyeY+headR*.07f,headR*.14f,p);
        c.drawCircle(x+eyeX,centerY-eyeY+headR*.07f,headR*.14f,p);
        p.setColor(Color.WHITE);
        c.drawCircle(x-eyeX-headR*.035f,centerY-eyeY-headR*.005f,headR*.05f,p);
        c.drawCircle(x+eyeX-headR*.035f,centerY-eyeY-headR*.005f,headR*.05f,p);
        p.setColor(Color.rgb(232,144,143));
        c.drawOval(new RectF(x-headR*.80f,centerY+headR*.31f,
                x-headR*.53f,centerY+headR*.46f),p);
        c.drawOval(new RectF(x+headR*.53f,centerY+headR*.31f,
                x+headR*.80f,centerY+headR*.46f),p);
        p.setColor(Color.rgb(122,63,83));
        if(talk) {
            c.drawOval(new RectF(x-headR*.13f,centerY+headR*.38f,
                    x+headR*.13f,centerY+headR*.62f),p);
        } else {
            p.setStyle(Paint.Style.STROKE);
            p.setStrokeWidth(Math.max(1.6f,headR*.05f));
            c.drawArc(new RectF(x-headR*.15f,centerY+headR*.36f,
                    x+headR*.15f,centerY+headR*.56f),5,170,false,p);
            p.setStyle(Paint.Style.FILL);
        }
        // Chibi-only decorative accent, tied to speaker but not story time.
        if (talk) {
            p.setColor(Color.rgb(241,174,87));
            p.setTextSize(Math.max(10f,r*.45f));
            c.drawText("✦",x+headR*.83f,centerY-headR*.60f,p);
        }
        float pad=Math.max(4f,r*.10f), textSize=Math.max(11f,r*.38f);
        p.setTextSize(textSize);p.setFakeBoldText(true);
        float textWidth=p.measureText(label);
        float y=feet+r*.17f;
        rounded(c,x-textWidth*.5f-pad,y,x+textWidth*.5f+pad,
                y+textSize+pad*1.6f,pad,
                talk?Color.rgb(103,76,119):Color.rgb(109,99,112));
        p.setColor(Color.WHITE);
        c.drawText(label,x-textWidth*.5f,y+textSize+pad*.27f,p);
        p.setFakeBoldText(false);
    }

    private void chibiAccent(Canvas c,float w,float h){
        // Stable deterministic background accents: no random draws or allocations per frame.
        p.setColor(0x25FFF5B7);
        c.drawCircle(w*.16f,h*.25f,w*.14f,p);
        c.drawCircle(w*.82f,h*.43f,w*.13f,p);
        p.setColor(0x22ECA9BE);
        c.drawCircle(w*.52f,h*.17f,w*.10f,p);
    }

    private void rect(Canvas c, float l, float t, float r, float b, int color) {
        p.setShader(null);p.setColor(color);c.drawRect(l,t,r,b,p);
    }
    private void rounded(Canvas c, float l, float t, float r, float b,float rad,int color){
        p.setShader(null);p.setColor(color);
        c.drawRoundRect(new RectF(l,t,r,b),rad,rad,p);
    }
}

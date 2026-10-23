package com.visualbook.core;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.regex.Pattern;

/**
 * Sandboxed, data-only 2D figure geometry. Does not execute scripts or evaluate expressions.
 * Every object owns a logical artboard; coordinates are virtual units, never device pixels.
 */
public final class VectorFigure {
    private static final Pattern ID = Pattern.compile("[a-z][a-z0-9_-]{0,31}");
    private static final int MAX_PARTS = 64;
    public final String id;
    public final int width, height;
    public final List<Part> parts;

    public static final class Part {
        public final String kind;
        public final float[] coords;
        // An RGBA color encoded as Android-compatible ARGB int; null = no paint.
        public final Integer fill, stroke;
        public final float strokeWidth;
        public final String[] operations; // path only: M/L/Q/C/Z, absolute
        private Part(String kind, float[] coords, String[] operations,
                     Integer fill, Integer stroke, float strokeWidth) {
            this.kind = kind;
            this.coords = coords.clone();
            this.operations = operations == null ? new String[0] : operations.clone();
            this.fill = fill;
            this.stroke = stroke;
            this.strokeWidth = strokeWidth;
        }
    }

    public static final class Placement {
        public final String figureId, slot;
        // 100x100 logical stage coordinates, independent of a figure's local artboard.
        public final float stageX, stageY, widthPercent;
        public Placement(String figureId, String slot) {
            this.figureId = figureId;
            this.slot = slot;
            stageX = "left".equals(slot) ? 22f : "right".equals(slot) ? 78f : 50f;
            stageY = 30f;
            widthPercent = "center".equals(slot) ? 48f : 29f;
        }
        public Placement(String figureId, float stageX, float stageY, float widthPercent) {
            this.figureId=figureId;slot="at";
            this.stageX=stageX;this.stageY=stageY;this.widthPercent=widthPercent;
        }
    }

    public static final class GeometryException extends Exception {
        public GeometryException(String message) { super(message); }
    }

    public static final class Builder {
        private final String id;
        private final int width, height;
        private final ArrayList<Part> parts = new ArrayList<>();
        public Builder(String id, String size) throws GeometryException {
            if (id == null || !ID.matcher(id).matches())
                throw new GeometryException("Figure id must be lowercase letters, digits, _ or -");
            String[] dims = size == null ? new String[0] : size.split("x", -1);
            if (dims.length != 2) throw new GeometryException("Artboard needs e.g. 100x100");
            try {
                width = Integer.parseInt(dims[0]);
                height = Integer.parseInt(dims[1]);
            } catch (NumberFormatException ex) {
                throw new GeometryException("Artboard requires integer width and height");
            }
            if (width < 16 || height < 16 || width > 1024 || height > 1024)
                throw new GeometryException("Artboard dimensions must be 16..1024");
            this.id = id;
        }
        public Builder add(String command) throws GeometryException {
            if (parts.size() >= MAX_PARTS)
                throw new GeometryException("Figure contains more than 64 elements");
            parts.add(parsePart(command, width, height));
            return this;
        }
        public VectorFigure build() throws GeometryException {
            if (parts.isEmpty()) throw new GeometryException("Empty figure");
            return new VectorFigure(id,width,height,parts);
        }
    }

    private VectorFigure(String id,int width,int height,List<Part> parts) {
        this.id=id; this.width=width; this.height=height;
        this.parts=Collections.unmodifiableList(new ArrayList<>(parts));
    }

    public static boolean validId(String id) {
        return id != null && ID.matcher(id).matches();
    }

    /** Fit the whole artboard into viewport without distorting circles. */
    public static float fitScale(double sourceWidth,double sourceHeight,double destWidth,double destHeight) {
        if (!(sourceWidth > 0 && sourceHeight > 0 && destWidth > 0 && destHeight > 0) ||
                !Double.isFinite(sourceWidth + sourceHeight + destWidth + destHeight))
            throw new IllegalArgumentException("Positive finite canvas dimensions required");
        return (float)Math.min(destWidth/sourceWidth, destHeight/sourceHeight);
    }
    public static float mapX(float localX,float originX,float scale) { return originX+localX*scale; }
    public static float mapY(float localY,float originY,float scale) { return originY+localY*scale; }

    private static float coordinate(String src, float max) throws GeometryException {
        float f;
        try {f=Float.parseFloat(src);}
        catch (NumberFormatException ex) {throw new GeometryException("Not a coordinate: "+src);}
        if (!Float.isFinite(f) || f<0 || f>max)
            throw new GeometryException("Coordinate outside virtual artboard: "+src);
        return f;
    }
    private static float positive(String src,float max,String what) throws GeometryException {
        float f;
        try {f=Float.parseFloat(src);}
        catch(NumberFormatException ex){throw new GeometryException("Invalid "+what+": "+src);}
        if(!Float.isFinite(f) || f<=0 || f>max)
            throw new GeometryException("Invalid "+what+": "+src);
        return f;
    }
    private static Integer color(String token) throws GeometryException {
        if ("none".equals(token)) return null;
        if (!token.matches("#[0-9a-fA-F]{6}") && !token.matches("#[0-9a-fA-F]{8}"))
            throw new GeometryException("Color needs #RRGGBB or #AARRGGBB: "+token);
        try {
            long value=Long.parseLong(token.substring(1),16);
            return (int)(token.length()==7?0xFF000000L|value:value);
        } catch(NumberFormatException ex){throw new GeometryException("Invalid color");}
    }
    private static Part parsePart(String command,int w,int h) throws GeometryException {
        if(command==null || command.length()>1000)
            throw new GeometryException("Geometry line missing or more than 1000 characters");
        String[] tokens=command.trim().split("\\s+");
        if(tokens.length<2)throw new GeometryException("Incomplete geometry command");
        String kind=tokens[0].toLowerCase(Locale.ROOT);
        int paintAt=-1;
        for(int i=1;i<tokens.length;i++)if(tokens[i].startsWith("fill:") ||
                tokens[i].startsWith("stroke:") || tokens[i].startsWith("width:")){
            paintAt=i;break;
        }
        if(paintAt<0)throw new GeometryException("Use fill:#RRGGBB or stroke:#RRGGBB");
        Integer fill=null,stroke=null;
        boolean fillSeen=false,strokeSeen=false,widthSeen=false;
        float strokeWidth=1.5f;
        for(int i=paintAt;i<tokens.length;i++){
            String s=tokens[i];
            if(s.startsWith("fill:")){
                if(fillSeen)throw new GeometryException("Duplicate fill");
                fillSeen=true;fill=color(s.substring(5));
            } else if(s.startsWith("stroke:")){
                if(strokeSeen)throw new GeometryException("Duplicate stroke");
                strokeSeen=true;stroke=color(s.substring(7));
            } else if(s.startsWith("width:")){
                if(widthSeen)throw new GeometryException("Duplicate width");
                widthSeen=true;strokeWidth=positive(s.substring(6),12,"stroke width");
            } else throw new GeometryException("Unexpected style option: "+s);
        }
        if(fill==null && stroke==null)throw new GeometryException("Element needs a visible paint");
        if(widthSeen && stroke==null)throw new GeometryException("width requires a stroke");
        if(paintAt<=1)throw new GeometryException("Geometry missing coordinates");
        ArrayList<Float> coords=new ArrayList<>();
        ArrayList<String> ops=new ArrayList<>();
        if("circle".equals(kind)) {
            if(paintAt!=4)throw new GeometryException("circle x y radius fill:#...");
            coords.add(coordinate(tokens[1],w));coords.add(coordinate(tokens[2],h));
            float radius=positive(tokens[3],Math.max(w,h),"radius");
            if(coords.get(0)-radius < 0 || coords.get(0)+radius>w ||
               coords.get(1)-radius < 0 || coords.get(1)+radius>h)
                throw new GeometryException("Circle extends beyond artboard");
            coords.add(radius);
        } else if("triangle".equals(kind) || "polygon".equals(kind)) {
            int n=paintAt-1;
            if("triangle".equals(kind) && n!=3)throw new GeometryException("triangle needs 3 x,y points");
            if("polygon".equals(kind) && (n<3||n>32))
                throw new GeometryException("polygon needs 3..32 x,y points");
            for(int i=1;i<paintAt;i++){
                String[] pair=tokens[i].split(",",-1);
                if(pair.length!=2)throw new GeometryException("Point must be x,y");
                coords.add(coordinate(pair[0],w));coords.add(coordinate(pair[1],h));
            }
        } else if("path".equals(kind)) {
            int at=1,commands=0;
            boolean moved=false;
            while(at<paintAt){
                String op=tokens[at++];
                if(!("M".equals(op)||"L".equals(op)||"Q".equals(op)||
                        "C".equals(op)||"Z".equals(op)))
                    throw new GeometryException("Path supports absolute M/L/Q/C/Z only");
                if(!moved && !"M".equals(op))throw new GeometryException("Path must start with M");
                if("M".equals(op))moved=true;
                int pairs="Z".equals(op)?0:("Q".equals(op)?2:("C".equals(op)?3:1));
                if(at+pairs*2>paintAt)throw new GeometryException("Not enough path coordinates");
                ops.add(op);
                for(int i=0;i<pairs;i++){
                    coords.add(coordinate(tokens[at++],w));
                    coords.add(coordinate(tokens[at++],h));
                }
                if(++commands>64)throw new GeometryException("Too many path commands");
                if("Z".equals(op) && at<paintAt)
                    throw new GeometryException("Z must terminate this path");
            }
        } else {
            throw new GeometryException("Unknown geometry primitive: "+kind);
        }
        float[] data=new float[coords.size()];
        for(int i=0;i<data.length;i++)data[i]=coords.get(i);
        return new Part(kind,data,ops.toArray(new String[0]),fill,stroke,strokeWidth);
    }
}

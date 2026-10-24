package com.visualbook.core;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.LinkedHashMap;
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
    /** Ordered root geometry plus reusable part instances (separate from legacy parts). */
    public final List<Element> elements;
    public final Map<String, Group> groups;
    /** Stable root instance handles for controlled part motion. */
    public final Map<String,Instance> namedRoots;
    public Instance rootInstance(String name){return namedRoots.get(name);}
    public static final class Motion {
        public final String figureId,instanceId;
        public final float swingDegrees;
        public final int periodMs;
        public Motion(String figureId,String instanceId,float swingDegrees,int periodMs){
            this.figureId=figureId;this.instanceId=instanceId;
            this.swingDegrees=swingDegrees;this.periodMs=periodMs;
        }
        public String key(){return figureId+"/"+instanceId;}
        public float offsetAt(long elapsedMs){
            if(elapsedMs<=0)return 0f;
            return (float)(swingDegrees*Math.sin(2.0*Math.PI*
                  (elapsedMs%periodMs)/(double)periodMs));
        }
    }

    public static final class Part {
        public final String kind;
        private final float[] coords;
        // An ARGB color encoded as Android-compatible int; null = no paint.
        public final Integer fill, stroke;
        public final float strokeWidth;
        private final String[] operations; // path only: M/L/Q/C/Z, absolute
        private Part(String kind, float[] coords, String[] operations,
                     Integer fill, Integer stroke, float strokeWidth) {
            this.kind = kind;
            this.coords = coords.clone();
            this.operations = operations == null ? new String[0] : operations.clone();
            this.fill = fill;
            this.stroke = stroke;
            this.strokeWidth = strokeWidth;
        }
        public int coordCount() { return coords.length; }
        public float coord(int i) { return coords[i]; }
        public int operationCount() { return operations.length; }
        public String operation(int i) { return operations[i]; }
    }


    /** Every drawable node is either a literal primitive or an instance of a named part. */
    public static final class Element {
        public final Part shape;
        public final Instance instance;
        private Element(Part shape, Instance instance) {
            this.shape=shape;
            this.instance=instance;
        }
    }

    /** A reusable shape part with its own independent virtual local artboard and pivot. */
    public static final class Group {
        public final String id;
        public final int width, height;
        public final float pivotX, pivotY;
        public final List<Element> elements;
        public final int expandedCost, depth;
        private Group(String id,int width,int height,float pivotX,float pivotY,
                      List<Element> elements,int expandedCost,int depth) {
            this.id=id;this.width=width;this.height=height;
            this.pivotX=pivotX;this.pivotY=pivotY;
            this.elements=Collections.unmodifiableList(new ArrayList<>(elements));
            this.expandedCost=expandedCost;this.depth=depth;
        }
    }

    /** Immutable instance transform: local pivot goes to (atX,atY) in parent coordinates. */
    public static final class Instance {
        public final String groupId, id;
        public final float atX, atY, rotation, scale;
        public final boolean mirror;
        private Instance(String groupId,String id,float atX,float atY,float rotation,float scale,boolean mirror){
            this.groupId=groupId;this.id=id;this.atX=atX;this.atY=atY;
            this.rotation=rotation;this.scale=scale;this.mirror=mirror;
        }
        /** Same order as Canvas: T(at)*R(rotation)*S(signedScale)*T(-pivot). */
        public float[] map(float localX,float localY,Group group) {
            float dx=(localX-group.pivotX)*scale*(mirror?-1f:1f);
            float dy=(localY-group.pivotY)*scale;
            double r=Math.toRadians(rotation);
            return new float[]{
                atX+(float)(dx*Math.cos(r)-dy*Math.sin(r)),
                atY+(float)(dx*Math.sin(r)+dy*Math.cos(r))
            };
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
        private static final int MAX_NODES=64;
        private static final int MAX_GROUPS=12;
        private static final int MAX_EXPANDED=256;
        private static final int MAX_DEPTH=4;
        private final String id;
        private final int width, height;
        private final ArrayList<Part> parts = new ArrayList<>();
        private final ArrayList<Element> root = new ArrayList<>();
        private final LinkedHashMap<String,Group> groups = new LinkedHashMap<>();
        private final LinkedHashMap<String,Instance> namedRoots = new LinkedHashMap<>();
        private String pendingId;
        private int pendingWidth,pendingHeight;
        private float pendingPivotX,pendingPivotY;
        private ArrayList<Element> pendingElements;
        private int pendingCost,pendingDepth;
        private int rootCost=0,rootDepth=0,totalNodes=0;

        public Builder(String id, String size) throws GeometryException {
            if (id == null || !ID.matcher(id).matches())
                throw new GeometryException("Figure id must be lowercase letters, digits, _ or -");
            int[] dims=dimensions(size);
            width=dims[0];height=dims[1];this.id=id;
        }
        private static int[] dimensions(String size) throws GeometryException {
            String[] dims = size == null ? new String[0] : size.split("x", -1);
            if (dims.length != 2) throw new GeometryException("Artboard needs e.g. 100x100");
            int w,h;
            try {w=Integer.parseInt(dims[0]);h=Integer.parseInt(dims[1]);}
            catch (NumberFormatException ex) {
                throw new GeometryException("Artboard requires integer width and height");
            }
            if (w < 16 || h < 16 || w > 1024 || h > 1024)
                throw new GeometryException("Artboard dimensions must be 16..1024");
            return new int[]{w,h};
        }
        private static float signedFloat(String text,float bound,String field)
                throws GeometryException {
            float x;
            try{x=Float.parseFloat(text);}
            catch(NumberFormatException e){throw new GeometryException("Invalid "+field);}
            if(!Float.isFinite(x) || x<-bound || x>bound)
                throw new GeometryException("Invalid "+field+": "+text);
            return x;
        }
        private static float[] xy(String text,int w,int h,String field) throws GeometryException {
            String[] pairs=text.split(",",-1);
            if(pairs.length!=2)throw new GeometryException(field+" needs x,y");
            return new float[]{coordinate(pairs[0],w),coordinate(pairs[1],h)};
        }
        public Builder add(String command) throws GeometryException {
            if(command==null || command.length()>1000)
                throw new GeometryException("Geometry command too long");
            String t=command.trim();
            if(t.startsWith("@part ")) {startPart(t);return this;}
            if("@endpart".equals(t)){endPart();return this;}
            if(t.startsWith("@use ")) {usePart(t);return this;}
            if(t.startsWith("@") || t.startsWith("use "))
                throw new GeometryException("Unknown figure command: "+t);
            if(totalNodes>=MAX_NODES)throw new GeometryException("Figure exceeds 64 total nodes");
            int localWidth=pendingId==null?width:pendingWidth;
            int localHeight=pendingId==null?height:pendingHeight;
            Part p=parsePart(t,localWidth,localHeight);
            totalNodes++;
            if(pendingId==null) {
                if(rootCost+1>MAX_EXPANDED)throw new GeometryException("Figure expansion exceeds 256 nodes");
                parts.add(p);root.add(new Element(p,null));rootCost++;
            } else {
                if(pendingCost+1>MAX_EXPANDED)
                    throw new GeometryException("Part expansion exceeds 256 nodes");
                pendingElements.add(new Element(p,null));pendingCost++;
            }
            return this;
        }
        private void startPart(String t) throws GeometryException {
            if(pendingId!=null)throw new GeometryException("Close @endpart before another @part");
            String[] tokens=t.split("\\s+");
            if(tokens.length<3||tokens.length>4)
                throw new GeometryException("@part id WxH [pivot:x,y]");
            if(!ID.matcher(tokens[1]).matches() || groups.containsKey(tokens[1]) ||
               tokens[1].equals(id))
                throw new GeometryException("Invalid or duplicate part id");
            if(groups.size()>=MAX_GROUPS)throw new GeometryException("More than 12 parts");
            int[] dims=dimensions(tokens[2]);
            float px=dims[0]/2f,py=dims[1]/2f;
            if(tokens.length==4){
                if(!tokens[3].startsWith("pivot:"))throw new GeometryException("Expected pivot:x,y");
                float[] xy=xy(tokens[3].substring(6),dims[0],dims[1],"pivot");
                px=xy[0];py=xy[1];
            }
            pendingId=tokens[1];pendingWidth=dims[0];pendingHeight=dims[1];
            pendingPivotX=px;pendingPivotY=py;
            pendingCost=0;pendingDepth=0;pendingElements=new ArrayList<>();
        }
        private void endPart() throws GeometryException {
            if(pendingId==null)throw new GeometryException("Unmatched @endpart");
            if(pendingElements.isEmpty())throw new GeometryException("Empty part: "+pendingId);
            groups.put(pendingId,new Group(pendingId,pendingWidth,pendingHeight,
                    pendingPivotX,pendingPivotY,pendingElements,pendingCost,pendingDepth));
            pendingId=null;pendingElements=null;
        }
        private void usePart(String t) throws GeometryException {
            String[] tokens=t.split("\\s+");
            if(tokens.length<3||tokens.length>7)
                throw new GeometryException("@use part at:x,y [rotate:d] [scale:n] [mirror:true|false]");
            Group g=groups.get(tokens[1]);
            if(g==null)throw new GeometryException("Unknown or unfinished part: "+tokens[1]);
            if(!tokens[2].startsWith("at:"))throw new GeometryException("@use needs at:x,y");
            int localW=pendingId==null?width:pendingWidth;
            int localH=pendingId==null?height:pendingHeight;
            float[] at=xy(tokens[2].substring(3),localW,localH,"at");
            float rotate=0f,scale=1f;boolean mirror=false;
            boolean gotRotate=false,gotScale=false,gotMirror=false,gotId=false;
            String instanceId=null;
            for(int i=3;i<tokens.length;i++) {
                String opt=tokens[i];
                if(opt.startsWith("id:")&&!gotId) {
                    if(pendingId!=null)throw new GeometryException("Named instances must be at figure root in v0.5");
                    instanceId=opt.substring(3);
                    if(!ID.matcher(instanceId).matches()||namedRoots.containsKey(instanceId))
                        throw new GeometryException("Invalid/duplicate instance id");
                    gotId=true;
                } else if(opt.startsWith("rotate:")&&!gotRotate) {
                    rotate=signedFloat(opt.substring(7),360f,"rotation");gotRotate=true;
                } else if(opt.startsWith("scale:")&&!gotScale) {
                    scale=positive(opt.substring(6),4f,"scale");
                    if(scale<.1f)throw new GeometryException("Scale must be between 0.1 and 4");
                    gotScale=true;
                } else if(opt.startsWith("mirror:")&&!gotMirror) {
                    String v=opt.substring(7);
                    if(!v.equals("true")&&!v.equals("false"))
                        throw new GeometryException("mirror must be true or false");
                    mirror="true".equals(v);gotMirror=true;
                } else throw new GeometryException("Unexpected or duplicate instance option: "+opt);
            }
            if(totalNodes>=MAX_NODES)throw new GeometryException("Figure exceeds 64 total nodes");
            int currentDepth=g.depth+1;
            int newCost=g.expandedCost+1;
            int existing=pendingId==null?rootCost:pendingCost;
            if(currentDepth>MAX_DEPTH)throw new GeometryException("Maximum 4 nested part levels");
            if(existing+newCost>MAX_EXPANDED)
                throw new GeometryException("Expanded figure would exceed 256 nodes");
            Instance instance=new Instance(g.id,instanceId,at[0],at[1],rotate,scale,mirror);
            Element e=new Element(null,instance);
            totalNodes++;
            if(pendingId==null){
                root.add(e);rootCost+=newCost;rootDepth=Math.max(rootDepth,currentDepth);
                if(instanceId!=null)namedRoots.put(instanceId,instance);
            }
            else {pendingElements.add(e);pendingCost+=newCost;pendingDepth=Math.max(pendingDepth,currentDepth);}
        }
        public VectorFigure build() throws GeometryException {
            if(pendingId!=null)throw new GeometryException("Missing @endpart for "+pendingId);
            if(root.isEmpty())throw new GeometryException("Empty figure");
            return new VectorFigure(id,width,height,parts,root,groups,namedRoots);
        }
    }

    private VectorFigure(String id,int width,int height,List<Part> parts,List<Element> elements,
                         Map<String,Group> groups,Map<String,Instance> namedRoots) {
        this.id=id; this.width=width; this.height=height;
        this.parts=Collections.unmodifiableList(new ArrayList<>(parts));
        this.elements=Collections.unmodifiableList(new ArrayList<>(elements));
        this.groups=Collections.unmodifiableMap(new LinkedHashMap<>(groups));
        this.namedRoots=Collections.unmodifiableMap(new LinkedHashMap<>(namedRoots));
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

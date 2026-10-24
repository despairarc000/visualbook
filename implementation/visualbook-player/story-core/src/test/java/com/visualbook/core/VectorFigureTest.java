package com.visualbook.core;

/** Pure JVM regression harness for generated vector figure grammar and transforms. */
public final class VectorFigureTest {
    private static int tests;
    private static void ok(boolean pass,String name) {
        ++tests; if(!pass) throw new AssertionError(name);
    }
    private static void bad(String command) {
        try {
            new VectorFigure.Builder("creature","100x100").add(command);
            throw new AssertionError("Unsafe command accepted: "+command);
        } catch (VectorFigure.GeometryException expected) {
            ++tests;
        }
    }
    private static void invalidArtboard(String id,String size) {
        try { new VectorFigure.Builder(id,size); throw new AssertionError("Bad artboard accepted"); }
        catch (VectorFigure.GeometryException expected){++tests;}
    }
    public static void main(String[] args) throws Exception {
        VectorFigure.Builder builder=new VectorFigure.Builder("mythical_being","100x100");
        builder.add("circle 77 56 10 fill:#FFE277 stroke:#664489 width:2");
        builder.add("triangle 10,80 45,10 88,80 fill:#7657A2");
        builder.add("polygon 10,10 20,15 30,10 45,40 fill:#8064D2");
        builder.add("path M 10 40 C 25 0 65 100 90 40 stroke:#663399 width:2");
        builder.add("path M 5 5 Q 12 20 80 80 L 90 90 Z fill:#73AA99");
        VectorFigure f=builder.build();
        ok(f.width==100&&f.height==100,"artboard dimensions in virtual units");
        ok("mythical_being".equals(f.id),"stable artist-defined ID");
        ok(f.parts.size()==5,"primitive composition retained");
        ok("circle".equals(f.parts.get(0).kind),"circle support");
        ok("triangle".equals(f.parts.get(1).kind),"triangle support");
        ok("polygon".equals(f.parts.get(2).kind),"arbitrary polygons supported");
        ok("path".equals(f.parts.get(3).kind),"curved paths supported");
        ok(f.parts.get(3).operationCount()==2,"M and C stored as commands");
        ok(f.parts.get(0).fill.intValue()==0xFFFFE277,"RGB alpha normalized");
        ok(f.parts.get(0).stroke.intValue()==0xFF664489,"stroke normalized");
        ok(f.parts.get(0).strokeWidth==2f,"stroke width as virtual units");
        ok(f.parts.get(0).coord(0)==77f && f.parts.get(0).coord(1)==56f,
                "circle controls retain exact local coordinates");
        ok(f.parts.get(3).operation(1).equals("C"),"cubic segment preserved");
        ok(Math.abs(VectorFigure.fitScale(100,100,360,720)-3.6f)<.0001f,
                "portrait view fits without distortion");
        ok(Math.abs(VectorFigure.fitScale(100,100,1920,1080)-10.8f)<.0001f,
                "landscape keeps circle circular");
        ok(Math.abs(VectorFigure.mapX(77,420,10.8f)-1251.6f)<.001f,
                "local X maps consistently from (0,0)");
        ok(Math.abs(VectorFigure.mapY(56,0,10.8f)-604.8f)<.001f,
                "local Y maps consistently");
        invalidArtboard("Bad ID","100x100");
        invalidArtboard("good","0x100");
        invalidArtboard("good","100x2048");
        invalidArtboard("good","100xNaN");
        bad("circle 77 56 -1 fill:#ffaa00");
        bad("circle 99 56 4 fill:#ffaa00");
        bad("circle NaN 40 5 fill:#ffaa00");
        bad("circle 40 Infinity 5 fill:#ffaa00");
        bad("circle 40 40 5 fill:#xyzabc");
        bad("triangle 10,10 30,20 fill:#abcdef");
        bad("triangle 10,10 30,20 40,40 fill:#ffffff unexpected");
        bad("polygon 10,10 20,20 fill:#aabbcc");
        bad("path L 10 20 stroke:#abc123");
        bad("path M 10 10 C 20 20 30 30 stroke:#abc123");
        bad("path M 10 10 Z L 20 20 fill:#abc123");
        bad("path M 10 10 X 20 20 fill:#abc123");
        bad("path M 10 10 fill:none");
        bad("circle 10 10 5 stroke:#ffffff width:0");
        bad("circle 10 10 5 fill:#ffffff width:1");
        bad("circle 10 10 5 fill:#ffffff fill:#ff0000");
        bad("circle 10 10 5 fill:#ffffff stroke:#000000 stroke:#ffffff");
        bad("exec onload=evil fill:#ffffff");
        bad("<script>alert(1)</script>");
        try {
            new VectorFigure.Builder("empty","100x100").build();
            throw new AssertionError("Empty figure accepted");
        } catch(VectorFigure.GeometryException expected){tests++;}
        try {
            VectorFigure.Builder many=new VectorFigure.Builder("many","100x100");
            for(int i=0;i<65;i++)many.add("circle 10 10 2 fill:#112233");
            throw new AssertionError("Node budget was not enforced");
        } catch(VectorFigure.GeometryException expected){tests++;}
        try {
            VectorFigure.fitScale(100,100,0,20);
            throw new AssertionError("Invalid viewport accepted");
        } catch(IllegalArgumentException expected){tests++;}

        // v0.4: genuinely reusable local-coordinate parts and recursive DAG composition.
        VectorFigure.Builder rig = new VectorFigure.Builder("celestial","100x100");
        rig.add("@part eye 20x20 pivot:10,10");
        rig.add("circle 10 10 8 fill:#ffcd54");
        rig.add("@endpart");
        rig.add("@part horn 25x40 pivot:12,35");
        rig.add("path M 4 35 Q 7 10 12 2 Q 23 26 20 37 Z fill:#c9b0e7");
        rig.add("@endpart");
        rig.add("@part head 50x50 pivot:25,25");
        rig.add("circle 25 25 23 fill:#aa88dd");
        rig.add("@use eye at:16,27");
        rig.add("@use eye at:34,27 mirror:true");
        rig.add("@use horn at:12,8 rotate:-15 scale:0.65");
        rig.add("@endpart");
        rig.add("@use head at:50,45 rotate:10 scale:1.1");
        rig.add("@use head at:15,30 scale:0.5 mirror:true");
        VectorFigure nested = rig.build();
        ok(nested.parts.isEmpty(), "figures may be assembled entirely from reusable groups");
        ok(nested.groups.size()==3 && nested.elements.size()==2,
                "three named reusable parts, two root instances");
        ok(nested.groups.get("head").elements.size()==4,
                "head includes eye and horn instances plus primitive");
        ok(nested.groups.get("head").depth==1 && nested.groups.get("head").expandedCost==7,
                "head has a bounded expanded draw cost");
        ok(nested.elements.get(0).instance.groupId.equals("head"),
                "root element refers to composed head");
        ok(nested.elements.get(1).instance.mirror, "mirror transform is represented as an instance");
        VectorFigure.Group head=nested.groups.get("head");
        float[] center=nested.elements.get(0).instance.map(25,25,head);
        ok(Math.abs(center[0]-50)<.0001 && Math.abs(center[1]-45)<.0001,
                "pivot remains at requested parent at:x,y under rotation and scale");
        float[] translated=nested.elements.get(0).instance.map(35,25,head);
        ok(Math.abs(translated[0]-60.8329)<.02 && Math.abs(translated[1]-46.9101)<.02,
                "rotate and scale compose around pivot with clockwise screen coordinates");
        VectorFigure.Instance flipped=nested.elements.get(1).instance;
        float[] reflected=flipped.map(35,25,head);
        ok(Math.abs(reflected[0]-10)<.001 && Math.abs(reflected[1]-30)<.001,
                "mirror reflects local X about the part pivot, not the stage origin");
        try {
            rig.add("@part missed 20x20");
            rig.build();
            throw new AssertionError("Unclosed part accepted");
        } catch (VectorFigure.GeometryException expected){tests++;}
        String[][] invalidParts={
            {"@endpart"},
            {"@use missing at:20,30"},
            {"@part broken 20x20 pivot:Infinity,0"},
            {"@part broken 20x20 pivot:21,10"},
            {"@part broken 4x20"},
            {"@part broken 20x20 pivot:1"},
            {"@part broken 20x20 pivot:1,2,3"},
            {"@part broken 20x20 foo:1"},
            {"@use eye at:NaN,10"},
            {"@use eye at:11,101"},
            {"@use eye at:5,5 scale:0"},
            {"@use eye at:5,5 scale:5"},
            {"@use eye at:5,5 rotate:400"},
            {"@use eye at:5,5 mirror:yes"},
            {"@use eye at:5,5 scale:1 scale:2"},
            {"@use eye at:5,5 rotate:1 rotate:2"},
            {"@use eye at:5,5 mirror:true mirror:false"},
            {"@use eye at:5,5 unknown:foo"},
            {"@use eye at:5,5 scale:NaN"},
            {"@use eye at:5,5 rotate:Infinity"},
            {"@part eye 20x20"}
        };
        for(String[] invalid:invalidParts) {
            try {
                VectorFigure.Builder b=new VectorFigure.Builder("tester","100x100");
                b.add("@part eye 20x20");
                b.add("circle 10 10 4 fill:#FFFFFF");
                b.add("@endpart");
                b.add(invalid[0]);
                throw new AssertionError("Invalid part accepted: "+invalid[0]);
            } catch (VectorFigure.GeometryException expected){tests++;}
        }
        try {
            VectorFigure.Builder repeat = new VectorFigure.Builder("repeats","100x100");
            repeat.add("@part bud 20x20");
            for(int i=0;i<5;i++)repeat.add("circle 10 10 4 fill:#abc123");
            repeat.add("@endpart");
            for(int i=0;i<50;i++)repeat.add("@use bud at:50,50");
            repeat.build();
            throw new AssertionError("Expanded scene budget was ignored");
        } catch(VectorFigure.GeometryException expected){tests++;}
        try {
            VectorFigure.Builder recursive=new VectorFigure.Builder("recursive","100x100");
            recursive.add("@part loop 20x20");
            recursive.add("@use loop at:10,10");
            throw new AssertionError("Part self-reference accepted");
        } catch(VectorFigure.GeometryException expected){tests++;}
        VectorFigure.Builder animated=new VectorFigure.Builder("dragon","100x100");
        animated.add("@part wing 30x30 pivot:5,25");
        animated.add("circle 12 13 7 fill:#AABBCC");
        animated.add("@endpart");
        animated.add("@use wing at:20,40 id:leftwing rotate:15 scale:1.2 mirror:true");
        animated.add("@use wing at:80,40 id:rightwing rotate:-15");
        VectorFigure built=animated.build();
        ok(built.namedRoots.size()==2 && built.rootInstance("leftwing")!=null,
                "explicit root IDs resolve into unique handles");
        ok("leftwing".equals(built.elements.get(0).instance.id),
                "authored instance ID is preserved");
        ok(built.rootInstance("rightwing").rotation==-15f,
                "instance base rotation remains fixed for motion offsets");
        VectorFigure.Motion sweep=new VectorFigure.Motion("dragon","leftwing",20f,1000);
        ok(sweep.key().equals("dragon/leftwing"),"motion keys disambiguate figures");
        ok(Math.abs(sweep.offsetAt(250)-20f)<.001,
                "quarter-period reaches the positive amplitude");
        ok(Math.abs(sweep.offsetAt(750)+20f)<.001,
                "three-quarter period reaches negative amplitude");
        ok(Math.abs(sweep.offsetAt(1000))<.001 && sweep.offsetAt(0)==0f,
                "cycle wraps exactly and starts at the authored rest angle");
        try {
            VectorFigure.Builder dup=new VectorFigure.Builder("bad","100x100");
            dup.add("@part wing 30x30");
            dup.add("circle 10 10 5 fill:#abcdef");
            dup.add("@endpart");
            dup.add("@use wing at:20,40 id:wing1");
            dup.add("@use wing at:30,40 id:wing1");
            throw new AssertionError("Repeated instance ID accepted");
        } catch(VectorFigure.GeometryException expected){tests++;}
        try {
            VectorFigure.Builder nestedId=new VectorFigure.Builder("bad","100x100");
            nestedId.add("@part wing 30x30");
            nestedId.add("circle 10 10 5 fill:#abcdef");
            nestedId.add("@endpart");
            nestedId.add("@part head 40x40");
            nestedId.add("@use wing at:20,20 id:nested");
            throw new AssertionError("Nested instance identifier accepted");
        } catch(VectorFigure.GeometryException expected){tests++;}
        System.out.println("VisualBook vector geometry tests passed: "+tests);
    }
}

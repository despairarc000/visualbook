package com.visualbook.player;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.SharedPreferences;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.database.Cursor;
import android.provider.OpenableColumns;
import android.content.ContentResolver;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.speech.tts.TextToSpeech;
import android.speech.tts.UtteranceProgressListener;
import android.speech.tts.Voice;
import android.view.View;
import android.view.MotionEvent;
import android.view.WindowInsets;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.Spinner;
import android.widget.TextView;

import com.visualbook.core.PlaybackState;
import com.visualbook.core.StoryCore;
import com.visualbook.core.VectorFigure;
import com.visualbook.core.RenderInspector;
import com.visualbook.core.SeriesDirector;
import java.util.Collections;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Set;

/**
 * First executable end-to-end story player.
 * No downloaded assets, INTERNET permission, audio model or timer-driven narrative events.
 */
public final class MainActivity extends Activity {
    private static final String DEMO =
            "@bg classroom\n" +
            "@figure spirit 100x100\n" +
            "@part eye 20x20 pivot:10,10\n" +
            "circle 10 10 8 fill:#F6D16B stroke:#714A95 width:2\n" +
            "circle 11 10 3 fill:#503575\n" +
            "@endpart\n" +
            "@part horn 25x40 pivot:12,35\n" +
            "path M 4 35 Q 7 10 12 2 Q 23 26 20 37 Z fill:#E8CCF5 stroke:#7D539C width:2\n" +
            "@endpart\n" +
            "@part wing 45x55 pivot:8,45\n" +
            "path M 8 45 Q 0 20 8 5 Q 25 11 42 8 L 35 21 L 43 29 Q 20 29 8 45 Z fill:#8D77CF stroke:#56438F width:2\n" +
            "@endpart\n" +
            "@part head 50x50 pivot:25,25\n" +
            "circle 25 25 23 fill:#B7A3E4 stroke:#7354A9 width:2\n" +
            "@use eye at:16,28 scale:0.75\n" +
            "@use eye at:34,28 scale:0.75\n" +
            "@use horn at:10,8 rotate:-15 scale:0.65\n" +
            "@use horn at:40,8 rotate:15 scale:0.65 mirror:true\n" +
            "@endpart\n" +
            "path M 38 74 Q 18 92 12 61 stroke:#654993 width:4\n" +
            "@use wing at:26,44 id:leftwing rotate:-24 scale:1.05 mirror:true\n" +
            "@use wing at:74,44 id:rightwing rotate:24 scale:1.05\n" +
            "circle 50 65 21 fill:#775AB1 stroke:#4C377C width:2\n" +
            "@use head at:50,35 scale:1\n" +
            "@endfigure\n" +
            "@show spirit at:50,35 width:45\n" +
            "@animate spirit leftwing swing:24 period:850\n" +
            "@animate spirit rightwing swing:24 period:850\n" +
            "@n\nIsang araw, nagkita sina Juan at Pedro sa paaralan. Isang mahiwagang nilalang ang lumitaw.\n\n" +
            "@still spirit leftwing\n" +
            "@still spirit rightwing\n" +
            "@hide spirit\n" +
            "@c1m\nPedro, may assignment ka ba?\n\n" +
            "@chibi\n" +
            "@c2m\nNAKALIMUTAN KO!!!\n\n" +
            "@c1\nANO?!\n\n" +
            "@end\n" +
            "@c2\nJoke lang! Nasa bag ko.\n";
    private static final int PICK_TEXT = 33;
    private static final String EPISODE_ASSET="juan_pedro_e01.story.md";
    private static final String EPISODE_TITLE="Juan at Pedro • S1E1: Ang Kampanang Nawala";
    private static final String PROGRESS="visualbook_series_v07";
    private String bundledSource;
    private boolean episodeMode=false;
    private SeriesDirector.DirectedStory directed;
    private TextView episodeStatus;
    private static final int MAX_TEXT_BYTES = 200_000;
    private static final String[] IDS = {"n", "c1", "c2"};
    private final Handler ui = new Handler(Looper.getMainLooper());
    private HandlerThread audioThread;
    private Handler audio;
    private volatile TextToSpeech tts;
    private volatile boolean ttsReady = false;
    private volatile boolean destroyed = false;
    private volatile String activeRequest = null; // stale background jobs never speak
    private Voice initialVoice;
    private final List<VoiceOption> voiceOptions = new ArrayList<>();
    private final String[] assignedVoices = {"", "", ""};
    private final Spinner[] voiceSelectors = new Spinner[3];

    private StoryCore.Story story;
    private PlaybackState playback;
    private TextView status, caption;
    private StoryStageView stage;
    private EditText editor;
    private CheckBox plainText, subtitles;
    private LinearLayout editorPane, voicePane, previewPane;
    private CheckBox previewGridToggle;
    private Button previewToggleButton;
    private boolean editorVisible = false, voicesVisible = false;
    private boolean previewMode = false, gridBeforePreview = false;
    private int previewIndex = 0;
    private RenderInspector.Hit previewHit = null;

    private static final class VoiceOption {
        final String label, id;
        final Voice voice;
        VoiceOption(String label, String id, Voice voice) {
            this.label = label;
            this.id = id;
            this.voice = voice;
        }
        @Override public String toString() { return label; }
    }

    @Override public void onCreate(Bundle saved) {
        super.onCreate(saved);
        audioThread = new HandlerThread("VisualBook-Audio");
        audioThread.start();
        audio = new Handler(audioThread.getLooper());
        buildUI();
        loadBundledEpisode();
        setStatus("Initializing installed text-to-speech engine…");
        tts = new TextToSpeech(getApplicationContext(), result -> {
            if (destroyed) return;
            if (result != TextToSpeech.SUCCESS) {
                ui.post(() -> setStatus("TTS unavailable (" + result + "). Read text or install an engine."));
                return;
            }
            audio.post(() -> {
                if (destroyed || tts == null) return;
                tts.setOnUtteranceProgressListener(listener);
                initialVoice = tts.getVoice();
                final String engine = tts.getDefaultEngine();
                final List<VoiceOption> candidates = enumerateVoices();
                ttsReady = true;
                ui.post(() -> {
                    if (destroyed) return;
                    configureVoiceSelectors(candidates);
                    setStatus("Ready • " + (story == null ? "Load a story" : story.segments.size() + " speech segments") +
                            " • engine preference: " + engine);
                });
            });
        });
    }

    private int dp(float value) { return Math.round(value * getResources().getDisplayMetrics().density); }

    private TextView label(String text, int size) {
        TextView result = new TextView(this);
        result.setText(text);
        result.setTextColor(0xFF27394C);
        result.setTextSize(size);
        return result;
    }

    private Button button(String text, Runnable action) {
        Button b = new Button(this);
        b.setText(text);
        b.setAllCaps(false);
        b.setMinHeight(dp(42));
        b.setTextSize(12);
        b.setOnClickListener(view -> action.run());
        return b;
    }

    private void buildUI() {
        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setBackgroundColor(0xFFF4F0E8);
        int padding = dp(10);
        root.setPadding(padding, padding, padding, padding);
        if (Build.VERSION.SDK_INT >= 35) {
            root.setOnApplyWindowInsetsListener((v, insets) -> {
                android.graphics.Insets s = insets.getInsets(
                        WindowInsets.Type.systemBars() | WindowInsets.Type.displayCutout());
                v.setPadding(padding+s.left, padding+s.top, padding+s.right,padding+s.bottom);
                return insets;
            });
        }
        TextView header = label("VISUALBOOK  •  SERIES PLAYER",17);
        header.setTypeface(null,android.graphics.Typeface.BOLD);
        header.setPadding(dp(3),dp(4),0,dp(8));
        root.addView(header);

        LinearLayout toggles = new LinearLayout(this);
        toggles.setOrientation(LinearLayout.HORIZONTAL);
        toggles.addView(button("Edit", this::toggleEditor),
                new LinearLayout.LayoutParams(0,dp(45),1));
        toggles.addView(button("Import",this::importText),
                new LinearLayout.LayoutParams(0,dp(45),1));
        toggles.addView(button("Voices",this::toggleVoices),
                new LinearLayout.LayoutParams(0,dp(45),1));
        root.addView(toggles);
        LinearLayout serialControls=new LinearLayout(this);
        serialControls.setOrientation(LinearLayout.HORIZONTAL);
        serialControls.addView(button("Series",this::openSeries),
                new LinearLayout.LayoutParams(0,dp(43),1));
        serialControls.addView(button("Chapters",this::openChapters),
                new LinearLayout.LayoutParams(0,dp(43),1));
        previewToggleButton=button("Preview",this::togglePreview);
        serialControls.addView(previewToggleButton,
                new LinearLayout.LayoutParams(0,dp(43),1));
        root.addView(serialControls);
        episodeStatus=label("S1E1 • First episode",12);
        episodeStatus.setPadding(dp(4),dp(2),dp(4),dp(4));
        root.addView(episodeStatus);

        previewPane=new LinearLayout(this);
        previewPane.setOrientation(LinearLayout.HORIZONTAL);
        previewPane.setVisibility(View.GONE);
        previewPane.addView(button("◀",()->previewStep(-1)),
                new LinearLayout.LayoutParams(0,dp(46),1));
        previewPane.addView(button("▶",()->previewStep(1)),
                new LinearLayout.LayoutParams(0,dp(46),1));
        previewPane.addView(button("Copy",this::copyInspection),
                new LinearLayout.LayoutParams(0,dp(46),1));
        previewPane.addView(button("Edit shape",this::editSelectedShape),
                new LinearLayout.LayoutParams(0,dp(46),1.5f));
        root.addView(previewPane);

        editorPane = new LinearLayout(this);
        editorPane.setOrientation(LinearLayout.VERTICAL);
        editorPane.setVisibility(View.GONE);
        ScrollView sourceScroll = new ScrollView(this);
        editor = new EditText(this);
        editor.setTextSize(14);
        editor.setMinLines(4);
        editor.setGravity(android.view.Gravity.TOP);
        editor.setSingleLine(false);
        editor.setBackgroundColor(0xFFFFFFFF);
        editor.setPadding(dp(9),dp(9),dp(9),dp(9));
        sourceScroll.addView(editor);
        editorPane.addView(sourceScroll, new LinearLayout.LayoutParams(-1,dp(150)));
        LinearLayout editRow = new LinearLayout(this);
        plainText = new CheckBox(this);
        plainText.setText("Plain TXT");
        editRow.addView(plainText,new LinearLayout.LayoutParams(0,dp(44),1));
        editRow.addView(button("Apply script",this::compileSource),
                new LinearLayout.LayoutParams(0,dp(44),1));
        editRow.addView(button("Demo", () -> { editor.setText(DEMO);plainText.setChecked(false);compileSource(); }),
                new LinearLayout.LayoutParams(0,dp(44),1));
        editorPane.addView(editRow);
        root.addView(editorPane);

        voicePane = new LinearLayout(this);
        voicePane.setOrientation(LinearLayout.VERTICAL);
        voicePane.setVisibility(View.GONE);
        String[] labels={"Narrator • @n","Character 1 • @c1","Character 2 • @c2"};
        for(int i=0;i<3;i++) {
            LinearLayout row=new LinearLayout(this);
            row.setGravity(android.view.Gravity.CENTER_VERTICAL);
            row.addView(label(labels[i],12),new LinearLayout.LayoutParams(dp(122),dp(45)));
            Spinner spinner=new Spinner(this);
            voiceSelectors[i]=spinner;
            row.addView(spinner,new LinearLayout.LayoutParams(0,dp(45),1));
            voicePane.addView(row);
        }
        subtitles=new CheckBox(this);
        subtitles.setText("Show subtitles");
        subtitles.setChecked(true);
        subtitles.setOnCheckedChangeListener((v,checked)->caption.setVisibility(checked?View.VISIBLE:View.GONE));
        voicePane.addView(subtitles);
        CheckBox gridToggle = new CheckBox(this);
        gridToggle.setText("Show 0..100 coordinate grid");
        gridToggle.setChecked(false);
        gridToggle.setOnCheckedChangeListener((view,checked)-> {
            if(stage!=null)stage.setCoordinateGridVisible(checked);
        });
        voicePane.addView(gridToggle);
        previewGridToggle=gridToggle;
        root.addView(voicePane);

        stage = new StoryStageView(this);
        stage.setOnTouchListener((v,e)-> {
            if(!previewMode)return false;
            if(e.getAction()==MotionEvent.ACTION_UP) {
                v.performClick();
                inspectTap(e.getX(),e.getY());
            }
            return true;
        });
        stage.setMinimumHeight(dp(190));
        root.addView(stage,new LinearLayout.LayoutParams(-1,0,1));
        caption = label("Tap Play to begin.",17);
        caption.setTextColor(0xFF182A39);
        caption.setGravity(android.view.Gravity.CENTER);
        caption.setMinHeight(dp(72));
        caption.setPadding(dp(10),dp(9),dp(10),dp(9));
        caption.setBackgroundColor(0xFFEAE5DA);
        root.addView(caption,new LinearLayout.LayoutParams(-1,dp(82)));

        LinearLayout controls = new LinearLayout(this);
        controls.addView(button("▶ Play",this::play),
                new LinearLayout.LayoutParams(0,dp(51),1));
        controls.addView(button("Ⅱ Pause",this::pause),
                new LinearLayout.LayoutParams(0,dp(51),1));
        controls.addView(button("■ Stop",this::stop),
                new LinearLayout.LayoutParams(0,dp(51),1));
        controls.addView(button("Next ›",this::skip),
                new LinearLayout.LayoutParams(0,dp(51),1));
        root.addView(controls);
        status=label("Initializing…",12);
        status.setPadding(dp(4),dp(8),dp(4),dp(9));
        status.setMaxLines(3);
        root.addView(status);
        setContentView(root);
    }


    private void importText() {
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("*/*");
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        try {startActivityForResult(intent,PICK_TEXT);}
        catch (RuntimeException problem) {setStatus("Document picker unavailable: " + problem.getMessage());}
    }

    @Override protected void onActivityResult(int request,int result,Intent data) {
        super.onActivityResult(request,result,data);
        if(request != PICK_TEXT || result != RESULT_OK || data==null || data.getData()==null)return;
        final Uri uri=data.getData();
        // OpenDocument grants read URI access without storage permissions.
        setStatus("Reading selected story file…");
        new Thread(() -> {
            try {
                String filename = "story.txt";
                try (Cursor cursor = getContentResolver().query(uri,
                        new String[]{OpenableColumns.DISPLAY_NAME},null,null,null)) {
                    if(cursor!=null && cursor.moveToFirst())
                        filename=cursor.getString(0);
                } catch (RuntimeException ignored) {
                    // Display name isn't needed to safely read the file.
                }
                ByteArrayOutputStream out = new ByteArrayOutputStream();
                try (InputStream stream=getContentResolver().openInputStream(uri)) {
                    if(stream==null)throw new IllegalArgumentException("No readable data");
                    byte[] buffer=new byte[4096];
                    int n;
                    while((n=stream.read(buffer))!=-1) {
                        if(out.size()+n>MAX_TEXT_BYTES)
                            throw new IllegalArgumentException("TXT exceeds 200 KB limit");
                        out.write(buffer,0,n);
                    }
                }
                String text = new String(out.toByteArray(),StandardCharsets.UTF_8);
                final String finalFilename = filename == null ? "story.txt" : filename;
                ui.post(() -> {
                    if(destroyed)return;
                    editor.setText(text);
                    plainText.setChecked(!finalFilename.toLowerCase(Locale.ROOT)
                            .endsWith(".story.md"));
                    compileSource();
                    if(story != null) setStatus("Imported " + finalFilename +
                            " • " + story.segments.size() + " narration segments");
                });
            } catch(Exception problem) {
                ui.post(() -> setStatus("Cannot import story: " + problem.getMessage()));
            }
        },"VisualBook-Text-Import").start();
    }


    /**
     * Silent deterministic preview. Does not call PlaybackState.begin/skip/
     * audibleDone; Play continues the original audio cursor.
     */
    private void togglePreview() {
        if(previewMode) { exitPreview(); return; }
        if(story==null || story.segments.isEmpty()) {
            setStatus("Import or compile a valid story before preview.");
            return;
        }
        pause(); // guards against stale asynchronous TTS callbacks
        previewMode=true;
        previewIndex=0;
        previewHit=null;
        gridBeforePreview=previewGridToggle.isChecked();
        previewGridToggle.setChecked(true);
        stage.setCoordinateGridVisible(true);
        previewToggleButton.setText("Close preview");
        previewPane.setVisibility(View.VISIBLE);
        editorVisible=false;
        voicesVisible=false;
        editorPane.setVisibility(View.GONE);
        voicePane.setVisibility(View.GONE);
        applyPreview();
    }

    private void exitPreview() {
        if(!previewMode)return;
        previewMode=false;
        previewHit=null;
        previewPane.setVisibility(View.GONE);
        previewToggleButton.setText("Preview");
        previewGridToggle.setChecked(gridBeforePreview);
        stage.setCoordinateGridVisible(gridBeforePreview);
        stage.setInspectionPoint(null);
        stage.setMotions(Collections.emptyMap());
        stage.show("n",false);
        presentRestingFrame();
    }

    private void previewStep(int delta) {
        if(!previewMode||story==null)return;
        previewIndex=Math.max(0,Math.min(story.segments.size()-1,previewIndex+delta));
        previewHit=null;
        applyPreview();
    }

    private void applyPreview() {
        if(!previewMode||story==null)return;
        StoryCore.Segment s=story.segments.get(previewIndex);
        stage.setVisualStyle(s.artStyle);
        stage.setStoryBackground(directed.segmentScenes.get(previewIndex).scene);
        stage.setVectors(story.figures,s.visibleFigures);
        // Preserve definitions of motion in the report. Static preview never
        // plays an animation or pretends that speech is audible.
        stage.setMotions(s.motions);
        stage.show(s.speaker,false);
        stage.setInspectionPoint(previewHit);
        caption.setText("Preview "+(previewIndex+1)+"/"+story.segments.size()+
                " • "+stage.speakerName(s.speaker)+": "+s.text);
        setStatus("Silent render preview • tap the stage to inspect local "+
                "coordinates; Copy produces an AI-friendly report.");
    }

    private void inspectTap(float x,float y) {
        if(!previewMode||story==null)return;
        StoryCore.Segment s=story.segments.get(previewIndex);
        previewHit=RenderInspector.inspect(story.figures,s.visibleFigures,
                stage.getWidth(),stage.getHeight(),x,y);
        stage.setInspectionPoint(previewHit);
        String point=String.format(Locale.ROOT,"(%.1f, %.1f)",
                previewHit.stageX,previewHit.stageY);
        if(previewHit.matched) {
            String local=String.format(Locale.ROOT,"(%.1f, %.1f)",
                    previewHit.localX,previewHit.localY);
            setStatus("Stage "+point+" -> "+previewHit.figureId+
                    " local "+local+". Copy the report or edit this shape.");
        } else setStatus("Stage "+point+" -> no figure bounding box. "+
                "Try tapping a visible custom figure.");
    }

    private void copyInspection() {
        if(!previewMode||story==null)return;
        String report=RenderInspector.report(story,previewIndex,previewHit);
        ClipboardManager manager=(ClipboardManager)getSystemService(Context.CLIPBOARD_SERVICE);
        if(manager==null){setStatus("Clipboard service unavailable.");return;}
        manager.setPrimaryClip(ClipData.newPlainText("VisualBook scene inspection",report));
        setStatus("Copied scene "+(previewIndex+1)+" report to clipboard. "+
                "Paste it into ChatGPT for precise geometry corrections.");
    }

    private void editSelectedShape() {
        if(!previewMode||story==null)return;
        String id=previewHit!=null && previewHit.matched?previewHit.figureId:null;
        if(id==null) {
            List<VectorFigure.Placement> visible=story.segments.get(previewIndex).visibleFigures;
            if(visible.isEmpty()) {setStatus("No visible custom shape to edit in this frame.");return;}
            id=visible.get(0).figureId;
        }
        String source=editor.getText().toString();
        int start=source.indexOf("@figure "+id+" ");
        if(start<0) {setStatus("Could not find the figure declaration.");return;}
        editorVisible=true;
        voicesVisible=false;
        editorPane.setVisibility(View.VISIBLE);
        voicePane.setVisibility(View.GONE);
        int end=source.indexOf('\n',start);
        editor.setSelection(start,end<0?source.length():end);
        editor.requestFocus();
        setStatus("Editing @figure "+id+". Change geometry, then Apply script "+
                "and reopen Preview. Current frame "+(previewIndex+1)+
                " remains a read-only snapshot.");
    }


    private void loadBundledEpisode(){
        try(InputStream stream=getAssets().open(EPISODE_ASSET)){
            ByteArrayOutputStream buf=new ByteArrayOutputStream();
            byte[] chunk=new byte[4096];int n;
            while((n=stream.read(chunk))!=-1) {
                if(buf.size()+n>MAX_TEXT_BYTES)throw new IllegalArgumentException("Bundled story too large");
                buf.write(chunk,0,n);
            }
            bundledSource=new String(buf.toByteArray(),StandardCharsets.UTF_8);
            editor.setText(bundledSource);
            plainText.setChecked(false);
            compileSource();
            if(story==null)throw new IllegalStateException("Bundled story compile failed");
        } catch(Exception e){
            bundledSource=null;episodeMode=false;
            editor.setText(DEMO);
            plainText.setChecked(false);
            compileSource();
            setStatus("Could not open bundled episode: "+e.getMessage());
        }
    }
    private SharedPreferences episodePrefs(){
        return getSharedPreferences(PROGRESS,Context.MODE_PRIVATE);
    }
    private int restoreProgress(){
        if(directed==null)return 0;
        SharedPreferences store=episodePrefs();
        return SeriesDirector.restoreIndex(store.getString("source",null),
                directed.sourceId,store.getInt("index",0),story.segments.size());
    }
    private void saveProgress(){
        if(!episodeMode||directed==null||playback==null)return;
        episodePrefs().edit().putString("source",directed.sourceId)
                .putInt("index",playback.index()).apply();
    }
    private void presentRestingFrame(){
        if(story==null||directed==null||playback==null)return;
        int i=playback.index();
        if(i>=playback.total()){
            stage.show("n",false);
            episodeStatus.setText("Episode completed • Play to restart");
            return;
        }
        StoryCore.Segment seg=story.segments.get(i);
        stage.setStoryBackground(directed.segmentScenes.get(i).scene);
        stage.setVisualStyle(seg.artStyle);
        stage.setVectors(story.figures,seg.visibleFigures);
        stage.setMotions(seg.motions);
        stage.show(seg.speaker,false);
        episodeStatus.setText(SeriesDirector.chapterAt(directed,i).title+
                "  •  "+(i+1)+"/"+playback.total());
    }
    private void openSeries(){
        final String[] entries={
            "Episode 1 • Ang Kampanang Nawala  (Playable)",
            "Episode 2 • Ang mga Bakas sa Ilalim ng Tulay  (Coming soon)",
            "Episode 3 • Ang Tindero ng Tatlong Selyo  (Coming soon)",
            "Episode 4 • Ang Bukal sa Ilalim ng Balete  (Coming soon)",
            "Episode 5 • Pitong Tunog, Isang Kasinungalingan  (Coming soon)",
            "Episode 6 • Ang Bahay na Walang Pangalan  (Coming soon)",
            "Episode 7 • Ang Tao sa Likod ng Pirma  (Coming soon)",
            "Episode 8 • Ang Kuwentong Hindi Nila Naagaw  (Coming soon)"
        };
        new AlertDialog.Builder(this).setTitle("Juan at Pedro • Season 1")
                .setItems(entries,(dialog,which)->{
                    if(which==0){
                        if(bundledSource==null){setStatus("Episode 1 asset missing");return;}
                        if(!episodeMode){
                            editor.setText(bundledSource);plainText.setChecked(false);
                            compileSource();
                        }
                        setStatus(EPISODE_TITLE+" • Play resumes saved position.");
                    } else {
                        setStatus("Episode "+(which+1)+" not yet produced. Episode 1 is available.");
                    }
                }).setNegativeButton("Close",null).show();
    }
    private void openChapters(){
        if(directed==null||playback==null)return;
        List<SeriesDirector.Chapter> list=directed.chapters;
        String[] labels=new String[list.size()];
        for(int i=0;i<list.size();i++)
            labels[i]=(i+1)+". "+list.get(i).title;
        new AlertDialog.Builder(this).setTitle("Jump to chapter (manual seek)")
                .setItems(labels,(dialog,which)->{
                    pause();
                    playback.seek(list.get(which).segmentIndex);
                    saveProgress();
                    presentRestingFrame();
                    caption.setText(list.get(which).title+" • Tap Play.");
                    setStatus("Manually moved to chapter "+(which+1)+
                            "; audio starts on Play, no fake completion.");
                }).setNegativeButton("Cancel",null).show();
    }

    private void toggleEditor() {
        editorVisible=!editorVisible;
        editorPane.setVisibility(editorVisible?View.VISIBLE:View.GONE);
        if(editorVisible) { voicesVisible=false; voicePane.setVisibility(View.GONE); }
    }

    private void toggleVoices() {
        voicesVisible=!voicesVisible;
        voicePane.setVisibility(voicesVisible?View.VISIBLE:View.GONE);
        if(voicesVisible) { editorVisible=false;editorPane.setVisibility(View.GONE); }
    }

    private void setStatus(String msg) {
        if(status!=null)status.setText(msg);
    }

    private void compileSource() {
        if(previewMode)exitPreview();
        pause(); // invalidate old utterance generation before replacing the compiled story
        try {
            boolean script=!plainText.isChecked();
            String raw=editor.getText().toString();
            episodeMode=bundledSource!=null && bundledSource.equals(raw);
            directed=SeriesDirector.compile(raw,script);
            StoryCore.Story result=directed.story;
            story=result;
            playback=new PlaybackState(result.segments);
            stage.setStoryBackground(directed.segmentScenes.get(0).scene);
            stage.setVisualStyle("normal");
            stage.setVectors(result.figures, Collections.emptyList());
            stage.setMotions(Collections.emptyMap());
            stage.setCastNames(directed.names);
            if(DEMO.equals(raw))stage.setActorLabels("Juan","Pedro");
            int position=episodeMode?restoreProgress():0;
            if(position>0 && position<playback.total())playback.seek(position);
            presentRestingFrame();
            caption.setText((episodeMode?EPISODE_TITLE:"Story ready")+
                    " • "+result.segments.size()+" speech segments.");
            setStatus("Compiled • " + result.cast.size() + " speakers • " +
                    result.segments.size() + " narration segments."+
                    (position>0?" Resumed at "+(position+1)+".":""));
            editorVisible=false;
            editorPane.setVisibility(View.GONE);
        } catch (StoryCore.ScriptError problem) {
            story=null; directed=null; playback=null;
            stage.show("n",false);
            caption.setText("Edit the story to correct the script.");
            setStatus(problem.getMessage());
        }
    }

    private List<VoiceOption> enumerateVoices() {
        ArrayList<VoiceOption> result=new ArrayList<>();
        result.add(new VoiceOption("Current system default", "", initialVoice));
        Set<Voice> raw=tts.getVoices();
        if(raw==null)return result;
        List<Voice> filipino=new ArrayList<>();
        for(Voice candidate: raw) {
            String language=candidate.getLocale().getLanguage();
            if(("fil".equals(language)||"tl".equals(language)) &&
                    !candidate.isNetworkConnectionRequired()) filipino.add(candidate);
        }
        filipino.sort(Comparator.comparing(Voice::getName));
        for(Voice v:filipino)
            result.add(new VoiceOption(v.getName(), v.getName(), v));
        return result;
    }

    private void configureVoiceSelectors(List<VoiceOption> options) {
        voiceOptions.clear();
        voiceOptions.addAll(options);
        ArrayAdapter<VoiceOption> adapter=new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item,voiceOptions);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        // Prefer three distinct locally advertised voices; actual synthesis might still fail.
        String[] suggestions={"fil-ph-x-fic-local","fil-ph-x-fid-local","fil-ph-x-fie-local"};
        for(int i=0;i<3;i++){
            final int slot=i;
            Spinner spinner=voiceSelectors[i];
            spinner.setAdapter(adapter);
            int chosen=0;
            for(int j=1;j<voiceOptions.size();j++) {
                if(voiceOptions.get(j).id.equals(suggestions[i])) {chosen=j;break;}
            }
            if(chosen==0 && voiceOptions.size()>1)
                chosen=1+(i%(voiceOptions.size()-1));
            spinner.setSelection(chosen);
            assignedVoices[i]=voiceOptions.get(chosen).id;
            spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener(){
                @Override public void onItemSelected(AdapterView<?> parent,View view,int position,long itemId) {
                    assignedVoices[slot]=voiceOptions.get(position).id;
                    if(playback!=null && playback.running())
                        setStatus("New voice will apply to the next utterance. Pause/Play to restart current.");
                }
                @Override public void onNothingSelected(AdapterView<?> parent) {}
            });
        }
    }

    private Voice voiceFor(String speaker) {
        int slot="n".equals(speaker)?0:("c1".equals(speaker)?1:("c2".equals(speaker)?2:-1));
        if(slot<0)return initialVoice;
        String id=assignedVoices[slot];
        for(VoiceOption o:voiceOptions) if(o.id.equals(id))return o.voice;
        return initialVoice;
    }

    private void play() {
        if(previewMode)exitPreview();
        if(playback==null) {setStatus("Load a valid script first.");return;}
        if(!ttsReady) {setStatus("TTS not ready. Text is available; check installed speech engine.");return;}
        if(playback.running())return;
        if(playback.isFinished()) {playback.reset();saveProgress();}
        dispatchCurrent();
    }

    private void dispatchCurrent() {
        if(playback==null) return;
        StoryCore.Segment segment=playback.current();
        if(segment==null) {
            activeRequest=null;
            stage.show("n",false);
            if(story!=null)stage.setVectors(story.figures,Collections.emptyList());
            saveProgress();
            caption.setText("The end • Tapos na ang kuwento.");
            setStatus("Finished all narrated speech segments.");
            return;
        }
        final String id=playback.begin();
        activeRequest=id;
        final Voice voice=voiceFor(segment.speaker);
        stage.show(segment.speaker,false);
        caption.setText("…");
        setStatus("Preparing " + (playback.index()+1) + "/" + playback.total() +
                " • " + segment.speaker + " • voice: " +
                (voice==null?"system":voice.getName()));
        audio.post(() -> {
            if(destroyed || !id.equals(activeRequest) || tts==null)return;
            if(voice!=null) {
                int changed=tts.setVoice(voice);
                if(changed!=TextToSpeech.SUCCESS) {
                    ui.post(() -> failIfCurrent(id,"Voice selection failed. Choose another voice."));
                    return;
                }
            }
            if(destroyed || !id.equals(activeRequest))return;
            int rc=tts.speak(segment.text,TextToSpeech.QUEUE_FLUSH,null,id);
            if(rc!=TextToSpeech.SUCCESS)
                ui.post(() -> failIfCurrent(id,"TTS refused to enqueue this sentence (" + rc + ")."));
        });
    }

    private final UtteranceProgressListener listener=new UtteranceProgressListener(){
        @Override public void onStart(String id) {
            ui.post(() -> {
                if(destroyed||playback==null||!playback.audibleStart(id))return;
                StoryCore.Segment s=playback.current();
                if(s==null)return;
                stage.setVisualStyle(s.artStyle);
                stage.setStoryBackground(directed.segmentScenes.get(playback.index()).scene);
                stage.setVectors(story.figures,s.visibleFigures);
                stage.setMotions(s.motions);
                stage.show(s.speaker,true);
                caption.setText(stage.speakerName(s.speaker) + ": " + s.text);
                episodeStatus.setText(SeriesDirector.chapterAt(directed,playback.index()).title+
                        "  •  "+(playback.index()+1)+"/"+playback.total());
                setStatus("Speaking " + (playback.index()+1) + "/" + playback.total());
            });
        }
        @Override public void onDone(String id) {
            ui.post(() -> {
                if(destroyed||playback==null||!playback.audibleDone(id))return;
                saveProgress();
                stage.show("n",false);
                activeRequest=null;
                dispatchCurrent();
            });
        }
        @Override public void onError(String id) { onError(id,TextToSpeech.ERROR); }
        @Override public void onError(String id,int code) {
            ui.post(() -> failIfCurrent(id,"TTS playback failed (code "+ code +
                    "). Try another voice, retry Play, or explicitly skip."));
        }
        @Override public void onStop(String id,boolean interrupted) {
            // Deliberately never signal successful completion on stopped speech.
        }
    };

    private void failIfCurrent(String id,String msg) {
        if(destroyed||playback==null||!playback.failed(id))return;
        activeRequest=null;
        stage.show("n",false);
        StoryCore.Segment current=playback.current();
        caption.setText(current==null?"No dialogue":current.text);
        setStatus(msg);
    }

    private void pause() {
        activeRequest=null;
        if(playback!=null)playback.pause();
        saveProgress();
        if(stage!=null)stage.show("n",false);
        final TextToSpeech engine = tts;
        if(engine!=null && audio!=null)audio.post(engine::stop);
        if(status!=null)setStatus("Paused. Play repeats the interrupted sentence from its beginning.");
    }

    private void stop() {
        if(previewMode)exitPreview();
        activeRequest=null;
        if(playback!=null)playback.reset();
        saveProgress();
        if(stage!=null) {
            stage.setVisualStyle("normal");
            if(story!=null)stage.setVectors(story.figures,Collections.emptyList());
            stage.show("n",false);
        }
        if(caption!=null)caption.setText("Stopped. Play begins the story from the start.");
        final TextToSpeech engine = tts;
        if(engine!=null)audio.post(engine::stop);
        setStatus("Stopped at beginning.");
    }

    private void skip() {
        if(previewMode)exitPreview();
        if(playback==null)return;
        activeRequest=null;
        playback.skip();
        saveProgress();
        // Explicit manual cursor move can display the next section's style.
        StoryCore.Segment nextSegment = playback.current();
        stage.setMotions(nextSegment==null?Collections.emptyMap():nextSegment.motions);
        stage.setVisualStyle(nextSegment==null ? "normal" : nextSegment.artStyle);
        if(directed!=null&&nextSegment!=null)
            stage.setStoryBackground(directed.segmentScenes.get(playback.index()).scene);
        if(story!=null)stage.setVectors(story.figures,
                nextSegment==null?Collections.emptyList():nextSegment.visibleFigures);
        stage.show("n",false);
        final TextToSpeech engine = tts;
        if(engine!=null)audio.post(engine::stop);
        caption.setText(playback.isFinished()?"Tapos na.":"Skipped by listener • tap Play.");
        setStatus("Manual text-only skip; no audio completion was fabricated.");
    }

    @Override protected void onPause(){
        if(playback!=null && playback.running())pause();
        else saveProgress();
        super.onPause();
    }

    @Override protected void onDestroy() {
        destroyed=true;
        activeRequest=null;
        if(playback!=null)playback.pause();
        final TextToSpeech instance=tts;
        tts=null;
        if(audio!=null)audio.post(() -> {
            try {if(instance!=null){instance.stop();instance.shutdown();}}
            finally{audioThread.quitSafely();}
        });
        super.onDestroy();
    }
}

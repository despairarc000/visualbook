package com.visualbook.player;

import android.app.Activity;
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
            "triangle 11,84 49,8 88,84 fill:#8465B9 stroke:#5A357B width:2\n" +
            "circle 77 56 11 fill:#F6D16B stroke:#714A95 width:2\n" +
            "path M 15 64 C 20 14 59 26 74 18 stroke:#F7E2B3 width:3\n" +
            "@endfigure\n" +
            "@show spirit center\n" +
            "@n\nIsang araw, nagkita sina Juan at Pedro sa paaralan. Isang mahiwagang nilalang ang lumitaw.\n\n" +
            "@hide spirit\n" +
            "@c1m\nPedro, may assignment ka ba?\n\n" +
            "@chibi\n" +
            "@c2m\nNAKALIMUTAN KO!!!\n\n" +
            "@c1\nANO?!\n\n" +
            "@end\n" +
            "@c2\nJoke lang! Nasa bag ko.\n";
    private static final int PICK_TEXT = 33;
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
    private LinearLayout editorPane, voicePane;
    private boolean editorVisible = false, voicesVisible = false;

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
        editor.setText(DEMO);
        compileSource();
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
        TextView header = label("VISUALBOOK  •  THE FIRST STORY",17);
        header.setTypeface(null,android.graphics.Typeface.BOLD);
        header.setPadding(dp(3),dp(4),0,dp(8));
        root.addView(header);

        LinearLayout toggles = new LinearLayout(this);
        toggles.setOrientation(LinearLayout.HORIZONTAL);
        toggles.addView(button("Edit", () -> toggleEditor()),
                new LinearLayout.LayoutParams(0,dp(45),1));
        toggles.addView(button("Import TXT", this::importText),
                new LinearLayout.LayoutParams(0,dp(45),1));
        toggles.addView(button("Voices", () -> toggleVoices()),
                new LinearLayout.LayoutParams(0,dp(45),1));
        root.addView(toggles);

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
        root.addView(voicePane);

        stage = new StoryStageView(this);
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
        pause(); // invalidate old utterance generation before replacing the compiled story
        try {
            boolean script=!plainText.isChecked();
            StoryCore.Story result=StoryCore.compile(editor.getText().toString(),script);
            story=result;
            playback=new PlaybackState(result.segments);
            stage.setStoryBackground(result.background);
            stage.setVisualStyle("normal");
            stage.setVectors(result.figures, Collections.emptyList());
            // The demo explicitly mentions Juan/Pedro; other scripts remain generic.
            stage.setActorLabels(DEMO.equals(editor.getText().toString()) ? "Juan" : "C1",
                    DEMO.equals(editor.getText().toString()) ? "Pedro" : "C2");
            stage.show("n",false);
            caption.setText("Story ready • " + result.segments.size() + " speech segments.");
            setStatus("Compiled • " + result.cast.size() + " implicit speakers • " +
                    result.segments.size() + " narration segments.");
            editorVisible=false;
            editorPane.setVisibility(View.GONE);
        } catch (StoryCore.ScriptError problem) {
            story=null; playback=null;
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
        if(playback==null) {setStatus("Load a valid script first.");return;}
        if(!ttsReady) {setStatus("TTS not ready. Text is available; check installed speech engine.");return;}
        if(playback.running())return;
        if(playback.isFinished()) playback.reset();
        dispatchCurrent();
    }

    private void dispatchCurrent() {
        if(playback==null) return;
        StoryCore.Segment segment=playback.current();
        if(segment==null) {
            activeRequest=null;
            stage.show("n",false);
            if(story!=null)stage.setVectors(story.figures,Collections.emptyList());
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
                stage.setVectors(story.figures,s.visibleFigures);
                stage.show(s.speaker,true);
                caption.setText(("n".equals(s.speaker)?"Narrator":s.speaker.toUpperCase()) +
                        ": " + s.text);
                setStatus("Speaking " + (playback.index()+1) + "/" + playback.total());
            });
        }
        @Override public void onDone(String id) {
            ui.post(() -> {
                if(destroyed||playback==null||!playback.audibleDone(id))return;
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
        if(stage!=null)stage.show("n",false);
        final TextToSpeech engine = tts;
        if(engine!=null && audio!=null)audio.post(engine::stop);
        if(status!=null)setStatus("Paused. Play repeats the interrupted sentence from its beginning.");
    }

    private void stop() {
        activeRequest=null;
        if(playback!=null)playback.reset();
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
        if(playback==null)return;
        activeRequest=null;
        playback.skip();
        // Explicit manual cursor move can display the next section's style.
        StoryCore.Segment nextSegment = playback.current();
        stage.setVisualStyle(nextSegment==null ? "normal" : nextSegment.artStyle);
        if(story!=null)stage.setVectors(story.figures,
                nextSegment==null?Collections.emptyList():nextSegment.visibleFigures);
        stage.show("n",false);
        final TextToSpeech engine = tts;
        if(engine!=null)audio.post(engine::stop);
        caption.setText(playback.isFinished()?"Tapos na.":"Skipped by listener • tap Play.");
        setStatus("Manual text-only skip; no audio completion was fabricated.");
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

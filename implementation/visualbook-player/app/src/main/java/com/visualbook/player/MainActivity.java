package com.visualbook.player;

import android.app.Activity;
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
            "@n\nIsang araw, nagkita sina Juan at Pedro sa paaralan.\n\n" +
            "@c1m\nPedro, may assignment ka ba?\n\n" +
            "@c2m\nMeron!\n\n" +
            "@c1\nNasaan?\n\n" +
            "@c2\nNasa panaginip ko!\n";
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
        toggles.addView(button("Edit story", () -> toggleEditor()),
                new LinearLayout.LayoutParams(0,dp(45),1));
        toggles.addView(button("Character voices", () -> toggleVoices()),
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
        if(tts!=null && audio!=null)audio.post(() -> tts.stop());
        if(status!=null)setStatus("Paused. Play repeats the interrupted sentence from its beginning.");
    }

    private void stop() {
        activeRequest=null;
        if(playback!=null)playback.reset();
        if(stage!=null)stage.show("n",false);
        if(caption!=null)caption.setText("Stopped. Play begins the story from the start.");
        if(tts!=null)audio.post(() -> tts.stop());
        setStatus("Stopped at beginning.");
    }

    private void skip() {
        if(playback==null)return;
        activeRequest=null;
        playback.skip();
        stage.show("n",false);
        if(tts!=null)audio.post(() -> tts.stop());
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

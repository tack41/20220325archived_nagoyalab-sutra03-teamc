package com.slack.nagoyalab_sutra03.teamc.mimamorukun;

import android.app.Activity;
import android.content.Intent;
import android.media.AudioAttributes;
import android.media.SoundPool;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;

import com.slack.nagoyalab_sutra03.teamc.mimamorukun.EventLog.EventLog;
import com.slack.nagoyalab_sutra03.teamc.mimamorukun.EventLog.EventLogUtility;

import java.text.SimpleDateFormat;
import java.util.Locale;

public class EventLogDetailActivity extends Activity implements OnClickListener {

    private Button button_ok;
    private TextView text_occured_date;
    private TextView text_event_name;
    private TextView text_event_content;

    private EventLog eventLog;

    //sound source
    private SoundPool soundPool;
    private int sound_decision3;
    private boolean loadFinished = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_eventlog_detail);

        //Load sound source
        loadSound();

        button_ok = findViewById(R.id.button_ok);
        button_ok.setOnClickListener(this);

        text_occured_date = findViewById(R.id.text_occured_date);
        text_event_name = findViewById(R.id.text_event_name);
        text_event_content = findViewById(R.id.text_event_content);

        //親画面から値を取得
        Intent intent = getIntent();
        eventLog = EventLogUtility.getEventFromIntent(intent);

        //取得した値を表示
        displayEvent(eventLog);
    }

    /**
     * Load sound source to class instance variable.
     */
    private void loadSound(){
        AudioAttributes audioAttributes = new AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_GAME)
                .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                .build();
        soundPool = new SoundPool.Builder()
                .setAudioAttributes(audioAttributes)
                .setMaxStreams(1)
                .build();
        sound_decision3 = soundPool.load(this, R.raw.decision3, 1);
    }

    /**
     * Dipsplay contents of EventLog instance.
     * @param eventLog contents to display.
     */
    private void displayEvent(EventLog eventLog){
        java.text.SimpleDateFormat sdf = new SimpleDateFormat("yyyy年MM月dd日 E曜日 H時mm分", new Locale("ja", "JP", "JP"));
        text_occured_date.setText(sdf.format(eventLog.getOccurredDate()));
        text_event_name.setText(eventLog.getType().getTitle());
        text_event_content.setText(eventLog.getContent());
    }

    @Override
    public void onClick(View v) {

        if(v==button_ok){
            //Play sound effect of tapping button
            soundPool.play(sound_decision3, 1.0f, 1.0f, 0, 0, 1);

            //この画面を閉じてメイン画面に戻る
            finish();
        }
    }
}

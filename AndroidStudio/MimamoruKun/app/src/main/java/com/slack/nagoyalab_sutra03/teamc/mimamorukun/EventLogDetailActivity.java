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

    private Button _buttonOK;
    private TextView _textOccurredDate;
    private TextView _textEventName;
    private TextView _textEventContent;

    //EventLog instance sent by parent activity.
    private EventLog _eventLog;

    //sound source
    private SoundPool _soundPool;
    private int _soundOK;
    private boolean _loadFinished = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_eventlog_detail);

        //Load sound source
        loadSound();

        //Get UI Instances to handle from code.
        getUIInstances();

        //Get values from caller activity
        Intent intent = getIntent();
        _eventLog = EventLogUtility.getEventFromIntent(intent);

        //Display events.
        displayEvent(_eventLog);
    }

    /**
     * Load sound source to class instance variable.
     */
    private void loadSound(){

        AudioAttributes audioAttributes = new AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_GAME)
                .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                .build();

        _soundPool = new SoundPool.Builder()
                .setAudioAttributes(audioAttributes)
                .setMaxStreams(1)
                .build();

        _soundOK = _soundPool.load(this, R.raw.decision3, 1);

        _soundPool.setOnLoadCompleteListener(new SoundPool.OnLoadCompleteListener() {
            @Override
            public void onLoadComplete(SoundPool soundPool, int sampleId, int status) {
                if(0 == status){
                    _loadFinished = true;
                }
            }
        });
    }

    /**
     * Get UI Instance and regist event handler.
     */
    private void getUIInstances() {

        _buttonOK = findViewById(R.id.button_ok);
        _buttonOK.setOnClickListener(this);

        _textOccurredDate = findViewById(R.id.text_occured_date);
        _textEventName = findViewById(R.id.text_event_name);
        _textEventContent = findViewById(R.id.text_event_content);
    }

    /**
     * Dipsplay contents of EventLog instance.
     * @param eventLog contents to display.
     */
    private void displayEvent(EventLog eventLog){
        java.text.SimpleDateFormat sdf = new SimpleDateFormat("yyyy年MM月dd日 E曜日 H時mm分", new Locale("ja", "JP", "JP"));
        _textOccurredDate.setText(sdf.format(eventLog.getOccurredDate()));
        _textEventName.setText(eventLog.getType().getTitle());
        _textEventContent.setText(eventLog.getContent());
    }

    @Override
    public void onClick(View v) {

        if(v == _buttonOK){
            if(_loadFinished){
                //Play sound effect of tapping button
                _soundPool.play(_soundOK, 1.0f, 1.0f, 0, 0, 1);
            }

            //この画面を閉じてメイン画面に戻る

            finish();
        }
    }
}

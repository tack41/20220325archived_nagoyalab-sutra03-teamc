package com.slack.nagoyalab_sutra03.teamc.mimamorukun;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.graphics.Color;
import android.media.AudioAttributes;
import android.media.SoundPool;
import android.os.Bundle;
import android.os.IBinder;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.slack.nagoyalab_sutra03.teamc.mimamorukun.EventLog.EventLog;
import com.slack.nagoyalab_sutra03.teamc.mimamorukun.EventLog.EventLogStoreService;
import com.slack.nagoyalab_sutra03.teamc.mimamorukun.EventLog.EventLogType;
import com.slack.nagoyalab_sutra03.teamc.mimamorukun.Sensor.EventUtility;
import com.slack.nagoyalab_sutra03.teamc.mimamorukun.Sensor.LightEvent;
import com.slack.nagoyalab_sutra03.teamc.mimamorukun.Sensor.SwingEvent;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Locale;

/*
  対応が必要なイベントを表示する
 */
public class HandlingRequiredEventActivity extends Activity implements OnClickListener {

    private LightEvent _lightEvent;
    private SwingEvent _swingEvent;

    private LinearLayout _linearLayoutTop;
    private TextView _textViewTitle;
    private TextView _textViewOccurredDate;
    private EditText _editTextComment;
    private Button _buttonOK;

    //sound source
    private SoundPool _soundPool;
    private int _soundSwing;
    private int _soundLight;
    private int _soundOK;
    private int _soundLoop;
    private boolean _loadSoundOKFinished;
    private boolean _loadSoundLoopFinished;

    EventLogStoreService _eventLogStoreService;
    private ServiceConnection _connectionEventLogStoreService = new ServiceConnection() {
        public void onServiceConnected(ComponentName className, IBinder service) {
            _eventLogStoreService = ((EventLogStoreService.LocalBinder)service).getService();
        }

        public void onServiceDisconnected(ComponentName className) {
            _eventLogStoreService = null;
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_handling_required_event);

        //Get UI Instances to handle from code.
        getUIInstances();

        // Bind EventLogStoreService
        Intent i1 = new Intent(getBaseContext(), EventLogStoreService.class);
        bindService(i1, _connectionEventLogStoreService, Context.BIND_AUTO_CREATE);

        //get Event instance from intent
        Intent intent = getIntent();
        _lightEvent = EventUtility.getLightEventFromIntent(intent);
        _swingEvent = EventUtility.getSwingEventFromIntent(intent);

        //Load sound source.
        loadAndPlaySound(_lightEvent != null);

        //display Event value
        if(_lightEvent != null) {
            displayEvent(_lightEvent);
        }else {
            displayEvent(_swingEvent);
        }
    }

    @Override
    protected void onDestroy(){
        super.onDestroy();

        unloadSound(_lightEvent != null);
        unbindService(_connectionEventLogStoreService);
    }

    /**
     * Get UI Instance and regist event handler.
     */
    private void getUIInstances(){
        _linearLayoutTop = findViewById(R.id.linearlayout_top);
        _textViewTitle = findViewById(R.id.textview_title);
        _textViewOccurredDate = findViewById(R.id.textview_occured_date);
        _editTextComment = findViewById(R.id.edittext_comment);
        _buttonOK = findViewById(R.id.button_ok);
        _buttonOK.setOnClickListener(this);
    }

    /**
     *
     * @param isLight whether event is "Light"(not "Swing")
     */
    private void loadAndPlaySound(boolean isLight){

        AudioAttributes audioAttributes = new AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_GAME)
                .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                .build();

        _soundPool = new SoundPool.Builder()
                .setAudioAttributes(audioAttributes)
                .setMaxStreams(1)
                .build();

        _soundLight = _soundPool.load(this, R.raw.passer_montanus_cry1, 1);
        _soundSwing = _soundPool.load(this, R.raw.bell1, 1);
        _soundOK = _soundPool.load(this,R.raw.decision3,1);

        // Play warning sound continuously after load sound source.
        _soundLoop = (isLight ? _soundSwing : _soundLight);
        _soundPool.setOnLoadCompleteListener(new SoundPool.OnLoadCompleteListener() {
            @Override
            public void onLoadComplete(SoundPool soundPool, int sampleId, int status) {
                if(0 == status){
                    if(_soundLoop == sampleId){
                        _loadSoundLoopFinished = true;
                        soundPool.play(_soundLoop, 1.0f, 1.0f, 0, -1, 1);
                    }else if (_soundOK == sampleId){
                        _loadSoundOKFinished = true;
                    }
                }
            }
        });
    }

    /**
     *
     * @param isLight whether event is "Light"(not "Swing")
     */
    private void unloadSound(boolean isLight){

        try{
            _soundPool.unload(_soundLight);
        }catch(Exception e){
        }
        try{
            _soundPool.unload(_soundSwing);
        }catch(Exception e) {
        }
        try{
            _soundPool.unload(_soundOK);
        }catch(Exception e){
        }

        _soundPool.release();
    }


    private void displayEvent(LightEvent event){
        _linearLayoutTop.setBackgroundColor(Color.parseColor("#66cdaa"));

        java.text.SimpleDateFormat sdf = new SimpleDateFormat("yyyy年MM月dd日 E曜日 H時mm分",new Locale("ja", "JP", "JP"));
        _textViewTitle.setText(event.getMessage());
        _textViewOccurredDate.setText(sdf.format(event.getOccurredDate()));
    }

    private void displayEvent(SwingEvent event){
        _linearLayoutTop.setBackgroundColor(Color.parseColor("#ffff00"));

        java.text.SimpleDateFormat sdf = new SimpleDateFormat("yyyy年MM月dd日 E曜日 H時mm分",new Locale("ja", "JP", "JP"));
        _textViewTitle.setText(event.getMessage());
        _textViewOccurredDate.setText(sdf.format(event.getOccurredDate()));
    }

    @Override
    public void onClick(View v) {

        if(v == _buttonOK){
            if(_loadSoundLoopFinished){
                //Stop continuous warning sound.
                _soundPool.stop(_soundLoop);
            }
            if(_loadSoundOKFinished){
                //Play sound effect of tapping button
                _soundPool.play(_soundOK, 1.0f, 1.0f, 0, 0, 1);
            }

            //対応完了イベントをログに記録
            EventLog eventLog = new EventLog();

            if(_lightEvent != null) {
                eventLog.setType(EventLogType.SwingHandled);
            }else{
                eventLog.setType(EventLogType.LightHandled);
            }
            eventLog.setOccurredDate(new java.util.Date());
            eventLog.setContent("対応コメント:" + _editTextComment.getText().toString());
            _eventLogStoreService.insertEventLog(eventLog);

            //Return to MainActivity.
            Intent intent = new Intent();
            this.setResult(RESULT_OK, intent);
            finish();
        }
    }
}

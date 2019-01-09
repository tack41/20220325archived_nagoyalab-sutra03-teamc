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
import android.provider.MediaStore;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RadioGroup;
import android.widget.TextView;

import com.slack.nagoyalab_sutra03.teamc.mimamorukun.EventLog.EventLog;
import com.slack.nagoyalab_sutra03.teamc.mimamorukun.EventLog.EventLogStoreService;
import com.slack.nagoyalab_sutra03.teamc.mimamorukun.EventLog.EventLogType;

import java.text.SimpleDateFormat;
import java.util.Locale;

/*
  対応が必要なイベントを表示する
 */
public class HandlingRequiredEventActivity extends Activity implements OnClickListener {

    private EventLog _eventLog;

    private LinearLayout _linearLayoutTop;
    private TextView _textViewTitle;
    private TextView _textViewOccurredDate;
    private EditText _editTextComment;
    private RadioGroup _radioGroupSensorRestart;
    private Button _buttonOK;

    //sound source
    private SoundPool _soundPool;
    private int _soundBGM;
    private int _soundOK;
    private boolean _loadSoundOKFinished;
    private boolean _loadSoundLoopFinished;

    private Setting _setting;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_handling_required_event);

        //Get UI Instances to handle from code.
        getUIInstances();

        //get Event instance from intent
        Intent intent = getIntent();
        _eventLog = EventLog.getFromIntent(intent);

        _setting = Setting.getFromIntent(intent);

        //Load sound source.
        loadAndPlaySound();

        //display Event value
        displayEvent();
    }

    @Override
    protected void onDestroy(){

        unloadSound();

        super.onDestroy();
    }

    /**
     * Get UI Instance and regist event handler.
     */
    private void getUIInstances(){
        _linearLayoutTop = findViewById(R.id.linearlayout_top);
        _textViewTitle = findViewById(R.id.textview_title);
        _textViewOccurredDate = findViewById(R.id.textview_occured_date);
        _editTextComment = findViewById(R.id.edittext_comment);

        _radioGroupSensorRestart = findViewById(R.id.radiogroup_sensor_restart);
        _radioGroupSensorRestart.check(R.id.radiobutton_sensor_restart_no);

        _buttonOK = findViewById(R.id.button_ok);
        _buttonOK.setOnClickListener(this);
    }

    private void loadAndPlaySound(){

        AudioAttributes audioAttributes = new AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_GAME)
                .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                .build();

        _soundPool = new SoundPool.Builder()
                .setAudioAttributes(audioAttributes)
                .setMaxStreams(1)
                .build();

        if(_eventLog.getType() == EventLogType.TemperatureUnusual){
            _soundBGM = _soundPool.load(this, R.raw.kettle_boiling1, 1);
        }else if(_eventLog.getType() == EventLogType.Light){
            _soundBGM = _soundPool.load(this, R.raw.passer_montanus_cry1, 1);
        }else{
            _soundBGM = _soundPool.load(this, R.raw.bell1, 1);
        }
        _soundOK = _soundPool.load(this,R.raw.decision3,1);

        // Play warning sound continuously after load sound source.
        _soundPool.setOnLoadCompleteListener(new SoundPool.OnLoadCompleteListener() {
            @Override
            public void onLoadComplete(SoundPool soundPool, int sampleId, int status) {
                if(0 == status){
                    if(_soundBGM == sampleId){
                        _loadSoundLoopFinished = true;
                        soundPool.play(_soundBGM, 1.0f, 1.0f, 0, -1, 1);
                    }else if (_soundOK == sampleId){
                        _loadSoundOKFinished = true;
                    }
                }
            }
        });
    }

    private void unloadSound(){

        try{
            _soundPool.unload(_soundBGM);
        }catch(Exception e){
        }
        try{
            _soundPool.unload(_soundOK);
        }catch(Exception e){
        }

        _soundPool.release();
    }


    private void displayEvent(){
        if(_eventLog.getType() == EventLogType.TemperatureUnusual){
            _linearLayoutTop.setBackgroundColor(Color.parseColor("#ff0000"));
        }else if(_eventLog.getType() == EventLogType.Light){
            _linearLayoutTop.setBackgroundColor(Color.parseColor("#66cdaa"));
        }else{
            _linearLayoutTop.setBackgroundColor(Color.parseColor("#ffff00"));
        }

        java.text.SimpleDateFormat sdf = new SimpleDateFormat("yyyy年MM月dd日 E曜日 H時mm分",new Locale("ja", "JP", "JP"));
        _textViewTitle.setText(_eventLog.getType().getTitle());
        _textViewOccurredDate.setText(sdf.format(_eventLog.getOccurredDate()));
    }

    @Override
    public void onClick(View v) {

        if(v == _buttonOK) {
            if (_loadSoundLoopFinished) {
                if (_loadSoundOKFinished) {
                    //Play sound effect of tapping button
                    _soundPool.play(_soundOK, 1.0f, 1.0f, 0, 0, 1);
                }

                //Stop continuous warning sound.
                _soundPool.stop(_soundBGM);
            }
            if (_loadSoundOKFinished) {
                //Play sound effect of tapping button
                _soundPool.play(_soundOK, 1.0f, 1.0f, 0, 0, 1);
            }

            //対応完了イベントログを作成
            EventLog eventLog = new EventLog();
            if (_eventLog.getType() == EventLogType.TemperatureUnusual) {
                eventLog.setType(EventLogType.TemperatureHandled);
            } else if (_eventLog.getType() == EventLogType.Light){
                eventLog.setType(EventLogType.LightHandled);
            }else{
                eventLog.setType(EventLogType.MovementHandled);
            }
            eventLog.setOccurredDate(new java.util.Date());

            //UserNameを取得できる場合は、コメントに追加する
            String contentHeader = "対応コメント";
            if(_setting != null && _setting.getUserName() != null && _setting.getUserName().length() > 0){
                contentHeader += "(" + _setting.getUserName() + ")";
            };
            contentHeader += ":";
            eventLog.setContent(contentHeader + _editTextComment.getText().toString());

            //Return to MainActivity.
            Intent intent = new Intent();
            eventLog.putToIntent(intent);
            if(_radioGroupSensorRestart.getCheckedRadioButtonId() == R.id.radiobutton_sensor_restart_yes){
                this.setResult(RESULT_OK, intent);
            }else{
                this.setResult(RESULT_CANCELED, intent);
            }
            finish();
        }
    }
}

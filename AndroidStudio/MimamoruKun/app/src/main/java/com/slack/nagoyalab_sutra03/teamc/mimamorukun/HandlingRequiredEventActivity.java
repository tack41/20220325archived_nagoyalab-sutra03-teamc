package com.slack.nagoyalab_sutra03.teamc.mimamorukun;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.media.AudioAttributes;
import android.media.SoundPool;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.slack.nagoyalab_sutra03.teamc.mimamorukun.EventLog.EventLog;
import com.slack.nagoyalab_sutra03.teamc.mimamorukun.EventLog.EventLogUtility;
import com.slack.nagoyalab_sutra03.teamc.mimamorukun.EventLog.EventLogType;

import java.text.SimpleDateFormat;
import java.util.Locale;

/*
  対応が必要なイベントを表示する
 */
public class HandlingRequiredEventActivity extends Activity implements OnClickListener {

    private LinearLayout _linearLayoutTop;
    private TextView _textViewTitle;
    private TextView _textViewOccurredDate;
    private EditText _editTextComment;
    private Button _buttonOK;

    private EventLog eventLog;

    //sound source
    private SoundPool _soundPool;
    private int _soundSwing;
    private int _soundLight;
    private int _soundOK;
    private int _soundLoop;
    private boolean _loadSoundOKFinished;
    private boolean _loadSoundLoopFinished;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_handling_required_event);

        //Get UI Instances to handle from code.
        getUIInstances();

        //親画面から値を取得
        Intent intent = getIntent();
        eventLog = EventLogUtility.getEventFromIntent(intent);

        //Load sound source.
        loadAndPlaySound(eventLog);

        //取得した値を表示
        displayEvent(eventLog);
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

    private void loadAndPlaySound(EventLog eventLog){

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
        _soundLoop = (eventLog.getType() == EventLogType.Swing ? _soundSwing : _soundLight);
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
     * Dipsplay contents of EventLog instance.
     * @param eventLog contents to display.
     */
    private void displayEvent(EventLog eventLog){
        //イベントの種類に応じて背景色を変える
        switch(eventLog.getType()){
            case Light:
                _linearLayoutTop.setBackgroundColor(Color.parseColor("#66cdaa"));
                break;
            case Swing:
                _linearLayoutTop.setBackgroundColor(Color.parseColor("#ffff00"));
                break;
        }

        java.text.SimpleDateFormat sdf = new SimpleDateFormat("yyyy年MM月dd日 E曜日 H時mm分",new Locale("ja", "JP", "JP"));
        _textViewTitle.setText(eventLog.getType().getTitle());
        _textViewOccurredDate.setText(sdf.format(eventLog.getOccurredDate()));
    }

    @Override
    public void onClick(View v) {

        if(v== _buttonOK){
            if(_loadSoundLoopFinished){
                //Stop continuous warning sound.
                _soundPool.stop(_soundLoop);
            }
            if(_loadSoundOKFinished){
                //Play sound effect of tapping button
                _soundPool.play(_soundOK, 1.0f, 1.0f, 0, 0, 1);
            }

            //対応完了イベントのインスタンスを生成
            EventLog event_Log_new = new EventLog();

            switch(eventLog.getType()){
                case Swing:
                    event_Log_new.setType(EventLogType.SwingHandled);
                    break;
                case Light:
                    event_Log_new.setType(EventLogType.LightHandled);
                    break;
                default:
                    event_Log_new.setType(EventLogType.Unknown);
                    break;
            }
            event_Log_new.setOccurredDate(new java.util.Date());
            event_Log_new.setContent("対応コメント:" + _editTextComment.getText().toString());

            //生成したインスタンスをメイン画面に渡す
            Intent intent = new Intent();
            EventLogUtility.putEventToIntent(intent, event_Log_new);
            this.setResult(RESULT_OK, intent);

            //この画面を閉じてメイン画面に戻る
            finish();
        }
    }
}

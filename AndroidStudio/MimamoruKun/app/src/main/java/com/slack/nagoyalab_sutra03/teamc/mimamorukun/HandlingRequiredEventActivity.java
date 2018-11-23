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

    private LinearLayout linearlayout_top;
    private TextView textview_title;
    private TextView textview_occured_date;
    private EditText edittext_comment;
    private Button button_ok;

    private EventLog eventLog;

    //sound source
    private SoundPool soundPool;
    private int sound_bell1;
    private int sound_passer_mountanus_cry1;
    private int sound_decision3;
    private int sound_now_playing;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_handling_required_event);

        linearlayout_top = findViewById(R.id.linearlayout_top);
        textview_title = findViewById(R.id.textview_title);
        textview_occured_date = findViewById(R.id.textview_occured_date);
        edittext_comment = findViewById(R.id.edittext_comment);
        button_ok = findViewById(R.id.button_ok);
        button_ok.setOnClickListener(this);

        //親画面から値を取得
        Intent intent = getIntent();
        eventLog = EventLogUtility.getEventFromIntent(intent);

        //Load and play sound effect.
        loadAndPlaySound(eventLog);

        //取得した値を表示
        displayEvent(eventLog);
    }

    private void loadAndPlaySound(EventLog eventLog){
        //Load sound source.
        AudioAttributes audioAttributes = new AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_GAME)
                .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                .build();
        soundPool = new SoundPool.Builder()
                .setAudioAttributes(audioAttributes)
                .setMaxStreams(1)
                .build();
        sound_passer_mountanus_cry1 = soundPool.load(this, R.raw.passer_montanus_cry1, 1);
        sound_bell1 = soundPool.load(this, R.raw.bell1, 1);
        sound_decision3 = soundPool.load(this,R.raw.decision3,1);
        // Play warning sound continuously after load sound source.
        sound_now_playing = (eventLog.getType() == EventLogType.Swing ? sound_bell1 : sound_passer_mountanus_cry1);
        soundPool.setOnLoadCompleteListener(new SoundPool.OnLoadCompleteListener() {
            @Override
            public void onLoadComplete(SoundPool soundPool, int sampleId, int status) {
                soundPool.play(sound_now_playing, 1.0f, 1.0f, 0, -1, 1);
            }
        });
    }

    private void displayEvent(EventLog eventLog){
        //イベントの種類に応じて背景色を変える
        switch(eventLog.getType()){
            case Light:
                linearlayout_top.setBackgroundColor(Color.parseColor("#66cdaa"));
                break;
            case Swing:
                linearlayout_top.setBackgroundColor(Color.parseColor("#ffff00"));
                break;
        }

        java.text.SimpleDateFormat sdf = new SimpleDateFormat("yyyy年MM月dd日 E曜日 H時mm分",new Locale("ja", "JP", "JP"));
        textview_title.setText(eventLog.getType().getTitle());
        textview_occured_date.setText(sdf.format(eventLog.getOccurredDate()));
    }
    //ボタンクリック時の関数
    @Override
    public void onClick(View v) {

        if(v==button_ok){
            //Stop continuous warning sound.
            soundPool.stop(sound_now_playing);
            //Play sound effect of tapping button
            soundPool.play(sound_decision3, 1.0f, 1.0f, 0, 0, 1);

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
            event_Log_new.setContent("対応コメント:" + edittext_comment.getText().toString());

            //生成したインスタンスをメイン画面に渡す
            Intent intent = new Intent();
            EventLogUtility.putEventToIntent(intent, event_Log_new);
            this.setResult(RESULT_OK, intent);

            //この画面を閉じてメイン画面に戻る
            finish();
        }
    }
}

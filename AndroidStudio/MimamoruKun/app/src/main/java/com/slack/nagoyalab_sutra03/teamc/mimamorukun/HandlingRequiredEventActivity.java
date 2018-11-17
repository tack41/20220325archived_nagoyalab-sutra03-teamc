package com.slack.nagoyalab_sutra03.teamc.mimamorukun;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
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

        //イベントの種類に応じて背景色を変える
        switch(eventLog.getType()){
            case Light:
                linearlayout_top.setBackgroundColor(Color.parseColor("#66cdaa"));
                break;
            case Swing:
                linearlayout_top.setBackgroundColor(Color.parseColor("#ffff00"));
                break;
        }

        //取得した値を表示
        displayEvent();
    }

    public void displayEvent(){
        java.text.SimpleDateFormat sdf = new SimpleDateFormat("yyyy年MM月dd日 E曜日 H時mm分",new Locale("ja", "JP", "JP"));
        textview_title.setText(eventLog.getType().getTitle());
        textview_occured_date.setText(sdf.format(eventLog.getOccurredDate()));
    }
    //ボタンクリック時の関数
    @Override
    public void onClick(View v) {

        if(v==button_ok){
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

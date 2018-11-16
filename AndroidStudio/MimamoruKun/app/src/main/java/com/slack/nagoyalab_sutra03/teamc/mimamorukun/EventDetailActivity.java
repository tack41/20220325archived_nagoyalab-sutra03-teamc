package com.slack.nagoyalab_sutra03.teamc.mimamorukun;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;

import com.slack.nagoyalab_sutra03.teamc.mimamorukun.Event.Event;
import com.slack.nagoyalab_sutra03.teamc.mimamorukun.Event.EventManager;

import java.text.SimpleDateFormat;
import java.util.Locale;

public class EventDetailActivity extends Activity implements OnClickListener {

    private Button button_ok;
    private TextView text_occured_date;
    private TextView text_event_name;
    private TextView text_event_content;

    private Event event;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_event_detail);

        button_ok = findViewById(R.id.button_ok);
        button_ok.setOnClickListener(this);

        text_occured_date = findViewById(R.id.text_occured_date);
        text_event_name = findViewById(R.id.text_event_name);
        text_event_content = findViewById(R.id.text_event_content);

        //親画面から値を取得
        Intent intent = getIntent();
        event = EventManager.getEventFromIntent(intent);

        //取得した値を表示
        displayEvent(event);
    }

    /*
      指定されたEventオブジェクトを画面に表示する
     */
    private void displayEvent(Event event){
        java.text.SimpleDateFormat sdf = new SimpleDateFormat("yyyy年MM月dd日 E曜日 H時mm分", new Locale("ja", "JP", "JP"));
        text_occured_date.setText(sdf.format(event.getOccurredDate()));
        text_event_name.setText(event.getType().getTitle());
        text_event_content.setText(event.getContent());
    }

    //ボタンクリック時の関数
    @Override
    public void onClick(View v) {

        if(v==button_ok){
            //この画面を閉じてメイン画面に戻る
            finish();
        }
    }
}

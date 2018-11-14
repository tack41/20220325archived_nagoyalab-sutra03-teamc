package com.slack.nagoyalab_sutra03.teamc.mimamorukun;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;
import java.text.SimpleDateFormat;

public class EventDetailActivity extends Activity implements OnClickListener {

    private Button button_ok;
    private TextView text_occured_date;
    private TextView text_event_name;
    private TextView text_event_content;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_event_detail);

        button_ok = findViewById(R.id.button_ok);
        button_ok.setOnClickListener(this);

        text_occured_date = findViewById(R.id.text_occured_date);
        text_event_name = findViewById(R.id.text_event_name);
        text_event_content = findViewById(R.id.text_event_content);

        //親画面から渡されたイベント情報を取得して表示
        Intent intent = getIntent();
        java.text.SimpleDateFormat sdf = new SimpleDateFormat("yyyy年MM月dd日 E曜日 H時mm分");
        text_occured_date.setText(sdf.format(new java.util.Date(intent.getLongExtra("DATE", 0))));
        text_event_name.setText(intent.getStringExtra("NAME"));
        text_event_content.setText(intent.getStringExtra("CONTENT"));
    }

    //ボタンクリック時の関数
    @Override
    public void onClick(View v) {

        if(v==button_ok){
            Intent intent = new Intent(this, MainActivity.class);
            startActivityForResult(intent, 0);
        }
    }
}

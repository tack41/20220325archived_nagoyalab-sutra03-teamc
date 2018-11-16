package com.slack.nagoyalab_sutra03.teamc.mimamorukun;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.TextView;

import com.slack.nagoyalab_sutra03.teamc.mimamorukun.Sensor.SensorManager;
import com.slack.nagoyalab_sutra03.teamc.mimamorukun.Sensor.TemperatureEvent;
import com.slack.nagoyalab_sutra03.teamc.mimamorukun.Sensor.TemperatureEventListener;

import java.text.SimpleDateFormat;
import java.util.Locale;

public class TemperatureEventActivity extends Activity implements OnClickListener, TemperatureEventListener {

    private TextView textview_title;
    private TextView textview_occured_date;
    private TextView textview_simulate_temperature_event_handled_event;

    private Event event;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_temperature_event);

        textview_title = findViewById(R.id.textview_title);
        textview_occured_date = findViewById(R.id.textview_occured_date);
        textview_simulate_temperature_event_handled_event = findViewById(R.id.textview_simulate_temperature_event_handled_event);
        textview_simulate_temperature_event_handled_event.setOnClickListener(this);

        SensorManager.addTemperatureEventListener(this);

        //親画面から値を取得
        Intent intent = getIntent();
        event = EventManager.getEventFromIntent(intent);

        //取得した値を表示
        displayEvent();
    }
    public void displayEvent(){
        java.text.SimpleDateFormat sdf = new SimpleDateFormat("yyyy年MM月dd日 E曜日 H時mm分",new Locale("ja", "JP", "JP"));
        textview_title.setText(event.getTitle());
        textview_occured_date.setText(sdf.format(event.getOccurredDate()));
    }

    @Override
    public void onClick(View v) {
        if(v==textview_simulate_temperature_event_handled_event){
            //温度異常解消イベントを疑似発生
            SensorManager.fireTemperatured(true, 28);
        }
    }

    @Override
    public void onTemperatured(TemperatureEvent e){

        //温度異常が解消された場合はメイン画面に戻る
        if(e.isNormal()){
            //Eventオブジェクトを生成
            Event event_new = new Event();
            event_new.setType(EventType.TemperatureBecomeNormal);
            event_new.setContent("温度が正常(" + e.getTemperature() + "℃)になりました");
            event_new.setOccurredDate(new java.util.Date());

            //生成したインスタンスを登録
            EventManager.addEvent(event_new);

            //メイン画面に戻る
            Intent intent = new Intent(this, MainActivity.class);
            startActivityForResult(intent, 0);
        }
    }
}

package com.slack.nagoyalab_sutra03.teamc.mimamorukun;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;

import com.slack.nagoyalab_sutra03.teamc.mimamorukun.Event.Event;
import com.slack.nagoyalab_sutra03.teamc.mimamorukun.Event.EventManager;
import com.slack.nagoyalab_sutra03.teamc.mimamorukun.Event.EventSQLiteOpenHelper;
import com.slack.nagoyalab_sutra03.teamc.mimamorukun.Event.EventStoreSQLite;
import com.slack.nagoyalab_sutra03.teamc.mimamorukun.Event.EventType;
import com.slack.nagoyalab_sutra03.teamc.mimamorukun.Sensor.LightEvent;
import com.slack.nagoyalab_sutra03.teamc.mimamorukun.Sensor.LightEventListener;
import com.slack.nagoyalab_sutra03.teamc.mimamorukun.Sensor.SensorManager;
import com.slack.nagoyalab_sutra03.teamc.mimamorukun.Sensor.SwingEvent;
import com.slack.nagoyalab_sutra03.teamc.mimamorukun.Sensor.SwingEventListener;
import com.slack.nagoyalab_sutra03.teamc.mimamorukun.Sensor.TemperatureEvent;
import com.slack.nagoyalab_sutra03.teamc.mimamorukun.Sensor.TemperatureEventListener;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;
import java.util.Timer;
import java.util.TimerTask;

public class MainActivity extends Activity implements OnClickListener, LightEventListener, SwingEventListener, TemperatureEventListener {

    private TextView textview_time;
    private List<TextView> textViewList;
    private List<Event> eventList;

    //各イベント発生をシミュレートするボタン
    private Button button_simulate_light_event;
    private Button button_simulate_swing_event;
    private Button button_simulate_temperature_event;

    //タイマーのイベントハンドラにてUIスレッドを取得するために生成
    Handler handler = new Handler();

    //イベントを保存するDB(複数生成するとロックエラーとなるため、これを使いまわす
    private EventManager _manager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        _manager = new EventManager(this);

        setContentView(R.layout.activity_main);

        textview_time = findViewById(R.id.textview_time);

        textViewList = new ArrayList<>();
        textViewList.add((TextView)findViewById(R.id.text_history1));
        textViewList.add((TextView)findViewById(R.id.text_history2));
        textViewList.add((TextView)findViewById(R.id.text_history3));
        textViewList.add((TextView)findViewById(R.id.text_history4));
        textViewList.add((TextView)findViewById(R.id.text_history5));
        textViewList.add((TextView)findViewById(R.id.text_history6));

        for(int i=0; i<textViewList.size(); i++){
            textViewList.get(i).setOnClickListener(this);
        }

        button_simulate_light_event = findViewById(R.id.button_simulate_light_event);
        button_simulate_light_event.setOnClickListener(this);
        button_simulate_swing_event = findViewById(R.id.button_simulate_swing_event);
        button_simulate_swing_event.setOnClickListener(this);
        button_simulate_temperature_event = findViewById(R.id.button_simulate_temperature_event);
        button_simulate_temperature_event.setOnClickListener(this);

        SensorManager.addLightEventListener(this);
        SensorManager.addSwingEventListener(this);
        SensorManager.addTemperatureEventListener(this);

        //イベント履歴を取得
        getEventHistory();

        //イベント履歴を表示
        displayEventHistory();

        //時刻を定期的に更新
        Timer timer = new Timer(true);
        timer.schedule(  new TimerTask(){
            @Override
            public void run() {
                handler.post( new Runnable() {
                    public void run() {
                        SimpleDateFormat sdf = new SimpleDateFormat("HH:mm",new Locale("ja", "JP", "JP"));
                        sdf.setTimeZone(TimeZone.getTimeZone("Asia/Tokyo"));
                        textview_time.setText(sdf.format(new java.util.Date()));
                    }
                });
            }
        }, 0, 1000); //1秒おきに時間を更新
    }

    // startActivityForResult で起動させたアクティビティが
    // finish() により破棄されたときにコールされる
    // requestCode : startActivityForResult の第二引数で指定した値が渡される
    // resultCode : 起動先のActivity.setResult の第一引数が渡される
    // Intent intent : 起動先Activityから送られてくる Intent
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent intent) {
        super.onActivityResult(requestCode, resultCode, intent);
        if(intent != null){
            Event event = EventManager.getEventFromIntent(intent);

            if(event != null){
                _manager.insertEvent(event);

                //イベント履歴を再度取得して表示
                getEventHistory();
                displayEventHistory();
            }
        }
    }

    @Override
    public void onClick(View v) {

        //イベント詳細画面に遷移
        for(int i=0; i<textViewList.size(); i++){
            if(v == textViewList.get(i)){
                Event event = eventList.get(eventList.size()-i-1);
                Intent intent = new Intent(this, EventDetailActivity.class);
                EventManager.putEventToIntent(intent, event);
                startActivityForResult(intent, 0);
            }
        }

        //各イベント発生を手動で発生
        if(v == button_simulate_light_event){
            Event event = new Event();
            event.setType(EventType.Light);
            event.setContent("光イベント手動発生");
            event.setOccurredDate(new java.util.Date());
            _manager.insertEvent(event);

            SensorManager.fireLighted(false);
        }else if(v == button_simulate_swing_event){
            Event event = new Event();
            event.setType(EventType.Swing);
            event.setContent("振動イベント手動発生");
            event.setOccurredDate(new java.util.Date());
            _manager.insertEvent(event);

            SensorManager.fireSwinged(false);
        }else if(v == button_simulate_temperature_event){
            Event event = new Event();
            event.setType(EventType.TemperatureUnusual);
            event.setContent("温度異常イベント手動発生");
            event.setOccurredDate(new java.util.Date());
            _manager.insertEvent(event);

            SensorManager.fireTemperatured(false, 35.0);
        }
    }

    /*
      光イベント検知時のEventHandlerの仮実装
     */
    @Override
    public void onLighted(LightEvent e){
        if(!e.isNormal()){
            //Eventオブジェクトを生成
            Event event = new Event();
            event.setType(EventType.Light);
            event.setContent("端末が光を検知しました");
            event.setOccurredDate(new java.util.Date());

            //イベント対応画面に遷移
            Intent intent = new Intent(this, HandlingRequiredEventActivity.class);
            EventManager.putEventToIntent(intent, event);
            startActivityForResult(intent, 0);
        }
    }

    /*
      振動イベント検知時のEventHandlerの仮実装
     */
    @Override
    public void onSwinged(SwingEvent e){
        if(!e.isNormal()){
            //Eventオブジェクトを生成
            Event event = new Event();
            event.setType(EventType.Swing);
            event.setContent("端末が振動を検知しました");
            event.setOccurredDate(new java.util.Date());

            //イベント対応画面に遷移
            Intent intent = new Intent(this, HandlingRequiredEventActivity.class);
            EventManager.putEventToIntent(intent, event);
            startActivityForResult(intent, 0);
        }
    }

    /*
      温度イベント検知時のEventHandlerの仮実装
     */
    @Override
    public void onTemperatured(TemperatureEvent e){
        if(!e.isNormal()){
            //Eventオブジェクトを生成
            Event event = new Event();
            event.setType(EventType.TemperatureUnusual);
            event.setContent("温度異常を検知しました(" + e.getTemperature() + "℃)");
            event.setOccurredDate(new java.util.Date());

            //イベント対応画面に遷移
            Intent intent = new Intent(this, TemperatureEventActivity.class);
            EventManager.putEventToIntent(intent, event);
            startActivityForResult(intent, 0);
        }
    }

    /*
    イベント履歴取得メソッドの仮実装
     */
    private void getEventHistory(){
        this.eventList = _manager.getAllEvent();
    }

    /*
    イベント履歴表示メソッドの仮実装
     */
    private void displayEventHistory(){

        DateFormat sdf= new SimpleDateFormat("MM/dd");

        //eventListの後ろ(最新)から表示
        for(int i=0; i<6; i++){
            if(eventList.size()-i > 0){
                Event event = eventList.get(eventList.size()-i-1);
                textViewList.get(i).setText(sdf.format(event.getOccurredDate()) + event.getType().getTitle());
            }
        }
    }

}
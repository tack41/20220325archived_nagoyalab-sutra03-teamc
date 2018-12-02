package com.slack.nagoyalab_sutra03.teamc.mimamorukun;

import android.app.Activity;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.media.AudioAttributes;
import android.media.SoundPool;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.NotificationManagerCompat;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;
import android.support.v4.app.NotificationCompat;
import android.content.ServiceConnection;
import android.content.ComponentName;
import android.os.IBinder;

import com.slack.nagoyalab_sutra03.teamc.mimamorukun.EventLog.EventLog;
import com.slack.nagoyalab_sutra03.teamc.mimamorukun.EventLog.EventLogUtility;
import com.slack.nagoyalab_sutra03.teamc.mimamorukun.EventLog.EventLogType;
import com.slack.nagoyalab_sutra03.teamc.mimamorukun.Sensor.LightEvent;
import com.slack.nagoyalab_sutra03.teamc.mimamorukun.Sensor.LightEventListener;
import com.slack.nagoyalab_sutra03.teamc.mimamorukun.Sensor.SensorManager;
import com.slack.nagoyalab_sutra03.teamc.mimamorukun.Sensor.SwingEvent;
import com.slack.nagoyalab_sutra03.teamc.mimamorukun.Sensor.SwingEventListener;
import com.slack.nagoyalab_sutra03.teamc.mimamorukun.Sensor.TemperatureEvent;
import com.slack.nagoyalab_sutra03.teamc.mimamorukun.Sensor.TemperatureEventListener;
import com.slack.nagoyalab_sutra03.teamc.mimamorukun.EventLog.EventLogStoreService;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;
import java.util.Timer;
import java.util.TimerTask;

public class MainActivity extends Activity implements OnClickListener, LightEventListener, SwingEventListener, TemperatureEventListener {

    private TextView _textView_time;
    private List<TextView> _textViewList;

    //Buttons to simulate each events.
    private Button _buttonSimulateLightEvent;
    private Button _buttonSimulateSwingEvent;
    private Button _buttonSimulateTemperatureEvent;

    //List of EventLog instance to display.
    private List<EventLog> _eventLogList;

    //This class level accessible _handler required to get UI thread in timer event _handler.
    private Handler _handler = new Handler();

    //sound source
    private SoundPool _soundPool;
    private int _soundTapButtonOrEvent;
    private boolean _loadFinished = false;

    EventLogStoreService _eventLogStoreService;
    // Serviceとのインターフェースクラス
    private ServiceConnection mConnection = new ServiceConnection() {
        public void onServiceConnected(ComponentName className, IBinder service) {
            // Serviceとの接続確立時に呼び出される。
            // service引数には、Onbind()で返却したBinderが渡される
            _eventLogStoreService = ((EventLogStoreService.LocalBinder)service).getService();
            //必要であればmBoundServiceを使ってバインドしたServiceへの制御を行う

            //Get all event history.
            _eventLogList = _eventLogStoreService.getAllEvent();

            //Display events.
            displayEventHistory();
        }

        public void onServiceDisconnected(ComponentName className) {
            // Serviceとの切断時に呼び出される。
            _eventLogStoreService = null;
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //Load sound source
        loadSound();

        //Get UI Instances to handle from code.
        getUIInstances();

        //Bind sensor events.
        SensorManager.addLightEventListener(this);
        SensorManager.addSwingEventListener(this);
        SensorManager.addTemperatureEventListener(this);

        // Bind EventLogStoreService
        Intent i = new Intent(getBaseContext(), EventLogStoreService.class);
        boolean ret = bindService(i, mConnection, Context.BIND_AUTO_CREATE);

        //Display time.
        displayTime();

        //Create Notification Channel
        NotificationManager manager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        NotificationChannel channel = new NotificationChannel(getString(R.string.app_name), getString(R.string.app_name), NotificationManager.IMPORTANCE_DEFAULT);
        channel.setLightColor(Color.YELLOW);
        channel.setLockscreenVisibility(Notification.VISIBILITY_PRIVATE);
        manager.createNotificationChannel(channel);
    }

    @Override
    protected void onDestroy(){
        super.onDestroy();
        unbindService(mConnection);
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

        _soundTapButtonOrEvent = _soundPool.load(this, R.raw.decision3, 1);

        _soundPool.setOnLoadCompleteListener(new SoundPool.OnLoadCompleteListener() {
            @Override
            public void onLoadComplete(SoundPool soundPool, int sampleId, int status) {
                _loadFinished = true;
            }
        });
    }

    /**
     * Get UI Instance and register event handler.
     */
    private void getUIInstances(){

        _textView_time = findViewById(R.id.textview_time);

        _textViewList = new ArrayList<>();
        _textViewList.add((TextView)findViewById(R.id.text_history1));
        _textViewList.add((TextView)findViewById(R.id.text_history2));
        _textViewList.add((TextView)findViewById(R.id.text_history3));
        _textViewList.add((TextView)findViewById(R.id.text_history4));
        _textViewList.add((TextView)findViewById(R.id.text_history5));
        _textViewList.add((TextView)findViewById(R.id.text_history6));

        for(int i = 0; i< _textViewList.size(); i++){
            _textViewList.get(i).setOnClickListener(this);
        }

        _buttonSimulateLightEvent = findViewById(R.id.button_simulate_light_event);
        _buttonSimulateLightEvent.setOnClickListener(this);
        _buttonSimulateSwingEvent = findViewById(R.id.button_simulate_swing_event);
        _buttonSimulateSwingEvent.setOnClickListener(this);
        _buttonSimulateTemperatureEvent = findViewById(R.id.button_simulate_temperature_event);
        _buttonSimulateTemperatureEvent.setOnClickListener(this);
    }

    /**
     * Display summary contents of event list
     */
    private void displayEventHistory(){

        DateFormat sdf= new SimpleDateFormat("MM/dd");

        //eventListの後ろ(最新)から表示
        for(int i=0; i<6; i++){
            if(_eventLogList.size()-i > 0){
                EventLog eventLog = _eventLogList.get(_eventLogList.size()-i-1);
                _textViewList.get(i).setText(sdf.format(eventLog.getOccurredDate()) + eventLog.getType().getTitle());
            }
        }
    }

    private void displayTime(){

        Timer timer = new Timer(true);

        timer.schedule(  new TimerTask(){
            @Override
            public void run() {
                _handler.post(new Runnable() {
                    public void run() {
                        SimpleDateFormat sdf = new SimpleDateFormat("HH:mm",new Locale("ja", "JP", "JP"));
                        sdf.setTimeZone(TimeZone.getTimeZone("Asia/Tokyo"));
                        _textView_time.setText(sdf.format(new java.util.Date()));
                    }
                });
            }
        }, 0, 1000); //update time per 1000ms(=1s)
   }

    @Override
    public void onClick(View v) {
        //Play sound effect of tapping button
        if(_loadFinished){
            _soundPool.play(_soundTapButtonOrEvent, 1.0f, 1.0f, 0, 0, 1);
        }

        //イベント詳細画面に遷移
        for(int i = 0; i< _textViewList.size(); i++){
            if(v == _textViewList.get(i)){
                EventLog eventLog = _eventLogList.get(_eventLogList.size()-i-1);
                Intent intent = new Intent(this, EventLogDetailActivity.class);
                EventLogUtility.putEventToIntent(intent, eventLog);
                startActivityForResult(intent, 0);
            }
        }

        //各イベント発生を手動で発生
        if(v == _buttonSimulateLightEvent){
            EventLog eventLog = new EventLog();
            eventLog.setType(EventLogType.Light);
            eventLog.setContent("光イベント手動発生");
            eventLog.setOccurredDate(new java.util.Date());
            _eventLogStoreService.insertEvent(eventLog);

            //Post notification on the channel
            NotificationManager manager = (NotificationManager)getSystemService(Context.NOTIFICATION_SERVICE);
            NotificationCompat.Builder builder
                    = new NotificationCompat.Builder(this, getString(R.string.app_name))
                    .setContentTitle(eventLog.getType().getTitle())
                    .setContentText(eventLog.getContent())
                    .setSmallIcon(R.drawable.notification_icon_background);
            NotificationManagerCompat.from(this).notify(1, builder.build());

            SensorManager.fireLighted(false);
        }else if(v == _buttonSimulateSwingEvent){
            EventLog eventLog = new EventLog();
            eventLog.setType(EventLogType.Swing);
            eventLog.setContent("振動イベント手動発生");
            eventLog.setOccurredDate(new java.util.Date());
            _eventLogStoreService.insertEvent(eventLog);

            //Post notification on the channel
            NotificationManager manager = (NotificationManager)getSystemService(Context.NOTIFICATION_SERVICE);
            NotificationCompat.Builder builder
                    = new NotificationCompat.Builder(this, getString(R.string.app_name))
                    .setContentTitle(eventLog.getType().getTitle())
                    .setContentText(eventLog.getContent())
                    .setSmallIcon(R.drawable.notification_icon_background);
            NotificationManagerCompat.from(this).notify(1, builder.build());

            SensorManager.fireSwinged(false);
        }else if(v == _buttonSimulateTemperatureEvent){
            EventLog eventLog = new EventLog();
            eventLog.setType(EventLogType.TemperatureUnusual);
            eventLog.setContent("温度異常イベント手動発生");
            eventLog.setOccurredDate(new java.util.Date());
            _eventLogStoreService.insertEvent(eventLog);

            //Post notification on the channel
            NotificationManager manager = (NotificationManager)getSystemService(Context.NOTIFICATION_SERVICE);
            NotificationCompat.Builder builder
                    = new NotificationCompat.Builder(this, getString(R.string.app_name))
                    .setContentTitle(eventLog.getType().getTitle())
                    .setContentText(eventLog.getContent())
                    .setSmallIcon(R.drawable.notification_icon_background);
            NotificationManagerCompat.from(this).notify(1, builder.build());

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
            EventLog eventLog = new EventLog();
            eventLog.setType(EventLogType.Light);
            eventLog.setContent("端末が光を検知しました");
            eventLog.setOccurredDate(new java.util.Date());

            //イベント対応画面に遷移
            Intent intent = new Intent(this, HandlingRequiredEventActivity.class);
            EventLogUtility.putEventToIntent(intent, eventLog);
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
            EventLog eventLog = new EventLog();
            eventLog.setType(EventLogType.Swing);
            eventLog.setContent("端末が振動を検知しました");
            eventLog.setOccurredDate(new java.util.Date());

            //イベント対応画面に遷移
            Intent intent = new Intent(this, HandlingRequiredEventActivity.class);
            EventLogUtility.putEventToIntent(intent, eventLog);
            startActivityForResult(intent, 0);
        }
    }

    /*
      温度イベント検知時のEventHandlerの仮実装
     */
    @Override
    public void onTemperatureChanged(TemperatureEvent e){
        if(!e.isNormal()){
            //Eventオブジェクトを生成
            EventLog eventLog = new EventLog();
            eventLog.setType(EventLogType.TemperatureUnusual);
            eventLog.setContent("温度異常を検知しました(" + e.getTemperature() + "℃)");
            eventLog.setOccurredDate(new java.util.Date());

            //イベント対応画面に遷移
            Intent intent = new Intent(this, TemperatureEventActivity.class);
            EventLogUtility.putEventToIntent(intent, eventLog);
            startActivityForResult(intent, 0);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent intent) {
        super.onActivityResult(requestCode, resultCode, intent);
        if(intent != null){
            EventLog eventLog = EventLogUtility.getEventFromIntent(intent);

            if(eventLog != null){
                _eventLogStoreService.insertEvent(eventLog);

                //Post notification on the channel
                NotificationManager manager = (NotificationManager)getSystemService(Context.NOTIFICATION_SERVICE);
                NotificationCompat.Builder builder
                        = new NotificationCompat.Builder(this, getString(R.string.app_name))
                        .setContentTitle(eventLog.getType().getTitle())
                        .setContentText(eventLog.getContent())
                        .setSmallIcon(R.drawable.notification_icon_background);
                NotificationManagerCompat.from(this).notify(1, builder.build());

                //イベント履歴を再度取得して表示
                this._eventLogList = _eventLogStoreService.getAllEvent();
                displayEventHistory();
            }
        }
    }
}

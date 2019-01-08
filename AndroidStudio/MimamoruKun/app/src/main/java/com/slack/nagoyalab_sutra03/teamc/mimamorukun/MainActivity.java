package com.slack.nagoyalab_sutra03.teamc.mimamorukun;

import android.Manifest;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.bluetooth.BluetoothDevice;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.media.AudioAttributes;
import android.media.SoundPool;
import android.os.Bundle;
import android.os.IBinder;
import android.os.Handler;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;


import com.slack.nagoyalab_sutra03.teamc.mimamorukun.EventLog.EventLog;
import com.slack.nagoyalab_sutra03.teamc.mimamorukun.EventLog.EventLogStoreService;
import com.slack.nagoyalab_sutra03.teamc.mimamorukun.Sensor.BluetoothDeviceListener;
import com.slack.nagoyalab_sutra03.teamc.mimamorukun.Sensor.LightEvent;
import com.slack.nagoyalab_sutra03.teamc.mimamorukun.Sensor.LightEventListener;
import com.slack.nagoyalab_sutra03.teamc.mimamorukun.Sensor.MeasuredEvent;
import com.slack.nagoyalab_sutra03.teamc.mimamorukun.Sensor.MeasuredEventListener;
import com.slack.nagoyalab_sutra03.teamc.mimamorukun.Sensor.SensorService;
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
import android.support.v7.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity  implements OnClickListener, LightEventListener, SwingEventListener, TemperatureEventListener , MeasuredEventListener {

    //Request Code
    private final int SELECT_DEVICE_REQUEST = 101;
    private final int SETTING_REQUEST = 201;

    //TextView to display current time.
    private TextView _textView_time;

    private TextView _textView_temperature;
    private TextView _textviewConnectionStatusContent;
    private TextView _textviewOpticalValue;
    private TextView _textviewMovementValue;


    //TextViews to display latest event history.
    private List<TextView> _textViewList;

    //Buttons to simulate each events.
    private Button _buttonSimulateLightEvent;
    private Button _buttonSimulateSwingEvent;

    //List of EventLog instance to display.
    private List<EventLog> _eventLogList;

    //Handler to get UI thread.
    private Handler _handler = new Handler();

    //This class level accessible _handler required to get UI thread in timer event _handler.
//    private Handler _handler = new Handler();

    //sound source
    private SoundPool _soundPool;
    private int _soundTapButtonOrEvent;
    private boolean _loadFinished = false;

    Setting _setting;

    EventLogStoreService _eventLogStoreService;
    private ServiceConnection _connectionEventLogStoreService = new ServiceConnection() {
        public void onServiceConnected(ComponentName className, IBinder service) {
            _eventLogStoreService = ((EventLogStoreService.LocalBinder)service).getService();

            //Get all event history.
            _eventLogList = _eventLogStoreService.getAllEvent();

            //Display events.
            displayEventHistory();

            //Display temperature
            displayTemperature();
        }

        public void onServiceDisconnected(ComponentName className) {
            _eventLogStoreService = null;
        }
    };

    SensorService _sensorService;
    private ServiceConnection _connectionSensorService = new ServiceConnection() {
        public void onServiceConnected(ComponentName className, IBinder service) {
            _sensorService = ((SensorService.LocalBinder)service).getService();

            _sensorService.addBluetoothDeviceListener(_bluetoothDeviceListener);

            //Set initinal settings.
            _sensorService.setTemperatureUpperLimit(_setting.getTemperatureUpperLimit());
            _sensorService.setTemperatureLowerLimit(_setting.getTemperatureLowerLimit());
            _sensorService.setInterval(_setting.getMeasurementInterval());

            //Bind sensor events.
            _sensorService.addLightEventListener(MainActivity.this);
            _sensorService.addSwingEventListener(MainActivity.this);
            _sensorService.addTemperatureEventListener(MainActivity.this);
            _sensorService.addMeasuredEventListener(MainActivity.this);
        }

        public void onServiceDisconnected(ComponentName className) {
            //Unbind sensor events.
            _sensorService.removeLightEventListener(MainActivity.this);
            _sensorService.removeSwingEventListener(MainActivity.this);
            _sensorService.removeTemperatureEventListener(MainActivity.this);
            _sensorService.removeMeasuredEventListner(MainActivity.this);

            _sensorService.removeBluetoothDeviceListener(_bluetoothDeviceListener);

            _sensorService = null;
        }
    };

    private BluetoothDeviceListener _bluetoothDeviceListener = new BluetoothDeviceListener() {
        @Override
        public void onFound(BluetoothDevice device) {}

        @Override
        public void onConnected(BluetoothDevice device) {
            _handler.post(new Runnable() {
                @Override
                public void run() {
                    _textviewConnectionStatusContent.setText("接続されました");
                }
            });
        }

        @Override
        public void onDisconnected(BluetoothDevice device) {
            _handler.post(new Runnable() {
                @Override
                public void run() {
                    _textviewConnectionStatusContent.setText("切断されました");
                }
            });
        }

        @Override
        public void onOpticalValueGet(final double value) {
            _handler.post(new Runnable() {
                @Override
                public void run() {
                    _textviewOpticalValue.setText(value + "lux");
                }
            });
        }

        @Override
        public void onMovementValueGet(final double accX, final double accY, final double accZ, final double gyroX, final double gyroY, final double gyroZ, final double magX, final double magY, final double magZ) {
            _handler.post(new Runnable() {
                @Override
                public void run() {
                    _textviewMovementValue.setText(String.format("%.2f,%.2f,%.2f  %.2f,%.2f,%.2f  %.2f,%.2f,%.2f",accX, accY, accZ, gyroX, gyroY, gyroZ, magX, magY, magZ));
                }
            });
        }

        @Override
        public void onTemperatureValueGet(double value) {}
    };

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        //main.xmlの内容を読み込む
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main, menu);
        return true;
    }

    // オプションメニューのアイテムが選択されたときに呼び出されるメソッド
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        Intent intent;
        switch (item.getItemId()) {
            case R.id.menuitem_settings:
                intent = new Intent(this, SettingActivity.class);
                _setting.putToIntent(intent);
                startActivityForResult(intent, SETTING_REQUEST);
                return true;
            case R.id.menuitem_connect:
                intent = new Intent(this, SelectDeviceActivity.class);
                _setting.putToIntent(intent);
                startActivityForResult(intent, SELECT_DEVICE_REQUEST);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // 権限がない場合はリクエスト
        if (checkSelfPermission(Manifest.permission.BLUETOOTH) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{Manifest.permission.BLUETOOTH}, 1);
        }
        if (checkSelfPermission(Manifest.permission.BLUETOOTH_ADMIN) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{Manifest.permission.BLUETOOTH_ADMIN}, 1);
        }
        if (checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{Manifest.permission.ACCESS_COARSE_LOCATION}, 1);
        }

        //Load setting
        _setting = Setting.load();

        //Load sound source
        loadSound();

        //Get UI Instances to handle from code.
        getUIInstances();

        // Bind EventLogStoreService
        Intent i1 = new Intent(getBaseContext(), EventLogStoreService.class);
        bindService(i1, _connectionEventLogStoreService, Context.BIND_AUTO_CREATE);

        // Bind SensorService
        Intent i2 = new Intent(getBaseContext(), SensorService.class);
        bindService(i2, _connectionSensorService, Context.BIND_AUTO_CREATE);

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

        _setting.save();

        unbindService(_connectionEventLogStoreService);
        unbindService(_connectionSensorService);
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
        _textView_temperature = findViewById(R.id.textview_temperature);

        _textviewConnectionStatusContent = findViewById(R.id.textview_connection_status_content);
        _textviewConnectionStatusContent.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, SelectDeviceActivity.class);
                _setting.putToIntent(intent);
                startActivityForResult(intent, SELECT_DEVICE_REQUEST);
            }
        });

        _textviewOpticalValue = findViewById(R.id.textview_optcal_value);
        _textviewMovementValue = findViewById(R.id.textview_movement_value);

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

   public void displayTemperature(){
        if(_sensorService != null)
       _textView_temperature.setText(String.format("%.1f℃", _sensorService.getTemperature()));
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
                eventLog.putToIntent(intent);
                startActivityForResult(intent, 0);
            }
        }

        //各イベント発生を手動で発生
        if(v == _buttonSimulateLightEvent){
            _sensorService.fireLighted(false);
        }else if(v == _buttonSimulateSwingEvent){
            _sensorService.fireSwinged(false);
        }
    }

    /*
      光イベント検知時
     */
    @Override
    public void onLighted(LightEvent e){
        if(!e.isNormal()){
            //イベント対応画面に遷移
            Intent intent = new Intent(this, HandlingRequiredEventActivity.class);
            e.putToIntent(intent);
            _setting.putToIntent(intent);
            startActivityForResult(intent, 0);
        }
    }

    /*
      振動イベント検知時
     */
    @Override
    public void onSwinged(SwingEvent e){
        if(!e.isNormal()){
            //イベント対応画面に遷移
            Intent intent = new Intent(this, HandlingRequiredEventActivity.class);
            e.putToIntent(intent);
            _setting.putToIntent(intent);
            startActivityForResult(intent, 0);
        }
    }

    /*
      温度イベント検知時のEventHandlerの仮実装
     */
    @Override
    public void onTemperatureChanged(TemperatureEvent e){
        if(!e.isNormal()){
            //イベント対応画面に遷移
            Intent intent = new Intent(this, TemperatureEventActivity.class);
            e.putToIntent(intent);
            startActivityForResult(intent, 0);
        }
    }

    @Override
    public void onMeasured(MeasuredEvent e){
        //Update temperature on UI thread.
        _handler.post(new Runnable() {
            @Override
            public void run() {
                displayTemperature();
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent intent) {
        super.onActivityResult(requestCode, resultCode, intent);

        switch(requestCode){
            case  SELECT_DEVICE_REQUEST:
                if(resultCode == RESULT_OK){
                    String deviceAddress = intent.getStringExtra(SelectDeviceActivity.INTENT_KEY_DEVICE_ADDRESS);
                    if(deviceAddress != null){
                        _sensorService.connetAndMeasure(deviceAddress);
                    }
                }
                break;
            case SETTING_REQUEST:
                if(requestCode == RESULT_OK){
                    if(Setting.getFromIntent(intent) != null) {
                        //設定画面で値を変更した場合は、その内容を反映する
                        _setting = Setting.getFromIntent(intent);
                        _sensorService.setTemperatureUpperLimit(_setting.getTemperatureUpperLimit());
                        _sensorService.setTemperatureLowerLimit(_setting.getTemperatureLowerLimit());
                        _sensorService.setInterval(_setting.getMeasurementInterval());
                    }
                }
        }

        //イベント履歴を再度取得して表示
        this._eventLogList = _eventLogStoreService.getAllEvent();
        displayEventHistory();
    }
}

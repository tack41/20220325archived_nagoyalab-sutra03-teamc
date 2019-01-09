package com.slack.nagoyalab_sutra03.teamc.mimamorukun;

import android.Manifest;
import android.bluetooth.BluetoothDevice;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
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
import com.slack.nagoyalab_sutra03.teamc.mimamorukun.EventLog.EventLogType;
import com.slack.nagoyalab_sutra03.teamc.mimamorukun.Sensor.BluetoothDeviceListener;
import com.slack.nagoyalab_sutra03.teamc.mimamorukun.Sensor.SensorService;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;
import java.util.Timer;
import java.util.TimerTask;
import android.support.v7.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity  implements OnClickListener{

    //Request Code
    private final int SELECT_DEVICE_REQUEST = 101;
    private final int SETTING_REQUEST = 201;
    private final int TEMPERATURE_EVENT_REQUEST = 301;
    private final int OPTICAL_EVENT_REQUEST = 401;
    private final int MOVEMENT_EVENT_REQUEST = 501;

    public static final String INTENT_KEY_TEMPERATURE_ENABLE = "MainActivityIntentKeyTemperatureEnable";
    public static final String INTENT_KEY_OPTICAL_ENABLE = "MainActivityIntentKeyOpticalEnable";
    public static final String INTENT_KEY_MOVEMENT_ENABLE = "MainActivityIntentKeyMovementEnable";

    //TextView to display current time.
    private TextView _textView_time;

    private TextView _textView_temperature;
    private TextView _textviewConnectionStatusContent;

    //TextViews to display latest event history.
    private List<TextView> _textViewList;

    //Buttons to simulate each events.
    private Button _buttonSimulateLightEvent;
    private Button _buttonSimulateSwingEvent;

    //List of EventLog instance to display.
    private List<EventLog> _eventLogList;

    //Handler to get UI thread.
    private Handler _handler = new Handler();

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
        }

        public void onServiceDisconnected(ComponentName className) {
            _eventLogStoreService = null;
        }
    };

    SensorService _sensorService;
    private ServiceConnection _connectionSensorService = new ServiceConnection() {
        public void onServiceConnected(ComponentName className, IBinder service) {

            _sensorService = ((SensorService.LocalBinder)service).getService();

            //Bind sensor events.
            _sensorService.addBluetoothDeviceListener(_bluetoothDeviceListener);
        }

        public void onServiceDisconnected(ComponentName className) {

            //Unbind sensor events.
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
        public void onTemperatureValueGet(final double  value) {
            if(_sensorService.isTemperatureEnabled()){
                _handler.post(new Runnable() {
                    @Override
                    public void run() {
                        _textView_temperature.setText(String.format("%.1f℃", value));
                    }
                });

                if(value < _setting.getTemperatureLowerLimit() || _setting.getTemperatureUpperLimit() < value){
                    //画面遷移前に全センサーを無効化(イベントの多重発生を防止)
                    _sensorService.setAllSensorPaused(true);

                    fireTemperatureEvent(value);
                }
            }
        }

        @Override
        public void onOpticalValueGet(final double value) {
            if(_sensorService.isOpticalEnabled()){
                if(value > _setting.getOpticalThreshold()){
                    //画面遷移前に全センサーを無効化(イベントの多重発生を防止)
                    _sensorService.setAllSensorPaused(true);

                    fireOpticalEvent(value);
                }
            }
        }

        @Override
        public void onMovementValueGet(final double accX, final double accY, final double accZ, final double gyroX, final double gyroY, final double gyroZ, final double magX, final double magY, final double magZ) {
            if(_sensorService.isMovementEnabled()){
                double value = Math.sqrt(gyroX*gyroX+gyroY*gyroY+gyroZ*gyroZ);
                if(value > _setting.getMovementThreshold()){
                    //画面遷移前に全センサーを無効化(イベントの多重発生を防止)
                    _sensorService.setAllSensorPaused(true);

                    fireMovementEvent(value);
                }
            }
        }
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
                //画面遷移前に全センサーを無効化(イベントの多重発生を防止)
                _sensorService.setAllSensorPaused(true);

                intent = new Intent(this, SettingActivity.class);
                _setting.putToIntent(intent);
                intent.putExtra(INTENT_KEY_TEMPERATURE_ENABLE, _sensorService.isTemperatureEnabled());
                intent.putExtra(INTENT_KEY_OPTICAL_ENABLE, _sensorService.isOpticalEnabled());
                intent.putExtra(INTENT_KEY_MOVEMENT_ENABLE, _sensorService.isMovementEnabled());
                startActivityForResult(intent, SETTING_REQUEST);

                return true;
            case R.id.menuitem_connect:
                intent = new Intent(this, SelectDeviceActivity.class);
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
        _textviewConnectionStatusContent.setText("接続されていません");

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
            fireOpticalEvent(_setting.getOpticalThreshold() + 1);
        }else if(v == _buttonSimulateSwingEvent){
            fireMovementEvent(_setting.getMovementThreshold() + 1);
        }
    }

    private void fireTemperatureEvent(double value){
        //Save EventLog
        EventLog eventLog = new EventLog();
        eventLog.setType(EventLogType.TemperatureUnusual);
        eventLog.setOccurredDate(new Date());
        eventLog.setContent("温度が閾値:" + _setting.getTemperatureLowerLimit() + "," + _setting.getTemperatureUpperLimit() + "を超えました: " + value);
        _eventLogStoreService.saveEvent(eventLog);

        //イベント対応画面に遷移
        Intent intent = new Intent(this, HandlingRequiredEventActivity.class);
        eventLog.putToIntent(intent);
        _setting.putToIntent(intent);
        startActivityForResult(intent, TEMPERATURE_EVENT_REQUEST);
    }

    private void fireOpticalEvent(double value){
        //Save EventLog
        EventLog eventLog = new EventLog();
        eventLog.setType(EventLogType.Light);
        eventLog.setOccurredDate(new Date());
        eventLog.setContent("照度が閾値:" + _setting.getOpticalThreshold() + "を超えました: " + value);
        _eventLogStoreService.saveEvent(eventLog);

        //イベント対応画面に遷移
        Intent intent = new Intent(this, HandlingRequiredEventActivity.class);
        eventLog.putToIntent(intent);
        _setting.putToIntent(intent);
        startActivityForResult(intent, OPTICAL_EVENT_REQUEST);
    }

    public void fireMovementEvent(double value){
        //Save EventLog
        EventLog eventLog = new EventLog();
        eventLog.setType(EventLogType.Movement);
        eventLog.setOccurredDate(new Date());
        eventLog.setContent("動きが閾値:" + _setting.getMovementThreshold() + "を超えました: " + value);
        _eventLogStoreService.saveEvent(eventLog);

        //イベント対応画面に遷移
        Intent intent = new Intent(this, HandlingRequiredEventActivity.class);
        eventLog.putToIntent(intent);
        _setting.putToIntent(intent);
        startActivityForResult(intent, MOVEMENT_EVENT_REQUEST);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent intent) {
        super.onActivityResult(requestCode, resultCode, intent);

        EventLog eventLog;
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
                if(resultCode == RESULT_OK){
                    //設定画面で値を変更した場合は、その内容を取得する
                    _setting = Setting.getFromIntent(intent);

                    //センサーの有効化状態を取得し、設定する
                    _sensorService.setTemperatureEnabled(intent.getBooleanExtra(INTENT_KEY_TEMPERATURE_ENABLE, true));
                    _sensorService.setOpticalEnabled(intent.getBooleanExtra(INTENT_KEY_OPTICAL_ENABLE, true));
                    _sensorService.setMovementEnabled(intent.getBooleanExtra(INTENT_KEY_MOVEMENT_ENABLE, true));

                    //全センサー無効化を解除
                    _sensorService.setAllSensorPaused(false);
                }
                break;
            case TEMPERATURE_EVENT_REQUEST:
                //温度センサーの再開は利用者の選択に応じて
                if(resultCode == RESULT_OK){
                    //計測を再開
                    _sensorService.setTemperatureEnabled(true);
                }else{
                    //計測は停止したまま
                    _sensorService.setTemperatureEnabled(false);
                }

                //全センサー無効化を解除
                _sensorService.setAllSensorPaused(false);

                //対応内容をイベントログに記録
                eventLog = EventLog.getFromIntent(intent);
                _eventLogStoreService.saveEvent(eventLog);

                break;

            case OPTICAL_EVENT_REQUEST:
                //照度センサーの再開は利用者の選択に応じて
                if(resultCode == RESULT_OK){
                    //計測を再開
                    _sensorService.setOpticalEnabled(true);
                }else{
                    //計測は停止したまま
                    _sensorService.setOpticalEnabled(false);
                }

                //全センサー無効化を解除
                _sensorService.setAllSensorPaused(false);

                //対応内容をイベントログに記録
                eventLog = EventLog.getFromIntent(intent);
                _eventLogStoreService.saveEvent(eventLog);

                break;

            case MOVEMENT_EVENT_REQUEST:
                //温度センサーの再開は利用者の選択に応じて
                if(resultCode == RESULT_OK){
                    //計測を再開
                    _sensorService.setMovementEnabled(true);
                }else{
                    //計測は停止したまま
                    _sensorService.setMovementEnabled(false);
                }

                //全センサー無効化を解除
                _sensorService.setAllSensorPaused(false);

                //対応内容をイベントログに記録
                eventLog = EventLog.getFromIntent(intent);
                _eventLogStoreService.saveEvent(eventLog);

                break;
        }

        //イベント履歴を再度取得して表示
        this._eventLogList = _eventLogStoreService.getAllEvent();
        displayEventHistory();
    }
}

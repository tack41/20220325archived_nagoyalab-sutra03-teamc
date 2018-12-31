package com.slack.nagoyalab_sutra03.teamc.mimamorukun;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.graphics.Color;
import android.media.AudioAttributes;
import android.media.SoundPool;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;
import android.view.Menu;
import android.view.MenuInflater;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;


import com.slack.nagoyalab_sutra03.teamc.mimamorukun.EventLog.EventLog;
import com.slack.nagoyalab_sutra03.teamc.mimamorukun.EventLog.EventLogStoreService;
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
import android.widget.Toast;

import xuzhongwei.gunsecury.common.GattInfo;
import xuzhongwei.gunsecury.controllers.BLEController;
import xuzhongwei.gunsecury.profile.AcceleroteProfile;
import xuzhongwei.gunsecury.profile.AmbientTemperatureProfile;
import xuzhongwei.gunsecury.profile.BarometerProfile;
import xuzhongwei.gunsecury.profile.GenericBleProfile;
import xuzhongwei.gunsecury.profile.HumidityProfile;
import xuzhongwei.gunsecury.profile.IRTTemperature;
import xuzhongwei.gunsecury.profile.LuxometerProfile;
import xuzhongwei.gunsecury.profile.MovementProfile;
import xuzhongwei.gunsecury.service.BluetoothLeService;
import android.Manifest;
import android.content.pm.PackageManager;

public class MainActivity extends AppCompatActivity implements OnClickListener, LightEventListener, SwingEventListener, TemperatureEventListener , MeasuredEventListener {

    private Activity mActivity;

    TextView _textViewOpticalValue;
    TextView _textViewAccelerationValue;
    TextView _textViewGyroValue;

    BLEController mainController;
    private BluetoothDevice mBluetoothDevice = null;
    ArrayList<GenericBleProfile> bleProfiles = new ArrayList<GenericBleProfile>();
    private BluetoothGatt mBtGatt = null;
    public static final String EXTRA_DEVICE = "EXTRA_DEVICE";
    private BroadcastReceiver receiver;
    private BluetoothLeService mBluetoothLeService;

    ArrayList<BluetoothGattCharacteristic> characteristicList = new ArrayList<BluetoothGattCharacteristic>();
    private static final int CHARACTERISTICS_FOUND = 1;
    private static final String CHARACTERISTICS_FOUND_RESULT = "CHARACTERISTICS_FOUND_RESULT";
    private List<GenericBleProfile> mProfiles;
    List<BluetoothGattService> bleServiceList = new ArrayList<BluetoothGattService>();

    private Boolean mIsSensorTag2 = false;
    public boolean isSensorTag2() {
        return mIsSensorTag2;
    }
    protected static MainActivity mThis = null;
    public static MainActivity getInstance() {
        return (MainActivity) mThis;
    }

    private UIHandler mUIHandler = new UIHandler();
    class UIHandler extends Handler {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what){
                case CHARACTERISTICS_FOUND:
                    int res = msg.getData().getInt(CHARACTERISTICS_FOUND_RESULT);
                    showToast(res+"");
                    break;
            }
        }
    }
    private void showToast(String str){
        Toast toast = Toast.makeText(mActivity,str,Toast.LENGTH_LONG);
        toast.show();
    }



    //TextView to display current time.
    private TextView _textView_time;

    private TextView _textView_temperature;

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

            _sensorService = null;
        }
    };

    private ServiceConnection mConnectionBLEService = new ServiceConnection() {
        public void onServiceConnected(ComponentName className, IBinder service) {
            mBluetoothLeService = ((BluetoothLeService.LocalBinder)service).getService();

            initialReceiver();
            onViewInfalted();
        }

        public void onServiceDisconnected(ComponentName className) {
            unregisterReceiver(receiver);
            mBluetoothLeService = null;
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

        Intent intent = null;

        switch (item.getItemId()) {
            case R.id.menuitem_settings:
                intent = new Intent(this, SettingActivity.class);
                _setting.putToIntent(intent);
                startActivityForResult(intent, 0);
                return true;
            case R.id.menuitem_scan_bluetooth:
                intent = new Intent(this, xuzhongwei.gunsecury.DeviceScanActivity.class);
                _setting.putToIntent(intent);
                startActivityForResult(intent, 0);
                return true;
            case R.id.menuitem_help:
                new AlertDialog.Builder(this)
                        .setTitle("使い方")
                        .setMessage("まだ実装してません")
                        .setPositiveButton("OK", null)
                        .show();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mActivity = this;

        if (checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // 権限がない場合はリクエスト
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

        mBluetoothLeService = BluetoothLeService.getInstance();
        Intent intent = getIntent();
        mBluetoothDevice = intent.getParcelableExtra(EXTRA_DEVICE);
        mainController = new BLEController(this);
        mProfiles = new ArrayList<GenericBleProfile>();

        mIsSensorTag2 = false;
        // Determine type of SensorTagGatt
        if(mBluetoothDevice != null){
            String deviceName = mBluetoothDevice.getName();
            if ((deviceName.equals("SensorTag2")) ||(deviceName.equals("CC2650 SensorTag"))) {
                mIsSensorTag2 = true;
            }
        }

        initialReceiver();
        onViewInfalted();
        mThis = this;

    }

    @Override
    protected void onDestroy(){
        super.onDestroy();

        _setting.save();

        unbindService(_connectionEventLogStoreService);
        unbindService(_connectionSensorService);
        if(mConnectionBLEService != null){
            unbindService(mConnectionBLEService);
        }
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

        _textViewOpticalValue = (TextView)findViewById(R.id.textview_optical_value);
        _textViewAccelerationValue = (TextView)findViewById(R.id.textview_acceleration_value);
        _textViewGyroValue = (TextView)findViewById(R.id.textview_gyro_value);
    }

    private void initialReceiver(){

        receiver  = new BroadcastReceiver() {

            List <BluetoothGattService> serviceList;
            List <BluetoothGattCharacteristic> charList = new ArrayList<BluetoothGattCharacteristic>();

            @Override
            public void onReceive(Context context, Intent intent) {

                final String action = intent.getAction();
                final int status = intent.getIntExtra(BluetoothLeService.EXTRA_STATUS,
                        BluetoothGatt.GATT_SUCCESS);

                if(intent.getAction().equals(BluetoothLeService.ACTION_DATA_NOTIFY)){
                    byte[] value = intent.getByteArrayExtra(BluetoothLeService.EXTRA_DATA);
                    String uuidStr = intent.getStringExtra(BluetoothLeService.EXTRA_UUID);

                    for(int i=0;i<characteristicList.size();i++){
                        BluetoothGattCharacteristic bleCharacteristic = characteristicList.get(i);
                        if(bleCharacteristic.getUuid().toString().equals(uuidStr)){
                            for(int j=0;j<mProfiles.size();j++){
                                if(mProfiles.get(j).checkNormalData(uuidStr)){
                                    mProfiles.get(j).updateData(value);
                                }
                            }
                        }
                    }
                }else if(intent.getAction().equals(BluetoothLeService.ACTION_DATA_READ)){
                    // Data read
                    byte[] value = intent.getByteArrayExtra(BluetoothLeService.EXTRA_DATA);
                    String uuidStr = intent.getStringExtra(BluetoothLeService.EXTRA_UUID);
                    for (int ii = 0; ii < charList.size(); ii++) {
                        BluetoothGattCharacteristic tempC = charList.get(ii);
                        if ((tempC.getUuid().toString().equals(uuidStr))) {
                            for (int jj = 0; jj < mProfiles.size(); jj++) {
                                GenericBleProfile p = mProfiles.get(jj);
                                p.didReadValueForCharacteristic(tempC);
                            }
                            //Log.d("DeviceActivity","Got Characteristic : " + tempC.getUuid().toString());
                            break;
                        }
                    }
                }else if(intent.getAction().equals(BluetoothLeService.ACTION_DATA_WRITE)){

                    byte[] value = intent.getByteArrayExtra(BluetoothLeService.EXTRA_DATA);
                    String uuidStr = intent.getStringExtra(BluetoothLeService.EXTRA_UUID);
                    for (int ii = 0; ii < charList.size(); ii++) {
                        BluetoothGattCharacteristic tempC = charList.get(ii);
                        if ((tempC.getUuid().toString().equals(uuidStr))) {
                            for (int jj = 0; jj < mProfiles.size(); jj++) {
                                GenericBleProfile p = mProfiles.get(jj);
                                p.didWriteValueForCharacteristic(tempC);
                            }
                            //Log.d("DeviceActivity","Got Characteristic : " + tempC.getUuid().toString());
                            break;
                        }
                    }

                }else if(intent.getAction().equals(BluetoothLeService.ACTION_GATT_SERVICES_DISCOVERED)){

                    if (status == BluetoothGatt.GATT_SUCCESS) {
                        bleServiceList = mBluetoothLeService.getBLEService();
                        if(bleServiceList.size() > 0){
                            for(int i=0;i<bleServiceList.size();i++){
                                List<BluetoothGattCharacteristic> characteristics = bleServiceList.get(i).getCharacteristics();
                                if(characteristics.size() > 0){
                                    for(int j=0;j<characteristics.size();j++){
                                        characteristicList.add(characteristics.get(j));
                                    }
                                }
                            }
                        }


                        serviceList = mBluetoothLeService.getSupportedGattServices();
                        if (serviceList.size() > 0) {
                            for (int ii = 0; ii < serviceList.size(); ii++) {
                                BluetoothGattService s = serviceList.get(ii);
                                List<BluetoothGattCharacteristic> c = s.getCharacteristics();
                                if (c.size() > 0) {
                                    for (int jj = 0; jj < c.size(); jj++) {
                                        charList.add(c.get(jj));
                                    }
                                }
                            }
                        }

                        Thread work = new Thread(new Runnable() {
                            @Override
                            public void run() {

                                //Iterate through the services and add GenericBluetoothServices for each service
                                int nrNotificationsOn = 0;
                                int maxNotifications;
                                int servicesDiscovered = 0;
                                int totalCharacteristics = 0;
                                //serviceList = mBtLeService.getSupportedGattServices();
                                for (BluetoothGattService s : serviceList) {
                                    List<BluetoothGattCharacteristic> chars = s.getCharacteristics();
                                    totalCharacteristics += chars.size();
                                }
                                //Special
                                if (totalCharacteristics == 0) {
                                    //Something bad happened, we have a problem
                                    runOnUiThread(new Runnable() {
                                        @Override
                                        public void run() {
                                            Toast toast = Toast.makeText(getApplicationContext(),"Service discovered but not characteristics has been found",Toast.LENGTH_SHORT);
                                            toast.show();
                                        }
                                    });
                                    return;
                                }

                                final int final_totalCharacteristics = totalCharacteristics;
                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        Toast toast = Toast.makeText(getApplicationContext(),"Found a total of " + serviceList.size() + " services with a total of " + final_totalCharacteristics + " characteristics on this device",Toast.LENGTH_SHORT );
                                        toast.show();
                                    }
                                });

                                if (Build.VERSION.SDK_INT > 18) maxNotifications = 7;
                                else {
                                    maxNotifications = 4;
                                    runOnUiThread(new Runnable() {
                                        @Override
                                        public void run() {
                                            Toast.makeText(getApplicationContext(), "Android version 4.3 detected, max 4 notifications enabled", Toast.LENGTH_LONG).show();
                                        }
                                    });
                                }
                                for (int ii = 0; ii < serviceList.size(); ii++) {
                                    BluetoothGattService s = serviceList.get(ii);
                                    List<BluetoothGattCharacteristic> chars = s.getCharacteristics();
                                    if (chars.size() == 0) {

                                        Log.d("DeviceActivity", "No characteristics found for this service !!!");

                                    }
                                    servicesDiscovered++;
                                    final float serviceDiscoveredcalc = (float)servicesDiscovered;
                                    final float serviceTotalcalc = (float)serviceList.size();
/*                                    runOnUiThread(new Runnable() {
                                        @Override
                                        public void run() {
                                            progressDialog.setProgress((int)((serviceDiscoveredcalc / (serviceTotalcalc - 1)) * 100));
                                        }
                                    });*/
                                    Log.d("DeviceActivity", "Configuring service with uuid : " + s.getUuid().toString());

                                    if (LuxometerProfile.isCorrectService(s)) {
                                        LuxometerProfile lux = new LuxometerProfile(mBluetoothLeService,s,mBluetoothDevice);
                                        mProfiles.add(lux);
                                        if (nrNotificationsOn < maxNotifications) {
                                            lux.configureService();
                                            nrNotificationsOn++;
                                        }
                                        lux.setmOnDataChangedListener(new GenericBleProfile.OnDataChangedListener() {
                                            @Override
                                            public void onDataChanged(String data) {
                                                final String finalData = data;
                                                Log.d("DeviceNotify", "Luxometer:" + data);
                                                _handler.post(new Runnable() {
                                                    @Override
                                                    public void run() {
                                                        _textViewOpticalValue.setText(finalData);
                                                    }
                                                });
//                                                ((TextView) mActivity.findViewById(com.slack.nagoyalab_sutra03.teamc.mimamorukun.R.id.luxometerValue)).setText(data);
                                            }
                                        });
                                    }


                                    if (HumidityProfile.isCorrectService(s)) {
                                        HumidityProfile hum = new HumidityProfile(mBluetoothLeService,s,mBluetoothDevice);
//                                                    hum.setmOnDataChangedListener(new GenericBleProfile.OnDataChangedListener() {
//                                                        @Override
//                                                        public void onDataChanged(String data) {
//                                                            ((TextView) mActivity.findViewById(R.id.humidityValue)).setText(data);
//                                                        }
//                                                    });

                                        hum.setmOnHumidityListener(new HumidityProfile.OnHumidityListener() {
                                            @Override
                                            public void onHumidityChanged(double data) {
                                                Log.d("DeviceNotify", "Humidity:" + data);
//ß                                                ((TextView) mActivity.findViewById(com.slack.nagoyalab_sutra03.teamc.mimamorukun.R.id.humidityValue)).setText(data+"");
                                            }
                                        });

                                        mProfiles.add(hum);
                                        if (nrNotificationsOn < maxNotifications) {
                                            hum.configureService();
                                            nrNotificationsOn++;
                                        }
                                        Log.d("DeviceActivity","Found Humidity !");
                                    }


                                    if (MovementProfile.isCorrectService(s)) {
                                        MovementProfile mov = new MovementProfile(mBluetoothLeService,s,mBluetoothDevice);
//                                                    mov.setmOnDataChangedListener(new GenericBleProfile.OnDataChangedListener() {
//                                                        @Override
//                                                        public void onDataChanged(String data) {
//                                                            ((TextView) mActivity.findViewById(R.id.movementValue)).setText(data);
//                                                        }
//                                                    });

                                        mov.setmOnMovementListener(new MovementProfile.OnMovementListener() {
                                            @Override
                                            public void onMovementACCChanged(double x, double y, double z) {
  /*                                              ((TextView) mActivity.findViewById(com.slack.nagoyalab_sutra03.teamc.mimamorukun.R.id.movementValue1X)).setText(x+"");
                                                ((TextView) mActivity.findViewById(com.slack.nagoyalab_sutra03.teamc.mimamorukun.R.id.movementValue1Y)).setText(y+"");
                                                ((TextView) mActivity.findViewById(com.slack.nagoyalab_sutra03.teamc.mimamorukun.R.id.movementValue1Z)).setText(z+"");*/
                                                Log.d("DeviceNotify", "Move_ACC:" + x + "," + y + "," + z);
                                            }

                                            @Override
                                            public void onMovementGYROChanged(double x, double y, double z) {
/*                                                ((TextView) mActivity.findViewById(com.slack.nagoyalab_sutra03.teamc.mimamorukun.R.id.movementValue2X)).setText(x+"");
                                                ((TextView) mActivity.findViewById(com.slack.nagoyalab_sutra03.teamc.mimamorukun.R.id.movementValue2Y)).setText(y+"");
                                                ((TextView) mActivity.findViewById(com.slack.nagoyalab_sutra03.teamc.mimamorukun.R.id.movementValue2Z)).setText(z+"");*/
                                                _textViewGyroValue.setText("ジャイロ: x=" + x + ", y=" + y + ", z=" + z);
                                                Log.d("DeviceNotify", "Move_GYRO:" + x + "," + y + "," + z);
                                            }

                                            @Override
                                            public void onMovementMAGChanged(double x, double y, double z) {
/*                                                ((TextView) mActivity.findViewById(com.slack.nagoyalab_sutra03.teamc.mimamorukun.R.id.movementValue3X)).setText(x+"");
                                                ((TextView) mActivity.findViewById(com.slack.nagoyalab_sutra03.teamc.mimamorukun.R.id.movementValue3Y)).setText(y+"");
                                                ((TextView) mActivity.findViewById(com.slack.nagoyalab_sutra03.teamc.mimamorukun.R.id.movementValue3Z)).setText(z+"");*/
                                                Log.d("DeviceNotify", "Move_MAG:" + x + "," + y + "," + z);
                                            }
                                        });
                                        mProfiles.add(mov);
                                        if (nrNotificationsOn < maxNotifications) {
                                            mov.configureService();
                                            nrNotificationsOn++;
                                        }
                                        Log.d("DeviceActivity","Found Motion !");
                                    }


                                    if (AcceleroteProfile.isCorrectService(s)) {
                                        AcceleroteProfile acc = new AcceleroteProfile(mBluetoothLeService,s,mBluetoothDevice);
                                        acc.setmOnDataChangedListener(new GenericBleProfile.OnDataChangedListener() {
                                            @Override
                                            public void onDataChanged(String data) {
//                                                ((TextView) mActivity.findViewById(com.slack.nagoyalab_sutra03.teamc.mimamorukun.R.id.acceleroterValue)).setText(data);
                                                final String finalData = data;
                                                _handler.post(new Runnable() {
                                                    @Override
                                                    public void run() {
                                                        _textViewAccelerationValue.setText("加速度:" + finalData);
                                                    }
                                                });
                                                Log.d("DeviceNotify", "Accele:" + data);
                                           }
                                        });
                                        mProfiles.add(acc);
                                        if (nrNotificationsOn < maxNotifications) {
                                            acc.configureService();
                                            nrNotificationsOn++;
                                        }
                                        Log.d("DeviceActivity","Found Motion !");
                                    }

                                    if (IRTTemperature.isCorrectService(s)) {
                                        IRTTemperature irTemp = new IRTTemperature(mBluetoothLeService,s,mBluetoothDevice);
                                        irTemp.setmOnDataChangedListener(new GenericBleProfile.OnDataChangedListener() {
                                            @Override
                                            public void onDataChanged(String data) {
//                                                ((TextView) mActivity.findViewById(com.slack.nagoyalab_sutra03.teamc.mimamorukun.R.id.irTempratureValue)).setText(data);
                                                Log.d("DeviceNotify", "IRTTemperature:" + data);
                                            }
                                        });
                                        mProfiles.add(irTemp);
                                        if (nrNotificationsOn < maxNotifications) {
                                            irTemp.configureService();
                                        }
                                        //No notifications add here because it is already enabled above ..
                                        Log.d("DeviceActivity","Found IR Temperature !");
                                    }

                                    if (BarometerProfile.isCorrectService(s)) {
                                        BarometerProfile bar = new BarometerProfile(mBluetoothLeService,s,mBluetoothDevice);
                                        bar.setmOnDataChangedListener(new GenericBleProfile.OnDataChangedListener() {
                                            @Override
                                            public void onDataChanged(String data) {
//                                                ((TextView) mActivity.findViewById(com.slack.nagoyalab_sutra03.teamc.mimamorukun.R.id.barometerValue)).setText(data);
                                                Log.d("DeviceNotify", "Barometer:" + data);
                                            }
                                        });
                                        mProfiles.add(bar);
                                        if (nrNotificationsOn < maxNotifications) {
                                            bar.configureService();
                                        }
                                        //No notifications add here because it is already enabled above ..
                                        Log.d("DeviceActivity","Found IR Temperature !");
                                    }

                                    if (AmbientTemperatureProfile.isCorrectService(s)) {
                                        AmbientTemperatureProfile ambient = new AmbientTemperatureProfile(mBluetoothLeService,s,mBluetoothDevice);
                                        ambient.setmOnDataChangedListener(new GenericBleProfile.OnDataChangedListener() {
                                            @Override
                                            public void onDataChanged(String data) {
//                                                ((TextView) mActivity.findViewById(com.slack.nagoyalab_sutra03.teamc.mimamorukun.R.id.ambientTempratureValue)).setText(data);
                                                Log.d("DeviceNotify", "AmbientTemperature" + data);
                                            }
                                        });
                                        mProfiles.add(ambient);
                                        if (nrNotificationsOn < maxNotifications) {
                                            ambient.configureService();
                                            nrNotificationsOn++;
                                        }
                                        Log.d("DeviceActivity","Found Ambient Temperature !");
                                    }


                                }


/*                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        progressDialog.setTitle("Enabling Services");
                                        progressDialog.setMax(mProfiles.size());
                                        progressDialog.setProgress(0);
                                    }
                                });*/

                                for (final GenericBleProfile p : mProfiles) {
                                    if(p != null){
                                        runOnUiThread(new Runnable() {
                                            @Override
                                            public void run() {
                                                p.enableService();
//                                            progressDialog.setProgress(progressDialog.getProgress() + 1);
                                            }
                                        });
                                    }
//                                                p.onResume();
                                }



/*                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        progressDialog.hide();
                                        progressDialog.dismiss();
                                    }
                                });*/



                            }
                        });
                        work.start();




                        Message msg = new Message();
                        msg.what = CHARACTERISTICS_FOUND;
                        Bundle bundle = new Bundle();
                        bundle.putInt(CHARACTERISTICS_FOUND_RESULT,characteristicList.size());
                        msg.setData(bundle);
                        mUIHandler.sendMessage(msg);


                    }else{
                        Toast toast = Toast.makeText(getApplicationContext(),"not success get services",Toast.LENGTH_SHORT);
                        toast.show();
                    }



                }else if(intent.getAction().equals(BluetoothLeService.ACTION_DATA_NOTIFY)){
                    byte[] value = intent.getByteArrayExtra(BluetoothLeService.EXTRA_DATA);
                    String uuidStr = intent.getStringExtra(BluetoothLeService.EXTRA_UUID);



                    for(int i=0;i<characteristicList.size();i++){
                        BluetoothGattCharacteristic bleCharacteristic = characteristicList.get(i);
                        if(bleCharacteristic.getUuid().toString().equals(uuidStr)){
                            for(int j=0;j<bleProfiles.size();j++){
                                if(bleProfiles.get(j).checkNormalData(uuidStr)){
                                    bleProfiles.get(j).updateData(value);
                                }
                            }
                        }

                    }

                }else{

                }


            }
        };

        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(BluetoothLeService.FIND_NEW_BLE_DEVICE);
        intentFilter.addAction(BluetoothLeService.ACTION_GATT_SERVICES_DISCOVERED);
        intentFilter.addAction(BluetoothLeService.ACTION_DATA_NOTIFY);
        registerReceiver(receiver,intentFilter);
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

        //設定画面で値を変更した場合は、その内容を反映する
        if(intent != null && Setting.getFromIntent(intent) != null){
            _setting = Setting.getFromIntent(intent);
            _sensorService.setTemperatureUpperLimit(_setting.getTemperatureUpperLimit());
            _sensorService.setTemperatureLowerLimit(_setting.getTemperatureLowerLimit());
            _sensorService.setInterval(_setting.getMeasurementInterval());
        }

        //イベント履歴を再度取得して表示
        this._eventLogList = _eventLogStoreService.getAllEvent();
        displayEventHistory();


        mBluetoothDevice = intent.getParcelableExtra(EXTRA_DEVICE);
        if(intent == null){
            Toast.makeText(this, "nullllllllllllllllllllllllllllllllllll" +
                    "", Toast.LENGTH_LONG);
        }

        if(mBluetoothDevice != null){
            Toast.makeText(this , "BLE機器取得:" + mBluetoothDevice.toString(), Toast.LENGTH_LONG).show();

            mBluetoothLeService = BluetoothLeService.getInstance();
            mainController = new BLEController(this);
            mProfiles = new ArrayList<GenericBleProfile>();

            mIsSensorTag2 = false;
            // Determine type of SensorTagGatt
            String deviceName = mBluetoothDevice.getName();
            if ((deviceName.equals("SensorTag2")) ||(deviceName.equals("CC2650 SensorTag"))) {
                mIsSensorTag2 = true;
            }



            // Bind BLEService
            Intent i1 = new Intent(getBaseContext(), BluetoothLeService.class);
            bindService(i1, mConnectionBLEService, Context.BIND_AUTO_CREATE);
        }
    }

    private void onViewInfalted(){
        mBtGatt = BluetoothLeService.getBtGatt();

        if(mBtGatt != null){
            if (mBtGatt.discoverServices()) {
                boolean succuess = true;
            } else {
                boolean succuess = false;
            }
        }
    }
}

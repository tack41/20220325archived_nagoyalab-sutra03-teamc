package com.slack.nagoyalab_sutra03.teamc.mimamorukun.Sensor;

import android.Manifest;
import android.app.NotificationManager;
import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothManager;
import android.bluetooth.le.BluetoothLeScanner;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.os.Binder;
import android.os.IBinder;
import java.util.Timer;
import java.util.TimerTask;

import com.slack.nagoyalab_sutra03.teamc.mimamorukun.EventLog.EventLogStoreService;
import com.slack.nagoyalab_sutra03.teamc.mimamorukun.MyApplication;
import com.slack.nagoyalab_sutra03.teamc.mimamorukun.R;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class SensorService extends Service {
    private final IBinder _binder = new SensorService.LocalBinder();

    public class LocalBinder extends Binder {
        public SensorService getService() {
            return SensorService.this;
        }
    }

    //Interval to get sensor values(seconds)
    private int _interval = 10;
    public int getInterval(){ return _interval;}
    public void setInterval(int interval){
        _interval = interval;
        stopTimer();
        startTimer();
    }

    private void startTimer(){
        //Initial measuring
        measure();

        _timer = new Timer(true);
        _timer.schedule(new TimerTask() {
            @Override
            public void run() {
                measure();
            }
        }, 0, _interval*1000);
    }

    public void stopTimer(){
        if(_timer != null){
            _timer.cancel();
            _timer = null;
        }
    }

    //Sensor values
    private float _temperature = 20;
    public float getTemperature(){return _temperature;}
    // and more ....

    //Threshold
    private float _temperatureLowerLimit = 10.0f;
    public float getTemperatureMin(){ return _temperatureLowerLimit; }
    public void setTemperatureLowerLimit(float temperatureLowerLimit){_temperatureLowerLimit = temperatureLowerLimit;}
    private float _temperatureUpperLimit = 30.0f;
    public float getTemperatureMax() { return _temperatureUpperLimit; }
    public void setTemperatureUpperLimit(float temperatureUpperLimit){_temperatureUpperLimit = temperatureUpperLimit;}

    //前回計測値が正常範囲かどうか
    private boolean _lastLightNormal = true;
    private boolean _lastSwingNormal = true;
    private boolean _lastTemperatureNormal = true;

    //計測値が正常範囲か判定する
    private boolean isLightNormal(){
        // blank
        return true;
    }
    private boolean isSwingNormal(){
        // blank
        return true;
    }
    private boolean isTemperatureNormal(){
        return _temperatureLowerLimit <= _temperature && _temperature <= _temperatureUpperLimit;
    }

    private List<LightEventListener> _lightEventList = new ArrayList<>();
    private List<SwingEventListener> _swingEventList = new ArrayList<>();
    private List<TemperatureEventListener> _temperatureEventList = new ArrayList<>();
    private List<MeasuredEventListener> _measuredEventList = new ArrayList<>();

    //Timer
    Timer _timer;

    BluetoothManager _bluetoothManager;
    BluetoothAdapter _bluetoothAdapter;
    BluetoothLeScanner _bleScanner;
    BluetoothGatt _bluetoothGatt;
    boolean _bleDeviceConnected = false;

    EventLogStoreService _eventLogStoreService;
    // Serviceとのインターフェースクラス
    private ServiceConnection mConnection = new ServiceConnection() {
        public void onServiceConnected(ComponentName className, IBinder service) {
            // Serviceとの接続確立時に呼び出される。
            // service引数には、Onbind()で返却したBinderが渡される
            _eventLogStoreService = ((EventLogStoreService.LocalBinder)service).getService();
        }

        public void onServiceDisconnected(ComponentName className) {
            // Serviceとの切断時に呼び出される。
            _eventLogStoreService = null;
        }
    };

    @Override
    public IBinder onBind(Intent intent) {
        // Bind EventLogStoreService
        Intent i = new Intent(getBaseContext(), EventLogStoreService.class);
        bindService(i, mConnection, Context.BIND_AUTO_CREATE);

        _bluetoothManager = (BluetoothManager) getSystemService(BLUETOOTH_SERVICE);
        _bluetoothAdapter = _bluetoothManager.getAdapter();
        _bleScanner = _bluetoothAdapter.getBluetoothLeScanner();

        // Start measuring
        stopTimer();
        startTimer();

        return _binder;
    }

    @Override
    public boolean onUnbind(Intent intent) {
        _lightEventList.clear();
        _swingEventList.clear();
        _temperatureEventList.clear();

        // Unbind EventLogStoreService
        unbindService(mConnection);

        // Stop measuring
        stopTimer();

        return true;
    }

    public void addLightEventListener(LightEventListener listener){
        _lightEventList.add(listener);
    }

    public void removeLightEventListener(LightEventListener listener){
        _lightEventList.remove(listener);
    }

    public void addSwingEventListener(SwingEventListener listener){
        _swingEventList.add(listener);
    }

    public void removeSwingEventListener(SwingEventListener listener){
        _swingEventList.remove(listener);
    }

    public void addTemperatureEventListener(TemperatureEventListener listener){
        _temperatureEventList.add(listener);
    }

    public void removeTemperatureEventListener(TemperatureEventListener listener){
        _temperatureEventList.remove(listener);
    }

    public void addMeasuredEventListener(MeasuredEventListener listener){
        _measuredEventList.add(listener);
    }

    public void removeMeasuredEventListner(MeasuredEventListener listener){
        _measuredEventList.remove(listener);
    }

    /**
     * 計測を実施する。
     */
    private void measure(){
        //Get value from sensor
        // _temperature = sensor.getTemperature();
        _temperature = (float)(25 + 7*Math.random());

        //計測終了イベント発生
        fireMeasured(_temperature);

        //前回閾値内で今回で閾値を越えた場合はイベント発生
        if(_lastLightNormal && !isLightNormal()){
            fireLighted(false);
        }
        if(_lastSwingNormal && !isSwingNormal()){
            fireSwinged(false);
        }
        if(_lastTemperatureNormal && !isTemperatureNormal()){
            fireTemperatured(false, _temperature);
        }

        //前回閾値を越え、今回閾値内の場合もイベント発生
        if(!_lastLightNormal && isLightNormal()){
            fireLighted(true);
        }
        if(!_lastSwingNormal && isSwingNormal()){
            fireSwinged(true);
        }
        if(!_lastTemperatureNormal && isTemperatureNormal()){
            fireTemperatured(true, _temperature);
        }

        //前回の状態を更新
        _lastLightNormal = isLightNormal();
        _lastSwingNormal = isSwingNormal();
        _lastTemperatureNormal = isTemperatureNormal();
    }

    // 以下、各イベントを手動発生させる。
    // 最終的にセンサー値から内部発生できるようになったらprivateにする

    // 光イベント
    public void fireLighted(boolean isNormal){
        //Create Event object.
        LightEvent event = new LightEvent(isNormal, new Date(),
                isNormal ? "光が閾値以下となったことを検知" : "光が閾値以上となったことを検知");

        //Save EventLog
        _eventLogStoreService.saveEvent(event);

        //Do all registered callback
        for(LightEventListener listner : _lightEventList){
            listner.onLighted(event);
        }
    }

    // 振動イベント
    public void fireSwinged(boolean isNormal){
        //Create Event object
        SwingEvent event = new SwingEvent(isNormal, new Date(),
                isNormal ? "振動が閾値以下となったことを検知" : "振動が閾値以上となったことを検知");

        //Save EventLog
        _eventLogStoreService.saveEvent(event);

        //Do all registered callback
        for(SwingEventListener listner : _swingEventList){
            listner.onSwinged(event);
        }
    }

    // 温度イベント
    public void fireTemperatured(boolean isNormal, float temperature) {
        //Create Event object
        TemperatureEvent event = new TemperatureEvent(isNormal, new Date(),
                temperature,
                isNormal ? "温度が閾値範囲となったことを検知" : "温度が閾値範囲外となったことを検知");

        //Save EventLog
        if(_eventLogStoreService != null){
            _eventLogStoreService.saveEvent(event);
        }

        //Do all registered callback
        for (TemperatureEventListener listner : _temperatureEventList) {
            listner.onTemperatureChanged(event);
        }
    }

    //計測イベント
    public void fireMeasured(double temperature){
        //Create Event object
        MeasuredEvent event = new MeasuredEvent(new Date(), temperature);

        //Do all registered callback
        for (MeasuredEventListener listner : _measuredEventList) {
            listner.onMeasured(event);
        }
    }
}

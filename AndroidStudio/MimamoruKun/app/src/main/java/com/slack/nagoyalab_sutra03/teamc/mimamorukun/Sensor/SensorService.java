package com.slack.nagoyalab_sutra03.teamc.mimamorukun.Sensor;

import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothManager;
import android.bluetooth.BluetoothProfile;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanResult;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Binder;
import android.os.IBinder;

import java.nio.ByteBuffer;
import java.util.Timer;
import java.util.TimerTask;

import com.slack.nagoyalab_sutra03.teamc.mimamorukun.EventLog.EventLogStoreService;
import com.slack.nagoyalab_sutra03.teamc.mimamorukun.MyApplication;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;

import android.os.Handler;

import static java.lang.Math.pow;

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
    private List<BluetoothDeviceListener> _bluetoothDeviceEventList = new ArrayList<>();

    //Timer
    Timer _timer;

    BluetoothManager _bluetoothManager;
    BluetoothAdapter _bluetoothAdapter;
    BluetoothLeScanner _bleScanner;
    BluetoothGatt _bluetoothGatt;
    boolean _bleScanning = false;
    String _bleDeviceAddress = null;

    boolean _bleDeviceConnected = false;
    public boolean connected(){
        return _bleDeviceConnected;
    }

    //Bluetooth scanのタイムアウト時間(ms)
    final int SCAN_PERIOD = 10000;

    final UUID UUID_CLIENT_CHARACTERISTIC_CONFIG = UUID.fromString("00002902-0000-1000-8000-00805f9b34fb");

    final UUID UUID_OPT_SERV = UUID.fromString("f000aa70-0451-4000-b000-000000000000");
    final UUID UUID_OPT_DATA = UUID.fromString("f000aa71-0451-4000-b000-000000000000");
    final UUID UUID_OPT_CONF = UUID.fromString("f000aa72-0451-4000-b000-000000000000"); // 0: disable, 1: enable
    final UUID UUID_OPT_PERI = UUID.fromString("f000aa73-0451-4000-b000-000000000000"); // Period in tens of milliseconds

    final UUID UUID_MOV_SERV = UUID.fromString("f000aa80-0451-4000-b000-000000000000");
    final UUID UUID_MOV_DATA = UUID.fromString("f000aa81-0451-4000-b000-000000000000");
    final UUID UUID_MOV_CONF = UUID.fromString("f000aa82-0451-4000-b000-000000000000"); // 0: disable, bit 0: enable x, bit 1: enable y, bit 2: enable z
    final UUID UUID_MOV_PERI = UUID.fromString("f000aa83-0451-4000-b000-000000000000"); // Period in tens of milliseconds

    Handler _handler;

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
    public void onCreate(){
        // Bind EventLogStoreService
        Intent i = new Intent(getBaseContext(), EventLogStoreService.class);
        bindService(i, mConnection, Context.BIND_AUTO_CREATE);

        _bluetoothManager = (BluetoothManager) getSystemService(BLUETOOTH_SERVICE);
        _bluetoothAdapter = _bluetoothManager.getAdapter();
        _bleScanner = _bluetoothAdapter.getBluetoothLeScanner();

        _handler = new Handler();
    }

    @Override
    public void onDestroy(){
        _lightEventList.clear();
        _swingEventList.clear();
        _temperatureEventList.clear();

        // Unbind EventLogStoreService
        unbindService(mConnection);

        _bleScanner = null;
        _bluetoothAdapter = null;
        _bluetoothManager = null;
    }

    @Override
    public IBinder onBind(Intent intent) {
        return _binder;
    }

    @Override
    public boolean onUnbind(Intent intent) {
        return true;
    }

    /**
     * Scanのみ行う
     */
    public void startScanOnly(){
        _bleDeviceAddress = null;
        startScan();
    }

    /**
     * 指定されたアドレスの機器に接続して計測を行う
     * @param deviceAddress
     */
    public void connetAndMeasure(String deviceAddress){
        _bleDeviceAddress = deviceAddress;
        startScan();
    }
    private void startScan() {
        if(_bleScanning) return; //既にscan中の場合は何もしない

        //SCAN_PERIODで指定した時間が過ぎたらscanを停止する
        _handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                _bleScanner.stopScan(_scanCallback);
                _bleScanning = false;
            }
        }, SCAN_PERIOD);

        _bleScanner.startScan(_scanCallback);
        _bleScanning = true;
    }

    public void stopScan(){
        if(!_bleScanning){
            _bleScanner.stopScan(_scanCallback);
            _bleScanning = false;
        }
    }

    private ScanCallback _scanCallback = new ScanCallback() {
        @Override
        public void onScanResult(int callbackType, ScanResult result) {
            super.onScanResult(callbackType, result);

            if (result != null && result.getDevice() != null) {
                for(BluetoothDeviceListener listner: _bluetoothDeviceEventList ){
                    listner.onFound(result.getDevice());
                }

                // デバイスが見つかった！
                if(result.getDevice().getAddress().equals(_bleDeviceAddress)){
                    stopScan();

                    _bluetoothGatt = result.getDevice().connectGatt(MyApplication.getInstance(), false, _gattCallback);
                }
            }
        }
    };

    private BluetoothGattCallback _gattCallback = new BluetoothGattCallback() {
        @Override
        public void onConnectionStateChange(BluetoothGatt gatt, int status,
                                            int newState) {
            super.onConnectionStateChange(gatt, status, newState);

            // 接続成功
            if (newState == BluetoothProfile.STATE_CONNECTED) {
                _bleDeviceConnected = true;
                for(BluetoothDeviceListener listner: _bluetoothDeviceEventList ){
                    listner.onConnected(gatt.getDevice());
                }

                gatt.discoverServices();

            } else if (newState == BluetoothProfile.STATE_DISCONNECTED) {
                _bleDeviceConnected = false;
                for(BluetoothDeviceListener listner: _bluetoothDeviceEventList ){
                    listner.onDisconnected(gatt.getDevice());
                }
            }

        }

        @Override
        public void onServicesDiscovered(BluetoothGatt gatt, int status) {
            super.onServicesDiscovered(gatt, status);

            if (status == BluetoothGatt.GATT_SUCCESS) {
                // Enable optical sensor
                BluetoothGattService opticalService = gatt.getService(UUID_OPT_SERV);
                if (opticalService != null) {
                    BluetoothGattCharacteristic opticalConfChar = opticalService.getCharacteristic(UUID_OPT_CONF);
                    if(opticalConfChar != null){
                        byte[] val = new byte[]{(byte)0x01};
                        opticalConfChar.setValue(val);
                        gatt.writeCharacteristic(opticalConfChar);
                    }
                }
            }
        }
        @Override
        public void onDescriptorWrite(BluetoothGatt gatt, BluetoothGattDescriptor descriptor, int status){
            if(descriptor.getCharacteristic().getUuid().equals(UUID_OPT_DATA)){
                // Optical value notification enabled

                // Enable Movement service
                BluetoothGattService movingService = gatt.getService(UUID_MOV_SERV);
                if (movingService != null) {
                    BluetoothGattCharacteristic movingConfChar = movingService.getCharacteristic(UUID_MOV_CONF);
                    if(movingConfChar != null){
                        byte[] val = new byte[]{(byte)0x7f,(byte)0xff};
                        movingConfChar.setValue(val);
                        gatt.writeCharacteristic(movingConfChar);
                    }
                }
            }else if(descriptor.getCharacteristic().getUuid().equals(UUID_MOV_DATA)){
                // Movement value notification enabled
            }
        }

        @Override
        public void onCharacteristicChanged(BluetoothGatt gatt,
                                            BluetoothGattCharacteristic characteristic) {
            if(UUID_OPT_DATA.equals(characteristic.getUuid())){
                byte[] value = characteristic.getValue();
                Integer sfloat= shortUnsignedAtOffset(value, 0);

                int mantissa;
                int exponent;
                mantissa = sfloat & 0x0FFF;
                exponent = (sfloat >> 12) & 0xFF;

                double output;
                double magnitude = pow(2.0f, exponent);
                output = (mantissa * magnitude);

                for(BluetoothDeviceListener listner: _bluetoothDeviceEventList ){
                    listner.onOpticalValueGet(output/100.0f);
                }
            }else if(UUID_MOV_DATA.equals(characteristic.getUuid())){
                byte[] value = characteristic.getValue();
                // Range 8G
                final float SCALE_ACC = (float) 4096.0;
                double accX = ((value[7]<<8) + value[6])/SCALE_ACC * -1;
                double accY = ((value[9]<<8) + value[8])/SCALE_ACC;
                double accZ = ((value[11]<<8) + value[10])/SCALE_ACC * -1;

                final float SCALE_GYRO = (float) 128.0;
                double gyroX = ((value[1]<<8) + value[0])/SCALE_GYRO;
                double gyroY = ((value[3]<<8) + value[2])/SCALE_GYRO;
                double gyroZ = ((value[5]<<8) + value[4])/SCALE_GYRO;

                final float SCALE_MAG = (float) (32768 / 4912);
                double magX, magY, magZ;
                if (value.length >= 18) {
                    magX = ((value[13]<<8) + value[12])/SCALE_MAG;
                    magY = ((value[15]<<8) + value[14])/SCALE_MAG;
                    magZ = ((value[17]<<8) + value[16])/SCALE_MAG;
                }else{
                    magX = 0;
                    magY = 0;
                    magZ = 0;
                }

                for(BluetoothDeviceListener listner: _bluetoothDeviceEventList ){
                    listner.onMovementValueGet(accX, accY, accZ, gyroX, gyroY, gyroZ, magX, magY, magZ);
                }
            }
        }

        @Override
        public void onCharacteristicRead(BluetoothGatt gatt,
                                         final BluetoothGattCharacteristic characteristic,
                                         int status) {
        }

        private Integer shortUnsignedAtOffset(byte[] c, int offset) {
            Integer lowerByte = (int) c[offset] & 0xFF;
            Integer upperByte = (int) c[offset+1] & 0xFF;
            return (upperByte << 8) + lowerByte;
        }

        @Override
        public void onCharacteristicWrite(BluetoothGatt gatt,
                                          BluetoothGattCharacteristic characteristic,
                                          int status)
        {
            if(characteristic.getUuid().equals(UUID_OPT_CONF)){
                // Optical service enabled

                // Request Optical value notification
                BluetoothGattService opticalService = gatt.getService(UUID_OPT_SERV);
                BluetoothGattCharacteristic opticalDataChar = opticalService.getCharacteristic(UUID_OPT_DATA);
                boolean registered = gatt.setCharacteristicNotification(opticalDataChar, true);
                if(registered){
                    BluetoothGattDescriptor descriptor = opticalDataChar.getDescriptor(UUID_CLIENT_CHARACTERISTIC_CONFIG);
                    descriptor.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);
                    gatt.writeDescriptor(descriptor);
                }
            }else if(characteristic.getUuid().equals(UUID_MOV_CONF)){
                // Movement service enabled

                // Request Movement value notification
                BluetoothGattService movementService = gatt.getService(UUID_MOV_SERV);
                BluetoothGattCharacteristic movementDataChar = movementService.getCharacteristic(UUID_MOV_DATA);
                boolean registered = gatt.setCharacteristicNotification(movementDataChar, true);
                if(registered){
                    // Characteristic の Notification 有効化
                    BluetoothGattDescriptor descriptor = movementDataChar.getDescriptor(UUID_CLIENT_CHARACTERISTIC_CONFIG);
                    descriptor.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);
                    gatt.writeDescriptor(descriptor);
                }
            }
        }
    };

    public void dissconnect(){
        if(_bleDeviceConnected){
            _bluetoothGatt.disconnect();
        }
    }

    public void addBluetoothDeviceListener(BluetoothDeviceListener listener){
        if(!_bluetoothDeviceEventList.contains(listener))
            _bluetoothDeviceEventList.add(listener);
    }
    public void removeBluetoothDeviceListener(BluetoothDeviceListener listener){
        if(_bluetoothDeviceEventList.contains(listener))
            _bluetoothDeviceEventList.remove(listener);
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

package com.slack.nagoyalab_sutra03.teamc.mimamorukun.Sensor;

import android.app.Service;
import android.bluetooth.BluetoothAdapter;
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
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import java.util.Timer;
import java.util.TimerTask;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import android.os.Handler;
import com.slack.nagoyalab_sutra03.teamc.mimamorukun.MyApplication;


public class SensorService extends Service {
    private final IBinder _binder = new SensorService.LocalBinder();

    public class LocalBinder extends Binder {
        public SensorService getService() {
            return SensorService.this;
        }
    }

    private void startTimer(){

        _timer = new Timer(true);
        _timer.schedule(new TimerTask() {
            @Override
            public void run() {
                if(!_bleDeviceConnected || _allSensorPaused || ! _temperatureEnabled) return;

                float temperature = (float)(25 + 7*Math.random());
                for(BluetoothDeviceListener listner: _bluetoothDeviceEventList ){
                    listner.onTemperatureValueGet(temperature);
                }
            }
        }, 0, 10000);
    }

    public void stopTimer(){
        if(_timer != null){
            _timer.cancel();
            _timer = null;
        }
    }

    private List<BluetoothDeviceListener> _bluetoothDeviceEventList = new ArrayList<>();

    //Timer
    Timer _timer;

    BluetoothManager _bluetoothManager;
    BluetoothAdapter _bluetoothAdapter;
    BluetoothLeScanner _bleScanner;
    BluetoothGatt _bluetoothGatt;
    boolean _bleScanning = false;
    String _bleDeviceAddress = null;

    //各センサーの計測を有効にするかどうか
    boolean _temperatureEnabled = true;
    public boolean isTemperatureEnabled(){return _temperatureEnabled;}
    public void setTemperatureEnabled(boolean enable){_temperatureEnabled = enable;}

    boolean _opticalEnabled = true;
    public boolean isOpticalEnabled(){return _opticalEnabled;}
    public void setOpticalEnabled(boolean enable){_opticalEnabled = enable;}

    boolean _movementEnabled = true;
    public boolean isMovementEnabled(){return _movementEnabled;}
    public void setMovementEnabled(boolean enable){_movementEnabled = enable;}

    boolean _allSensorPaused = false;
    public boolean isAllSensorPaused(){return _allSensorPaused;}
    public void setAllSensorPaused(boolean pause){_allSensorPaused = pause;}

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

    @Override
    public void onCreate(){
        super.onCreate();

        _handler = new Handler();

        _bluetoothManager = (BluetoothManager) getSystemService(BLUETOOTH_SERVICE);
        _bluetoothAdapter = _bluetoothManager.getAdapter();
        _bleScanner = _bluetoothAdapter.getBluetoothLeScanner();

        startTimer();
    }

    @Override
    public void onDestroy(){
        super.onDestroy();

        _bluetoothDeviceEventList.clear();

        _bluetoothGatt = null;
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

            if (result != null && result.getDevice() != null &&
                    result.getDevice().getName() != null && result.getDevice().getName().contains("SensorTag")) {
                for(BluetoothDeviceListener listener: _bluetoothDeviceEventList ){
                    listener.onFound(result.getDevice());
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
                if(_allSensorPaused || !_opticalEnabled) return;

                byte[] value = characteristic.getValue();
                Integer sfloat= shortUnsignedAtOffset(value, 0);

                int mantissa;
                int exponent;
                mantissa = sfloat & 0x0FFF;
                exponent = (sfloat >> 12) & 0xFF;

                double output;
                double magnitude = Math.pow(2.0f, exponent);
                output = (mantissa * magnitude);

                for(BluetoothDeviceListener listener: _bluetoothDeviceEventList ){
                    listener.onOpticalValueGet(output/100.0f);
                }
            }else if(UUID_MOV_DATA.equals(characteristic.getUuid())){
                if(_allSensorPaused || !_movementEnabled) return;

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

                for(BluetoothDeviceListener listener: _bluetoothDeviceEventList ){
                    listener.onMovementValueGet(accX, accY, accZ, gyroX, gyroY, gyroZ, magX, magY, magZ);
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
}

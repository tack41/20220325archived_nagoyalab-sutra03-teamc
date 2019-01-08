package com.slack.nagoyalab_sutra03.teamc.mimamorukun;

import android.bluetooth.BluetoothDevice;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import com.slack.nagoyalab_sutra03.teamc.mimamorukun.Sensor.BluetoothDeviceListener;
import com.slack.nagoyalab_sutra03.teamc.mimamorukun.Sensor.SensorService;
import android.widget.Button;

public class SelectDeviceActivity extends AppCompatActivity{

    public static String INTENT_KEY_DEVICE_ADDRESS = "SelectDeviceActivity_IntentKey_DeviceName";

    ListView _listViewDevice;
    ArrayAdapter<String> _listViewDeviceAdapter;
    Button _button_back;

    SensorService _sensorService;
    private ServiceConnection _connectionSensorService = new ServiceConnection() {
        public void onServiceConnected(ComponentName className, IBinder service) {
            _sensorService = ((SensorService.LocalBinder) service).getService();

            //Bind sensor events.
            _sensorService.addBluetoothDeviceListener(_bluetoothDeviceListner);

            _sensorService.startScanOnly();
        }

        public void onServiceDisconnected(ComponentName className) {
            _sensorService.stopScan();

            //Unbind sensor events.
            _sensorService.removeBluetoothDeviceListener(_bluetoothDeviceListner);

            _sensorService = null;
        }
    };
    private BluetoothDeviceListener _bluetoothDeviceListner = new BluetoothDeviceListener() {
        @Override
        public void onFound(BluetoothDevice device) {
            //機器アドレスが重複している場合は登録しない
            for(int i=0; i<_listViewDeviceAdapter.getCount(); i++){
                if(_listViewDeviceAdapter.getItem(i).contains(device.getAddress()))
                    return;
            }

            _listViewDeviceAdapter.add((device.getName() == null ? "(名前なし)" : device.getName().trim()) + " " + device.getAddress().trim());
        }

        @Override
        public void onConnected(BluetoothDevice device){}
        @Override
        public void onDisconnected(BluetoothDevice device){}
        @Override
        public void onOpticalValueGet(double value){}
        @Override
        public void onMovementValueGet(double accX, double accY, double accZ, double gyroX, double gyroY, double gyroZ, double magX, double magY, double magZ){}
        @Override
        public void onTemperatureValueGet(double value){}
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_select_device);

        _listViewDevice = findViewById(R.id.listview_device);
        _listViewDevice.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                _sensorService.stopScan();
                _sensorService.removeBluetoothDeviceListener(_bluetoothDeviceListner);

                //Return to main activity
                Intent intent = getIntent();
                //表示内容を空白で区切った際の一番最後の要素がアドレス
                String[] deviceNameAddress =  _listViewDeviceAdapter.getItem(position).split(" ");
                String deviceAddress = deviceNameAddress[deviceNameAddress.length-1];
                intent.putExtra(INTENT_KEY_DEVICE_ADDRESS, deviceAddress);
                setResult(RESULT_OK, intent);
                finish();
            }
        });

        _listViewDeviceAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1);
        _listViewDevice.setAdapter(_listViewDeviceAdapter);

        _button_back = findViewById(R.id.button_select_device_back);
        _button_back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                _sensorService.stopScan();
                _sensorService.removeBluetoothDeviceListener(_bluetoothDeviceListner);

                setResult(RESULT_CANCELED);
                finish();
            }
        });

        // Bind SensorService
        Intent intent = new Intent(getBaseContext(), SensorService.class);
        bindService(intent, _connectionSensorService, Context.BIND_AUTO_CREATE);
    }

    @Override
    protected void onDestroy(){
        super.onDestroy();

        unbindService(_connectionSensorService);

        _listViewDevice = null;
        _listViewDeviceAdapter = null;
        _button_back = null;
    }
}

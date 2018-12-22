package xuzhongwei.gunsecury;

import android.app.Activity;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

import xuzhongwei.gunsecury.profile.AcceleroteProfile;
import xuzhongwei.gunsecury.profile.AmbientTemperatureProfile;
import xuzhongwei.gunsecury.profile.BarometerProfile;
import xuzhongwei.gunsecury.profile.GenericBleProfile;
import xuzhongwei.gunsecury.profile.HumidityProfile;
import xuzhongwei.gunsecury.profile.IRTTemperature;
import xuzhongwei.gunsecury.profile.LuxometerProfile;
import xuzhongwei.gunsecury.profile.MovementProfile;
import xuzhongwei.gunsecury.service.BluetoothLeService;

public class MyWorldActivity extends AppCompatActivity {
    public static final String EXTRA_DEVICE = "EXTRA_DEVICE";
    BluetoothDevice mBluetoothDevice;
    Activity mActivity;
    private BroadcastReceiver receiver;
    private BluetoothLeService mBluetoothLeService;
    ArrayList<GenericBleProfile> bleProfiles = new ArrayList<GenericBleProfile>();
    List<BluetoothGattService> bleServiceList = new ArrayList<BluetoothGattService>();
    ArrayList<BluetoothGattCharacteristic> characteristicList = new ArrayList<BluetoothGattCharacteristic>();
    List<GenericBleProfile> mProfiles;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(com.slack.nagoyalab_sutra03.teamc.mimamorukun.R.layout.activity_my_world);

        Intent intent = getIntent();
        mBluetoothLeService = BluetoothLeService.getInstance();
        mBluetoothDevice = intent.getParcelableExtra(EXTRA_DEVICE);
        mActivity = this;

        initialReceiver();
    }


    private void initialReceiver(){

        receiver  = new BroadcastReceiver() {

            List <BluetoothGattService> serviceList;
            List<BluetoothGattCharacteristic> charList = new ArrayList<BluetoothGattCharacteristic>();

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
                                int maxNotifications = 16;
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

                                for (int ii = 0; ii < serviceList.size(); ii++) {
                                    BluetoothGattService s = serviceList.get(ii);

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
                                                ((TextView) mActivity.findViewById(com.slack.nagoyalab_sutra03.teamc.mimamorukun.R.id.luxometerValue)).setText(data);
                                            }
                                        });
                                    }


                                    if (HumidityProfile.isCorrectService(s)) {
                                        HumidityProfile hum = new HumidityProfile(mBluetoothLeService,s,mBluetoothDevice);

                                        hum.setmOnHumidityListener(new HumidityProfile.OnHumidityListener() {
                                            @Override
                                            public void onHumidityChanged(double data) {

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

                                        mov.setmOnMovementListener(new MovementProfile.OnMovementListener() {
                                            @Override
                                            public void onMovementACCChanged(double x, double y, double z) {

                                            }

                                            @Override
                                            public void onMovementGYROChanged(double x, double y, double z) {

                                            }

                                            @Override
                                            public void onMovementMAGChanged(double x, double y, double z) {

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

                                for (final GenericBleProfile p : mProfiles) {

                                    runOnUiThread(new Runnable() {
                                        @Override
                                        public void run() {
                                            p.enableService();
                                        }
                                    });
                                }
                            }
                        });
                        work.start();



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



}

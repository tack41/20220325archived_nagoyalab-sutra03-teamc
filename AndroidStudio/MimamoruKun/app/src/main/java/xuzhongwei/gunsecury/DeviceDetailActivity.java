package xuzhongwei.gunsecury;

import android.app.Activity;
import android.app.ProgressDialog;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

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

public class DeviceDetailActivity extends AppCompatActivity {
    BLEController mainController;
    private String[] mPlanetTitles;
    private DrawerLayout mDrawerLayout;
    private ListView mDrawerList;
    private Activity mActivity;
    private BroadcastReceiver receiver;
    private BluetoothLeService mBluetoothLeService;
    private BluetoothDevice mBluetoothDevice = null;

    ArrayList<GenericBleProfile> bleProfiles = new ArrayList<GenericBleProfile>();
    List<BluetoothGattService> bleServiceList = new ArrayList<BluetoothGattService>();
    ArrayList<BluetoothGattCharacteristic> characteristicList = new ArrayList<BluetoothGattCharacteristic>();
    private static final int CHARACTERISTICS_FOUND = 1;

    private static final String CHARACTERISTICS_FOUND_RESULT = "CHARACTERISTICS_FOUND_RESULT";
    public static final String EXTRA_DEVICE = "EXTRA_DEVICE";


    private UIHandler mUIHandler = new UIHandler();
    private BluetoothGatt mBtGatt = null;
    public ProgressDialog progressDialog;


    private List<GenericBleProfile> mProfiles;

    private Boolean mIsSensorTag2 = false;

    protected static DeviceDetailActivity mThis = null;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(com.slack.nagoyalab_sutra03.teamc.mimamorukun.R.layout.device_detail);
//        startBLEService();
        mBluetoothLeService = BluetoothLeService.getInstance();
        Intent intent = getIntent();
        mBluetoothDevice = intent.getParcelableExtra(EXTRA_DEVICE);
        mainController = new BLEController(this);
        mProfiles = new ArrayList<GenericBleProfile>();

        mIsSensorTag2 = false;
        // Determine type of SensorTagGatt
        String deviceName = mBluetoothDevice.getName();
        if ((deviceName.equals("SensorTag2")) ||(deviceName.equals("CC2650 SensorTag"))) {
            mIsSensorTag2 = true;
        }


        initialLayout();
        initialReceiver();
        onViewInfalted();
        initialProgressBar();
        mThis = this;
    }

    public boolean isSensorTag2() {
        return mIsSensorTag2;
    }

    public static DeviceDetailActivity getInstance() {
        return (DeviceDetailActivity) mThis;
    }


    private void initialProgressBar(){
        progressDialog = new ProgressDialog(DeviceDetailActivity.this);
        progressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
        progressDialog.setIndeterminate(true);
        progressDialog.setTitle("Discovering Services");
        progressDialog.setMessage("");
        progressDialog.setMax(100);
        progressDialog.setProgress(0);
        progressDialog.show();
    }

    private void initialLayout(){
        mActivity = this;
        //mPlanetTitles = new String[]{"周囲温度", "赤外線温度", "加速度", "湿度","磁気","気圧","ジャイロスコープ","DeviceInformation"};
        mPlanetTitles = new String[]{ "赤外温度","周囲温度", "加速度", "湿度","三次元動作","明るさ","気圧"};
        mDrawerLayout = (DrawerLayout) findViewById(com.slack.nagoyalab_sutra03.teamc.mimamorukun.R.id.drawer_layout);
        mDrawerList = (ListView) findViewById(com.slack.nagoyalab_sutra03.teamc.mimamorukun.R.id.left_drawer);
        mDrawerList.setAdapter(new ArrayAdapter<String>(this,
                com.slack.nagoyalab_sutra03.teamc.mimamorukun.R.layout.drawer_list_item, mPlanetTitles));
        mDrawerList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long l) {
                selectItem(position);
            }
        });

    }


    private void selectItem(int position) {
        mDrawerList.setItemChecked(position, true);
        setTitle(mPlanetTitles[position]);
        mDrawerLayout.closeDrawer(mDrawerList);
        ChangeContent(position);
    }

    private void ChangeContent(int n){
        //String[] ids = {"ambient_temprature_layout","ir_temprature_layout","ir_accelerometer_layout","ir_humidity_layout","ir_magnetometer_layout","ir_barometer_layout","ir_gyroscope_layout","deviceInformationLayout"};

        String[] ids = {"ir_temprature_layout","ambient_temprature_layout","ir_accelerometer_layout","ir_humidity_layout","movement_layout","luxometer_layout","barometer_layout","deviceInformationLayout"};

        for(int i=0;i<ids.length;i++){
            ((LinearLayout) findViewById(getResourceId(ids[i],"id",getPackageName()))).setVisibility(View.GONE);
        }

        ((LinearLayout) findViewById(getResourceId(ids[n],"id",getPackageName()))).setVisibility(View.VISIBLE);

    }

    private  int getResourceId(String pVariableName, String pResourcename, String pPackageName)
    {
        try {
            return getResources().getIdentifier(pVariableName, pResourcename, pPackageName);
        } catch (Exception e) {
            e.printStackTrace();
            return -1;
        }
    }



    private void onViewInfalted(){
        mBtGatt = BluetoothLeService.getBtGatt();

        if (mBtGatt.discoverServices()) {
            boolean succuess = true;
        } else {
            boolean succuess = false;
        }
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
                                                runOnUiThread(new Runnable() {
                                                    @Override
                                                    public void run() {
                                                        progressDialog.setProgress((int)((serviceDiscoveredcalc / (serviceTotalcalc - 1)) * 100));
                                                    }
                                                });
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
                                                            ((TextView) mActivity.findViewById(com.slack.nagoyalab_sutra03.teamc.mimamorukun.R.id.luxometerValue)).setText(data);
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
                                                            ((TextView) mActivity.findViewById(com.slack.nagoyalab_sutra03.teamc.mimamorukun.R.id.humidityValue)).setText(data+"");
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
                                                            ((TextView) mActivity.findViewById(com.slack.nagoyalab_sutra03.teamc.mimamorukun.R.id.movementValue1X)).setText(x+"");
                                                            ((TextView) mActivity.findViewById(com.slack.nagoyalab_sutra03.teamc.mimamorukun.R.id.movementValue1Y)).setText(y+"");
                                                            ((TextView) mActivity.findViewById(com.slack.nagoyalab_sutra03.teamc.mimamorukun.R.id.movementValue1Z)).setText(z+"");
                                                        }

                                                        @Override
                                                        public void onMovementGYROChanged(double x, double y, double z) {
                                                            ((TextView) mActivity.findViewById(com.slack.nagoyalab_sutra03.teamc.mimamorukun.R.id.movementValue2X)).setText(x+"");
                                                            ((TextView) mActivity.findViewById(com.slack.nagoyalab_sutra03.teamc.mimamorukun.R.id.movementValue2Y)).setText(y+"");
                                                            ((TextView) mActivity.findViewById(com.slack.nagoyalab_sutra03.teamc.mimamorukun.R.id.movementValue2Z)).setText(z+"");
                                                        }

                                                        @Override
                                                        public void onMovementMAGChanged(double x, double y, double z) {
                                                            ((TextView) mActivity.findViewById(com.slack.nagoyalab_sutra03.teamc.mimamorukun.R.id.movementValue3X)).setText(x+"");
                                                            ((TextView) mActivity.findViewById(com.slack.nagoyalab_sutra03.teamc.mimamorukun.R.id.movementValue3Y)).setText(y+"");
                                                            ((TextView) mActivity.findViewById(com.slack.nagoyalab_sutra03.teamc.mimamorukun.R.id.movementValue3Z)).setText(z+"");
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
                                                            ((TextView) mActivity.findViewById(com.slack.nagoyalab_sutra03.teamc.mimamorukun.R.id.acceleroterValue)).setText(data);
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
                                                            ((TextView) mActivity.findViewById(com.slack.nagoyalab_sutra03.teamc.mimamorukun.R.id.irTempratureValue)).setText(data);
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
                                                            ((TextView) mActivity.findViewById(com.slack.nagoyalab_sutra03.teamc.mimamorukun.R.id.barometerValue)).setText(data);
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
                                                            ((TextView) mActivity.findViewById(com.slack.nagoyalab_sutra03.teamc.mimamorukun.R.id.ambientTempratureValue)).setText(data);
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


                                            runOnUiThread(new Runnable() {
                                                @Override
                                                public void run() {
                                                    progressDialog.setTitle("Enabling Services");
                                                    progressDialog.setMax(mProfiles.size());
                                                    progressDialog.setProgress(0);
                                                }
                                            });

                                            for (final GenericBleProfile p : mProfiles) {

                                                runOnUiThread(new Runnable() {
                                                    @Override
                                                    public void run() {
                                                        p.enableService();
                                                        progressDialog.setProgress(progressDialog.getProgress() + 1);
                                                    }
                                                });
//                                                p.onResume();
                                            }



                                            runOnUiThread(new Runnable() {
                                                @Override
                                                public void run() {
                                                    progressDialog.hide();
                                                    progressDialog.dismiss();
                                                }
                                            });



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




}

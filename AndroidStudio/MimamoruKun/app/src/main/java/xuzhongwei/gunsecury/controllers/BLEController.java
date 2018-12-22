package xuzhongwei.gunsecury.controllers;

import android.Manifest;
import android.app.Activity;

import xuzhongwei.gunsecury.MyPermissionManager;
//BC:6A:29:AE:CD:4E DEVICE

public class BLEController {
    private Activity mActivity;



    public final static String ACTION_GATT_CONNECTED = "ACTION_GATT_CONNECTED";
    public final static String ACTION_GATT_CONNECTING = "ACTION_GATT_CONNECTING";
    public final static String ACTION_GATT_DISCONNECTED = "ACTION_GATT_DISCONNECTED";
    public final static String ACTION_GATT_DISCONNECTING = "ACTION_GATT_DISCONNECTING";
    public final static String ACTION_GATT_SERVICES_DISCOVERED = "ACTION_GATT_SERVICES_DISCOVERED";
    public final static String ACTION_DATA_READ = "ACTION_DATA_READ";
    public final static String ACTION_DATA_NOTIFY = "ACTION_DATA_NOTIFY";
    public final static String ACTION_DATA_WRITE = "ACTION_DATA_WRITE";
    public final static String EXTRA_DATA = "EXTRA_DATA";
    public final static String EXTRA_UUID = "EXTRA_UUID";
    public final static String EXTRA_STATUS = "EXTRA_STATUS";
    public final static String EXTRA_ADDRESS = "EXTRA_ADDRESS";


    public BLEController(Activity activity) {
        mActivity = activity;
        checkPermission();
    }


    private void checkPermission(){
        String[] permissions = new String[]{
                Manifest.permission.READ_PHONE_STATE,
                Manifest.permission.ANSWER_PHONE_CALLS,
                Manifest.permission.MODIFY_PHONE_STATE,
                Manifest.permission.MODIFY_AUDIO_SETTINGS,
                Manifest.permission.READ_CONTACTS,
                Manifest.permission.INTERNET,
                Manifest.permission.ACCESS_COARSE_LOCATION,
                Manifest.permission.ACCESS_FINE_LOCATION};

        MyPermissionManager.checkPermission(mActivity,permissions);
    }




}

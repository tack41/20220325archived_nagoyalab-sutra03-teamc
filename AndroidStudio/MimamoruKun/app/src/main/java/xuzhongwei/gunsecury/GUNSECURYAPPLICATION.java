package xuzhongwei.gunsecury;

import android.app.Application;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;

import xuzhongwei.gunsecury.service.BluetoothLeService;

public class GUNSECURYAPPLICATION extends Application {
    private BluetoothManager mBluetoothManager;
    private BluetoothAdapter mBluetoothAdapter;
    private BluetoothLeService mBluetoothLeService;
    @Override
    public void onCreate() {
        super.onCreate();
        initialBLE();
        startBLEService();
        startBluetoothLeService();
    }


    private void initialBLE(){
        mBluetoothManager =
                (BluetoothManager) getApplicationContext().getSystemService(Context.BLUETOOTH_SERVICE);
        mBluetoothAdapter = mBluetoothManager.getAdapter();

        if (mBluetoothAdapter == null || !mBluetoothAdapter.isEnabled()) {
            Intent enableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            enableIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(enableIntent);
        }
    }

    private void startBLEService(){
        Intent bindIntent = new Intent(this, BluetoothLeService.class);
        startService(bindIntent);
    }

    private void startBluetoothLeService(){
        boolean f;

        Intent bindIntent = new Intent(this, BluetoothLeService.class);
        startService(bindIntent);

        f = bindService(bindIntent, mServiceConnection, Context.BIND_AUTO_CREATE);
        if(f){
            Boolean success = true;
        }else{
            Boolean success = false;
        }
    }

    private ServiceConnection mServiceConnection = new ServiceConnection(){
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            mBluetoothLeService = ((BluetoothLeService.LocalBinder) service)
                    .getService();
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {

        }
    };
}

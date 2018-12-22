package xuzhongwei.gunsecury.service;

//public class BluetoothLeService extends Service {
//
//    private final static String TAG = BluetoothLeService.class.getSimpleName();
//
//    public final static String ACTION_GATT_CONNECTED =
//            "ACTION_GATT_CONNECTED";
//
//    public final static String ACTION_GATT_CONNECTING =
//            "ACTION_GATT_CONNECTING";
//
//    public final static String ACTION_GATT_DISCONNECTED =
//            "ACTION_GATT_DISCONNECTED";
//
//    public final static String ACTION_GATT_DISCONNECTING =
//            "ACTION_GATT_DISCONNECTING";
//
//    public final static String ACTION_GATT_SERVICES_DISCOVERED =
//            "ACTION_GATT_SERVICES_DISCOVERED";
//
//    public final static String ACTION_DATA_AVAILABLE =
//            "ACTION_DATA_AVAILABLE";
//
//    public final static String ACTION_DATA_NOTIFY =
//            "ACTION_DATA_NOTIFY";
//
//    public final static String EXTRA_DATA =
//            "EXTRA_DATA";
//
//    public final static String EXTRA_UUID =
//            "EXTRA_UUID";
//
//    public final static String EXTRA_STATUS =
//            "EXTRA_STATUS";
//
//
//    public static final String  FIND_NEW_BLE_DEVICE = "FIND_NEW_BLE_DEVICE";
//
//    private static BluetoothLeService mService;
//    private BluetoothAdapter mBluetoothAdapter;
//    private BluetoothManager mBluetoothManager;
//    private Handler mHandler = new Handler();
//    private ScanCallback mLeScanCallback;
//    private BluetoothGatt mBluetoothGatt;

//    private LinkedList<BluetoothGattCharacteristic> writeList = new LinkedList<BluetoothGattCharacteristic>();
//
//    private volatile bleRequest curBleRequest = null;
//
//
//    private volatile LinkedList<bleRequest> procQueue;
//    private final Lock lock = new ReentrantLock();
//    public enum bleRequestOperation {
//        wrBlocking,
//        wr,
//        rdBlocking,
//        rd,
//        nsBlocking,
//    }
//
//    public enum bleRequestStatus {
//        not_queued,
//        queued,
//        processing,
//        timeout,
//        done,
//        no_such_request,
//        failed,
//    }
//
//    public class bleRequest {
//        public int id;
//        public BluetoothGattCharacteristic characteristic;
//        public bleRequestOperation operation;
//        public volatile bleRequestStatus status;
//        public int timeout;
//        public int curTimeout;
//        public boolean notifyenable;
//    }
//
//
//
//
//    @Override
//    public void onCreate() {
//        super.onCreate();
//        // Initializes Bluetooth adapter.
//        initialBlueTooth();
//    }
//
//    @Override
//    public IBinder onBind(Intent intent) {
//        // TODO: Return the communication channel to the service.
//        throw new UnsupportedOperationException("Not yet implemented");
//    }
//
//
//    @Override
//    public int onStartCommand(Intent intent, int flags, int startId) {
//        mService = this;
//        initialBlueTooth();
//        return super.onStartCommand(intent, flags, startId);
//    }
//
//
//    public static BluetoothLeService getInstance(){
//        if(mService == null){
//            mService = new BluetoothLeService();
//        }
//        return mService;
//    }
//
//


//

//
//    private void initialBlueTooth(){
//        procQueue = new LinkedList<bleRequest>();
//        mBluetoothManager =
//                (BluetoothManager) this.getSystemService(Context.BLUETOOTH_SERVICE);
//        mBluetoothAdapter = mBluetoothManager.getAdapter();
//        mLeScanCallback = new ScanCallback() {
//            @Override
//            public void onScanResult(int callbackType, ScanResult result) {
//                super.onScanResult(callbackType, result);
//                if(result == null) return;
//                if(result.getDevice() == null) return;
//                String name = result.getDevice().getName();
//                if(name == null) name="UNKNOWN DEVICE";
//                String address = result.getDevice().getAddress();
//                if(address == null) return;
//                BLEDeviceDAO bleDevice = new BLEDeviceDAO(name,address,result.getDevice());
//                broadcastUpdate(FIND_NEW_BLE_DEVICE,result.getDevice());
//            }
//
//            @Override
//            public void onBatchScanResults(List<ScanResult> results) {
//                super.onBatchScanResults(results);
//            }
//
//            @Override
//            public void onScanFailed(int errorCode) {
//                super.onScanFailed(errorCode);
//            }
//        };
//
//
//        Thread queueThread = new Thread(new Runnable() {
//            @Override
//            public void run() {
//                while(true){
//                    executeQueue();
//                    try {
//                        Thread.sleep(50,0);
//                    }
//                    catch (Exception e) {
//                        e.printStackTrace();
//                    }
//                }
//            }
//        });
//
//        queueThread.start();
//
//
//    }
//
//
//
//    public List<BluetoothGattService> getBLEService(){
//        if(mBluetoothGatt == null) return null;
//        return  mBluetoothGatt.getServices();
//    }
//
//    private void broadcastUpdate(String status ,BluetoothDevice bleDevice){
//        Intent intent = new Intent();
//        intent.putExtra(status,(Parcelable) bleDevice);
//        intent.setAction(status);
//        sendBroadcast(intent);
//    }
//
//    private void broadcastUpdate(final String action,
//                                 final BluetoothGattCharacteristic characteristic, final int status) {
//        final Intent intent = new Intent(action);
//        intent.putExtra(EXTRA_UUID, characteristic.getUuid().toString());
//        intent.putExtra(EXTRA_DATA, characteristic.getValue());
//        intent.putExtra(EXTRA_STATUS, status);
//        sendBroadcast(intent);
//    }
//
//    public void writeCharacteristic(BluetoothGattCharacteristic characteristic, byte b) {
//        byte[] val = new byte[1];
//        val[0] = b;
//        characteristic.setValue(val);
//
//        bleRequest req = new bleRequest();
//        req.status = bleRequestStatus.not_queued;
//        req.characteristic = characteristic;
//        req.operation = bleRequestOperation.wrBlocking;
//        addRequestToQueue(req);
//    }
//
//    public void writeCharacteristic(BluetoothGattCharacteristic characteristic){
//        mBluetoothGatt.writeCharacteristic(characteristic);
//    }
//
//    public void readCharacteristic(BluetoothGattCharacteristic characteristic){
//        mBluetoothGatt.writeCharacteristic(characteristic);
//    }
//
//
//    public void setCharacteristicNotification(
//        BluetoothGattCharacteristic characteristic, boolean enable) {
//        bleRequest req = new bleRequest();
//        req.status = bleRequestStatus.not_queued;
//        req.characteristic = characteristic;
//        req.operation = bleRequestOperation.nsBlocking;
//        req.notifyenable = enable;
//        addRequestToQueue(req);
//    }
//
//    private void addRequestToQueue(bleRequest req){
//        lock.lock();
//        if (procQueue.peekLast() != null) {
//            req.id = procQueue.peek().id++;
//        }
//        else {
//            req.id = 0;
//        }
//
//        procQueue.add(req);
//        lock.unlock();
//    }
//
//
//    private void executeQueue(){
//        lock.lock();
//        if (curBleRequest != null) {
//            Log.d(TAG, "executeQueue, curBleRequest running");
//            try {
//                Thread.sleep(10, 0);
//            } catch (Exception e) {
//                e.printStackTrace();
//            }
//            lock.unlock();
//            return;
//        }
//
//
//
//        if (procQueue == null) {
//            lock.unlock();
//            return;
//        }
//        if (procQueue.size() == 0) {
//            lock.unlock();
//            return;
//        }
//
//        bleRequest procReq = procQueue.removeFirst();
//        curBleRequest = procReq;
//        switch (procReq.operation) {
//            case rd:
//                //Read, do non blocking read
//
//                break;
//            case rdBlocking:
//                //Normal (blocking) read
//                mBluetoothGatt.readCharacteristic(procReq.characteristic);
//                break;
//            case wr:
//                mBluetoothGatt.writeCharacteristic(procReq.characteristic);
//                //Write, do non blocking write (Ex: OAD)
//                break;
//            case wrBlocking:
//                //Normal (blocking) write
//
//                Log.d(TAG, "executeQueue, wrBlocking running" + procReq.characteristic.getUuid().toString()+"");
//                procReq.status = bleRequestStatus.processing;
//                mBluetoothGatt.writeCharacteristic(procReq.characteristic);
//                procReq.status = bleRequestStatus.done;
//                break;
//            case nsBlocking:
//                Log.d(TAG, "executeQueue, nsBlocking running" +  procReq.characteristic.getUuid().toString()+"");
//                Boolean res = mBluetoothGatt.setCharacteristicNotification(procReq.characteristic, procReq.notifyenable);
//                if (res) {
//                    procReq.status = bleRequestStatus.processing;
//                    BluetoothGattDescriptor clientConfig = procReq.characteristic
//                            .getDescriptor(GattInfo.CLIENT_CHARACTERISTIC_CONFIG);
//                    if (clientConfig != null) {
//
//                        if (procReq.notifyenable) {
//                            // Log.i(TAG, "Enable notification: " +
//                            // characteristic.getUuid().toString());
//                            clientConfig
//                                    .setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);
//                        } else {
//                            // Log.i(TAG, "Disable notification: " +
//                            // characteristic.getUuid().toString());
//                            clientConfig
//                                    .setValue(BluetoothGattDescriptor.DISABLE_NOTIFICATION_VALUE);
//                        }
//                        mBluetoothGatt.writeDescriptor(clientConfig);
//
//
//                    }
//
//                    procReq.status = bleRequestStatus.done;
//
//                }
//
//                break;
//            default:
//                break;
//        }
//
//        curBleRequest = null;
//        lock.unlock();
//    }
//
//
//
//    public bleRequestStatus pollForStatusofRequest(bleRequest req) {
//        lock.lock();
//        if (req == curBleRequest) {
//            bleRequestStatus stat = curBleRequest.status;
//            if (stat == bleRequestStatus.done) {
//                curBleRequest = null;
//            }
//            if (stat == bleRequestStatus.timeout) {
//                curBleRequest = null;
//            }
//            lock.unlock();
//            return stat;
//        }
//        else {
//            lock.unlock();
//            return bleRequestStatus.no_such_request;
//        }
//    }
//
//}


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
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanResult;
import android.content.Context;
import android.content.Intent;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;
import android.os.Parcelable;
import android.util.Log;

import java.lang.reflect.Method;
import java.util.LinkedList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.UUID;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import xuzhongwei.gunsecury.common.GattInfo;
import xuzhongwei.gunsecury.model.BLEDeviceDAO;
import xuzhongwei.gunsecury.util.Adapter.PreferenceWR;

/**
 * Service for managing connection and data communication with a GATT server
 * hosted on a given Bluetooth LE device.
 */
public class BluetoothLeService extends Service {
    static final String TAG = "BluetoothLeService";

    public final static String ACTION_GATT_CONNECTED = "com.example.ti.ble.common.ACTION_GATT_CONNECTED";
    public final static String ACTION_GATT_DISCONNECTED = "com.example.ti.ble.common.ACTION_GATT_DISCONNECTED";
    public final static String ACTION_GATT_SERVICES_DISCOVERED = "com.example.ti.ble.common.ACTION_GATT_SERVICES_DISCOVERED";
    public final static String ACTION_DEVICE_CONNECTED = "com.example.ti.ble.common.ACTION_DEVICE_CONNECTED";


    public final static String ACTION_DATA_READ = "com.example.ti.ble.common.ACTION_DATA_READ";
    public final static String ACTION_DATA_NOTIFY = "com.example.ti.ble.common.ACTION_DATA_NOTIFY";
    public final static String ACTION_DATA_WRITE = "com.example.ti.ble.common.ACTION_DATA_WRITE";
    public final static String EXTRA_DATA = "com.example.ti.ble.common.EXTRA_DATA";
    public final static String EXTRA_UUID = "com.example.ti.ble.common.EXTRA_UUID";
    public final static String EXTRA_STATUS = "com.example.ti.ble.common.EXTRA_STATUS";
    public final static String EXTRA_ADDRESS = "com.example.ti.ble.common.EXTRA_ADDRESS";
    public final static int GATT_TIMEOUT = 150;

    // BLE
    private BluetoothManager mBluetoothManager = null;
    private BluetoothAdapter mBtAdapter = null;
    private BluetoothGatt mBluetoothGatt = null;
    private static BluetoothLeService mThis = null;
    private String mBluetoothDeviceAddress;
    private PreferenceWR mDevicePrefs = null;

    public Timer disconnectionTimer;
    private final Lock lock = new ReentrantLock();

    private volatile boolean blocking = false;
    private volatile int lastGattStatus = 0; //Success

    private volatile bleRequest curBleRequest = null;

    public enum bleRequestOperation {
        wrBlocking,
        wr,
        rdBlocking,
        rd,
        nsBlocking,
    }

    public enum bleRequestStatus {
        not_queued,
        queued,
        processing,
        timeout,
        done,
        no_such_request,
        failed,
    }

    public class bleRequest {
        public int id;
        public BluetoothGattCharacteristic characteristic;
        public bleRequestOperation operation;
        public volatile bleRequestStatus status;
        public int timeout;
        public int curTimeout;
        public boolean notifyenable;
    }

    // Queuing for fast application response.
    private volatile LinkedList<bleRequest> procQueue;
    private volatile LinkedList<bleRequest> nonBlockQueue;

    //

    /**
     * GATT client callbacks
     */
    private BluetoothGattCallback mGattCallbacks = new BluetoothGattCallback() {

        @Override
        public void onConnectionStateChange(BluetoothGatt gatt, int status,
                                            int newState) {
            if (mBluetoothGatt == null) {
                // Log.e(TAG, "mBluetoothGatt not created!");
                return;
            }

            BluetoothDevice device = gatt.getDevice();
            String address = device.getAddress();
            // Log.d(TAG, "onConnectionStateChange (" + address + ") " + newState +
            // " status: " + status);

            try {
                switch (newState) {
                    case BluetoothProfile.STATE_CONNECTED:
                        broadcastUpdate(ACTION_GATT_CONNECTED, address, status);
                        break;
                    case BluetoothProfile.STATE_DISCONNECTED:
                        broadcastUpdate(ACTION_GATT_DISCONNECTED, address, status);
                        break;
                    default:
                        // Log.e(TAG, "New state not processed: " + newState);
                        break;
                }
            } catch (NullPointerException e) {
                e.printStackTrace();
            }
        }

        @Override
        public void onServicesDiscovered(BluetoothGatt gatt, int status) {
            BluetoothDevice device = gatt.getDevice();
            broadcastUpdate(ACTION_GATT_SERVICES_DISCOVERED, device.getAddress(),
                    status);
        }

        @Override
        public void onCharacteristicChanged(BluetoothGatt gatt,
                                            BluetoothGattCharacteristic characteristic) {
            broadcastUpdate(ACTION_DATA_NOTIFY, characteristic,
                    BluetoothGatt.GATT_SUCCESS);
        }

        @Override
        public void onCharacteristicRead(BluetoothGatt gatt,
                                         BluetoothGattCharacteristic characteristic, int status) {
            if (blocking)unlockBlockingThread(status);
            if (nonBlockQueue.size() > 0) {
                lock.lock();
                for (int ii = 0; ii < nonBlockQueue.size(); ii++) {
                    bleRequest req = nonBlockQueue.get(ii);
                    if (req.characteristic == characteristic) {
                        req.status = bleRequestStatus.done;
                        nonBlockQueue.remove(ii);
                        break;
                    }
                }
                lock.unlock();
            }
            broadcastUpdate(ACTION_DATA_READ, characteristic, status);
        }

        @Override
        public void onCharacteristicWrite(BluetoothGatt gatt,
                                          BluetoothGattCharacteristic characteristic, int status) {
            if (blocking)unlockBlockingThread(status);
            if (nonBlockQueue.size() > 0) {
                lock.lock();
                for (int ii = 0; ii < nonBlockQueue.size(); ii++) {
                    bleRequest req = nonBlockQueue.get(ii);
                    if (req.characteristic == characteristic) {
                        req.status = bleRequestStatus.done;
                        nonBlockQueue.remove(ii);
                        break;
                    }
                }
                lock.unlock();
            }
            broadcastUpdate(ACTION_DATA_WRITE, characteristic, status);
        }

        @Override
        public void onDescriptorRead(BluetoothGatt gatt,
                                     BluetoothGattDescriptor descriptor, int status) {
            if (blocking)unlockBlockingThread(status);
            unlockBlockingThread(status);
        }

        @Override
        public void onDescriptorWrite(BluetoothGatt gatt,
                                      BluetoothGattDescriptor descriptor, int status) {
            if (blocking)unlockBlockingThread(status);
            // Log.i(TAG, "onDescriptorWrite: " + descriptor.getUuid().toString());
        }
    };

    @Override
    public void onCreate() {
        super.onCreate();
        initialBlueTooth();

        this.initialize();
    }

    private void unlockBlockingThread(int status) {
        this.lastGattStatus = status;
        this.blocking = false;
    }

    private void broadcastUpdate(final String action, final String address,
                                 final int status) {
        final Intent intent = new Intent(action);
        intent.putExtra(EXTRA_ADDRESS, address);
        intent.putExtra(EXTRA_STATUS, status);
        sendBroadcast(intent);
    }

    private void broadcastUpdate(final String action,
                                 final BluetoothGattCharacteristic characteristic, final int status) {
        final Intent intent = new Intent(action);
        intent.putExtra(EXTRA_UUID, characteristic.getUuid().toString());
        intent.putExtra(EXTRA_DATA, characteristic.getValue());
        intent.putExtra(EXTRA_STATUS, status);
        sendBroadcast(intent);
    }

    public boolean checkGatt() {
        if (mBtAdapter == null) {
            // Log.w(TAG, "BluetoothAdapter not initialized");
            return false;
        }
        if (mBluetoothGatt == null) {
            // Log.w(TAG, "BluetoothGatt not initialized");
            return false;
        }
        if (this.blocking) {
            Log.d(TAG,"Cannot start operation : Blocked");
            return false;
        }
        return true;

    }

    /**
     * Manage the BLE service
     */
    public class LocalBinder extends Binder {
        public BluetoothLeService getService() {
            return BluetoothLeService.this;
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        return binder;
    }

    @Override
    public boolean onUnbind(Intent intent) {
        // After using a given device, you should make sure that
        // BluetoothGatt.close() is called
        // such that resources are cleaned up properly. In this particular example,
        // close() is
        // invoked when the UI is disconnected from the Service.
        close();
        return super.onUnbind(intent);
    }

    private final IBinder binder = new LocalBinder();

    /**
     * Initializes a reference to the local Bluetooth adapter.
     *
     * @return Return true if the initialization is successful.
     */
    public boolean initialize() {
        // For API level 18 and above, get a reference to BluetoothAdapter through
        // BluetoothManager.
        mThis = this;
        if (mBluetoothManager == null) {
            mBluetoothManager = (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
            if (mBluetoothManager == null) {
                // Log.e(TAG, "Unable to initialize BluetoothManager.");
                return false;
            }
        }

        mBtAdapter = mBluetoothManager.getAdapter();
        if (mBtAdapter == null) {
            // Log.e(TAG, "Unable to obtain a BluetoothAdapter.");
            return false;
        }

        procQueue = new LinkedList<bleRequest>();
        nonBlockQueue = new LinkedList<bleRequest>();


        Thread queueThread = new Thread() {
            @Override
            public void run() {
                while (true) {
                    executeQueue();
                    try {
                        Thread.sleep(0,100000);
                    }
                    catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        };

        queueThread.start();
        return true;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        // Log.i(TAG, "Received start id " + startId + ": " + intent);
        // We want this service to continue running until it is explicitly
        // stopped, so return sticky.
        this.initialize();
        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mBluetoothGatt != null) {
            mBluetoothGatt.close();
            mBluetoothGatt = null;
        }
    }

    //
    // GATT API
    //

    /**
     * Request a read on a given {@code BluetoothGattCharacteristic}. The read
     * result is reported asynchronously through the
     * {@code BluetoothGattCallback#onCharacteristicRead(android.bluetooth.BluetoothGatt, android.bluetooth.BluetoothGattCharacteristic, int)}
     * callback.
     *
     * @param characteristic
     *          The characteristic to read from.
     */
    public int readCharacteristic(BluetoothGattCharacteristic characteristic) {
        bleRequest req = new bleRequest();
        req.status = bleRequestStatus.not_queued;
        req.characteristic = characteristic;
        req.operation = bleRequestOperation.rdBlocking;
        addRequestToQueue(req);
        boolean finished = false;
        while (!finished) {
            bleRequestStatus stat = pollForStatusofRequest(req);
            if (stat == bleRequestStatus.done) {
                finished = true;
                return 0;
            }
            else if (stat == bleRequestStatus.timeout) {
                finished = true;
                return -3;
            }
        }
        return -2;
    }

    public int writeCharacteristic(
            BluetoothGattCharacteristic characteristic, byte b) {


        byte[] val = new byte[1];
        val[0] = b;
        characteristic.setValue(val);

        bleRequest req = new bleRequest();
        req.status = bleRequestStatus.not_queued;
        req.characteristic = characteristic;
        req.operation = bleRequestOperation.wrBlocking;
        addRequestToQueue(req);
        boolean finished = false;
        while (!finished) {
            bleRequestStatus stat = pollForStatusofRequest(req);
            if (stat == bleRequestStatus.done) {
                finished = true;
                return 0;
            }
            else if (stat == bleRequestStatus.timeout) {
                finished = true;
                return -3;
            }
        }
        return -2;
    }
    public int writeCharacteristic(
            BluetoothGattCharacteristic characteristic, byte[] b) {
        characteristic.setValue(b);
        bleRequest req = new bleRequest();
        req.status = bleRequestStatus.not_queued;
        req.characteristic = characteristic;
        req.operation = bleRequestOperation.wrBlocking;
        addRequestToQueue(req);
        boolean finished = false;
        while (!finished) {
            bleRequestStatus stat = pollForStatusofRequest(req);
            if (stat == bleRequestStatus.done) {
                finished = true;
                return 0;
            }
            else if (stat == bleRequestStatus.timeout) {
                finished = true;
                return -3;
            }
        }
        return -2;
    }
    public int writeCharacteristic(BluetoothGattCharacteristic characteristic) {
        bleRequest req = new bleRequest();
        req.status = bleRequestStatus.not_queued;
        req.characteristic = characteristic;
        req.operation = bleRequestOperation.wrBlocking;
        addRequestToQueue(req);
        boolean finished = false;
        while (!finished) {
            bleRequestStatus stat = pollForStatusofRequest(req);
            if (stat == bleRequestStatus.done) {
                finished = true;
                return 0;
            }
            else if (stat == bleRequestStatus.timeout) {
                finished = true;
                return -3;
            }
        }
        return -2;
    }

    public boolean writeCharacteristicNonBlock(BluetoothGattCharacteristic characteristic) {
        bleRequest req = new bleRequest();
        req.status = bleRequestStatus.not_queued;
        req.characteristic = characteristic;
        req.operation = bleRequestOperation.wr;
        addRequestToQueue(req);
        return true;
    }

    /**
     * Retrieves the number of GATT services on the connected device. This should
     * be invoked only after {@code BluetoothGatt#discoverServices()} completes
     * successfully.
     *
     * @return A {@code integer} number of supported services.
     */
    public int getNumServices() {
        if (mBluetoothGatt == null)
            return 0;

        return mBluetoothGatt.getServices().size();
    }

    /**
     * Retrieves a list of supported GATT services on the connected device. This
     * should be invoked only after {@code BluetoothGatt#discoverServices()}
     * completes successfully.
     *
     * @return A {@code List} of supported services.
     */
    public List<BluetoothGattService> getSupportedGattServices() {
        if (mBluetoothGatt == null)
            return null;

        return mBluetoothGatt.getServices();
    }

    /**
     * Enables or disables notification on a give characteristic.
     *
     * @param characteristic
     *          Characteristic to act on.
     * @param enable
     *          If true, enable notification. False otherwise.
     */
    public int setCharacteristicNotification(
            BluetoothGattCharacteristic characteristic, boolean enable) {
        bleRequest req = new bleRequest();
        req.status = bleRequestStatus.not_queued;
        req.characteristic = characteristic;
        req.operation = bleRequestOperation.nsBlocking;
        req.notifyenable = enable;
        addRequestToQueue(req);
        boolean finished = false;
        while (!finished) {
            bleRequestStatus stat = pollForStatusofRequest(req);
            if (stat == bleRequestStatus.done) {
                finished = true;
                return 0;
            }
            else if (stat == bleRequestStatus.timeout) {
                finished = true;
                return -3;
            }
        }
        return -2;
    }

    public boolean isNotificationEnabled(
            BluetoothGattCharacteristic characteristic) {
        if (characteristic == null) {
            return false;
        }
        if (!checkGatt())
            return false;

        BluetoothGattDescriptor clientConfig = characteristic
                .getDescriptor(GattInfo.CLIENT_CHARACTERISTIC_CONFIG);
        if (clientConfig == null)
            return false;

        return clientConfig.getValue() == BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE;
    }

    /**
     * Connects to the GATT server hosted on the Bluetooth LE device.
     *
     * @param address
     *          The device address of the destination device.
     *
     * @return Return true if the connection is initiated successfully. The
     *         connection result is reported asynchronously through the
     *         {@code BluetoothGattCallback#onConnectionStateChange(android.bluetooth.BluetoothGatt, int, int)}
     *         callback.
     */
    public boolean connect(final String address) {
        if (mBtAdapter == null || address == null) {
            // Log.w(TAG, "BluetoothAdapter not initialized or unspecified address.");
            return false;
        }
        final BluetoothDevice device = mBtAdapter.getRemoteDevice(address);
        this.mDevicePrefs = new PreferenceWR(device.getAddress(),this.getApplicationContext());
        int connectionState = mBluetoothManager.getConnectionState(device,
                BluetoothProfile.GATT);

        if (connectionState == BluetoothProfile.STATE_DISCONNECTED) {

            // Previously connected device. Try to reconnect.
            if (mBluetoothDeviceAddress != null
                    && address.equals(mBluetoothDeviceAddress) && mBluetoothGatt != null) {
                // Log.d(TAG, "Re-use GATT connection");
                if (mBluetoothGatt.connect()) {
                    return true;
                } else {
                    // Log.w(TAG, "GATT re-connect failed.");
                    return false;
                }
            }

            if (device == null) {
                // Log.w(TAG, "Device not found.  Unable to connect.");
                return false;
            }
            // We want to directly connect to the device, so we are setting the
            // autoConnect parameter to false.
            // Log.d(TAG, "Create a new GATT connection.");
            mBluetoothGatt = device.connectGatt(this, false, mGattCallbacks);
            mBluetoothDeviceAddress = address;
        } else {
            // Log.w(TAG, "Attempt to connect in state: " + connectionState);
            return false;
        }
        return true;
    }

    /**
     * Disconnects an existing connection or cancel a pending connection. The
     * disconnection result is reported asynchronously through the
     * {@code BluetoothGattCallback#onConnectionStateChange(android.bluetooth.BluetoothGatt, int, int)}
     * callback.
     */
    public void disconnect(String address) {
        if (mBtAdapter == null) {
            // Log.w(TAG, "disconnect: BluetoothAdapter not initialized");
            return;
        }
        final BluetoothDevice device = mBtAdapter.getRemoteDevice(address);
        int connectionState = mBluetoothManager.getConnectionState(device,
                BluetoothProfile.GATT);

        if (mBluetoothGatt != null) {
            if (connectionState != BluetoothProfile.STATE_DISCONNECTED) {
                mBluetoothGatt.disconnect();
            } else {
                // Log.w(TAG, "Attempt to disconnect in state: " + connectionState);
            }
        }
    }

    /**
     * After using a given BLE device, the app must call this method to ensure
     * resources are released properly.
     */
    public void close() {
        if (mBluetoothGatt != null) {
            // Log.i(TAG, "close");
            mBluetoothGatt.close();
            mBluetoothGatt = null;
        }
    }

    public int numConnectedDevices() {
        int n = 0;

        if (mBluetoothGatt != null) {
            List<BluetoothDevice> devList;
            devList = mBluetoothManager.getConnectedDevices(BluetoothProfile.GATT);
            n = devList.size();
        }
        return n;
    }

    //
    // Utility functions
    //
    public static BluetoothGatt getBtGatt() {
        return mThis.mBluetoothGatt;
    }

    public static BluetoothManager getBtManager() {
        return mThis.mBluetoothManager;
    }

    public static BluetoothLeService getInstance() {
        if(mThis == null){
            mThis = new BluetoothLeService();
        }
        return mThis;
    }

    public void waitIdle(int timeout) {
        while (timeout-- > 0) {
            try {
                Thread.sleep(1);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    public boolean refreshDeviceCache(BluetoothGatt gatt){
        try {
            BluetoothGatt localBluetoothGatt = gatt;
            Method localMethod = localBluetoothGatt.getClass().getMethod("refresh", new Class[0]);
            if (localMethod != null) {
                boolean bool = ((Boolean) localMethod.invoke(localBluetoothGatt, new Object[0])).booleanValue();
                return bool;
            }
        }
        catch (Exception localException) {
            Log.e(TAG, "An exception occured while refreshing device");
        }
        return false;
    }

    public void timedDisconnect() {
        disconnectTimerTask disconnectionTimerTask;
        this.disconnectionTimer = new Timer();
        disconnectionTimerTask = new disconnectTimerTask(this);
        this.disconnectionTimer.schedule(disconnectionTimerTask, 20000);
    }
    public void abortTimedDisconnect() {
        if (this.disconnectionTimer != null) {
            this.disconnectionTimer.cancel();
        }
    }
    class disconnectTimerTask extends TimerTask {
        BluetoothLeService param;

        public disconnectTimerTask(final BluetoothLeService param) {
            this.param = param;
        }

        @Override
        public void run() {
            this.param.disconnect(mBluetoothDeviceAddress);
        }
    }

    public boolean requestConnectionPriority(int connectionPriority) {
        return this.mBluetoothGatt.requestConnectionPriority(connectionPriority);
    }

    public boolean addRequestToQueue(bleRequest req) {
        lock.lock();
        if (procQueue.peekLast() != null) {
            req.id = procQueue.peek().id++;
        }
        else {
            req.id = 0;
            procQueue.add(req);
        }
        lock.unlock();
        return true;
    }

    public bleRequestStatus pollForStatusofRequest(bleRequest req) {
        lock.lock();
        if (req == curBleRequest) {
            bleRequestStatus stat = curBleRequest.status;
            if (stat == bleRequestStatus.done) {
                curBleRequest = null;
            }
            if (stat == bleRequestStatus.timeout) {
                curBleRequest = null;
            }
            lock.unlock();
            return stat;
        }
        else {
            lock.unlock();
            return bleRequestStatus.no_such_request;
        }
    }

    private Handler mHandler = new Handler();
    private static final long SCAN_PERIOD = 100000;
    private ScanCallback mLeScanCallback;
    public static final String  FIND_NEW_BLE_DEVICE = "FIND_NEW_BLE_DEVICE";

    public void startScan() {
        // Stops scanning after a pre-defined scan period.
        mHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                stopScan();
            }
        }, SCAN_PERIOD);

        if(mBtAdapter == null) return;
        mBtAdapter.getBluetoothLeScanner().startScan(mLeScanCallback);
    }

    public void stopScan(){
        mBtAdapter.getBluetoothLeScanner().stopScan(mLeScanCallback);
    }

    private void initialBlueTooth(){
        procQueue = new LinkedList<bleRequest>();
        mBluetoothManager =
                (BluetoothManager) this.getSystemService(Context.BLUETOOTH_SERVICE);
        mLeScanCallback = new ScanCallback() {
            @Override
            public void onScanResult(int callbackType, ScanResult result) {
                super.onScanResult(callbackType, result);
                if(result == null) return;
                if(result.getDevice() == null) return;
                String name = result.getDevice().getName();
                if(name == null) name="UNKNOWN DEVICE";
                String address = result.getDevice().getAddress();
                if(address == null) return;
                BLEDeviceDAO bleDevice = new BLEDeviceDAO(name,address,result.getDevice());
                broadcastUpdate(FIND_NEW_BLE_DEVICE,result.getDevice());
            }

            @Override
            public void onBatchScanResults(List<ScanResult> results) {
                super.onBatchScanResults(results);
            }

            @Override
            public void onScanFailed(int errorCode) {
                super.onScanFailed(errorCode);
            }
        };

    }


    public List<BluetoothGattService> getBLEService(){
        if(mBluetoothGatt == null) return null;
        return  mBluetoothGatt.getServices();
    }


    private void broadcastUpdate(String status ,BluetoothDevice bleDevice){
        Intent intent = new Intent();
        intent.putExtra(status,(Parcelable) bleDevice);
        intent.setAction(status);
        sendBroadcast(intent);
    }





    private void executeQueue() {
        // Everything here is done on the queue
        lock.lock();
        if (curBleRequest != null) {
            Log.d(TAG, "executeQueue, curBleRequest running");
            try {
                curBleRequest.curTimeout++;
                if (curBleRequest.curTimeout > GATT_TIMEOUT) {
                    curBleRequest.status = bleRequestStatus.timeout;
                    curBleRequest = null;
                }
                Thread.sleep(10, 0);
            } catch (Exception e) {
                e.printStackTrace();
            }
            lock.unlock();
            return;
        }
        if (procQueue == null) {
            lock.unlock();
            return;
        }
        if (procQueue.size() == 0) {
            lock.unlock();
            return;
        }
        bleRequest procReq = procQueue.removeFirst();

        switch (procReq.operation) {
            case rd:
                //Read, do non blocking read
                break;
            case rdBlocking:
                //Normal (blocking) read
                if (procReq.timeout == 0) {
                    procReq.timeout = GATT_TIMEOUT;
                }
                procReq.curTimeout = 0;
                curBleRequest = procReq;
                int stat = sendBlockingReadRequest(procReq);
                if (stat == -2) {
                    Log.d(TAG,"executeQueue rdBlocking: error, BLE was busy or device disconnected");
                    lock.unlock();
                    return;
                }
                break;
            case wr:
                //Write, do non blocking write (Ex: OAD)
                nonBlockQueue.add(procReq);
                sendNonBlockingWriteRequest(procReq);
                break;
            case wrBlocking:
                //Normal (blocking) write
                if (procReq.timeout == 0) {
                    procReq.timeout = GATT_TIMEOUT;
                }
                curBleRequest = procReq;
                stat = sendBlockingWriteRequest(procReq);
                if (stat == -2) {
                    Log.d(TAG,"executeQueue wrBlocking: error, BLE was busy or device disconnected");
                    lock.unlock();
                    return;
                }
                break;
            case nsBlocking:
                if (procReq.timeout == 0) {
                    procReq.timeout = GATT_TIMEOUT;
                }
                curBleRequest = procReq;
                stat = sendBlockingNotifySetting(procReq);
                if (stat == -2) {
                    Log.d(TAG,"executeQueue nsBlocking: error, BLE was busy or device disconnected");
                    lock.unlock();
                    return;
                }
                break;
            default:
                break;

        }
        lock.unlock();
    }

    public int sendNonBlockingReadRequest(bleRequest request) {
        request.status = bleRequestStatus.processing;
        if (!checkGatt()) {
            request.status = bleRequestStatus.failed;
            return -2;
        }
        mBluetoothGatt.readCharacteristic(request.characteristic);
        return 0;
    }

    public int sendNonBlockingWriteRequest(bleRequest request) {
        request.status = bleRequestStatus.processing;
        if (!checkGatt()) {
            request.status = bleRequestStatus.failed;
            return -2;
        }
        mBluetoothGatt.writeCharacteristic(request.characteristic);
        return 0;
    }

    public int sendBlockingReadRequest(bleRequest request) {
        request.status = bleRequestStatus.processing;
        int timeout = 0;
        if (!checkGatt()) {
            request.status = bleRequestStatus.failed;
            return -2;
        }
        mBluetoothGatt.readCharacteristic(request.characteristic);
        this.blocking = true; // Set read to be blocking
        while (this.blocking) {
            timeout ++;
            waitIdle(1);
            if (timeout > GATT_TIMEOUT) {this.blocking = false; request.status = bleRequestStatus.timeout; return -1;}  //Read failed TODO: Fix this to follow connection interval !
        }
        request.status = bleRequestStatus.done;
        return lastGattStatus;
    }

    public int sendBlockingWriteRequest(bleRequest request) {
        request.status = bleRequestStatus.processing;
        int timeout = 0;
        if (!checkGatt()) {
            request.status = bleRequestStatus.failed;
            return -2;
        }
        mBluetoothGatt.writeCharacteristic(request.characteristic);
        this.blocking = true; // Set read to be blocking
        while (this.blocking) {
            timeout ++;
            waitIdle(1);
            if (timeout > GATT_TIMEOUT) {this.blocking = false; request.status = bleRequestStatus.timeout; return -1;}  //Read failed TODO: Fix this to follow connection interval !
        }
        request.status = bleRequestStatus.done;
        return lastGattStatus;
    }
    public int sendBlockingNotifySetting(bleRequest request) {
        request.status = bleRequestStatus.processing;
        int timeout = 0;
        if (request.characteristic == null) {
            return -1;
        }
        if (!checkGatt())
            return -2;

        if (mBluetoothGatt.setCharacteristicNotification(request.characteristic, request.notifyenable)) {

            BluetoothGattDescriptor clientConfig = request.characteristic
                    .getDescriptor(GattInfo.CLIENT_CHARACTERISTIC_CONFIG);
            if (clientConfig != null) {

                if (request.notifyenable) {
                    // Log.i(TAG, "Enable notification: " +
                    // characteristic.getUuid().toString());
                    clientConfig
                            .setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);
                } else {
                    // Log.i(TAG, "Disable notification: " +
                    // characteristic.getUuid().toString());
                    clientConfig
                            .setValue(BluetoothGattDescriptor.DISABLE_NOTIFICATION_VALUE);
                }
                mBluetoothGatt.writeDescriptor(clientConfig);
                // Log.i(TAG, "writeDescriptor: " +
                // characteristic.getUuid().toString());
                this.blocking = true; // Set read to be blocking
                while (this.blocking) {
                    timeout ++;
                    waitIdle(1);
                    if (timeout > GATT_TIMEOUT) {this.blocking = false; request.status = bleRequestStatus.timeout; return -1;}  //Read failed TODO: Fix this to follow connection interval !
                }
                request.status = bleRequestStatus.done;
                return lastGattStatus;
            }
        }
        return -3; // Set notification to android was wrong ...
    }
    public String getConnectedDeviceAddress() {
        return this.mBluetoothDeviceAddress;
    }

        public BluetoothDevice getDevice(){
        if(mBluetoothGatt != null){
            return mBluetoothGatt.getDevice();
        }
        return null;
    }


}

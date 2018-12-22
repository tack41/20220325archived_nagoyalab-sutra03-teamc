package xuzhongwei.gunsecury.profile;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;

import java.util.List;

import xuzhongwei.gunsecury.service.BluetoothLeService;

public class DeviceInfoProfile extends GenericBleProfile {
    private static final String dISService_UUID = "0000180a-0000-1000-8000-00805f9b34fb";
    private static final String dISSystemID_UUID = "00002a23-0000-1000-8000-00805f9b34fb";
    private static final String dISModelNR_UUID = "00002a24-0000-1000-8000-00805f9b34fb";
    private static final String dISSerialNR_UUID = "00002a25-0000-1000-8000-00805f9b34fb";
    private static final String dISFirmwareREV_UUID = "00002a26-0000-1000-8000-00805f9b34fb";
    private static final String dISHardwareREV_UUID = "00002a27-0000-1000-8000-00805f9b34fb";
    private static final String dISSoftwareREV_UUID = "00002a28-0000-1000-8000-00805f9b34fb";
    private static final String dISManifacturerNAME_UUID = "00002a29-0000-1000-8000-00805f9b34fb";

    BluetoothGattCharacteristic systemIDc;
    BluetoothGattCharacteristic modelNRc;
    BluetoothGattCharacteristic serialNRc;
    BluetoothGattCharacteristic firmwareREVc;
    BluetoothGattCharacteristic hardwareREVc;
    BluetoothGattCharacteristic softwareREVc;
    BluetoothGattCharacteristic ManifacturerNAMEc;

    public DeviceInfoProfile(BluetoothLeService bluetoothLeService, BluetoothGattService bluetoothGattService,BluetoothDevice device) {
        super(bluetoothLeService, bluetoothGattService,device);
        List<BluetoothGattCharacteristic> charalist = bluetoothGattService.getCharacteristics();


        for (BluetoothGattCharacteristic c : charalist) {
            if (c.getUuid().toString().equals(dISSystemID_UUID)) {
                this.systemIDc = c;
            }
            if (c.getUuid().toString().equals(dISModelNR_UUID)) {
                this.modelNRc = c;
            }
            if (c.getUuid().toString().equals(dISSerialNR_UUID)) {
                this.serialNRc = c;
            }
            if (c.getUuid().toString().equals(dISFirmwareREV_UUID)) {
                this.firmwareREVc = c;
            }
            if (c.getUuid().toString().equals(dISHardwareREV_UUID)) {
                this.hardwareREVc = c;
            }
            if (c.getUuid().toString().equals(dISSoftwareREV_UUID)) {
                this.softwareREVc = c;
            }
            if (c.getUuid().toString().equals(dISManifacturerNAME_UUID)) {
                this.ManifacturerNAMEc = c;
            }
        }

    }

    @Override
    public void enableService() {
        mBluetoothLeService.readCharacteristic(this.systemIDc);
        mBluetoothLeService.readCharacteristic(this.modelNRc);
        mBluetoothLeService.readCharacteristic(this.serialNRc);
        mBluetoothLeService.readCharacteristic(this.firmwareREVc);
        mBluetoothLeService.readCharacteristic(this.hardwareREVc);
        mBluetoothLeService.readCharacteristic(this.softwareREVc);
        mBluetoothLeService.readCharacteristic(this.ManifacturerNAMEc);
    }


    public static boolean isCorrectService(BluetoothGattService service) {
        if ((service.getUuid().toString().compareTo(dISService_UUID)) == 0) {
            return true;
        }
        else return false;
    }

    public void configureService(){

    }

    public void writeCharactristic(){
        mBluetoothLeService.writeCharacteristic(this.configData);
    }
}

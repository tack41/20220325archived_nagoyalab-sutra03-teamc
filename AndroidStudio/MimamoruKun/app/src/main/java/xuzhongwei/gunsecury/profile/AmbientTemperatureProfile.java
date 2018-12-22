package xuzhongwei.gunsecury.profile;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;
import android.util.Log;

import java.util.List;

import xuzhongwei.gunsecury.common.GattInfo;
import xuzhongwei.gunsecury.service.BluetoothLeService;
import xuzhongwei.gunsecury.util.Adapter.Point3D;
import xuzhongwei.gunsecury.util.Adapter.sensor.Sensor;

public class AmbientTemperatureProfile extends GenericBleProfile {
    public AmbientTemperatureProfile(BluetoothLeService bluetoothLeService, BluetoothGattService bluetoothGattService, BluetoothDevice device) {
        super(bluetoothLeService, bluetoothGattService, device);
        List<BluetoothGattCharacteristic> charalist = bluetoothGattService.getCharacteristics();
        for(BluetoothGattCharacteristic c:charalist){

            if(c.getUuid().toString().equals(GattInfo.UUID_IRT_DATA.toString())){
                this.normalData = c;
            }

            if(c.getUuid().toString().equals(GattInfo.UUID_IRT_CONF.toString())){
                this.configData = c;
            }

            if(c.getUuid().toString().equals(GattInfo.UUID_IRT_PERI.toString())){
                this.periodData = c;
            }
        }
    }

    public static boolean isCorrectService(BluetoothGattService service) {
        if ((service.getUuid().toString().compareTo(GattInfo.UUID_IRT_SERV.toString())) == 0) {
            return true;
        } else return false;
    }



    @Override
    public void configureService() {
        int error = mBluetoothLeService.writeCharacteristic(this.configData, (byte)0x01);
        if (error != 0) {
            if (this.configData != null)
                Log.d("SensorTagAmbientTemp","Sensor config failed: " + this.configData.getUuid().toString() + " Error: " + error);
        }
//        error = this.mBluetoothLeService.setCharacteristicNotification(this.configData, true);
//        if (error != 0) {
//            if (this.dataC != null)
//                Log.d("SensorTagAmbientTemperatureProfile","Sensor notification enable failed: " + this.configData.getUuid().toString() + " Error: " + error);
//        }

        this.isConfigured = true;
    }

    @Override
    public void updateData(byte[] data) {

        Point3D v = Sensor.IR_TEMPERATURE.convert(data);
        if (mOnDataChangedListener != null) {
            mOnDataChangedListener.onDataChanged(String.format("%.1f°F", (v.x * 1.8) + 32));
        }

        if(mAmbientTemperatureListener != null){
            mAmbientTemperatureListener.onAmbientTemperatureChanged((v.x * 1.8) + 32);
        }

    }

    public interface AmbientTemperatureListener{
        void onAmbientTemperatureChanged(double tem);
    }

    private AmbientTemperatureListener mAmbientTemperatureListener;

    public void setmAmbientTemperatureListener(AmbientTemperatureListener mAmbientTemperatureListener) {
        this.mAmbientTemperatureListener = mAmbientTemperatureListener;
    }
}

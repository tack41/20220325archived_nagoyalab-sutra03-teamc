package xuzhongwei.gunsecury.profile;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;

import java.util.List;

import xuzhongwei.gunsecury.common.GattInfo;
import xuzhongwei.gunsecury.service.BluetoothLeService;
import xuzhongwei.gunsecury.util.Adapter.Point3D;
import xuzhongwei.gunsecury.util.Adapter.sensor.Sensor;

public class IRTTemperature extends GenericBleProfile {
    public IRTTemperature(BluetoothLeService bluetoothLeService, BluetoothGattService bluetoothGattService,BluetoothDevice device) {
        super(bluetoothLeService, bluetoothGattService,device);
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
        }
        else return false;
    }



    public void configureService(){
        //mBluetoothLeService.writeCharacteristic(configData,(byte)0x01);
        mBluetoothLeService.setCharacteristicNotification(normalData,true);
    }

    @Override
    public void updateData(byte[] value) {
        Point3D v = Sensor.IR_TEMPERATURE.convert(value);
        if(mOnDataChangedListener != null){
            mOnDataChangedListener.onDataChanged(String.format("%.1f°C", v.y));
        }

        if(mOnIRTemperatureListener != null){
            mOnIRTemperatureListener.onIRTemperatureChanged(v.y);
        }
    }

    public void writeCharactristic(){
        mBluetoothLeService.writeCharacteristic(this.configData);
    }


    public interface OnIRTemperatureListener{
        void onIRTemperatureChanged(double tem);
    }

    OnIRTemperatureListener mOnIRTemperatureListener;

    public void setmOnIRTemperatureListener(OnIRTemperatureListener mOnIRTemperatureListener) {
        this.mOnIRTemperatureListener = mOnIRTemperatureListener;
    }
}

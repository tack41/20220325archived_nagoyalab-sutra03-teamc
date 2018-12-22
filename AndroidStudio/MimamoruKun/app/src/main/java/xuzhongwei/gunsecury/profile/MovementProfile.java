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

public class MovementProfile extends GenericBleProfile {
    public MovementProfile(BluetoothLeService bluetoothLeService, BluetoothGattService bluetoothGattService,BluetoothDevice device) {
        super(bluetoothLeService, bluetoothGattService,device);


        List<BluetoothGattCharacteristic> charalist = bluetoothGattService.getCharacteristics();
        for(BluetoothGattCharacteristic c:charalist){

            if(c.getUuid().toString().equals(GattInfo.UUID_MOV_DATA.toString())){
                this.normalData = c;
            }

            if(c.getUuid().toString().equals(GattInfo.UUID_MOV_CONF.toString())){
                this.configData = c;
            }

            if(c.getUuid().toString().equals(GattInfo.UUID_MOV_PERI.toString())){
                this.periodData = c;
            }
        }

    }


    public static boolean isCorrectService(BluetoothGattService service) {
        if ((service.getUuid().toString().compareTo(GattInfo.UUID_MOV_SERV.toString())) == 0) {
            return true;
        }
        else return false;
    }


    @Override
    public void enableService() {
        byte b[] = new byte[] {0x7F,0x00};

        int error = mBluetoothLeService.writeCharacteristic(this.configData, b);
        if (error != 0) {
            if (this.configData != null)
                Log.d("SensorTagMovementProfile","Sensor config failed: " + this.configData.getUuid().toString() + " Error: " + error);
        }
//        error = this.mBluetoothLeService.setCharacteristicNotification(this.configData, true);
//        if (error != 0) {
//            if (this.configData != null)
//                Log.d("SensorTagMovementProfile","Sensor notification enable failed: " + this.configData.getUuid().toString() + " Error: " + error);
//        }

        this.isEnabled = true;
    }

    public void updateData(byte[] value){
        Point3D vAcc;
        Point3D vGyro;
        Point3D vMag;
        String resACC = "";
        String resGYRO = "";
        String resMAG = "";

        vAcc = Sensor.MOVEMENT_ACC.convert(value);
        resACC = vAcc.x+"-"+vAcc.y+"-"+vAcc.z+"\n";

        vGyro = Sensor.MOVEMENT_GYRO.convert(value);
        resGYRO += vGyro.x+"-"+vGyro.y+"-"+vGyro.z+"\n";

        vMag = Sensor.MOVEMENT_MAG.convert(value);
        resMAG += vMag.x+"-"+vMag.y+"-"+vMag.z+"\n";

        if(mOnDataChangedListener != null){
            mOnDataChangedListener.onDataChanged(resGYRO);
        }

        if(mOnMovementListener != null){
            mOnMovementListener.onMovementACCChanged(vAcc.x,vAcc.y,vAcc.z);
            mOnMovementListener.onMovementGYROChanged(vGyro.x,vGyro.y,vGyro.z);
            mOnMovementListener.onMovementMAGChanged(vMag.x,vMag.y,vMag.z);
        }
    }

    public interface OnMovementListener{
        void onMovementACCChanged(double x,double y,double z);
        void onMovementGYROChanged(double x,double y,double z);
        void onMovementMAGChanged(double x,double y,double z);
    }

    private OnMovementListener mOnMovementListener;

    public void setmOnMovementListener(OnMovementListener mOnMovementListener) {
        this.mOnMovementListener = mOnMovementListener;
    }
}

package xuzhongwei.gunsecury.profile;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;

import xuzhongwei.gunsecury.common.GattInfo;
import xuzhongwei.gunsecury.service.BluetoothLeService;
import xuzhongwei.gunsecury.util.Adapter.Point3D;
import xuzhongwei.gunsecury.util.Adapter.sensor.BarometerCalibrationCoefficients;
import xuzhongwei.gunsecury.util.Adapter.sensor.Sensor;

public class BarometerProfile extends GenericBleProfile {
    private static final double PA_PER_METER = 12.0;
    private BluetoothGattCharacteristic calibC;
    private boolean isCalibrated;

    public BarometerProfile(BluetoothLeService bluetoothLeService, BluetoothGattService bluetoothGattService, BluetoothDevice device) {
        super(bluetoothLeService, bluetoothGattService, device);

        List<BluetoothGattCharacteristic> charalist = bluetoothGattService.getCharacteristics();
        for (BluetoothGattCharacteristic c : charalist) {

            if (c.getUuid().toString().equals(GattInfo.UUID_BAR_DATA.toString())) {
                this.normalData = c;
            }

            if (c.getUuid().toString().equals(GattInfo.UUID_BAR_CONF.toString())) {
                this.configData = c;
            }

            if (c.getUuid().toString().equals(GattInfo.UUID_BAR_PERI.toString())) {
                this.periodData = c;
            }
            if (c.getUuid().toString().equals(GattInfo.UUID_BAR_CALI.toString())) {
                this.calibC = c;
            }
        }

        if (this.mBTDevice.getName().equals("CC2650 SensorTag")) {
            this.isCalibrated = true;
        }
        else {
            this.isCalibrated = false;
        }



    }


    @Override
    public void didReadValueForCharacteristic(BluetoothGattCharacteristic c) {
        super.didReadValueForCharacteristic(c);
        if (this.calibC != null) {
            if (this.calibC.equals(c)) {
                //We have been calibrated
                // Sanity check
                byte[] value = c.getValue();
                if (value.length != 16) {
                    return;
                }

                // Barometer calibration values are read.
                List<Integer> cal = new ArrayList<Integer>();
                for (int offset = 0; offset < 8; offset += 2) {
                    Integer lowerByte = (int) value[offset] & 0xFF;
                    Integer upperByte = (int) value[offset + 1] & 0xFF;
                    cal.add((upperByte << 8) + lowerByte);
                }

                for (int offset = 8; offset < 16; offset += 2) {
                    Integer lowerByte = (int) value[offset] & 0xFF;
                    Integer upperByte = (int) value[offset + 1];
                    cal.add((upperByte << 8) + lowerByte);
                }
                Log.d("SensorTagBarometerProfile", "Barometer calibrated !!!!!");
                BarometerCalibrationCoefficients.INSTANCE.barometerCalibrationCoefficients = cal;
                this.isCalibrated = true;
                int error = mBluetoothLeService.writeCharacteristic(this.configData, (byte)0x01);
                if (error != 0) {
                    if (this.configData != null)
                        Log.d("SensorTagBarometerProfile","Sensor config failed: " + this.configData.getUuid().toString() + " Error: " + error);
                }
                error = this.mBluetoothLeService.setCharacteristicNotification(this.configData, true);
                if (error != 0) {
                    if (this.configData != null)
                        Log.d("SensorTagBarometerProfile","Sensor notification enable failed: " + this.configData.getUuid().toString() + " Error: " + error);
                }
            }
        }

    }

    public static boolean isCorrectService(BluetoothGattService service) {
        if ((service.getUuid().toString().compareTo(GattInfo.UUID_BAR_SERV.toString())) == 0) {
            return true;
        } else return false;
    }


    public void updateData(byte[] value) {

        Point3D v;
        v = Sensor.BAROMETER.convert(value);

        double h = (v.x - BarometerCalibrationCoefficients.INSTANCE.heightCalibration)
                / PA_PER_METER;
        h = (double) Math.round(-h * 10.0) / 10.0;

        if (mOnDataChangedListener != null) {
            mOnDataChangedListener.onDataChanged(String.format("%.1f mBar %.1f meter", v.x / 100, h));
        }

        if(mOnBarometerListener != null){
            mOnBarometerListener.onBarometerChanged(v.x / 100,h);
        }

    }


    public interface OnBarometerListener{
        void onBarometerChanged(double data1,double data2);
    }

    private OnBarometerListener mOnBarometerListener;

    public void setmOnBarometerListener(OnBarometerListener mOnBarometerListener) {
        this.mOnBarometerListener = mOnBarometerListener;
    }
}

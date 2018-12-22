package xuzhongwei.gunsecury.model;

import android.bluetooth.BluetoothDevice;
import android.os.Parcel;
import android.os.Parcelable;

public class BLEDeviceDAO implements Parcelable {
    String deviceName;
    String deviceAddress;
    android.bluetooth.BluetoothDevice device;

    public BLEDeviceDAO(String deviceName, String deviceAddress, BluetoothDevice _deivice) {
        this.deviceName = deviceName;
        this.deviceAddress = deviceAddress;
        this.device = _deivice;
    }

    public BluetoothDevice getDevice() {
        return device;
    }

    public void setDevice(BluetoothDevice device) {
        this.device = device;
    }

    public String getDeviceName() {
        return deviceName;
    }

    public void setDeviceName(String deviceName) {
        this.deviceName = deviceName;
    }

    public String getDeviceAddress() {
        return deviceAddress;
    }

    public void setDeviceAddress(String deviceAddress) {
        this.deviceAddress = deviceAddress;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.deviceName);
        dest.writeString(this.deviceAddress);
        dest.writeParcelable(this.device, flags);
    }

    protected BLEDeviceDAO(Parcel in) {
        this.deviceName = in.readString();
        this.deviceAddress = in.readString();
        this.device = in.readParcelable(BluetoothDevice.class.getClassLoader());
    }

    public static final Creator<BLEDeviceDAO> CREATOR = new Creator<BLEDeviceDAO>() {
        @Override
        public BLEDeviceDAO createFromParcel(Parcel source) {
            return new BLEDeviceDAO(source);
        }

        @Override
        public BLEDeviceDAO[] newArray(int size) {
            return new BLEDeviceDAO[size];
        }
    };
}

package com.slack.nagoyalab_sutra03.teamc.mimamorukun.Sensor;

import android.bluetooth.BluetoothDevice;

import java.util.EventListener;

public interface BluetoothDeviceListener extends EventListener {
    void onFound(BluetoothDevice device);
    void onConnected(BluetoothDevice device);
    void onDisconnected(BluetoothDevice device);
    void onOpticalValueGet(double value);
    void onMovementValueGet(double accX, double accY, double accZ, double gyroX, double gyroY, double gyroZ, double magX, double magY, double magZ);
    void onTemperatureValueGet(double value);
}

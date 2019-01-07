package com.slack.nagoyalab_sutra03.teamc.mimamorukun.Sensor;

import android.bluetooth.BluetoothDevice;

import java.util.EventListener;

public interface BluetoothDeviceListener extends EventListener {
    void onFound(BluetoothDevice device);
}

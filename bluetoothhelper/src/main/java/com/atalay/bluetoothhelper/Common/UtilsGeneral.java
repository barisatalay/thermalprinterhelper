package com.atalay.bluetoothhelper.Common;

import android.bluetooth.BluetoothAdapter;

/**
 * Created by baris on 10.03.2017.
 */

public class UtilsGeneral {
    public static boolean bluetoothIsEnabled(){
        BluetoothAdapter mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (mBluetoothAdapter == null) {
            return false;
        } else {
            if (!mBluetoothAdapter.isEnabled()) {
                return false;
            }
        }
        return true;
    }
}

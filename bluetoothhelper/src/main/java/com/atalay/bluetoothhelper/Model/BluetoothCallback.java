package com.atalay.bluetoothhelper.Model;

/**
 * Created by baris on 9.03.2017.
 */

public interface BluetoothCallback {
    void onBegin();
    void onErrorImportant(String errorMessage);
    void onError(String errorMessage);
    void onSuccessful();
}

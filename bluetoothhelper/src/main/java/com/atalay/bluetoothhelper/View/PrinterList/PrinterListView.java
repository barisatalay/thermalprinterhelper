package com.atalay.bluetoothhelper.View.PrinterList;

import com.atalay.bluetoothhelper.Base.BaseView;
import com.atalay.bluetoothhelper.Adapter.BluetoothDeviceAdapter;

/**
 * Created by baris on 10.03.2017.
 */

public interface PrinterListView extends BaseView {
    void enabledRefreshButton();

    void refreshAdapter();

    void loadDevices(BluetoothDeviceAdapter bluetoothDeviceAdapter);

    void bluetoothSearchStarted();

    void bluetoothSearchEnded();

}

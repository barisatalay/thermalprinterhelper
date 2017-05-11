package com.atalay.bluetoothhelper.Provider;

import android.Manifest;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.text.TextUtils;

import com.atalay.bluetoothhelper.Common.PrinterCommands;
import com.atalay.bluetoothhelper.Common.UtilsDialog;
import com.atalay.bluetoothhelper.Common.UtilsGeneral;
import com.atalay.bluetoothhelper.Common.UtilsHtml;
import com.atalay.bluetoothhelper.Common.UtilsPermission;
import com.atalay.bluetoothhelper.Model.BluetoothCallback;
import com.atalay.bluetoothhelper.Model.PermissionCallback;
import com.atalay.bluetoothhelper.R;
import com.atalay.bluetoothhelper.View.PrinterList.PrinterListActivity;
import com.karumi.dexter.MultiplePermissionsReport;

import java.io.IOException;
import java.io.OutputStream;
import java.util.UUID;

/**
 * Created by baris on 9.03.2017.
 */

public class BluetoothProvider extends Thread implements PermissionCallback {
    //region Private
    private static final UUID MY_UUID_SECURE = UUID.fromString("00001101-0000-1000-8000-00805f9b34fb");
    private BluetoothAdapter mBluetoothAdapter;
    private Activity mActivity;
    private BluetoothDevice pairedDevice;
    private BluetoothSocket mSocket;
    private OutputStream outputStream;
    private String deviceAddress = "";
    private boolean isTest = false;
    private boolean haveError = false;
    private boolean showPrinterListActivity = true;
    private String printingText = "";
    private String prefKey;
    private BluetoothCallback callback;
    private SharedPreferences preferences;
    private int copyCount = 1;
    //endregion

    //region Public

    //endregion


    public BluetoothProvider(Activity mActivity, BluetoothCallback callback){
        this.mActivity = mActivity;
        this.callback = callback;

        prefKey = mActivity.getString(R.string.pref_general_printer_address);
        preferences = PreferenceManager.getDefaultSharedPreferences(mActivity.getApplicationContext());

        loadDeviceAddress();
    }

    public BluetoothProvider connect(){
        if(checkPermissions()) {
            if (deviceAddress.isEmpty()) {
                sendError(R.string.err_bluetooth_unsyncbluetooth, false);
                haveError = true;
                openPrintList();
            } else {
                if (callback != null)
                    callback.onBegin();
                loadAdapter();
                connectSocket();
            }
        }
        return this;
    }

    private boolean checkPermissions() {
        if(UtilsPermission.checkPermissionManuel(mActivity, Manifest.permission.ACCESS_FINE_LOCATION, 1) == -1){
            UtilsPermission.checkPermission(mActivity,this,R.string.err_permission,Manifest.permission.ACCESS_FINE_LOCATION);
            haveError = true;
            return false;
        }
        return true;
    }

    private void openPrintList() {
        if(!showPrinterListActivity) return;

        UtilsDialog.createAlertDialog(mActivity)
                .setTitle(mActivity.getString(R.string.printerlist_title))
                .setMessage(mActivity.getString(R.string.pair_device))
                .setPositiveButton(mActivity.getString(R.string.yes), new DialogInterface.OnClickListener(){
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        openPrinterListActivity();
                        dialog.dismiss();
                    }

                })
                .setNegativeButton(mActivity.getString(R.string.cancel), null)
                .show();
    }

    public BluetoothProvider openPrinterListActivity() {
        Intent newActivity = new Intent(mActivity.getApplicationContext(), PrinterListActivity.class);
        mActivity.startActivity(newActivity);

        return this;
    }

    private void loadAdapter() {
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        pairedDevice = mBluetoothAdapter.getRemoteDevice(deviceAddress);
    }

    private void connectSocket() {
        if(pairedDevice != null) {
            try {
                mSocket = pairedDevice.createRfcommSocketToServiceRecord(MY_UUID_SECURE);
                mSocket.connect();

                loadStream(mSocket.getOutputStream());
            } catch (IOException e) {

                sendError(R.string.reconnected,false);

                try {
                    mSocket =(BluetoothSocket) pairedDevice.getClass().getMethod("createRfcommSocket", new Class[] {int.class}).invoke(pairedDevice,1);
                    mSocket.connect();

                    loadStream(mSocket.getOutputStream());

                    sendError(R.string.connected,false);
                } catch (Exception  e1) {
                    sendError(e1.getMessage(),false);
                }
            }
        }else{
            sendError(R.string.err_bluetooth_unsyncbluetooth, true);
        }
    }

    private void loadDeviceAddress() {
        deviceAddress = preferences.getString(prefKey,"");
    }

    private void sendError(int resourceId, boolean important){
        if(callback != null) {
            if(important)
                callback.onErrorImportant(mActivity.getString(resourceId));
            else
                callback.onError(mActivity.getString(resourceId));
            haveError = important;
        }
    }

    private void sendError(String errorMessage, boolean important){
        if(callback != null) {
            if(important)
                callback.onErrorImportant(errorMessage);
            else
                callback.onError(errorMessage);
            haveError = true;
        }
    }

    private void loadStream(OutputStream newOutputStream) {
        if(outputStream == null)
            outputStream = newOutputStream;
    }

    private void onDestroy(){
        mActivity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if(mBluetoothAdapter != null)
                    if(mBluetoothAdapter.isDiscovering())
                        mBluetoothAdapter.cancelDiscovery();

                if(mSocket!= null) {
                    try {
                        if(outputStream != null)
                            outputStream.close();
                        mSocket.close();
                        mSocket = null;
                    } catch (IOException e) {
                        sendError(e.getMessage(), true);
                    }
                }

                if(callback != null)
                    callback.onSuccessful();
            }
        });
    }

    @Override
    public void successful(MultiplePermissionsReport report) {
        connect();
    }

    @Override
    public void denied(MultiplePermissionsReport report) {}

    public BluetoothProvider printText(String message) {
        printingText = message;

        return this;
    }

    @Override
    public void run() {
        try {
            if (haveError)
                return;

            sendPrint();
        }finally {
            onDestroy();
        }
    }

    private boolean sendPrint() {
        if(!UtilsGeneral.bluetoothIsEnabled()){
            sendError(R.string.err_bluetooth_notenabled, true);
            return false;
        }

        String newString = "";

        if(!isTest) {
            newString = UtilsHtml.brToLB(printingText, 5);
            newString = UtilsHtml.clearHtmlTags(newString);
            newString = UtilsHtml.HtmlDecode(newString);

        }else{
            newString = prepareTestData();
        }

        try {
            for(int i=0;i<copyCount;i++) {
                outputStream.write(newString.getBytes("UTF-8"));

                Thread.sleep(1000);
            }

            outputStream.flush();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        return true;
    }

    private String prepareTestData() {
        StringBuffer bufer = new StringBuffer();
        bufer.append("0123456789").append("<br/>")
                .append("abcdefghijklmnoprstuvyz").append("<br/>")
                .append("ABCDEFGHIJKLMNOPRSTUVYZ").append("<br/>")
                .append("/-*?!'#+%&{}[]()").append("<br/>");
        return bufer.toString();
    }

    public String getDeviceAddress(){
        loadDeviceAddress();
        return deviceAddress;
    }

    public BluetoothProvider setDeviceAddress(String deviceAddress){
        this.deviceAddress = deviceAddress;

        return this;
    }

    /**
     * If you want to design a separate screen for yourself to call your terminal printer, you should make changes here.
     *
     * If you submit the "true" value, you will see the screen I designed.
     *
     * If you submit an "false" value, you must export the device's DeviceAddress using the "setDeviceAddress" method.
     *
     * @param show default value "true"
     */
    public BluetoothProvider showPrinterListActivity(boolean show){
        this.showPrinterListActivity = show;

        return this;
    }

    private void setBreakLine(int count) {
        for(int i=0;i<count;i++) {
            try {
                outputStream.write(PrinterCommands.FEED_LINE);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * If you want to print test page send true value.
     */
    public BluetoothProvider isTest(boolean value){
        this.isTest = value;

        return this;
    }

    public BluetoothProvider setCopyCount(int copyCount){
        this.copyCount = copyCount;

        return this;
    }
}

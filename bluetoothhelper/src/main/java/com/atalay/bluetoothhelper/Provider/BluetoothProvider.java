package com.atalay.bluetoothhelper.Provider;

import android.Manifest;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.preference.PreferenceManager;

import com.atalay.bluetoothhelper.Common.PrinterCommands;
import com.atalay.bluetoothhelper.Common.UtilsDialog;
import com.atalay.bluetoothhelper.Common.UtilsGeneral;
import com.atalay.bluetoothhelper.Common.UtilsHtml;
import com.atalay.bluetoothhelper.Common.UtilsImage;
import com.atalay.bluetoothhelper.Common.UtilsPermission;
import com.atalay.bluetoothhelper.Model.BluetoothCallback;
import com.atalay.bluetoothhelper.Model.PermissionCallback;
import com.atalay.bluetoothhelper.R;
import com.atalay.bluetoothhelper.View.PrinterList.PrinterListActivity;
import com.karumi.dexter.MultiplePermissionsReport;

import java.io.IOException;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.util.UUID;

/**
 * Created by baris on 9.03.2017.
 */

public class BluetoothProvider /*extends AsyncTask<Void, Void, Void>*/ implements PermissionCallback {
    //region Private
    private static final UUID MY_UUID_SECURE = UUID.fromString("00001101-0000-1000-8000-00805f9b34fb");
    private BluetoothAdapter mBluetoothAdapter;
    private Activity mActivity;
    private BluetoothDevice pairedDevice;
    private BluetoothSocket mSocket;
    private OutputStream outputStream;
    private String deviceAddress = "";
    private boolean haveError = false;
    private boolean showPrinterListActivity = true;
//    private String printingText = "";
    private byte[] printingByte ;
//    private List<Byte> printingByte ;
    private BluetoothCallback callback;
    private int copyCount = 1;
    private Bitmap imageBitmap = null;

    //endregion

    //region Public
    //endregion


    public BluetoothProvider(Activity mActivity, BluetoothCallback callback){
        this.mActivity = mActivity;
        this.callback = callback;

        getDeviceAddress();
    }

    public BluetoothProvider() {
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

    public Intent openPrinterListActivity() {
        return new Intent(mActivity.getApplicationContext(), PrinterListActivity.class);
    }

    private void loadAdapter() {
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        pairedDevice = mBluetoothAdapter.getRemoteDevice(deviceAddress);
    }

    private void connectSocket() {
        if(pairedDevice != null) {
            try {
                mSocket = pairedDevice.createRfcommSocketToServiceRecord(MY_UUID_SECURE);
                try {
                    Thread.sleep(700);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                mSocket.connect();

                loadStream(mSocket.getOutputStream());
            } catch (IOException e) {

                //sendError(R.string.reconnected,false);

                try {
                    mSocket =(BluetoothSocket) pairedDevice.getClass().getMethod("createRfcommSocket", int.class).invoke(pairedDevice,1);
                    Thread.sleep(700);
                    mSocket.connect();

                    loadStream(mSocket.getOutputStream());

                    //sendError(R.string.connected,false);
                } catch (Exception  e1) {
                    sendError(e1.getMessage(),false);
                    haveError = true;
                }
            }
        }else{
            sendError(R.string.err_bluetooth_unsyncbluetooth, true);
        }
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
        outputStream = newOutputStream;
    }

    private void onDestroy(){
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

    @Override
    public void successful(MultiplePermissionsReport report) {
        connect();
    }

    @Override
    public void denied(MultiplePermissionsReport report) {}

    public String prepareTestData() {
        StringBuffer bufer = new StringBuffer();
        bufer.append("0123456789").append("<br/>")
                .append("abcdefghijklmnoprstuvyz").append("<br/>")
                .append("ABCDEFGHIJKLMNOPRSTUVYZ").append("<br/>")
                .append("/-*?!'#+%&{}[]()").append("<br/>");
        return bufer.toString();
    }

    public String getDeviceAddress(){
        String prefKey = mActivity.getString(R.string.pref_general_printer_address);
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(mActivity.getApplicationContext());
        deviceAddress = preferences.getString(prefKey,"");
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

    public BluetoothProvider setCopyCount(int copyCount){
        this.copyCount = copyCount;

        return this;
    }

//    @Override
//    protected Void doInBackground(Void... params) {
//        printProcess();
//
//        return null;
//    }

    private void printProcess() {
        if (haveError)
            return;

        if(!UtilsGeneral.bluetoothIsEnabled()){
            sendError(R.string.err_bluetooth_notenabled, true);
            return;
        }

        if(printingByte == null){
            return;
        }

        try {
            for(int i=0;i<copyCount;i++) {
                outputStream.write(printingByte);
                setBreakLine(2);
                if(copyCount>1) {
                    Thread.sleep(printingByte.length * 10);
                }else
                    Thread.sleep(1000);
            }
            outputStream.flush();
            printingByte = new byte[0];
        } catch (IOException e) {
            if(e.getMessage() != null)
                sendError(e.getMessage(), true);
        } catch (Exception e){
            if(e.getMessage() != null)
                sendError(e.getMessage(), true);
        }
    }

    public void printByte(byte byteItem) {
        printingByte = UtilsGeneral.bytePush(printingByte,byteItem);
    }

//    @Override
//    protected void onPostExecute(Void result) {
//        super.onPostExecute(result);
//        onDestroy();
//    }

    public BluetoothProvider setImage(Bitmap bitmap){
        this.imageBitmap = bitmap;

        return this;
    }

    public Bitmap getImage(){
        return this.imageBitmap;
    }

    public BluetoothProvider printText(String text) {
        if(text == null || text.isEmpty())
            return this;

        String newString = UtilsHtml.brToLB(text,0);
        newString = UtilsHtml.runAllHtmMethod(newString);
        try {
            printingByte = UtilsGeneral.bytePushAll(printingByte,newString.getBytes("UTF-8"));
            printByteArray(PrinterCommands.FEED_LINE_2);
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        return this;
    }


    public BluetoothProvider printImageText(String text) {
        if(text == null || text.isEmpty())
            return this;

        printImage(UtilsImage.base64ToBitmap(text));

        return this;
    }

    public BluetoothProvider printImage(Bitmap imageBitmap) {
        if(imageBitmap == null)
            return this;

        if(imageBitmap != null){
            byte[] command = UtilsImage.decodeBitmap(imageBitmap);
            printingByte = UtilsGeneral.bytePushAll(printingByte,command);
        }
        return this;
    }

    public BluetoothProvider printByteArray(byte[] printingBytes) {
        if(printingBytes == null)
            return this;

        printingByte = UtilsGeneral.bytePushAll(this.printingByte,printingBytes);

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

    public void setCallback(BluetoothCallback callback) {
        this.callback = callback;
    }


    public void setActivity(Activity mActivity) {
        this.mActivity = mActivity;
        getDeviceAddress();
    }

    public void execute() {
        printProcess();

        onDestroy();
    }
}

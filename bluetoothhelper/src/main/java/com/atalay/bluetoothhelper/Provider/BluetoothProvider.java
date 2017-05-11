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
import android.graphics.Canvas;
import android.graphics.ColorMatrix;
import android.graphics.ColorMatrixColorFilter;
import android.graphics.Paint;
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
import java.util.UUID;

import static android.R.attr.width;

/**
 * Created by baris on 9.03.2017.
 */

public class BluetoothProvider extends AsyncTask<BluetoothProvider.PrintType, Void, Void> implements PermissionCallback {
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
    private byte[] printingByte ;
    private BluetoothCallback callback;
    private int copyCount = 1;
    private Bitmap imageBitmap = null;


    public enum PrintType{TEXT, IMAGE, BYTE}
    //endregion

    //region Public
    public boolean bluetoothConEnable;
    //endregion


    public BluetoothProvider(Activity mActivity, BluetoothCallback callback){
        this.mActivity = mActivity;
        this.callback = callback;

        getDeviceAddress();
//        createBluetoothReceiver();

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
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                mSocket.connect();

                loadStream(mSocket.getOutputStream());
            } catch (IOException e) {

                //sendError(R.string.reconnected,false);

                try {
                    mSocket =(BluetoothSocket) pairedDevice.getClass().getMethod("createRfcommSocket", new Class[] {int.class}).invoke(pairedDevice,1);
                    Thread.sleep(1000);
                    mSocket.connect();

                    loadStream(mSocket.getOutputStream());

                    //sendError(R.string.connected,false);
                } catch (Exception  e1) {
                    sendError(e1.getMessage(),false);
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

    /**
     * Use @print
     * */
    @Deprecated
    public BluetoothProvider printText(String message) {
        printingText = message;

        return this;
    }

    private boolean printText() {
        String newString = "";

        if(!isTest)
            newString = printingText;
        else
            newString = prepareTestData();

        newString = UtilsHtml.brToLB(newString, 5);
        newString = UtilsHtml.runAllHtmMethod(newString);

        try {
            for(int i=0;i<copyCount;i++) {
                outputStream.write(newString.getBytes("UTF-8"));

                Thread.sleep(1000);
            }

            outputStream.flush();
        } catch (IOException e) {
            if(e.getMessage() != null)
                sendError(e.getMessage(), true);
        } catch (InterruptedException e) {
            if(e.getMessage() != null)
                sendError(e.getMessage(), true);
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

    @Override
    protected Void doInBackground(PrintType... params) {
        if (haveError)
            return null;

        if(!UtilsGeneral.bluetoothIsEnabled()){
            sendError(R.string.err_bluetooth_notenabled, true);
            return null;
        }

        switch (params[0]){
            case TEXT: printText(); break;
            case IMAGE: printImage(); break;
            case BYTE: printByte(); break;
        }

        return null;
    }

    private void printByte() {
        if(printingByte == null || printingByte.length == 0)
            return;

        try {
            for(int i=0;i<copyCount;i++) {
                outputStream.write(printingByte);

                Thread.sleep(1000);
            }

            outputStream.flush();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    private void printImage() {
        Bitmap newBitmap = null;

        if(!printingText.isEmpty())
            newBitmap = UtilsImage.base64ToBitmap(printingText);
        else
            newBitmap = imageBitmap;

        if(newBitmap != null){
            byte[] command = UtilsImage.decodeBitmap(newBitmap);
            try {
                for(int i=0;i<copyCount;i++) {
                    outputStream.write(command);

                    Thread.sleep(1000);
                }
                outputStream.flush();
            } catch (IOException e) {
                e.printStackTrace();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    protected void onPostExecute(Void result) {
        super.onPostExecute(result);
        onDestroy();
    }

    public BluetoothProvider setImage(Bitmap bitmap){
        this.imageBitmap = bitmap;

        return this;
    }

    public Bitmap getImage(){
        return this.imageBitmap;
    }

    public BluetoothProvider print(String text) {
        printingText = text;
        return this;
    }

    public BluetoothProvider print(byte[] printingBytes) {
        this.printingByte = printingBytes;

        return this;
    }

    /**
     * For very long text. Examp: Base64Image
     * */
    public BluetoothProvider printAddText(String text) {
        printingText += text;
        return this;
    }

}

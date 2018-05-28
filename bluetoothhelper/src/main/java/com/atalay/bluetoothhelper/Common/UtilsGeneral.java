package com.atalay.bluetoothhelper.Common;

import android.bluetooth.BluetoothAdapter;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Base64;

/**
 * Created by baris on 10.03.2017.
 */

public class UtilsGeneral {
    public static boolean bluetoothIsEnabled(){
        BluetoothAdapter mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (mBluetoothAdapter == null) {
            return false;
        } else {
            return mBluetoothAdapter.isEnabled();
        }
    }

    public static byte[] bytePush(byte[] array, byte push) {
        if(array == null) return new byte[0];

        byte[] longer = new byte[array.length + 1];
        System.arraycopy(array, 0, longer, 0, array.length);
        longer[array.length] = push;
        return longer;
    }

    public static byte[] bytePushAll(byte[] array, byte[] push) {
        byte[] result;

        if(array == null)
            result = new byte[0];
        else
            result = array;

        if(push == null) return result;

        for(int i=0;i<push.length;i++)
            result = bytePush(result,push[i]);

        return result;
    }


}

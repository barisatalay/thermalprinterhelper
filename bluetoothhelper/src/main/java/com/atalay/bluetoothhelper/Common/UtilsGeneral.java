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
            if (!mBluetoothAdapter.isEnabled()) {
                return false;
            }
        }
        return true;
    }

    public static byte[] bytePush(byte[] array, byte push) {
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

        for(int i=0;i<push.length;i++)
            result = bytePush(result,push[i]);

        return result;
    }


}

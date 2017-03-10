package com.atalay.bluetoothhelper.Common;

import android.app.Activity;
import android.support.v7.app.AlertDialog;

import com.atalay.bluetoothhelper.R;

/**
 * Created by baris on 9.03.2017.
 */

public class UtilsDialog {
    public static AlertDialog.Builder createAlertDialog(Activity mActivity){
        return new AlertDialog.Builder(mActivity, R.style.MyDialogTheme);
    }
}

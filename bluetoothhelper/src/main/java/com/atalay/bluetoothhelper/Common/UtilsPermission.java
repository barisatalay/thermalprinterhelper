package com.atalay.bluetoothhelper.Common;

import android.app.Activity;
import android.widget.Toast;

import com.atalay.bluetoothhelper.Model.PermissionCallback;
import com.karumi.dexter.Dexter;
import com.karumi.dexter.MultiplePermissionsReport;
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.PermissionRequest;
import com.karumi.dexter.listener.multi.MultiplePermissionsListener;

import java.util.List;

/**
 * Created by baris on 10.03.2017.
 */

public class UtilsPermission {
    public static void checkPermission(final Activity mActivity, final PermissionCallback callback, final int errorResource, String... permissions){
        Dexter.withActivity(mActivity)
                .withPermissions(permissions)
                .withListener(new MultiplePermissionsListener() {
                    @Override public void onPermissionsChecked(MultiplePermissionsReport report) {
                        if(report.getDeniedPermissionResponses().size() > 0) {
                            if(callback != null)
                                callback.denied(report);
                            Toast.makeText(mActivity.getApplicationContext(), mActivity.getString(errorResource), Toast.LENGTH_LONG).show();
                        }else{
                            if(callback != null)
                                callback.successful(report);
                        }
                    }
                    @Override public void onPermissionRationaleShouldBeShown(List<PermissionRequest> permissions, PermissionToken token) {
                        token.continuePermissionRequest();
                    }
                }).check();
    }

    /*
    * -1: Yetkisi yok 0: Yetkisi var
    * */
    public static int checkPermissionManuel(Activity mActivity, String permission, int requestCode){
        int hasWriteContactsPermission = 0;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
            hasWriteContactsPermission = mActivity.checkSelfPermission(permission);

        }else{
            hasWriteContactsPermission = 0;
        }
        return hasWriteContactsPermission;
    }
}

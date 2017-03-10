package com.atalay.bluetoothhelper.Model;

import com.karumi.dexter.MultiplePermissionsReport;

/**
 * Created by baris on 10.03.2017.
 */

public interface PermissionCallback {
    void successful(MultiplePermissionsReport report);

    void denied(MultiplePermissionsReport report);
}

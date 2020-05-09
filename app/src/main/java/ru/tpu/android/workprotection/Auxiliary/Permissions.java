package ru.tpu.android.workprotection.Auxiliary;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;

import androidx.core.app.ActivityCompat;

public class Permissions {
    //переменные, необходимые для разрешения сохранения файлов
    private static final int REQUEST_EXTERNAL_STORAGE = 1;
    private static String[] PERMISSIONS_STORAGE = {
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
    };

    //получение разрешения на сохранение файлов на смартфон
    public static void verifyStoragePermissions(Activity activity) {
        // Check if we have write permission
        int permission = ActivityCompat.checkSelfPermission(activity, Manifest.permission.WRITE_EXTERNAL_STORAGE);

        if (permission != PackageManager.PERMISSION_GRANTED) {
            // We don't have permission so prompt the user
            ActivityCompat.requestPermissions(
                    activity,
                    PERMISSIONS_STORAGE,
                    REQUEST_EXTERNAL_STORAGE
            );
        }
    }

    public static boolean checkPermission(Context context) {
        String requiredPermission = Manifest.permission.WRITE_EXTERNAL_STORAGE;
        int checkValWrite = context.checkCallingOrSelfPermission(PERMISSIONS_STORAGE[0]);
        int checkValRead = context.checkCallingOrSelfPermission(PERMISSIONS_STORAGE[1]);
        if ((checkValWrite==PackageManager.PERMISSION_GRANTED)&&(checkValRead==PackageManager.PERMISSION_GRANTED))
            return true;
        else return false;
    }
}

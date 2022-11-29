package com.chacha.notigif;

import static android.Manifest.permission.READ_EXTERNAL_STORAGE;
import static android.Manifest.permission.WRITE_EXTERNAL_STORAGE;
import static android.os.Build.VERSION.SDK_INT;
import static com.chacha.notigif.Files.createFiles;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.Settings;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

public class Permissions {
    public static void checkPermission(MainActivity mainActivity) {
        if (SDK_INT >= Build.VERSION_CODES.R) {
            if (Environment.isExternalStorageManager()) {
                createFiles(mainActivity);
            } else {
                requestPermission(mainActivity);
            }
        } else {
            int result = ContextCompat.checkSelfPermission(mainActivity, READ_EXTERNAL_STORAGE);
            int result1 = ContextCompat.checkSelfPermission(mainActivity, WRITE_EXTERNAL_STORAGE);
            if (result == PackageManager.PERMISSION_GRANTED && result1 == PackageManager.PERMISSION_GRANTED){
                createFiles(mainActivity);
            } else {
                requestPermission(mainActivity);
            }
        }
    }

    public static void requestPermission(MainActivity mainActivity) {
        if (SDK_INT >= Build.VERSION_CODES.R) {
            try {
                Intent intent = new Intent(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION);
                intent.addCategory("android.intent.category.DEFAULT");
                intent.setData(Uri.parse(String.format("package:%s", mainActivity.getPackageName())));
                mainActivity.startActivityForResult(intent, 2296);
            } catch (Exception e) {
                Intent intent = new Intent();
                intent.setAction(Settings.ACTION_MANAGE_ALL_FILES_ACCESS_PERMISSION);
                mainActivity.startActivityForResult(intent, 2296);
            }
        } else {
            //below android 11
            ActivityCompat.requestPermissions(mainActivity, new String[]{WRITE_EXTERNAL_STORAGE}, 10);
        }
    }
}

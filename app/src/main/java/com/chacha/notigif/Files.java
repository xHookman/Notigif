package com.chacha.notigif;

import static com.chacha.notigif.CopyAssetsFiles.copyAssetFolder;

import android.content.Context;
import android.os.Environment;
import android.util.Log;
import android.widget.Toast;

import java.io.File;
import java.util.Objects;

public class Files {
    static File dir = new File(Environment.getExternalStorageDirectory() + "/.Notigif/");

    public static void readfile(MainActivity mainActivity) {
        int i = 0;
        String name = "";
        try {
            for (File f : Objects.requireNonNull(Files.dir.listFiles())) {
                if (f.isFile())
                    name = f.getName();
                mainActivity.imagenames[i] = Environment.getExternalStorageDirectory().getPath() + "/.Notigif/" + name;
                i++;
            }
            // make something with the name
        } catch (NullPointerException e) {
            Permissions.checkPermission(mainActivity);
        }
    }

    public static void createFiles(MainActivity mainActivity) {
        try {
            dir = new File(Environment.getExternalStorageDirectory().getPath() + "/.Notigif/");
            if (!dir.exists()) {
                dir.mkdirs();
                copyAssetFolder(mainActivity.getAssets(), "gifs", dir.getPath());
            }
            dir.listFiles();
            mainActivity.startProgram();
        }catch (Exception e){
            Permissions.requestPermission(mainActivity);
        }
    }
}

package com.chacha.notigif;

import static com.chacha.notigif.Preferences.editor;

import android.app.AlertDialog;
import android.app.DownloadManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Environment;
import android.widget.Toast;

import com.coniy.fileprefs.FileSharedPreferences;

import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;

public class GifUtil {
    protected static void downloadGif(String mediaID, Context ctx, MainActivity mainActivity) {
        String url = "https://media.giphy.com/media/" + mediaID + "/giphy.gif";
        String fileName = mediaID + ".gif";
        DownloadManager downloadmanager = (DownloadManager) ctx.getSystemService(Context.DOWNLOAD_SERVICE);
        Uri uri = Uri.parse(url);
        DownloadManager.Request request = new DownloadManager.Request(uri);
        request.setAllowedNetworkTypes(DownloadManager.Request.NETWORK_WIFI | DownloadManager.Request.NETWORK_MOBILE);
        request.setAllowedOverRoaming(true);
        request.setTitle("Notigif");
        request.setDescription("Downloading...");
        request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);
        request.setVisibleInDownloadsUi(false);
        request.setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, fileName);
        downloadmanager.enqueue(request);
        BroadcastReceiver onComplete = new BroadcastReceiver() {
            public void onReceive(Context context, Intent intent) {
                File src = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS) + "/" + fileName);
                try {
                    FileUtils.moveFileToDirectory(src, Files.dir, false);
                    Toast.makeText(context, "Downloaded !", Toast.LENGTH_SHORT).show();
                    mainActivity.gridViewSelectedItemPosition++;
                    editor.putInt("ChoosenImagePos"+mainActivity.gifPosition, mainActivity.gridViewSelectedItemPosition);
                    editor.commit();
                    FileSharedPreferences.makeWorldReadable(context.getPackageName(), "user_settings");
                    mainActivity.refreshGridView();
                } catch (IOException ignored) {}
            }
        };
        ctx.registerReceiver(onComplete, new IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE));
    }

    protected static void showMobileDataWarning(String mediaID, Context ctx, MainActivity mainActivity) {
        AlertDialog.Builder builder = new AlertDialog.Builder(ctx, 4);
        builder.setMessage("Paid attention, if you choose to download this gif it will use your mobile data (it may cause costs) ! ")
                .setNegativeButton("Do not download !", (dialog, id) -> {
                })
                .setPositiveButton("Never show & download", (dialog, id) -> {
                    editor.putBoolean("showMobileDataWarning", false);
                    editor.apply();
                    FileSharedPreferences.makeWorldReadable(ctx.getPackageName(), "user_settings");
                    GifUtil.downloadGif(mediaID, ctx, mainActivity);
                });
        AlertDialog alert = builder.create();
        alert.show();
    }

    protected static boolean isWifiEnabled(Context ctx) {
        ConnectivityManager cm = (ConnectivityManager) ctx.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo ni = cm.getActiveNetworkInfo();
        return (ni != null && ni.getType() == ConnectivityManager.TYPE_WIFI);
    }
}

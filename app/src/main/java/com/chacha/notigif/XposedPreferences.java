package com.chacha.notigif;

import static com.chacha.notigif.Utils.MY_PACKAGE_NAME;

import android.os.Environment;
import java.io.File;
import de.robv.android.xposed.XSharedPreferences;
import de.robv.android.xposed.XposedBridge;

public class XposedPreferences extends Preferences {
    private static XSharedPreferences pref;

    private static XSharedPreferences getPref() {
        XSharedPreferences pref = new XSharedPreferences(MY_PACKAGE_NAME, Utils.PREFERENCES_FILE_NAME);
        return pref.getFile().canRead() ? pref : null;
    }

    private static XSharedPreferences getLegacyPrefs() {
        File f = new File(Environment.getDataDirectory(), "data/" + MY_PACKAGE_NAME + "/shared_prefs/" + Utils.PREFERENCES_FILE_NAME + ".xml");
        return new XSharedPreferences(f);
    }

    public static XSharedPreferences loadPreferences() {
        if (XposedBridge.getXposedVersion() < 93) {
            pref = getLegacyPrefs();
        } else {
            pref = getPref();
        }

        if (pref != null) {
            pref.reload();
        } else {
            XposedBridge.log("Can't load preference in the module");
        }

        return pref;
    }

    public static boolean hasPrefsChanged(){
        return pref.hasFileChanged();
    }
    public static void reloadPrefs(){
        pref.reload();
    }

    public static XSharedPreferences getPrefs(){
        return pref;
    }
}

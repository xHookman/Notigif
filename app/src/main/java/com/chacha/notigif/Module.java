package com.chacha.notigif;

import android.app.AndroidAppHelper;
import android.content.Context;
import android.graphics.Color;
import android.net.Uri;
import android.os.Environment;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import com.bumptech.glide.Glide;
import java.io.File;
import de.robv.android.xposed.IXposedHookInitPackageResources;
import de.robv.android.xposed.IXposedHookLoadPackage;
import de.robv.android.xposed.IXposedHookZygoteInit;
import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XC_MethodReplacement;
import de.robv.android.xposed.XSharedPreferences;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.XposedHelpers;
import de.robv.android.xposed.callbacks.XC_InitPackageResources;
import de.robv.android.xposed.callbacks.XC_LayoutInflated;
import de.robv.android.xposed.callbacks.XC_LoadPackage;

import static de.robv.android.xposed.XposedHelpers.callMethod;
import static de.robv.android.xposed.XposedHelpers.findAndHookMethod;

public class Module implements IXposedHookInitPackageResources, IXposedHookLoadPackage, IXposedHookZygoteInit {
    String SYSTEMUI_PACKAGE_NAME = "com.android.systemui";
    static String MY_PACKAGE_NAME = "com.chacha.notigif";
    String choosenImagePath;
    XSharedPreferences pref;
    int i, gifPosCount;
    int size, right_margin, left_margin, bottom_margin, top_margin, horizontalSpinnerPosition, verticalSpinnerPosition, plansSpinnerPosition;
    float transparency;
    boolean carrierLabelBoolean, disableBoolean, fade, showLock;
    FrameLayout gifFrameLayout, allGifsLayout;
    FrameLayout BgLayout;
    FrameLayout.LayoutParams frameLayoutparms, frameLayoutParams2;
    ImageView imageview;
    Context ctx;

    public void prefLoad(){
        if(XposedBridge.getXposedVersion() < 93) {
            pref = getLegacyPrefs();
        } else {
            pref = getPref();
        }

        if(pref!=null) {
            pref.reload();
        } else {
            XposedBridge.log("Can't load preference in the module");
        }
    }

    @Override
    public void initZygote(StartupParam startupParam) {
        prefLoad();
    }

    public static XSharedPreferences getPref() {
        XSharedPreferences pref = new XSharedPreferences(MY_PACKAGE_NAME, "user_settings");
        return pref.getFile().canRead() ? pref : null;
    }

    private XSharedPreferences getLegacyPrefs() {
        File f = new File(Environment.getDataDirectory(), "data/" + MY_PACKAGE_NAME + "/shared_prefs/user_settings.xml");
        return new XSharedPreferences(f);
    }

    @Override
    public void handleLoadPackage(final XC_LoadPackage.LoadPackageParam lpparam) {
            prefLoad();
            pref.reload();

        fade=pref.getBoolean("fade", true);
        showLock=pref.getBoolean("showLock", true);

        if (lpparam.packageName.equals(MY_PACKAGE_NAME)) {
            findAndHookMethod(MY_PACKAGE_NAME + ".Utils", lpparam.classLoader,
                    "isModuleActive", XC_MethodReplacement.returnConstant(true));
        }

        if(lpparam.packageName.equals(SYSTEMUI_PACKAGE_NAME)) {
            XposedBridge.log("Hooked SystemUI !");
            try {
                findAndHookMethod(SYSTEMUI_PACKAGE_NAME + ".statusbar.phone.NotificationPanelViewController", lpparam.classLoader,
                        "onHeightUpdated", float.class, new XC_MethodHook() {
                            @Override
                            protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                                super.beforeHookedMethod(param);
                                boolean mKeyguardShowing = XposedHelpers.getBooleanField(param.thisObject, "mKeyguardShowing");
                                if (pref.hasFileChanged()) {
                                    prefLoad();
                                    gifPosCount = pref.getInt("gifPosCount", 1);
                                    fade = pref.getBoolean("fade", true);
                                    showLock = pref.getBoolean("showLock", true);
                                    allGifsLayout.removeAllViews();
                                    for (i = 0; i < gifPosCount; i++) {
                                        prefs(i);
                                        if (!disableBoolean) {
                                            refreshImage(ctx);
                                        }
                                    }
                                }
                                if (!mKeyguardShowing)
                                    if(fade)
                                        gifFrameLayout.setAlpha((float) param.args[0] / ((int) callMethod(param.thisObject, "getMaxPanelHeight")));
                                    else
                                        gifFrameLayout.setAlpha(1);
                                else
                                    if(!showLock)
                                        gifFrameLayout.setAlpha(0); //Without this line the matrix will randomly be showed on lockscreen
                                    else
                                        gifFrameLayout.setAlpha(1);
                            }
                        });
            } catch (XposedHelpers.ClassNotFoundError e){
                findAndHookMethod(SYSTEMUI_PACKAGE_NAME + ".statusbar.phone.NotificationPanelView", lpparam.classLoader, //Android Pie
                        "onHeightUpdated", float.class, new XC_MethodHook() {
                            @Override
                            protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                                super.beforeHookedMethod(param);
                                boolean mKeyguardShowing = XposedHelpers.getBooleanField(param.thisObject, "mKeyguardShowing");
                                if (pref.hasFileChanged()) {
                                    prefLoad();
                                    gifPosCount = pref.getInt("gifPosCount", 1);
                                    fade = pref.getBoolean("fade", true);
                                    showLock = pref.getBoolean("showLock", true);
                                    allGifsLayout.removeAllViews();
                                    for (i = 0; i < gifPosCount; i++) {
                                        prefs(i);
                                        if (!disableBoolean) {
                                            refreshImage(ctx);
                                        }
                                    }
                                }
                                if (!mKeyguardShowing)
                                    if(fade)
                                        gifFrameLayout.setAlpha((float) param.args[0] / ((int) callMethod(param.thisObject, "getMaxPanelHeight")));
                                    else
                                        gifFrameLayout.setAlpha(1);
                                else
                                if(!showLock)
                                    gifFrameLayout.setAlpha(0); //Without this line the matrix will randomly be showed on lockscreen
                                else
                                    gifFrameLayout.setAlpha(1);
                            }
                        });
            }

            findAndHookMethod("com.android.keyguard.KeyguardUpdateMonitor", lpparam.classLoader,
                    "onDisplayChanged", int.class, new XC_MethodHook() {
                        @Override
                        protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                            super.beforeHookedMethod(param);
                            boolean mIsScreenOffInDexMode = XposedHelpers.getBooleanField(param.thisObject, "mIsScreenOffInDexMode");
                            if(mIsScreenOffInDexMode)
                                gifFrameLayout.setAlpha(1);
                            else
                                gifFrameLayout.setAlpha(0);
                        }
                    });
                }
            }

    public void prefs(int gifposition){
        size = pref.getInt("size"+gifposition, 390);
        transparency = pref.getInt("transparency"+gifposition, 100);
        right_margin = pref.getInt("marginRight"+gifposition, 0);
        left_margin = pref.getInt("marginLeft"+gifposition, 0);
        bottom_margin = pref.getInt("marginBottom"+gifposition, 0);
        top_margin = pref.getInt("marginTop"+gifposition, 0);
        horizontalSpinnerPosition = pref.getInt("horizontalSpinnerPosition"+gifposition, 2);
        verticalSpinnerPosition = pref.getInt("verticalSpinnerPosition"+gifposition, 1);
        plansSpinnerPosition = pref.getInt("plansSpinnerPosition"+gifposition, 0);
        disableBoolean = pref.getBoolean("disableBoolean"+gifposition, false);
        carrierLabelBoolean = pref.getBoolean("carrierLabelBoolean", false);
        choosenImagePath = pref.getString("ChoosenImagePath"+gifposition, "");
    }

    @Override
    public void handleInitPackageResources(XC_InitPackageResources.InitPackageResourcesParam resparam) {
            if (!resparam.packageName.equals(SYSTEMUI_PACKAGE_NAME))
                return;
            resparam.res.hookLayout(SYSTEMUI_PACKAGE_NAME, "layout", "status_bar_expanded", new XC_LayoutInflated() {
                @Override
                public void handleLayoutInflated(LayoutInflatedParam liparam) {
                    gifPosCount = pref.getInt("gifPosCount", 1);
                    BgLayout = liparam.view.findViewById(liparam.res.getIdentifier("notification_panel", "id", SYSTEMUI_PACKAGE_NAME));
                    for (i = 0; i < gifPosCount; i++) {
                        prefs(i);
                        if (!disableBoolean) { //Execute next code only if the gif is not disabled
                            ctx = AndroidAppHelper.currentApplication();
                            allGifsLayout = new FrameLayout(ctx);
                            frameLayoutParams2 = new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
                            allGifsLayout.setLayoutParams(frameLayoutParams2);
                            allGifsLayout.requestLayout();
                            frameLayoutparms = new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);

                            refreshImage(ctx);
                        }
                    }
                    try {
                        TextView carrierTextView = liparam.view.findViewById(liparam.res.getIdentifier("notification_panel_carrier_label", "id", SYSTEMUI_PACKAGE_NAME));
                        if (!carrierLabelBoolean) {
                            carrierTextView.setTextColor(Color.WHITE);
                        } else {
                            carrierTextView.setTextColor(Color.TRANSPARENT);
                        }
                    } catch (Exception e) {
                        try {
                            LinearLayout carrierLabelLinearLayout = liparam.view.findViewById(liparam.res.getIdentifier("panel_carrier_label_container", "id", SYSTEMUI_PACKAGE_NAME));
                            if (carrierLabelBoolean) { //User want a hidden carrier label
                                ViewGroup.LayoutParams layoutParams = carrierLabelLinearLayout.getLayoutParams();
                                layoutParams.width = 0;
                                layoutParams.height = 0;
                                carrierLabelLinearLayout.setLayoutParams(layoutParams);
                                carrierLabelLinearLayout.requestLayout();
                            }
                        } catch (Exception ignored) {
                        }
                    }
                }
            });
    }
    public void refreshImage(Context ctx){
        imageview = new ImageView(ctx);
        gifFrameLayout = new FrameLayout(ctx);

        if (verticalSpinnerPosition == 0) {
            frameLayoutparms.gravity = Gravity.TOP;
        } else if (verticalSpinnerPosition == 1) {
            frameLayoutparms.gravity = Gravity.BOTTOM;
        } else {
            frameLayoutparms.gravity = Gravity.CENTER;
        }
        frameLayoutparms.rightMargin = right_margin;
        frameLayoutparms.leftMargin = left_margin;
        frameLayoutparms.bottomMargin = bottom_margin;
        frameLayoutparms.topMargin = top_margin;

        gifFrameLayout.setLayoutParams(frameLayoutparms);
        gifFrameLayout.requestLayout();

        FrameLayout.LayoutParams imageViewparms;
        if (size != 0) {
            imageViewparms = new FrameLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT + size, ViewGroup.LayoutParams.WRAP_CONTENT + size);///OOO
        } else {
            imageViewparms = new FrameLayout.LayoutParams(0, 0);
        }
        if (horizontalSpinnerPosition == 0) {
            imageViewparms.gravity = Gravity.START;
        } else if (horizontalSpinnerPosition == 1) {
            imageViewparms.gravity = Gravity.END;
        } else {
            imageViewparms.gravity = Gravity.CENTER;
        }
        imageview.setLayoutParams(imageViewparms);
        imageview.requestLayout();
        imageview.setAdjustViewBounds(true);
        gifFrameLayout.addView(imageview);
        allGifsLayout.addView(gifFrameLayout);
        try {
            Glide
                    .with(ctx)
                    .load(Uri.fromFile(new File(choosenImagePath)))
                    .into(imageview);
        } catch (NullPointerException ignored) {
        }
        imageview.setAlpha(transparency / 100);
        BgLayout.removeView(allGifsLayout);
        switch (plansSpinnerPosition){
            case 0:
                BgLayout.addView(allGifsLayout, BgLayout.getChildCount());
                break;
            case 1:
                BgLayout.addView(allGifsLayout, 1);
                break;
        }
    }
}
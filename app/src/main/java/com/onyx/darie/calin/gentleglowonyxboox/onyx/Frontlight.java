package com.onyx.darie.calin.gentleglowonyxboox.onyx;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.provider.Settings;

public class Frontlight {
    private Frontlight() {}

    @SuppressLint("StaticFieldLeak") // because we're storing the application context
    private static Context applicationContext;

    public static boolean hasPermissions() {
        return Settings.System.canWrite(applicationContext);
    }

    public static void injectApplicationContext(Context context) {
        Frontlight.applicationContext = context;
    }

    public static Intent getPermissionsIntent() {
        final Intent intent = new Intent(android.provider.Settings.ACTION_MANAGE_WRITE_SETTINGS);
        intent.setData(Uri.parse("package:" + applicationContext.getPackageName()));
        return intent;
    }
}

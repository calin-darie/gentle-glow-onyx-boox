package com.onyx.darie.calin.gentleglowonyxboox;

import android.app.Application;
import android.content.Context;


public class GentleGlowApplication extends Application {

    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(base);
        Frontlight.initContext(base);
    }

}

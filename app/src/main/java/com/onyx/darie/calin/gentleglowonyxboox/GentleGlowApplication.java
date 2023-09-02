package com.onyx.darie.calin.gentleglowonyxboox;

import android.app.Application;
import android.content.Context;


public class GentleGlowApplication extends Application {
    private Dependencies dependencies;
    public Dependencies getDependencies() { return dependencies; }

    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(base);
        Frontlight.injectApplicationContext(base);
        dependencies = new Dependencies(base);
    }

}

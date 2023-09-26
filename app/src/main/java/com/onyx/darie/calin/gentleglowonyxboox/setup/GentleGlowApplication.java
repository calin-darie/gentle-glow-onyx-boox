package com.onyx.darie.calin.gentleglowonyxboox.setup;

import android.app.Application;
import android.content.Context;

import com.onyx.darie.calin.gentleglowonyxboox.onyx.Frontlight;
import com.onyx.darie.calin.gentleglowonyxboox.onyx.setup.OnyxSetup;
import com.onyx.darie.calin.gentleglowonyxboox.schedule.setup.ScheduleDependencies;

public class GentleGlowApplication extends Application {
    private Dependencies dependencies;

    public Dependencies getDependencies() { return dependencies; }

    private ScheduleDependencies scheduleDependencies;
    public ScheduleDependencies getScheduleDependencies() {
        return scheduleDependencies;
    }


    @Override
    protected void attachBaseContext(Context context) {
        super.attachBaseContext(context);
        Frontlight.injectApplicationContext(context);
        dependencies = OnyxSetup.getDependencies(context);

        scheduleDependencies = new ScheduleDependencies(context, dependencies);
    }
}

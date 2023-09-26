package com.onyx.darie.calin.gentleglowonyxboox.schedule.setup;

import android.content.Context;

import com.onyx.darie.calin.gentleglowonyxboox.schedule.LightScheduler;
import com.onyx.darie.calin.gentleglowonyxboox.setup.Dependencies;
import com.onyx.darie.calin.gentleglowonyxboox.storage.FileStorage;

public class ScheduleDependencies {
    private LightScheduler lightScheduler;
    private Context context;
    private final Dependencies dependencies;

    public ScheduleDependencies(Context context, Dependencies dependencies) {
        this.context = context;
        this.dependencies = dependencies;
    }

    public LightScheduler getLightScheduler() {
        if (lightScheduler != null)
            return lightScheduler;

        lightScheduler = new LightScheduler(
                context,
                new FileStorage<LightScheduler.Schedule>(context.getFilesDir(), "schedule"),
                dependencies.getLightConfigurationEditor());

        return lightScheduler;
    }
}

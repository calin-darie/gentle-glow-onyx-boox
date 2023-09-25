package com.onyx.darie.calin.gentleglowonyxboox.schedule.setup;

import com.onyx.darie.calin.gentleglowonyxboox.schedule.LightScheduler;
import com.onyx.darie.calin.gentleglowonyxboox.schedule.LightSchedulerImpl;

public class ScheduleDependencies {
    private LightScheduler lightScheduler;

    public LightScheduler getLightScheduler() {
        if (lightScheduler != null)
            return lightScheduler;

        lightScheduler = new LightSchedulerImpl();

        return lightScheduler;
    }
}

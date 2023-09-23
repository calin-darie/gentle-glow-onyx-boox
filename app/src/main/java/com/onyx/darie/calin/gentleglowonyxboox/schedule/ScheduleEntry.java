package com.onyx.darie.calin.gentleglowonyxboox.schedule;

import java.time.LocalTime;

public class ScheduleEntry {
    public final LocalTime timeOfDay;
    public final boolean isLightOn;
    public final int lightConfigurationIndex;
    public ScheduleEntry(LocalTime timeOfDay, boolean isLightOn, int lightConfigurationIndex) {
        this.timeOfDay = timeOfDay;
        this.isLightOn = isLightOn;
        this.lightConfigurationIndex = lightConfigurationIndex;
    }
}

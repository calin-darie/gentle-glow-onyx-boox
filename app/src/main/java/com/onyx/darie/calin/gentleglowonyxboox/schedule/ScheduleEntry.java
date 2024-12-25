package com.onyx.darie.calin.gentleglowonyxboox.schedule;

import java.time.LocalTime;

public class ScheduleEntry {
    public final LocalTime timeOfDay;
    public final ScheduledLightState scheduledLightState;
    public ScheduleEntry(LocalTime timeOfDay, ScheduledLightState scheduledLightState) {
        this.timeOfDay = timeOfDay;
        this.scheduledLightState = scheduledLightState;
    }


    @Override
    public String toString() {
        return "ScheduleEntry{" +
                "timeOfDay=" + timeOfDay +
                ", scheduledLightState=" + scheduledLightState +
                '}';
    }
}

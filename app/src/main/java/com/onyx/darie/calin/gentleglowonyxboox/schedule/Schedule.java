package com.onyx.darie.calin.gentleglowonyxboox.schedule;

import java.time.LocalTime;

public class Schedule implements Cloneable {
    public static final Schedule Default = new Schedule()
            .withNewEntry(LocalTime.parse("20:00"), true, 0)
            .withNewEntry(LocalTime.parse("04:00"), true, 1)
            .withNewEntry(LocalTime.parse("08:00"), false, 0)
            .withNewEntry(LocalTime.parse("18:00"), true, 3);

    private Schedule withNewEntry(LocalTime time, boolean isLightOn, int lightConfigurationIndex) {
        try {
            Schedule newSchedule = (Schedule) this.clone();
            return newSchedule;
        } catch (CloneNotSupportedException e) {
            return null; // this will never happen
        }
    }
}

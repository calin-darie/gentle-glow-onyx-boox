package com.onyx.darie.calin.gentleglowonyxboox.schedule;

public class ScheduledLightState {
    public final boolean isOn;
    public final String lightConfigurationNameMain;
    public final int LightConfigurationIndexFallback;

    public ScheduledLightState(boolean isOn,
                               String lightConfigurationNameMain,
                               int lightConfigurationIndexFallback) {
        this.isOn = isOn;
        this.lightConfigurationNameMain = lightConfigurationNameMain;
        LightConfigurationIndexFallback = lightConfigurationIndexFallback;
    }

    public static ScheduledLightState off() {
        return new ScheduledLightState(false, "", 0);
    }

    public static ScheduledLightState onWithConfiguration(String configurationNameMain, int configurationIndexFallback) {
        return new ScheduledLightState(true, configurationNameMain, configurationIndexFallback);
    }

    @Override
    public String
    toString() {
        return "ScheduledLightState{" +
                "isOn=" + isOn +
                ", lightConfigurationNameMain='" + lightConfigurationNameMain + '\'' +
                ", LightConfigurationIndexFallback=" + LightConfigurationIndexFallback +
                '}';
    }
}

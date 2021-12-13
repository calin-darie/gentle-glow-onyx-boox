package com.onyx.darie.calin.gentleglowonyxboox;

public interface NativeWarmColdLightController {
    LedGroup warm();
    LedGroup cold();

    Range<Integer> getWarmAndColdBrightnessRange();

    boolean isDeviceSupported();
}


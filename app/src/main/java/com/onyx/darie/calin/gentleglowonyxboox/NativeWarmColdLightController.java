package com.onyx.darie.calin.gentleglowonyxboox;

public interface NativeWarmColdLightController {

    Result turnOn(boolean warm, boolean cold);
    Result turnOff();

    Result<WarmAndColdLedOutput> getLedOutput();
    Result setLedOutput(WarmAndColdLedOutput output);

    Range<Integer> getWarmAndColdLedOutputRange();

    boolean isDeviceSupported();
}


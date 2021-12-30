package com.onyx.darie.calin.gentleglowonyxboox;

import io.reactivex.rxjava3.core.Observable;

public interface NativeWarmColdLightController {

    Result turnOn(boolean warm, boolean cold);
    Result turnOff();

    Result<WarmAndColdLedOutput> getLedOutput();
    Result setLedOutput(WarmAndColdLedOutput output);
    Observable<WarmAndColdLedOutput> getWarmAndColdLedOutput$();

    Range<Integer> getWarmAndColdLedOutputRange();

    boolean isDeviceSupported();
}


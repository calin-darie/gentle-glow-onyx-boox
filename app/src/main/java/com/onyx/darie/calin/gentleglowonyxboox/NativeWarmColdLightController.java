package com.onyx.darie.calin.gentleglowonyxboox;

import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.core.Single;

public interface NativeWarmColdLightController {

    Result turnOn(boolean warm, boolean cold);
    Result turnOff();
    Result turnOn();

    Single<Result> setLedOutput(WarmAndColdLedOutput output);

    Observable<WarmAndColdLedOutput> getWarmAndColdLedOutput$();
    Observable<Boolean> isOn$();

    Range<Integer> getWarmAndColdLedOutputRange();

    boolean isDeviceSupported();

    void toggleOnOff();
}


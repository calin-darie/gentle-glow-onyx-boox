package com.onyx.darie.calin.gentleglowonyxboox;

import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.core.Single;

public interface NativeWarmColdLightController {

    Result turnOn(boolean warm, boolean cold);
    Result turnOff();

    Single<Result> setLedOutput(WarmAndColdLedOutput output);
    Observable<WarmAndColdLedOutput> getWarmAndColdLedOutput$();

    Range<Integer> getWarmAndColdLedOutputRange();

    boolean isDeviceSupported();

}


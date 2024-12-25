package com.onyx.darie.calin.gentleglowonyxboox.light;

import com.onyx.darie.calin.gentleglowonyxboox.util.Result;

import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.core.Single;

public interface NativeLightController<TOutput> {

    Result turnOff();
    Result turnOn();
    void toggleOnOff();
    boolean isOn();
    TOutput getOutput();
    Single<Result> setOutput(TOutput output);

    Observable<Boolean> isOn$();
    Observable<TOutput> getOutput$();

    boolean isDeviceSupported();
}


package com.onyx.darie.calin.gentleglowonyxboox;

import android.content.Intent;

import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.core.Single;

public interface BrightnessAndWarmthLightController {
    Result turnOn();
    Result turnOff();
    Observable<Boolean> isOn$();

    Single<Result> setBrightnessAndWarmth(BrightnessAndWarmth brightnessAndWarmth);

    boolean hasDualFrontlight();
    Intent[] getMissingPermissionIntents();
}


package com.onyx.darie.calin.gentleglowonyxboox;

import android.content.Intent;

import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.core.Single;

class OnyxBrightnessAndWarmthLightController implements BrightnessAndWarmthLightController{
    private final NativeWarmColdLightController nativeWarmColdLightController;
    private final BrightnessAndWarmthToWarmAndColdLedOutputAdapter adapter;

    OnyxBrightnessAndWarmthLightController(NativeWarmColdLightController nativeWarmColdLightController) {
        this.nativeWarmColdLightController = nativeWarmColdLightController;
        this.adapter = new BrightnessAndWarmthToWarmAndColdLedOutputAdapter(
                nativeWarmColdLightController.getWarmAndColdLedOutputRange()
        );
    }

    @Override
    public Result turnOn() {
        return null;
    }

    @Override
    public Result turnOff() {
        return null;
    }

    @Override
    public Observable<Boolean> isOn$() {
        return null;
    }

    @Override
    public Single<Result> setBrightnessAndWarmth(BrightnessAndWarmth brightnessAndWarmth) {
        final WarmAndColdLedOutput warmCold = adapter.toWarmCold(brightnessAndWarmth);
        nativeWarmColdLightController.setLedOutput(warmCold);
        return Single.just(Result.success()); // todo handle errors?
    }

    @Override
    public boolean hasDualFrontlight() {
        return false;
    }

    @Override
    public Intent[] getMissingPermissionIntents() {
        return new Intent[0];
    }
}

package com.onyx.darie.calin.gentleglowonyxboox;

import android.content.Intent;

import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.core.Single;
import io.reactivex.rxjava3.subjects.BehaviorSubject;

//todo rename to BrightnessAndWarmthLightController?
public class Light {
    public Observable<Boolean> isOn$() { return null; }

    public void turnOn() { nativeWarmColdLightController.turnOn(true, true); } // todo result?

    public void turnOff() { nativeWarmColdLightController.turnOff(); } // todo result?

    private BehaviorSubject<BrightnessAndWarmth> brightnessAndWarmth = BehaviorSubject.create();
    public Observable<BrightnessAndWarmth> brightnessAndWarmth$() { return brightnessAndWarmth; }

    public Single<Result> setBrightnessAndWarmth (BrightnessAndWarmth brightnessAndWarmth) {
        final WarmAndColdLedOutput warmCold = adapter.toWarmAndColdLedOutput(brightnessAndWarmth);
        nativeWarmColdLightController.setLedOutput(warmCold);
        return Single.just(Result.success()); // todo handle errors?
    }

    public Single<Result> applyDeltaBrightness(int delta) {
        final Result<BrightnessAndWarmth> brightnessAndWarmthResult = brightnessAndWarmth.getValue()
                .withDeltaBrightness(delta);
        if (brightnessAndWarmthResult.hasError()) {
            return Single.just(brightnessAndWarmthResult);
        }
        return setBrightnessAndWarmth(brightnessAndWarmthResult.value);
    }

    public Single<Result> applyDeltaWarmth(int delta) {
        final Result<BrightnessAndWarmth> brightnessAndWarmthResult = brightnessAndWarmth.getValue()
                .withDeltaWarmth(delta);
        if (brightnessAndWarmthResult.hasError()) {
            return Single.just(brightnessAndWarmthResult);
        }
        return setBrightnessAndWarmth(brightnessAndWarmthResult.value);
    }

    ///////////// one time checks
    public boolean isDeviceSupported() { return nativeWarmColdLightController.isDeviceSupported(); }
    public Intent[] missingPermissionIntents() { return new Intent[0]; }

    private NativeWarmColdLightController nativeWarmColdLightController;
    private final BrightnessAndWarmthToWarmAndColdLedOutputAdapter adapter;

    public Light(
            NativeWarmColdLightController nativeWarmColdLightController,
            BrightnessAndWarmthToWarmAndColdLedOutputAdapter adapter) {
        this.nativeWarmColdLightController = nativeWarmColdLightController;
        this.adapter = adapter;
    }



    ////////////// light configuration editor ///////////////
    // accesses Light: set to preview changes, read to update current config
    // 1. load state of MutuallyExclusiveChoice<>
    // 2. ask OnyxLight.areCurrentBrightnessAndWarmthEqualTo(savedConfiguration.brightnessAndWarmth)
    // setCurrent(index) / setCurrent (LightConfiguration)
    // presets
    // replaceCurrentWith(preset)
    // renameCurrent(string name)
    //
    // boolean isCurrentConfigurationEditedOnBrightnessAndWarmthChanges()
    // ! simply don't update current if light changes while can't edit
    // todo migrateSavedSettings()


    // todo schedule
    // Intent.ACTION_SCREEN_ON
}
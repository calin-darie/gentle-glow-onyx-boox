package com.onyx.darie.calin.gentleglowonyxboox;

import android.content.Intent;

import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.core.Single;
import io.reactivex.rxjava3.subjects.BehaviorSubject;

public class Light {
    public Observable<Boolean> isOn$() { return brightnessAndWarmthLightController.isOn$(); }

    public void turnOn() { brightnessAndWarmthLightController.turnOn(); } // todo result?

    public void turnOff() { brightnessAndWarmthLightController.turnOff(); } // todo result?

    private BehaviorSubject<BrightnessAndWarmth> brightnessAndWarmth = BehaviorSubject.create();
    public Observable<BrightnessAndWarmth> brightnessAndWarmth$() { return brightnessAndWarmth; }

    public Single<Result> setBrightnessAndWarmth (BrightnessAndWarmth brightnessAndWarmth) {
        Single<Result> setResult = brightnessAndWarmthLightController
                .setBrightnessAndWarmth(brightnessAndWarmth);

        return setResult.doOnSuccess(result -> {
            this.brightnessAndWarmth.onNext(brightnessAndWarmth); // todo handle error
        });
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
    public boolean isDeviceSupported() { return brightnessAndWarmthLightController.hasDualFrontlight(); }
    public Intent[] missingPermissionIntents() { return brightnessAndWarmthLightController.getMissingPermissionIntents(); }

    public Light(BrightnessAndWarmthLightController brightnessAndWarmthLightController) {
        this.brightnessAndWarmthLightController = brightnessAndWarmthLightController;
    }

    private final BrightnessAndWarmthLightController brightnessAndWarmthLightController;

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
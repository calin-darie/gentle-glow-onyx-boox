package com.onyx.darie.calin.gentleglowonyxboox;

import android.content.Intent;

import io.reactivex.rxjava3.core.BackpressureStrategy;
import io.reactivex.rxjava3.core.Observable;

//todo rename to BrightnessAndWarmthLightController?
public class Light {

    public Observable<Boolean> isOn$() { return null; }

    public void turnOn() { nativeWarmColdLightController.turnOn(true, true); } // todo result?

    public void turnOff() { nativeWarmColdLightController.turnOff(); } // todo result?

    private void setBrightnessAndWarmth (BrightnessAndWarmth brightnessAndWarmth) {
        final WarmAndColdLedOutput warmCold = adapter.toWarmAndColdLedOutput(brightnessAndWarmth);
        nativeWarmColdLightController.setLedOutput(warmCold);
        lastSetBrightnessAndWarmth = brightnessAndWarmth;
    }

    public Observable<BrightnessAndWarmthState> getBrightnessAndWarmthState$() {
        return brightnessAndWarmthState$;
    }

    public Result applyDeltaBrightness(int delta) {
        final Result<BrightnessAndWarmth> brightnessAndWarmthResult = lastSetBrightnessAndWarmth
                .withDeltaBrightness(delta);
        if (brightnessAndWarmthResult.hasError()) {
            return brightnessAndWarmthResult;
        }
        setBrightnessAndWarmth(brightnessAndWarmthResult.value);
        return Result.success();
    }

    public Result applyDeltaWarmth(int delta) {
        final Result<BrightnessAndWarmth> brightnessAndWarmthResult = lastSetBrightnessAndWarmth
                .withDeltaWarmth(delta);
        if (brightnessAndWarmthResult.hasError()) {
            return brightnessAndWarmthResult;
        }
        return Result.success();
    }

    ///////////// one time checks
    public boolean isDeviceSupported() { return nativeWarmColdLightController.isDeviceSupported(); }
    public Intent[] missingPermissionIntents() { return new Intent[0]; }

    private BrightnessAndWarmth lastSetBrightnessAndWarmth;
    private final Observable<BrightnessAndWarmthState> brightnessAndWarmthState$;

    private NativeWarmColdLightController nativeWarmColdLightController;
    private final BrightnessAndWarmthToWarmAndColdLedOutputAdapter adapter;

    public Light(
            LightCommandSource lightCommandSource,
            NativeWarmColdLightController nativeWarmColdLightController,
            BrightnessAndWarmthToWarmAndColdLedOutputAdapter adapter) {
        this.nativeWarmColdLightController = nativeWarmColdLightController;
        Observable<WarmAndColdLedOutput> warmAndColdLedOutput$ = nativeWarmColdLightController.getWarmAndColdLedOutput$();
        this.brightnessAndWarmthState$ = warmAndColdLedOutput$.map(warmAndColdLedOutput ->
                adapter.toWarmAndColdLedOutput(lastSetBrightnessAndWarmth).equals(warmAndColdLedOutput)?
                        new BrightnessAndWarmthState(false, lastSetBrightnessAndWarmth) :
                        new BrightnessAndWarmthState(true, adapter.findBrightnessAndWarmthApproximationForWarmAndColdLedOutput(warmAndColdLedOutput)));
        this.adapter = adapter;
        subscribeSetBrightnessAndWarmthRequestHandler(lightCommandSource, warmAndColdLedOutput$);
        subscribeRestoreBrightnesAndWarmthRequestHandler(lightCommandSource, lightCommandSource.getBrightnessAndWarmthRestoreFromStorageRequest$());
    }

    private void subscribeRestoreBrightnesAndWarmthRequestHandler(LightCommandSource lightCommandSource, Observable<BrightnessAndWarmth> brightnessAndWarmthRestoreFromStorageRequest$) {
        brightnessAndWarmthRestoreFromStorageRequest$.subscribe(loaded -> lastSetBrightnessAndWarmth = loaded);
    }

    private void subscribeSetBrightnessAndWarmthRequestHandler(LightCommandSource lightCommandSource, Observable<WarmAndColdLedOutput> warmAndColdLedOutput$) {
        lightCommandSource.getBrightnessAndWarmthChangeRequest$()
                .onBackpressureLatest()
                .concatMap(r -> {
                    setBrightnessAndWarmth(r);
                    return warmAndColdLedOutput$.take(1).toFlowable(BackpressureStrategy.LATEST);
                })
                .subscribe();
    }



    ////////////// light configuration editor ///////////////
    // accesses Light: set to preview changes, read to update current config
    // 1. load state of MutuallyExclusiveChoice<>
    // 2. ask OnyxLight.areCurrentBrightnessAndWarmthEqualTo(savedConfiguration.brightnessAndWarmth)
    // ///// or maybe init desired brightness and warmth and let Light emit an externalChange?
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
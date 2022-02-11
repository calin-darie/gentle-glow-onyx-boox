package com.onyx.darie.calin.gentleglowonyxboox;

import android.content.Intent;

import io.reactivex.rxjava3.core.BackpressureStrategy;
import io.reactivex.rxjava3.core.Flowable;
import io.reactivex.rxjava3.core.Observable;

public class Light {

    private final Observable<WarmAndColdLedOutput> warmAndColdLedOutput$;

    public Observable<Boolean> isOn$() { return null; }

    public void turnOn() { nativeWarmColdLightController.turnOn(true, true); } // todo result?

    public void turnOff() { nativeWarmColdLightController.turnOff(); } // todo result?

    private void setBrightnessAndWarmth (BrightnessAndWarmth brightnessAndWarmth) {
        final WarmAndColdLedOutput warmCold = adapter.toWarmAndColdLedOutput(brightnessAndWarmth);
        setOutput(warmCold);
        lastSetBrightnessAndWarmth = brightnessAndWarmth;
    }

    private void setOutput(WarmAndColdLedOutput warmCold) {
        nativeWarmColdLightController.setLedOutput(warmCold);
        this.output = warmCold;
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
        setBrightnessAndWarmth(brightnessAndWarmthResult.value);
        return Result.success();
    }

    ///////////// one time checks
    public boolean isDeviceSupported() { return nativeWarmColdLightController.isDeviceSupported(); }
    public Intent[] missingPermissionIntents() { return new Intent[0]; }

    private BrightnessAndWarmth lastSetBrightnessAndWarmth;
    private final Observable<BrightnessAndWarmthState> brightnessAndWarmthState$;

    private NativeWarmColdLightController nativeWarmColdLightController;
    private final BrightnessAndWarmthToWarmAndColdLedOutputAdapter adapter;
    private Storage<WarmAndColdLedOutput> externallySetLedOutputStorage;
    private WarmAndColdLedOutput output;

    public Light(
            NativeWarmColdLightController nativeWarmColdLightController,
            BrightnessAndWarmthToWarmAndColdLedOutputAdapter adapter,
            Storage<WarmAndColdLedOutput> externallySetLedOutputStorage) {
        this.nativeWarmColdLightController = nativeWarmColdLightController;
        warmAndColdLedOutput$ = nativeWarmColdLightController.getWarmAndColdLedOutput$()
            .distinctUntilChanged();
        this.brightnessAndWarmthState$ = warmAndColdLedOutput$.map(warmAndColdLedOutput -> {
            boolean isExternal =
                    !adapter.toWarmAndColdLedOutput(lastSetBrightnessAndWarmth).equals(warmAndColdLedOutput) ||
                    !warmAndColdLedOutput.equals(output); // todo test or remove!
            if (isExternal) {
                output = warmAndColdLedOutput; // todo test!
                saveExternallySetLedOutput(warmAndColdLedOutput);
            }
            return new BrightnessAndWarmthState(isExternal, isExternal?
                    adapter.findBrightnessAndWarmthApproximationForWarmAndColdLedOutput(warmAndColdLedOutput):
                    lastSetBrightnessAndWarmth);
        });
        this.adapter = adapter;
        this.externallySetLedOutputStorage = externallySetLedOutputStorage;
    }

    public void setCommandSource(LightCommandSource lightCommandSource) {
        subscribeSetBrightnessAndWarmthRequestHandler(lightCommandSource, warmAndColdLedOutput$);
        subscribeRestoreBrightnesAndWarmthRequestHandler(lightCommandSource.getBrightnessAndWarmthRestoreFromStorageRequest$());
        subscribeRestoreExternalSetting(lightCommandSource.getRestoreExternalSettingRequest$());
        subscribeApplyDeltaBrightness(lightCommandSource.getApplyDeltaBrightnessRequest$());
        subscribeApplyDeltaWarmth(lightCommandSource.getApplyDeltaWarmthRequest$());
    }

    private void subscribeApplyDeltaWarmth(Observable<Integer> applyDeltaWarmthRequest$) {
        applyDeltaWarmthRequest$.subscribe(delta -> {
            applyDeltaWarmth(delta);
        });
    }

    private void subscribeApplyDeltaBrightness(Observable<Integer> applyDeltaBrightnessRequest$) {
        applyDeltaBrightnessRequest$.subscribe(delta -> {
            applyDeltaBrightness(delta);
        });
    }

    private void saveExternallySetLedOutput(WarmAndColdLedOutput warmAndColdLedOutput) {
        externallySetLedOutputStorage.save(warmAndColdLedOutput);
    }

    private void subscribeRestoreExternalSetting(Observable restoreExternalSettingRequest$) {
        restoreExternalSettingRequest$.subscribe(_ignore -> {
            Result<WarmAndColdLedOutput> loadResult = externallySetLedOutputStorage.loadOrDefault(new WarmAndColdLedOutput(128, 128));
            setOutput(loadResult.value);
            lastSetBrightnessAndWarmth = adapter.findBrightnessAndWarmthApproximationForWarmAndColdLedOutput(loadResult.value);
        });
    }

    private void subscribeRestoreBrightnesAndWarmthRequestHandler(Observable<BrightnessAndWarmth> brightnessAndWarmthRestoreFromStorageRequest$) {
        brightnessAndWarmthRestoreFromStorageRequest$.subscribe(loaded -> lastSetBrightnessAndWarmth = loaded);
    }

    private void subscribeSetBrightnessAndWarmthRequestHandler(LightCommandSource lightCommandSource, Observable<WarmAndColdLedOutput> warmAndColdLedOutput$) {
        lightCommandSource.getBrightnessAndWarmthChangeRequest$()
                .onBackpressureLatest()
                .concatMap(brightnessAndWarmth -> {
                    lastSetBrightnessAndWarmth = brightnessAndWarmth;
                    WarmAndColdLedOutput output = adapter.toWarmAndColdLedOutput(brightnessAndWarmth);
                    if (output.equals(this.output)) return Flowable.empty();
                    setOutput(output);
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
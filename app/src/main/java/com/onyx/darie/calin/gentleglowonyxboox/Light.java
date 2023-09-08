package com.onyx.darie.calin.gentleglowonyxboox;

import android.content.Intent;

import io.reactivex.rxjava3.core.BackpressureStrategy;
import io.reactivex.rxjava3.core.Flowable;
import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.core.Single;
import io.reactivex.rxjava3.subjects.PublishSubject;

public class Light {

    private final Observable<WarmAndColdLedOutput> warmAndColdLedOutput$;
    public final PublishSubject<BrightnessAndWarmth> setBrightnessAndWarmthRequest$ = PublishSubject.create();
    public final PublishSubject<Object> restoreExternallySetLedOutput$ = PublishSubject.create();
    public final PublishSubject<Integer> applyDeltaBrightnessRequest$ = PublishSubject.create();
    public final PublishSubject<Integer> applyDeltaWarmthRequest$ = PublishSubject.create();
    public final PublishSubject<BrightnessAndWarmth> restoreBrightnessAndWarmthRequest$ = PublishSubject.create();

    public Observable<Boolean> isOn$() { return null; }

    public void turnOn() { nativeWarmColdLightController.turnOn(true, true); } // todo result?

    public void turnOff() { nativeWarmColdLightController.turnOff(); } // todo result?

    private void setBrightnessAndWarmth (BrightnessAndWarmth brightnessAndWarmth) {
        final WarmAndColdLedOutput warmCold = adapter.toWarmAndColdLedOutput(brightnessAndWarmth);
        setOutput(warmCold);
        lastSetBrightnessAndWarmth = brightnessAndWarmth;
    }

    private Single<Result> setOutput(WarmAndColdLedOutput warmCold) {
        Single<Result> result = nativeWarmColdLightController.setLedOutput(warmCold);
        this.output = warmCold;
        return result;
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
        this.brightnessAndWarmthState$ = Observable.merge(
                restoreBrightnessAndWarmthRequest$.map(brightnessAndWarmth -> new BrightnessAndWarmthState(false, brightnessAndWarmth)),
                warmAndColdLedOutput$.map(warmAndColdLedOutput -> {
                    boolean isExternal = !
                            adapter.toWarmAndColdLedOutput(lastSetBrightnessAndWarmth)
                            .equals(warmAndColdLedOutput);
                    if (isExternal) {
                        output = warmAndColdLedOutput; // todo test!
                        saveExternallySetLedOutput(warmAndColdLedOutput);
                    }
                    return new BrightnessAndWarmthState(isExternal, isExternal?
                            adapter.findBrightnessAndWarmthApproximationForWarmAndColdLedOutput(warmAndColdLedOutput):
                            lastSetBrightnessAndWarmth);
                }));
        this.adapter = adapter;
        this.externallySetLedOutputStorage = externallySetLedOutputStorage;
        setCommandSource();
    }

    private void setCommandSource() {
        subscribeSetBrightnessAndWarmthRequestHandler();
        subscribeRestoreBrightnesAndWarmthRequestHandler();
        subscribeRestoreExternalSetting();
        subscribeApplyDeltaBrightness();
        subscribeApplyDeltaWarmth();
    }

    private void subscribeApplyDeltaWarmth() {
        applyDeltaWarmthRequest$.subscribe(delta -> {
            applyDeltaWarmth(delta);
        });
    }

    private void subscribeApplyDeltaBrightness() {
        applyDeltaBrightnessRequest$.subscribe(delta -> {
            applyDeltaBrightness(delta);
        });
    }

    private void saveExternallySetLedOutput(WarmAndColdLedOutput warmAndColdLedOutput) {
        externallySetLedOutputStorage.save(warmAndColdLedOutput);
    }

    private void subscribeRestoreExternalSetting() {
        restoreExternallySetLedOutput$.subscribe(_ignore -> {
            Result<WarmAndColdLedOutput> loadResult = externallySetLedOutputStorage.loadOrDefault(new WarmAndColdLedOutput(128, 128));
            setOutput(loadResult.value);
            lastSetBrightnessAndWarmth = adapter.findBrightnessAndWarmthApproximationForWarmAndColdLedOutput(loadResult.value);
        });
    }

    private void subscribeRestoreBrightnesAndWarmthRequestHandler() {
        restoreBrightnessAndWarmthRequest$.subscribe(loaded -> lastSetBrightnessAndWarmth = loaded);
    }

    private void subscribeSetBrightnessAndWarmthRequestHandler() {
        setBrightnessAndWarmthRequest$
                .toFlowable(BackpressureStrategy.LATEST)
                .concatMapEager(brightnessAndWarmth -> {
                    lastSetBrightnessAndWarmth = brightnessAndWarmth;
                    WarmAndColdLedOutput output = adapter.toWarmAndColdLedOutput(brightnessAndWarmth);
                    if (output.equals(this.output)) return Flowable.empty();
                    return setOutput(output).toFlowable();
                }, 1, 1)
                .subscribe();
    }
    // todo schedule
    // Intent.ACTION_SCREEN_ON
}
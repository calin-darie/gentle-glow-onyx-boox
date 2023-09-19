package com.onyx.darie.calin.gentleglowonyxboox;

import android.content.Intent;

import io.reactivex.rxjava3.annotations.NonNull;
import io.reactivex.rxjava3.core.BackpressureStrategy;
import io.reactivex.rxjava3.core.Flowable;
import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.core.Single;
import io.reactivex.rxjava3.subjects.BehaviorSubject;
import io.reactivex.rxjava3.subjects.PublishSubject;

public class Light {

    private final Observable<WarmAndColdLedOutput> warmAndColdLedOutput$;
    public final PublishSubject<BrightnessAndWarmth> setBrightnessAndWarmthRequest$ = PublishSubject.create();
    public final PublishSubject<Object> restoreExternallySetLedOutput$ = PublishSubject.create();
    public final PublishSubject<Integer> applyDeltaBrightnessRequest$ = PublishSubject.create();
    public final PublishSubject<Integer> applyDeltaWarmthRequest$ = PublishSubject.create();
    public final BehaviorSubject<BrightnessAndWarmth> restoreBrightnessAndWarmthRequest$ = BehaviorSubject.create();

    public Observable<Boolean> isOn$() { return nativeWarmColdLightController.isOn$(); }

    public void turnOn() { nativeWarmColdLightController.turnOn(); } // todo result?

    public void turnOff() { nativeWarmColdLightController.turnOff(); } // todo result?

    private Single<Result> setOutput(WarmAndColdLedOutput warmCold) {
        Single<Result> result = nativeWarmColdLightController.setLedOutput(warmCold);
        this.output = warmCold;
        return result;
    }

    public Observable<BrightnessAndWarmthState> getBrightnessAndWarmthState$() {
        return brightnessAndWarmthState$.startWith(Observable.defer(() -> {
            BrightnessAndWarmthState latest = getLatestState();
            return latest != null? Observable.just(latest): Observable.empty();
                }));
    }

    private BrightnessAndWarmthState getLatestState() {
        return latestState;
    }

    private Result applyDeltaBrightness(int delta) {
        final Result<BrightnessAndWarmth> brightnessAndWarmthResult = latestState.brightnessAndWarmth
                .withDeltaBrightness(delta);
        if (brightnessAndWarmthResult.hasError()) {
            return brightnessAndWarmthResult;
        }
        setBrightnessAndWarmthRequest$.onNext(brightnessAndWarmthResult.value);
        return Result.success();
    }

    private Result applyDeltaWarmth(int delta) {
        final Result<BrightnessAndWarmth> brightnessAndWarmthResult = latestState.brightnessAndWarmth
                .withDeltaWarmth(delta);
        if (brightnessAndWarmthResult.hasError()) {
            return brightnessAndWarmthResult;
        }
        setBrightnessAndWarmthRequest$.onNext(brightnessAndWarmthResult.value);
        return Result.success();
    }

    ///////////// one time checks
    public boolean isDeviceSupported() { return nativeWarmColdLightController.isDeviceSupported(); }
    public Intent[] missingPermissionIntents() { return new Intent[0]; }

    private BrightnessAndWarmth lastSetBrightnessAndWarmth;
    private final Observable<BrightnessAndWarmthState> brightnessAndWarmthState$;
    private final Flowable<BrightnessAndWarmth> setBrightnessAndWarmthResponse$;

    private NativeWarmColdLightController nativeWarmColdLightController;
    private final BrightnessAndWarmthToWarmAndColdLedOutputAdapter adapter;
    private Storage<WarmAndColdLedOutput> externallySetLedOutputStorage;
    private WarmAndColdLedOutput output;
    private  final Observable<BrightnessAndWarmth> brightnessAndWarmthRestored$;
     private BrightnessAndWarmthState latestState;
    public Light(
            NativeWarmColdLightController nativeWarmColdLightController,
            BrightnessAndWarmthToWarmAndColdLedOutputAdapter adapter,
            Storage<WarmAndColdLedOutput> externallySetLedOutputStorage) {
        this.nativeWarmColdLightController = nativeWarmColdLightController;
        warmAndColdLedOutput$ = nativeWarmColdLightController.getWarmAndColdLedOutput$()
            .distinctUntilChanged();
        setBrightnessAndWarmthResponse$ = getSetBrightnessAndWarmthResponse$();

        brightnessAndWarmthRestored$ = setupRestoreBrightnesAndWarmthRequestHandler();
        this.brightnessAndWarmthState$ = Observable.merge(
                restoreBrightnessAndWarmthRequest$.map(brightnessAndWarmth -> new BrightnessAndWarmthState(false, brightnessAndWarmth)),
                setBrightnessAndWarmthResponse$.toObservable().map(brightnessAndWarmth -> new BrightnessAndWarmthState(false, brightnessAndWarmth)),
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
                }))
                .startWith(brightnessAndWarmthRestored$
                        .map(brightnessAndWarmth -> new BrightnessAndWarmthState(false, brightnessAndWarmth))
                        .firstOrError())
                .doOnNext(bw -> latestState = bw)
                .distinctUntilChanged()
                .share()
        ;
        this.adapter = adapter;
        this.externallySetLedOutputStorage = externallySetLedOutputStorage;
        setCommandSource();
    }

    private void setCommandSource() {
        subscribeSetBrightnessAndWarmthRequestHandler();
        setupRestoreBrightnesAndWarmthRequestHandler();
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
        });
    }

    private @NonNull Observable<BrightnessAndWarmth> setupRestoreBrightnesAndWarmthRequestHandler() {
        return restoreBrightnessAndWarmthRequest$
                .doOnNext(loaded -> lastSetBrightnessAndWarmth = loaded);
    }

    private Flowable<BrightnessAndWarmth> getSetBrightnessAndWarmthResponse$() {
        return setBrightnessAndWarmthRequest$
                .toFlowable(BackpressureStrategy.LATEST)
                .concatMapEager(brightnessAndWarmth -> {
                    BrightnessAndWarmth oldBrightnessAndWarmth = lastSetBrightnessAndWarmth;
                    lastSetBrightnessAndWarmth = brightnessAndWarmth;
                    WarmAndColdLedOutput output = adapter.toWarmAndColdLedOutput(brightnessAndWarmth);
                    if (output.equals(this.output)) return Flowable.just(brightnessAndWarmth);
                    return setOutput(output).map(result -> result.hasError()? oldBrightnessAndWarmth : brightnessAndWarmth).toFlowable();
                }, 1, 1)
                .share();
    }
    private void subscribeSetBrightnessAndWarmthRequestHandler() {
        setBrightnessAndWarmthResponse$.subscribe();
    }

    public void toggleOnOff() {
        nativeWarmColdLightController.toggleOnOff();
    }

    // Intent.ACTION_SCREEN_ON
}
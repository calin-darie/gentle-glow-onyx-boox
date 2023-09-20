package com.onyx.darie.calin.gentleglowonyxboox;

import android.content.Intent;

import io.reactivex.rxjava3.annotations.NonNull;
import io.reactivex.rxjava3.core.BackpressureStrategy;
import io.reactivex.rxjava3.core.Flowable;
import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.core.Single;
import io.reactivex.rxjava3.subjects.BehaviorSubject;
import io.reactivex.rxjava3.subjects.PublishSubject;

public class LightImpl<TNativeOutput> implements Light{
    public PublishSubject<BrightnessAndWarmth> getSetBrightnessAndWarmthRequest$() {
        return setBrightnessAndWarmthRequest$;
    }
    public PublishSubject<Object> getRestoreExternallySetLedOutput$() {
        return restoreExternallySetLedOutput$;
    }
    public PublishSubject<Integer> getApplyDeltaBrightnessRequest$() {
        return applyDeltaBrightnessRequest$;
    }
    public PublishSubject<Integer> getApplyDeltaWarmthRequest$() {
        return applyDeltaWarmthRequest$;
    }
    public BehaviorSubject<BrightnessAndWarmth> getRestoreBrightnessAndWarmthRequest$() {
        return restoreBrightnessAndWarmthRequest$;
    }

    private final Observable<TNativeOutput> nativeOutput$;

    public Observable<Boolean> isOn$() { return nativeLightController.isOn$(); }

    public void turnOn() { nativeLightController.turnOn(); } // todo result?

    public void turnOff() { nativeLightController.turnOff(); } // todo result?

    private Single<Result> setOutput(TNativeOutput nativeOutput) {
        Single<Result> result = nativeLightController.setOutput(nativeOutput);
        this.output = nativeOutput;
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
    public boolean isDeviceSupported() { return nativeLightController.isDeviceSupported(); }
    public Intent[] missingPermissionIntents() { return new Intent[0]; }

    private final PublishSubject<BrightnessAndWarmth> setBrightnessAndWarmthRequest$ = PublishSubject.create();
    private final PublishSubject<Object> restoreExternallySetLedOutput$ = PublishSubject.create();
    private final PublishSubject<Integer> applyDeltaBrightnessRequest$ = PublishSubject.create();
    private final PublishSubject<Integer> applyDeltaWarmthRequest$ = PublishSubject.create();
    private final BehaviorSubject<BrightnessAndWarmth> restoreBrightnessAndWarmthRequest$ = BehaviorSubject.create();

    private BrightnessAndWarmth lastSetBrightnessAndWarmth;
    private final Observable<BrightnessAndWarmthState> brightnessAndWarmthState$;
    private final Flowable<BrightnessAndWarmth> setBrightnessAndWarmthResponse$;

    private NativeLightController<TNativeOutput> nativeLightController;
    private final BrightnessAndWarmthToNativeOutputAdapter<TNativeOutput> adapter;
    private Storage<TNativeOutput> externallySetLedOutputStorage;
    private TNativeOutput output;
    private  final Observable<BrightnessAndWarmth> brightnessAndWarmthRestored$;
     private BrightnessAndWarmthState latestState;

    public LightImpl(
            NativeLightController<TNativeOutput> nativeLightController,
            BrightnessAndWarmthToNativeOutputAdapter adapter,
            Storage<TNativeOutput> externallySetLedOutputStorage) {
        this.nativeLightController = nativeLightController;
        nativeOutput$ = nativeLightController.getOutput$()
            .distinctUntilChanged();
        setBrightnessAndWarmthResponse$ = getSetBrightnessAndWarmthResponse$();

        brightnessAndWarmthRestored$ = setupRestoreBrightnesAndWarmthRequestHandler();
        this.brightnessAndWarmthState$ = Observable.merge(
                restoreBrightnessAndWarmthRequest$.map(brightnessAndWarmth -> new BrightnessAndWarmthState(false, brightnessAndWarmth)),
                setBrightnessAndWarmthResponse$.toObservable().map(brightnessAndWarmth -> new BrightnessAndWarmthState(false, brightnessAndWarmth)),
                nativeOutput$.map(nativeOutput -> {
                    boolean isExternal = !
                            adapter.toNativeOutput(lastSetBrightnessAndWarmth)
                            .equals(nativeOutput);
                    if (isExternal) {
                        output = nativeOutput; // todo test!
                        saveExternallySetLedOutput(nativeOutput);
                    }
                    return new BrightnessAndWarmthState(isExternal, isExternal?
                            adapter.findBrightnessAndWarmthApproximationForNativeOutput(nativeOutput):
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

    private void saveExternallySetLedOutput(TNativeOutput nativeOutput) {
        externallySetLedOutputStorage.save(nativeOutput);
    }

    private void subscribeRestoreExternalSetting() {
        restoreExternallySetLedOutput$.subscribe(_ignore -> {
            Result<TNativeOutput> loadResult = externallySetLedOutputStorage.loadOrDefault(output); // todo save ono first use
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
                    TNativeOutput output = adapter.toNativeOutput(brightnessAndWarmth);
                    if (output.equals(this.output)) return Flowable.just(brightnessAndWarmth);
                    return setOutput(output).map(result -> result.hasError()? oldBrightnessAndWarmth : brightnessAndWarmth).toFlowable();
                }, 1, 1)
                .share();
    }
    private void subscribeSetBrightnessAndWarmthRequestHandler() {
        setBrightnessAndWarmthResponse$.subscribe();
    }

    @Override
    public void toggleOnOff() {
        nativeLightController.toggleOnOff();
    }

    // Intent.ACTION_SCREEN_ON
}
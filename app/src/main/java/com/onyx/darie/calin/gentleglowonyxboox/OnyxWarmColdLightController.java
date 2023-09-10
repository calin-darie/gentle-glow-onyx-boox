package com.onyx.darie.calin.gentleglowonyxboox;

import android.content.Context;
import android.net.Uri;

import com.onyx.android.sdk.api.device.FrontLightController;

import java.util.concurrent.TimeUnit;

import io.reactivex.rxjava3.annotations.NonNull;
import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.core.Single;
import io.reactivex.rxjava3.functions.Function;

public class OnyxWarmColdLightController implements NativeWarmColdLightController{
    @Override
    public Result turnOn(boolean warm, boolean cold) {
        boolean success = true;
        if (warm) {
            success &= FrontLightController.openWarmLight();
        }
        if (cold) {
            success &= FrontLightController.openColdLight();
        }
        return success? Result.success() : Result.error("could not turn on the light");
    }

    @Override
    public Result turnOn() {
        WarmAndColdLedOutput outputAfterWeTurnOn = getCurrentWarmAndColdLedOutput();
        if (outputAfterWeTurnOn.warm == 0 && outputAfterWeTurnOn.cold == 0)
            turnOn(true, true);
        else if (outputAfterWeTurnOn.warm == 0)
            turnOn(false, true);
        else
            turnOn(true, false);
        return Result.success();
    }

    @Override
    public Result turnOff() {
        boolean success =
                FrontLightController.closeWarmLight()
                && FrontLightController.closeColdLight();
        return success ? Result.success(): Result.error("could not turn off the light");
    }

    @Override
    public Single<Result> setLedOutput(WarmAndColdLedOutput output) {
        WarmAndColdLedOutput currentLedOutput = getCurrentWarmAndColdLedOutput();
        desiredLedOutput = output;
        if (currentLedOutput.equals(output)) {
            return Single.just(Result.success());
        }

        boolean success = FrontLightController.setWarmLightDeviceValue(context, output.warm) &&
                          FrontLightController.setColdLightDeviceValue(context, output.cold);
        if (!success) {
            return Single.just(Result.error("driver said no"));
        }
        return ledOutputRaw$
                .takeWhile(futureOutput -> !futureOutput.equals(output))
                .timeout(3, TimeUnit.SECONDS)
                .doOnComplete(() -> desiredLedOutput = null)
                .map(any -> Result.error("light not changed"))
                .concatWith(Single.just(Result.success()))
                .lastOrError().onErrorReturnItem(Result.error("light not changed"));
    }

    @Override
    public Observable<WarmAndColdLedOutput> getWarmAndColdLedOutput$() {
        return ledOutput$.startWith(Observable.defer(() ->
                Observable.just(getCurrentWarmAndColdLedOutput())));
    }

    @Override
    public Observable<Boolean> isOn$() {
        return isOn$.startWith(Single.just(isOn()));
    }

    private void initLedOutputObservable() {
        ledOutputRaw$ = Observable.merge(
                isOn$.map(isOn -> getCurrentWarmAndColdLedOutput()),
                ContentObserverSubscriber
                        .create(
                                context.getContentResolver(),
                                new Uri[]{
                                        Uri.parse("content://settings/system/screen_cold_brightness"),
                                        Uri.parse("content://settings/system/screen_warm_brightness"),
                                },
                                uri -> getCurrentWarmAndColdLedOutput()
                        )
                )
                .share();
        ledOutput$ =  ledOutputRaw$
                .filter(output -> desiredLedOutput == null || desiredLedOutput.equals(output))
                .share();
    }

    @Override
    public Range<Integer> getWarmAndColdLedOutputRange() {
        Integer[] warmLightValues = FrontLightController.getWarmLightValues(context);
        return new Range<>(warmLightValues[1], warmLightValues[warmLightValues.length - 1]);
    }

    @Override
    public boolean isDeviceSupported() {
        return FrontLightController.hasCTMBrightness(context);
    }

    @Override
    public void toggleOnOff() {
        if (isOn())
            turnOff();
        else {
            turnOn();
        }
    }

    public OnyxWarmColdLightController(Context context) {
        this.context = context;
        initIsOnObservable();
        initLedOutputObservable();
    }

    private void initIsOnObservable() {
        isOn$ = ContentObserverSubscriber
                .create(
                        context.getContentResolver(),
                        new Uri[]{
                                Uri.parse("content://settings/system/cold_brightness_state_key"),
                                Uri.parse("content://settings/system/warm_brightness_state_key"),
                        },
                        new Function<Uri, Boolean>() {
                            @Override
                            public Boolean apply(@NonNull Uri uri) {
                                return isOn();
                            }
                        }
                )
                .share();
    }

    private boolean isOn() {
        return FrontLightController.isColdLightOn(context) ||
               FrontLightController.isWarmLightOn(context);
    }

    private WarmAndColdLedOutput getCurrentWarmAndColdLedOutput () {
        return new WarmAndColdLedOutput(
                FrontLightController.isWarmLightOn(context)? FrontLightController.getWarmLightConfigValue(context): 0,
                FrontLightController.isColdLightOn(context)? FrontLightController.getColdLightConfigValue(context): 0
        );
    }

    private final Context context;
    private Observable<Boolean> isOn$;
    private Observable<WarmAndColdLedOutput> ledOutput$;
    private Observable<WarmAndColdLedOutput> ledOutputRaw$;
    private WarmAndColdLedOutput desiredLedOutput;
}

package com.onyx.darie.calin.gentleglowonyxboox;

import android.content.Context;
import android.net.Uri;

import com.onyx.android.sdk.api.device.FrontLightController;

import java.util.concurrent.TimeUnit;

import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.core.Single;

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
                .timeout(1, TimeUnit.SECONDS)
                .doOnComplete(() -> desiredLedOutput = null)
                .map(any -> Result.error("light not changed"))
                .concatWith(Single.just(Result.success()))
                .lastOrError();
    }

    @Override
    public Observable<WarmAndColdLedOutput> getWarmAndColdLedOutput$() {
        return ledOutput$;
    }

    private void initLedOutputObservable() {
        ledOutputRaw$ =
                ContentObserverSubscriber
                        .create(
                                context.getContentResolver(),
                                new Uri[]{
                                        Uri.parse("content://settings/system/screen_cold_brightness"),
                                        Uri.parse("content://settings/system/screen_warm_brightness"),
                                },
                                uri -> getCurrentWarmAndColdLedOutput()
                        )
                        .share();
        ledOutput$ =  ledOutputRaw$.filter(output -> desiredLedOutput == null || desiredLedOutput.equals(output));
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

    public OnyxWarmColdLightController(Context context) {
        this.context = context;
        initLedOutputObservable();
    }

    private WarmAndColdLedOutput getCurrentWarmAndColdLedOutput () {
        return new WarmAndColdLedOutput(
                FrontLightController.isWarmLightOn(context)? FrontLightController.getWarmLightConfigValue(context): 0,
                FrontLightController.isColdLightOn(context)? FrontLightController.getColdLightConfigValue(context): 0
        );
    }

    private final Context context;
    private Observable<WarmAndColdLedOutput> ledOutput$;
    private Observable<WarmAndColdLedOutput> ledOutputRaw$;
    private WarmAndColdLedOutput desiredLedOutput;
}

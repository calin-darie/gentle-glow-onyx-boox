package com.onyx.darie.calin.gentleglowonyxboox;

import android.content.Context;
import android.net.Uri;

import com.onyx.android.sdk.api.device.FrontLightController;

import io.reactivex.rxjava3.core.Observable;

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
    public Result setLedOutput(WarmAndColdLedOutput output) {
        boolean success = FrontLightController.setWarmLightDeviceValue(context, output.warm) &&
                          FrontLightController.setColdLightDeviceValue(context, output.cold);
        return success? Result.success() : Result.error("could not change light");
    }

    @Override
    public Observable<WarmAndColdLedOutput> getWarmAndColdLedOutput$() {
        return ledOutput$;
    }

    private void initLedOutputObservable() {
        ledOutput$ =
                ContentObserverSubscriber
                        .create(
                                context.getContentResolver(),
                                new Uri[]{
                                        Uri.parse("content://settings/system/screen_cold_brightness"),
                                        Uri.parse("content://settings/system/screen_warm_brightness"),
                                },
                                uri -> new WarmAndColdLedOutput(
                                        FrontLightController.isWarmLightOn(context)? FrontLightController.getWarmLightConfigValue(context): 0,
                                        FrontLightController.isColdLightOn(context)? FrontLightController.getColdLightConfigValue(context): 0
                                )
                        )
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

    public OnyxWarmColdLightController(Context context) {
        this.context = context;
        initLedOutputObservable();
    }

    private final Context context;
    private Observable<WarmAndColdLedOutput> ledOutput$;
}

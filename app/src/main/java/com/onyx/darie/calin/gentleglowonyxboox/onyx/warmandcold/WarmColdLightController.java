package com.onyx.darie.calin.gentleglowonyxboox.onyx.warmandcold;

import android.content.Context;
import android.net.Uri;

import com.onyx.android.sdk.api.device.brightness.BaseBrightnessProvider;
import com.onyx.android.sdk.api.device.brightness.BrightnessController;
import com.onyx.android.sdk.api.device.brightness.BrightnessType;
import com.onyx.android.sdk.device.BaseDevice;
import com.onyx.darie.calin.gentleglowonyxboox.binding.ContentObserverSubscriber;
import com.onyx.darie.calin.gentleglowonyxboox.light.NativeLightController;
import com.onyx.darie.calin.gentleglowonyxboox.util.Result;

import java.util.concurrent.TimeUnit;

import io.reactivex.rxjava3.annotations.NonNull;
import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.core.Single;
import io.reactivex.rxjava3.functions.Function;

public class WarmColdLightController implements NativeLightController<WarmAndColdLedOutput> {

    public Result turnOn(boolean warm, boolean cold) {
        boolean success = true;
        if (warm) {
            success &= warmLight.open();
        }
        if (cold) {
            success &= coldLight.open();
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
        boolean success = warmLight.close() && coldLight.close();
        return success ? Result.success(): Result.error("could not turn off the light");
    }

    @Override
    public Single<Result> setOutput(WarmAndColdLedOutput output) {
        WarmAndColdLedOutput currentLedOutput = getCurrentWarmAndColdLedOutput();
        desiredLedOutput = output;
        if (currentLedOutput.equals(output)) {
            return Single.just(Result.success());
        }

        boolean success = warmLight.setValue(output.warm) && coldLight.setValue(output.cold);
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
    public Observable<WarmAndColdLedOutput> getOutput$() {
        return ledOutput$.startWith(Observable.defer(() ->
                Observable.just(getCurrentWarmAndColdLedOutput())));
    }

    @Override
    public WarmAndColdLedOutput getOutput() {
        return getCurrentWarmAndColdLedOutput();
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
    public boolean isDeviceSupported() {
        return BrightnessController.getBrightnessType(context) == BrightnessType.WARM_AND_COLD;
    }

    @Override
    public void toggleOnOff() {
        if (isOn())
            turnOff();
        else {
            turnOn();
        }
    }

    public WarmColdLightController(Context context) {
        this.context = context;
        initIsOnObservable();
        initLedOutputObservable();

        warmLight = BrightnessController.getBrightnessProvider(context, BaseDevice.LIGHT_TYPE_CTM_WARM);
        coldLight = BrightnessController.getBrightnessProvider(context, BaseDevice.LIGHT_TYPE_CTM_COLD);
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

    public boolean isOn() {
        return warmLight.isLightOn() ||
               coldLight.isLightOn();
    }

    private WarmAndColdLedOutput getCurrentWarmAndColdLedOutput () {
        return new WarmAndColdLedOutput(
                warmLight.isLightOn()? warmLight.getValue(): 0,
                coldLight.isLightOn()? coldLight.getValue(): 0
        );
    }

    private final Context context;
    private Observable<Boolean> isOn$;
    private Observable<WarmAndColdLedOutput> ledOutput$;
    private Observable<WarmAndColdLedOutput> ledOutputRaw$;
    private WarmAndColdLedOutput desiredLedOutput;

    private final BaseBrightnessProvider warmLight;
    private final BaseBrightnessProvider coldLight;
}

package com.onyx.darie.calin.gentleglowonyxboox.onyx.brightnessandtemperature;

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

import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.core.Single;

public class BrightnessAndTemperatureController implements NativeLightController<BrightnessAndTemperatureOutput> {

    public Result turnOn(boolean warm, boolean cold) {
        boolean success = true;
        if (warm || cold) {
            return turnOn();
        }
        return Result.success();
    }

    @Override
    public Result turnOn() {
        boolean success = brightness.open();
        return success? Result.success() : Result.error("could not turn on the light");
    }

    @Override
    public Result turnOff() {
        boolean success = brightness.close();
        return success ? Result.success(): Result.error("could not turn off the light");
    }

    @Override
    public Single<Result> setOutput(BrightnessAndTemperatureOutput output) {
        BrightnessAndTemperatureOutput currentLedOutput = getCurrentOutput();
        desiredOutput = output;
        if (currentLedOutput.equals(output)) {
            return Single.just(Result.success());
        }

        boolean success = brightness.setValue(output.brightness)
                && temperature.setValue(output.temperature);
        if (!success) {
            return Single.just(Result.error("driver said no"));
        }
        return outputRaw$
                .takeWhile(futureOutput -> !futureOutput.equals(output))
                .timeout(10, TimeUnit.SECONDS)
                .doOnComplete(() -> desiredOutput = null)
                .map(any -> Result.error("light not changed"))
                .concatWith(Single.just(Result.success()))
                .lastOrError().onErrorReturnItem(Result.error("light not changed"));
    }

    @Override
    public Observable<BrightnessAndTemperatureOutput> getOutput$() {
        return output$.startWith(Observable.defer(() ->
                Observable.just(getCurrentOutput())));
    }

    @Override
    public Observable<Boolean> isOn$() {
        return isOn$.startWith(Single.just(isOn()));
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

    public BrightnessAndTemperatureController(Context context) {
        this.context = context;
        initIsOnObservable();
        initOutput$();

        brightness = BrightnessController.getBrightnessProvider(context, BaseDevice.LIGHT_TYPE_CTM_BRIGHTNESS);
        temperature = BrightnessController.getBrightnessProvider(context, BaseDevice.LIGHT_TYPE_CTM_TEMPERATURE);
    }

    private void initOutput$() {
        outputRaw$ = Observable.merge(
                        isOn$.map(isOn -> getCurrentOutput()),
                        ContentObserverSubscriber
                                .create(
                                        context.getContentResolver(),
                                        new Uri[]{
                                                Uri.parse("content://settings/system"),
                                        },
                                        uri -> getCurrentOutput()
                                )
                )
                .share();
        output$ =  outputRaw$
                .filter(output -> desiredOutput == null || desiredOutput.equals(output))
                .share();
    }

    private void initIsOnObservable() {
        isOn$ = ContentObserverSubscriber
                .create(
                        context.getContentResolver(),
                        new Uri[]{
                                Uri.parse("content://settings/system"),
                        },
                        uri -> isOn()
                )
                .share();
    }

    private boolean isOn() {
        return brightness.isLightOn();
    }

    private BrightnessAndTemperatureOutput getCurrentOutput() {
        return new BrightnessAndTemperatureOutput(
                brightness.isLightOn()? brightness.getValue(): 0,
                temperature.getValue()
        );
    }

    private final Context context;
    private Observable<Boolean> isOn$;
    private Observable<BrightnessAndTemperatureOutput> output$;
    private Observable<BrightnessAndTemperatureOutput> outputRaw$;
    private BrightnessAndTemperatureOutput desiredOutput;

    private final BaseBrightnessProvider brightness;
    private final BaseBrightnessProvider temperature;
}

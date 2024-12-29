package com.onyx.darie.calin.gentleglowonyxboox.onyx.setup;

import android.content.Context;

import com.onyx.android.sdk.api.device.brightness.BaseBrightnessProvider;
import com.onyx.android.sdk.api.device.brightness.BrightnessController;
import com.onyx.darie.calin.gentleglowonyxboox.light.BrightnessAndWarmth;
import com.onyx.darie.calin.gentleglowonyxboox.light.BrightnessAndWarmthState;
import com.onyx.darie.calin.gentleglowonyxboox.light.Light;
import com.onyx.darie.calin.gentleglowonyxboox.light.LightConfigurationEditor;
import com.onyx.darie.calin.gentleglowonyxboox.onyx.brightnessandtemperature.setup.OnyxBrightnessAndTemperatureDependencies;
import com.onyx.darie.calin.gentleglowonyxboox.onyx.warmandcold.setup.OnyxWarmAndColdDependencies;
import com.onyx.darie.calin.gentleglowonyxboox.setup.Dependencies;
import com.onyx.darie.calin.gentleglowonyxboox.util.Range;

import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.subjects.BehaviorSubject;
import io.reactivex.rxjava3.subjects.PublishSubject;

public class OnyxSetup{
    public static Dependencies getDependencies(Context context) {
        switch(BrightnessController.getBrightnessType(context)) {
            case WARM_AND_COLD:
                return new OnyxWarmAndColdDependencies(context);
            case CTM:
                return new OnyxBrightnessAndTemperatureDependencies(context);
            case FL:
            case NONE:
            default:
                return getUnsupportedDependenciesStub();
        }
    }

    public static Range<Integer> getOutputRange(Context context, int deviceType) {
        BaseBrightnessProvider output = BrightnessController.getBrightnessProvider(context, deviceType);
        return new Range<>(
                output.getValueByIndex(1),
                output.getValueByIndex(output.getMaxIndex())
        );
    }

    private static Dependencies getUnsupportedDependenciesStub() {
        return new Dependencies() {
            @Override
            public Light getLight() {
                return new Light() {
                    @Override
                    public PublishSubject<BrightnessAndWarmth> getSetBrightnessAndWarmthRequest$() {
                        return null;
                    }
                    @Override
                    public PublishSubject<Object> getRestoreExternallySetLedOutput$() {
                        return null;
                    }

                    @Override
                    public PublishSubject<Integer> getApplyDeltaBrightnessRequest$() {
                        return null;
                    }

                    @Override
                    public PublishSubject<Integer> getApplyDeltaWarmthRequest$() {
                        return null;
                    }

                    @Override
                    public BehaviorSubject<BrightnessAndWarmth> getRestoreBrightnessAndWarmthRequest$() {
                        return null;
                    }

                    @Override
                    public Observable<Boolean> isOn$() {
                        return null;
                    }

                    @Override
                    public void turnOn() {

                    }

                    @Override
                    public void turnOff() {

                    }

                    @Override
                    public Observable<BrightnessAndWarmthState> getBrightnessAndWarmthState$() {
                        return null;
                    }

                    @Override
                    public boolean isDeviceSupported() {
                        return false;
                    }

                    @Override
                    public void toggleOnOff() {

                    }

                    @Override
                    public boolean isOn() {
                        return true;
                    }
                };
            }
            @Override
            public LightConfigurationEditor getLightConfigurationEditor() {
                return null;
            }
        };
    }
}


package com.onyx.darie.calin.gentleglowonyxboox.onyx.brightnessandtemperature.setup;

import android.content.Context;

import com.onyx.android.sdk.device.BaseDevice;
import com.onyx.darie.calin.gentleglowonyxboox.light.Light;
import com.onyx.darie.calin.gentleglowonyxboox.light.LightConfigurationChoice;
import com.onyx.darie.calin.gentleglowonyxboox.light.LightConfigurationEditor;
import com.onyx.darie.calin.gentleglowonyxboox.light.LightConfigurationEditorImpl;
import com.onyx.darie.calin.gentleglowonyxboox.light.LightImpl;
import com.onyx.darie.calin.gentleglowonyxboox.onyx.brightnessandtemperature.BrightnessAndTemperatureController;
import com.onyx.darie.calin.gentleglowonyxboox.onyx.brightnessandtemperature.BrightnessAndTemperatureOutput;
import com.onyx.darie.calin.gentleglowonyxboox.onyx.brightnessandtemperature.BrightnessAndWarmthToBrightnessAndTemperatureOutputAdapter;
import com.onyx.darie.calin.gentleglowonyxboox.onyx.setup.OnyxSetup;
import com.onyx.darie.calin.gentleglowonyxboox.setup.Dependencies;
import com.onyx.darie.calin.gentleglowonyxboox.storage.FileStorage;

public class OnyxBrightnessAndTemperatureDependencies implements Dependencies {
    private Context context;
    private LightConfigurationEditor onyxLightConfigurationEditor;

    public OnyxBrightnessAndTemperatureDependencies(Context context) {
        this.context = context;
    };
    private LightImpl<BrightnessAndTemperatureOutput> onyxLight;
    @Override
    public Light getLight() {
        return getLightImplementation();
    }

    private LightImpl<BrightnessAndTemperatureOutput> getLightImplementation() {
        if (onyxLight == null) {
            BrightnessAndTemperatureController nativeWarmColdLightController = new BrightnessAndTemperatureController(context);
            onyxLight = new LightImpl<>(
                    nativeWarmColdLightController,
                    new BrightnessAndWarmthToBrightnessAndTemperatureOutputAdapter(
                            OnyxSetup.getOutputRange(context, BaseDevice.LIGHT_TYPE_CTM_BRIGHTNESS),
                            OnyxSetup.getOutputRange(context, BaseDevice.LIGHT_TYPE_CTM_TEMPERATURE)),
                    new FileStorage<>(context.getFilesDir(), "onyxSlider.json")
            );
        }
        return onyxLight;
    }

    @Override
    public LightConfigurationEditor getLightConfigurationEditor() {
        if (onyxLightConfigurationEditor == null) {
            onyxLightConfigurationEditor = new LightConfigurationEditorImpl<BrightnessAndTemperatureOutput>(
                    getLightImplementation(),
                    new FileStorage<LightConfigurationChoice>(context.getFilesDir(), "`lightConfigurations`.json")
            );
        }
        return onyxLightConfigurationEditor;
    }
}

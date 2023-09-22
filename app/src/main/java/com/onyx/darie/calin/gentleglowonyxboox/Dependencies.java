package com.onyx.darie.calin.gentleglowonyxboox;

import android.content.Context;

import com.onyx.android.sdk.api.device.brightness.BaseBrightnessProvider;
import com.onyx.android.sdk.api.device.brightness.BrightnessController;
import com.onyx.android.sdk.device.BaseDevice;

class Dependencies {
    private Context context;
    private LightConfigurationEditor onyxLightConfigurationEditor;

    public Dependencies (Context context) {
        this.context = context;
    };
    private LightImpl<WarmAndColdLedOutput> onyxLight;
    public Light getLight() {
        return getOnyxWarmAndColdLight();
    }

    private LightImpl<WarmAndColdLedOutput> getOnyxWarmAndColdLight() {
        if (onyxLight == null) {
            OnyxWarmColdLightController nativeWarmColdLightController = new OnyxWarmColdLightController(context);
            onyxLight = new LightImpl<WarmAndColdLedOutput>(
                    nativeWarmColdLightController,
                    new OnyxBrightnessAndWarmthToWarmAndColdLedOutputAdapter(getOnyxWarmAndColdOutputRange()),
                    new FileStorage<WarmAndColdLedOutput>(context.getFilesDir(), "onyxSlider.json")
            );
        }
        return onyxLight;
    }

    public LightConfigurationEditor getLightConfigurationEditor() {
        if (onyxLightConfigurationEditor == null) {
            onyxLightConfigurationEditor = new LightConfigurationEditorImpl<WarmAndColdLedOutput>(
                    getOnyxWarmAndColdLight(),
                    new LightConfigurationMigrationStorage(context.getFilesDir())
                    //new FileStorage<LightConfigurationChoice>(context.getFilesDir(), "`lightConfigurations`.json")
            );
        }
        return onyxLightConfigurationEditor;
    }

    private Range<Integer> getOnyxWarmAndColdOutputRange() {
        BaseBrightnessProvider warmLight = BrightnessController.getBrightnessProvider(context, BaseDevice.LIGHT_TYPE_CTM_WARM);
        return new Range<>(
                warmLight.getValueByIndex(0),
                warmLight.getValueByIndex(warmLight.getMaxIndex())
        );
    }
}
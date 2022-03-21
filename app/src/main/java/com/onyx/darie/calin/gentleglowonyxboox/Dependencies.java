package com.onyx.darie.calin.gentleglowonyxboox;

import android.content.Context;

class Dependencies {
    private Context context;
    private LightConfigurationEditor onyxLightConfigurationEditor;

    public Dependencies (Context context) {
        this.context = context;
    };
    private Light onyxLight;
    public Light getOnyxLight () {
        if (onyxLight == null) {
            OnyxWarmColdLightController nativeWarmColdLightController = new OnyxWarmColdLightController(context);
            onyxLight = new Light(
                    nativeWarmColdLightController,
                    new OnyxBrightnessAndWarmthToWarmAndColdLedOutputAdapter(nativeWarmColdLightController.getWarmAndColdLedOutputRange()),
                    new FileStorage<WarmAndColdLedOutput>(context.getFilesDir(), "onyxSlider.json")
            );
        }
        return onyxLight;
    }

    public LightConfigurationEditor getOnyxLightConfigurationEditor() {
        if (onyxLightConfigurationEditor == null) {
            onyxLightConfigurationEditor = new LightConfigurationEditor(
                    getOnyxLight(),
                    new FileStorage<LightConfigurationChoice>(context.getFilesDir(), "lightConfigurations.json")
            );
        }
        return onyxLightConfigurationEditor;
    }
}
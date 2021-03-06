package com.onyx.darie.calin.gentleglowonyxboox;

import android.content.Context;

class Dependencies {
    private Context context;

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
                    new Storage<WarmAndColdLedOutput>(context.getFilesDir(), "onyxSlider.json")
            );
        }
        return onyxLight;
    }
}
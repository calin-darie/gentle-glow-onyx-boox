package com.onyx.darie.calin.gentleglowonyxboox.onyx.warmandcold.setup;

import android.content.Context;

import com.onyx.android.sdk.device.BaseDevice;
import com.onyx.darie.calin.gentleglowonyxboox.light.Light;
import com.onyx.darie.calin.gentleglowonyxboox.light.LightConfigurationEditor;
import com.onyx.darie.calin.gentleglowonyxboox.light.LightConfigurationEditorImpl;
import com.onyx.darie.calin.gentleglowonyxboox.light.LightConfigurationMigrationStorage;
import com.onyx.darie.calin.gentleglowonyxboox.light.LightImpl;
import com.onyx.darie.calin.gentleglowonyxboox.onyx.setup.OnyxSetup;
import com.onyx.darie.calin.gentleglowonyxboox.onyx.warmandcold.BrightnessAndWarmthToWarmAndColdLedOutputAdapter;
import com.onyx.darie.calin.gentleglowonyxboox.onyx.warmandcold.WarmAndColdLedOutput;
import com.onyx.darie.calin.gentleglowonyxboox.onyx.warmandcold.WarmColdLightController;
import com.onyx.darie.calin.gentleglowonyxboox.setup.Dependencies;
import com.onyx.darie.calin.gentleglowonyxboox.storage.FileStorage;

public class OnyxWarmAndColdDependencies implements Dependencies {
    private Context context;
    private LightConfigurationEditor onyxLightConfigurationEditor;

    public OnyxWarmAndColdDependencies (Context context) {
        this.context = context;
    };
    private LightImpl<WarmAndColdLedOutput> onyxLight;
    @Override
    public Light getLight() {
        return getOnyxWarmAndColdLight();
    }

    private LightImpl<WarmAndColdLedOutput> getOnyxWarmAndColdLight() {
        if (onyxLight == null) {
            WarmColdLightController nativeWarmColdLightController = new WarmColdLightController(context);
            onyxLight = new LightImpl<WarmAndColdLedOutput>(
                    nativeWarmColdLightController,
                    new BrightnessAndWarmthToWarmAndColdLedOutputAdapter(OnyxSetup.getOutputRange(context, BaseDevice.LIGHT_TYPE_CTM_WARM)),
                    new FileStorage<WarmAndColdLedOutput>(context.getFilesDir(), "onyxSlider.json")
            );
        }
        return onyxLight;
    }

    @Override
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
}

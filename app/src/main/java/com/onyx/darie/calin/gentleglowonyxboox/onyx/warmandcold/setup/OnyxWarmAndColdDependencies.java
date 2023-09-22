package com.onyx.darie.calin.gentleglowonyxboox.onyx.warmandcold.setup;

import android.content.Context;

import com.onyx.android.sdk.api.device.brightness.BaseBrightnessProvider;
import com.onyx.android.sdk.api.device.brightness.BrightnessController;
import com.onyx.android.sdk.device.BaseDevice;
import com.onyx.darie.calin.gentleglowonyxboox.light.Light;
import com.onyx.darie.calin.gentleglowonyxboox.light.LightConfigurationEditor;
import com.onyx.darie.calin.gentleglowonyxboox.light.LightConfigurationEditorImpl;
import com.onyx.darie.calin.gentleglowonyxboox.light.LightConfigurationMigrationStorage;
import com.onyx.darie.calin.gentleglowonyxboox.light.LightImpl;
import com.onyx.darie.calin.gentleglowonyxboox.onyx.warmandcold.OnyxBrightnessAndWarmthToWarmAndColdLedOutputAdapter;
import com.onyx.darie.calin.gentleglowonyxboox.onyx.warmandcold.OnyxWarmColdLightController;
import com.onyx.darie.calin.gentleglowonyxboox.onyx.warmandcold.WarmAndColdLedOutput;
import com.onyx.darie.calin.gentleglowonyxboox.setup.Dependencies;
import com.onyx.darie.calin.gentleglowonyxboox.storage.FileStorage;
import com.onyx.darie.calin.gentleglowonyxboox.util.Range;

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
            OnyxWarmColdLightController nativeWarmColdLightController = new OnyxWarmColdLightController(context);
            onyxLight = new LightImpl<WarmAndColdLedOutput>(
                    nativeWarmColdLightController,
                    new OnyxBrightnessAndWarmthToWarmAndColdLedOutputAdapter(getOnyxWarmAndColdOutputRange()),
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

    private Range<Integer> getOnyxWarmAndColdOutputRange() {
        BaseBrightnessProvider warmLight = BrightnessController.getBrightnessProvider(context, BaseDevice.LIGHT_TYPE_CTM_WARM);
        return new Range<>(
                warmLight.getValueByIndex(0),
                warmLight.getValueByIndex(warmLight.getMaxIndex())
        );
    }
}

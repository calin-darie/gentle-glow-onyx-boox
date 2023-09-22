package com.onyx.darie.calin.gentleglowonyxboox.onyx.warmandcold;

import com.onyx.darie.calin.gentleglowonyxboox.light.BrightnessAndWarmth;

/**
 * @deprecated
 * still supported until everyone migrates their saved data to {@link BrightnessAndWarmth}
 */
@Deprecated
public class WarmthBrightnessSetting {
    public final int warmth;
    public final int brightness;

    public WarmthBrightnessSetting(int warmthSetting, int brightnessSetting) {
        this.warmth = warmthSetting;
        this.brightness = brightnessSetting;
    }
}


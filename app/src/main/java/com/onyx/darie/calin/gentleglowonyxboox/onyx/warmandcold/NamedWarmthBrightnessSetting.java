package com.onyx.darie.calin.gentleglowonyxboox.onyx.warmandcold;

import com.onyx.darie.calin.gentleglowonyxboox.light.LightConfigurationChoice;

/**
 * @deprecated
 * still supported until everyone migrates their saved data to {@link LightConfigurationChoice}
 */
public class NamedWarmthBrightnessSetting {
    public final WarmthBrightnessSetting setting;
    public final String name;
    public final boolean isForOnyxCompatibility;

    public NamedWarmthBrightnessSetting(String name, WarmthBrightnessSetting setting, boolean isForOnyxCompatibility) {
        this.setting = setting;
        this.name = name;
        this.isForOnyxCompatibility = isForOnyxCompatibility;
    }
}

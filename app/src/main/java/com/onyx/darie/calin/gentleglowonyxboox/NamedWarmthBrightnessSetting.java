package com.onyx.darie.calin.gentleglowonyxboox;

import java.util.Arrays;
import java.util.Objects;

public class NamedWarmthBrightnessSetting {
    public final WarmthBrightnessSetting setting;
    public final String name;
    public final boolean isForOnyxCompatibility;
    public boolean canEdit() {
        return !isForOnyxCompatibility;
    }

    public NamedWarmthBrightnessSetting(String name, WarmthBrightnessSetting setting, boolean isForOnyxCompatibility) {
        this.setting = setting;
        this.name = name;
        this.isForOnyxCompatibility = isForOnyxCompatibility;
    }

    public static NamedWarmthBrightnessSetting[] getNamedSettings(NamedWarmthBrightnessSetting[] savedNamedWarmthBrightnessSettings, WarmthBrightnessSetting currentSetting) {
        final NamedWarmthBrightnessSetting[] availableNamedSettings = savedNamedWarmthBrightnessSettings.length != 0?
                savedNamedWarmthBrightnessSettings :
                Arrays.copyOf(presets, presets.length);

        boolean currentMatchesAnyAvailable = false;
        for (NamedWarmthBrightnessSetting setting : availableNamedSettings) {
            if (setting.setting.equals(currentSetting)){
                currentMatchesAnyAvailable = true;
            }
        }

        if (!currentMatchesAnyAvailable) {
            final NamedWarmthBrightnessSetting lastNewSetting = availableNamedSettings[availableNamedSettings.length - 1];
            availableNamedSettings[availableNamedSettings.length - 1] = new NamedWarmthBrightnessSetting(lastNewSetting.name, currentSetting, lastNewSetting.isForOnyxCompatibility);
        }

        return availableNamedSettings;
    }

    private static final NamedWarmthBrightnessSetting[] presets = new NamedWarmthBrightnessSetting[] {
            new NamedWarmthBrightnessSetting("Night", new WarmthBrightnessSetting(85, 23), false),
            new NamedWarmthBrightnessSetting("Dawn", new WarmthBrightnessSetting(0, 23), false),
            new NamedWarmthBrightnessSetting("Day", new WarmthBrightnessSetting(33, 80), false),
            // new NamedWarmthBrightnessSetting("Lamp", new WarmthBrightnessSetting(87, 37)),
            new NamedWarmthBrightnessSetting("Onyx slider", new WarmthBrightnessSetting(50, 50), true),
    };

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        NamedWarmthBrightnessSetting that = (NamedWarmthBrightnessSetting) o;
        return Objects.equals(setting, that.setting) &&
                Objects.equals(name, that.name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(setting, name);
    }
}

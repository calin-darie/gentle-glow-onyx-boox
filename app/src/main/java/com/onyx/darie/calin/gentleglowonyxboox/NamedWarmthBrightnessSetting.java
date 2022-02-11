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

    public static NamedWarmthBrightnessOptions getPresetNamedSettings() {
        return new NamedWarmthBrightnessOptions(Arrays.copyOf(presets, presets.length), 3);
    }

    public static NamedWarmthBrightnessOptions getPresetNamedSettings(WarmthBrightnessSetting onyxSliderApproximationAsWarmthBrightness) {
        return getNamedWarmthBrightnessOptionsWithOnyxSliderSelected(Arrays.copyOf(presets, presets.length), onyxSliderApproximationAsWarmthBrightness);
    }

    public static NamedWarmthBrightnessOptions getNamedSettings(
            NamedWarmthBrightnessSetting[] savedNamedWarmthBrightnessSettings,
            int savedSelectedIndex) {
        return new NamedWarmthBrightnessOptions(savedNamedWarmthBrightnessSettings, savedSelectedIndex);
    }

    public static NamedWarmthBrightnessOptions getNamedSettingsWithOnyxSliderSelected(
            NamedWarmthBrightnessSetting[] savedNamedWarmthBrightnessSettings,
            WarmthBrightnessSetting onyxSliderApproximationAsWarmthBrightness) {
        return getNamedWarmthBrightnessOptionsWithOnyxSliderSelected(savedNamedWarmthBrightnessSettings, onyxSliderApproximationAsWarmthBrightness);
    }

    private static NamedWarmthBrightnessOptions getNamedWarmthBrightnessOptionsWithOnyxSliderSelected(
            NamedWarmthBrightnessSetting[] savedNamedSettings,
            WarmthBrightnessSetting onyxSliderApproximationAsWarmthBrightness) {
        final NamedWarmthBrightnessSetting[] availableNamedSettings = Arrays.copyOf(savedNamedSettings, savedNamedSettings.length);
        final int indexOfOnyxCompatibilityPreset = getIndexOfOnyxCompatibilityPreset(availableNamedSettings);
        final NamedWarmthBrightnessSetting onyxCompatibilityPreset = availableNamedSettings[indexOfOnyxCompatibilityPreset];
        NamedWarmthBrightnessSetting selectedWarmthBrightness = new NamedWarmthBrightnessSetting(
                onyxCompatibilityPreset.name,
                onyxSliderApproximationAsWarmthBrightness,
                true);
        availableNamedSettings[indexOfOnyxCompatibilityPreset] = selectedWarmthBrightness;

        return new NamedWarmthBrightnessOptions(availableNamedSettings, indexOfOnyxCompatibilityPreset);
    }

    private static int getIndexOfOnyxCompatibilityPreset(NamedWarmthBrightnessSetting[] availableNamedSettings) {
        for (int index = 0; index < availableNamedSettings.length; index++) {
            if (availableNamedSettings[index].isForOnyxCompatibility)
                return index;
        }
        throw new ArrayIndexOutOfBoundsException("onyx compatibility setting is missing!");
    }

    public static final NamedWarmthBrightnessSetting[] presets = new NamedWarmthBrightnessSetting[] {
            new NamedWarmthBrightnessSetting("Night", new WarmthBrightnessSetting(85, 12), false),
            new NamedWarmthBrightnessSetting("Dawn", new WarmthBrightnessSetting(0, 2), false),
            new NamedWarmthBrightnessSetting("Day", new WarmthBrightnessSetting(33, 60), false),
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

    public static NamedWarmthBrightnessSetting onyxSliderPreset() {
        return presets[presets.length - 1];
    }

    @Override
    public int hashCode() {
        return Objects.hash(setting, name);
    }

    @Override
    public String toString() {
        return "{" +
                "setting=" + setting +
                ", name='" + name + '\'' +
                '}';
    }
}

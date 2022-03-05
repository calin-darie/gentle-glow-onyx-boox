package com.onyx.darie.calin.gentleglowonyxboox;

import java.util.Arrays;
import java.util.Objects;

public class LightConfiguration {
    public final BrightnessAndWarmth brightnessAndWarmth;
    public final String name;

    public LightConfiguration(String name, BrightnessAndWarmth brightnessAndWarmth) {
        this.brightnessAndWarmth = brightnessAndWarmth;
        this.name = name;
    }

    public static WarmColdSetting.LightConfiguration[] getPresets() {
        return Arrays.copyOf(presets, presets.length);
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

    public static final WarmColdSetting.LightConfiguration[] presets = new WarmColdSetting.LightConfiguration[] {
            new WarmColdSetting.LightConfiguration("Night", new BrightnessAndWarmth(new Brightness(12), new Warmth(85))),
            new WarmColdSetting.LightConfiguration("Dawn", new BrightnessAndWarmth(new Brightness(2), new Warmth(0))),
            new WarmColdSetting.LightConfiguration("Day", new BrightnessAndWarmth(new Brightness(60), new Warmth(33))),
            new WarmColdSetting.LightConfiguration("Sunset", new BrightnessAndWarmth(new Brightness(30), new Warmth(80)))
    };

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof WarmColdSetting.LightConfiguration)) return false;
        WarmColdSetting.LightConfiguration that = (WarmColdSetting.LightConfiguration) o;
        return Objects.equals(brightnessAndWarmth, that.brightnessAndWarmth) && Objects.equals(name, that.name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(brightnessAndWarmth, name);
    }

    @Override
    public String toString() {
        return "{" +
                "brightnessAndWarmth=" + brightnessAndWarmth +
                ", name='" + name + '\'' +
                '}';
    }
}

package com.onyx.darie.calin.gentleglowonyxboox.light;

import java.util.Arrays;
import java.util.Objects;

public class LightConfiguration {
    public final BrightnessAndWarmth brightnessAndWarmth;
    public final String name;

    public LightConfiguration(String name, BrightnessAndWarmth brightnessAndWarmth) {
        this.brightnessAndWarmth = brightnessAndWarmth;
        this.name = name;
    }

    public static LightConfiguration[] getPresets() {
        return Arrays.copyOf(presets, presets.length);
    }

    public LightConfiguration cloneAndRename(String name) {
        return new LightConfiguration(name, brightnessAndWarmth);
    }

    public LightConfiguration cloneWithBrightnessAndWarmth(BrightnessAndWarmth brightnessAndWarmth) {
        return new LightConfiguration(name, brightnessAndWarmth);
    }

    private static final LightConfiguration[] presets = new LightConfiguration[] {
            new LightConfiguration("Night", new BrightnessAndWarmth(new Brightness(12), new Warmth(85))),
            new LightConfiguration("Dawn", new BrightnessAndWarmth(new Brightness(2), new Warmth(0))),
            new LightConfiguration("Day", new BrightnessAndWarmth(new Brightness(60), new Warmth(33))),
            new LightConfiguration("Sunset", new BrightnessAndWarmth(new Brightness(30), new Warmth(80)))
    };

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof LightConfiguration)) return false;
        LightConfiguration that = (LightConfiguration) o;
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

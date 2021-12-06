package com.onyx.darie.calin.gentleglowonyxboox;

import java.util.Objects;

public class WarmthBrightnessSetting {
    public final int warmth;
    public final int brightness;

    public WarmthBrightnessSetting(int warmthSetting, int brightnessSetting) {
        this.warmth = warmthSetting;
        this.brightness = brightnessSetting;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        WarmthBrightnessSetting that = (WarmthBrightnessSetting) o;
        return warmth == that.warmth &&
                brightness == that.brightness;
    }

    @Override
    public int hashCode() {
        return Objects.hash(warmth, brightness);
    }

    @Override
    public String toString() {
        return "WarmthBrightnessSetting{" +
                "warmth=" + warmth +
                ", brightness=" + brightness +
                '}';
    }
}

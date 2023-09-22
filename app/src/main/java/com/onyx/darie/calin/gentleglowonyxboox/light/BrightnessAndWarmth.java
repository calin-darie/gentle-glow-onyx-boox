package com.onyx.darie.calin.gentleglowonyxboox.light;

import com.onyx.darie.calin.gentleglowonyxboox.util.Result;

import java.util.Objects;

public class BrightnessAndWarmth{
    public final Brightness brightness;
    public final Warmth warmth;

    public Result<BrightnessAndWarmth> withDeltaBrightness(int delta) {
        final Result<Brightness> result = PercentValue.withDelta(brightness, delta);
        if (result.hasError()) {
            return Result.error(result.error);
        }
        return Result.success(new BrightnessAndWarmth(result.value, warmth));
    }

    public Result<BrightnessAndWarmth> withDeltaWarmth(int delta) {
        final Result<Warmth> result = PercentValue.withDelta(warmth, delta);
        if (result.hasError()) {
            return Result.error(result.error);
        }
        return Result.success(new BrightnessAndWarmth(brightness, result.value));
    }

    public BrightnessAndWarmth(Brightness brightness, Warmth warmth) {
        this.brightness = brightness;
        this.warmth = warmth;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof BrightnessAndWarmth)) return false;
        BrightnessAndWarmth that = (BrightnessAndWarmth) o;
        return Objects.equals(brightness, that.brightness) &&
                Objects.equals(warmth, that.warmth);
    }

    @Override
    public int hashCode() {
        return Objects.hash(brightness, warmth);
    }

    @Override
    public String toString() {
        return "{" +
                "brightness=" + brightness +
                ", warmth=" + warmth +
                '}';
    }
}

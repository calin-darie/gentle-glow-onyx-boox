package com.onyx.darie.calin.gentleglowonyxboox.light;

import java.util.Objects;

public class BrightnessAndWarmthState {
    public final boolean isExternalChange;
    public final BrightnessAndWarmth brightnessAndWarmth;

    public BrightnessAndWarmthState(boolean isExternalChange, BrightnessAndWarmth brightnessAndWarmth) {
        this.isExternalChange = isExternalChange;
        this.brightnessAndWarmth = brightnessAndWarmth;
    }

    @Override
    public String toString() {
        return "{" +
                "\nisExternalChange=" + isExternalChange +
                "\n, brightnessAndWarmth=" + brightnessAndWarmth +
                "\n}";
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof BrightnessAndWarmthState)) return false;
        BrightnessAndWarmthState that = (BrightnessAndWarmthState) o;
        return isExternalChange == that.isExternalChange && Objects.equals(brightnessAndWarmth, that.brightnessAndWarmth);
    }

    @Override
    public int hashCode() {
        return Objects.hash(isExternalChange, brightnessAndWarmth);
    }
}

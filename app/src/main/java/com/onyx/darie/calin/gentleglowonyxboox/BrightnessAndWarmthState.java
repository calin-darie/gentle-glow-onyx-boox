package com.onyx.darie.calin.gentleglowonyxboox;

public class BrightnessAndWarmthState {
    public final boolean isExternalChange;
    public final BrightnessAndWarmth brightnessAndWarmth;

    public BrightnessAndWarmthState(boolean isExternalChange, BrightnessAndWarmth brightnessAndWarmth) {
        this.isExternalChange = isExternalChange;
        this.brightnessAndWarmth = brightnessAndWarmth;
    }
}

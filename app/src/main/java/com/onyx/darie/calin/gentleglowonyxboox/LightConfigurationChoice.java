package com.onyx.darie.calin.gentleglowonyxboox;

public class LightConfigurationChoice extends MutuallyExclusiveChoice<LightConfiguration>{
    public LightConfigurationChoice(LightConfiguration[] choices, int selectedIndex) {
        super(choices, selectedIndex, new LightConfiguration("Onyx/other", new BrightnessAndWarmth(new Brightness(1), new Warmth((100)))));
    }
}

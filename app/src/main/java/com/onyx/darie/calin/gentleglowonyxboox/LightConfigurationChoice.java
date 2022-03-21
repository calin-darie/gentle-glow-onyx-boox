package com.onyx.darie.calin.gentleglowonyxboox;

public class LightConfigurationChoice extends MutuallyExclusiveChoice<LightConfiguration>{
    public LightConfigurationChoice(LightConfiguration[] choices, int selectedIndex) {
        super(choices, selectedIndex);
    }
}

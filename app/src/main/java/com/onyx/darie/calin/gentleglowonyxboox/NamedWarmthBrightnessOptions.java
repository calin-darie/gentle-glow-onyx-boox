package com.onyx.darie.calin.gentleglowonyxboox;

public class NamedWarmthBrightnessOptions {

    private final NamedWarmthBrightnessSetting[] available;
    private NamedWarmthBrightnessSetting selected;

    public NamedWarmthBrightnessOptions(NamedWarmthBrightnessSetting[] available, NamedWarmthBrightnessSetting selected) {
        this.available = available;
        this.selected = selected;
    }

    NamedWarmthBrightnessSetting[] getAvailable() {
        return available;
    }

    NamedWarmthBrightnessSetting getSelected() {
        return selected;
    }

    public void replaceAndSelect(NamedWarmthBrightnessSetting oldSetting, NamedWarmthBrightnessSetting namedWarmthBrightnessSetting) {
        selected = namedWarmthBrightnessSetting;
        for (int i = 0; i < available.length; i ++ ) {
            if (available[i] == oldSetting) {
                available[i] = namedWarmthBrightnessSetting;
                break;
            }
        }
    }
}

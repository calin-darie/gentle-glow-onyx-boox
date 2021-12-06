package com.onyx.darie.calin.gentleglowonyxboox;

import java.util.Arrays;

public class NamedWarmthBrightnessOptions {

    private final NamedWarmthBrightnessSetting[] available;
    private int selectedIndex;

    public NamedWarmthBrightnessOptions(NamedWarmthBrightnessSetting[] available, int selectedIndex) {
        this.available = available;
        setSelectedIndex(selectedIndex);
    }

    public void setSelectedIndex(int value) {
        if (value < 0 || value >= available.length) {
            throw new ArrayIndexOutOfBoundsException();
        }
        this.selectedIndex = value;
    }

    NamedWarmthBrightnessSetting[] getAvailable() {
        return available;
    }

    NamedWarmthBrightnessSetting getSelected() {
        return available[selectedIndex];
    }

    int getSelectedIndex() {
        return selectedIndex;
    }

    public void replaceAndSelect(NamedWarmthBrightnessSetting oldSetting, NamedWarmthBrightnessSetting namedWarmthBrightnessSetting) {
        for (int i = 0; i < available.length; i ++ ) {
            if (available[i] == oldSetting) {
                available[i] = namedWarmthBrightnessSetting;
                setSelectedIndex(i);
                break;
            }
        }
    }

    public void select(NamedWarmthBrightnessSetting namedWarmthBrightnessSetting) {
        for (int i = 0; i < available.length; i ++ ) {
            if (available[i] == namedWarmthBrightnessSetting) {
                setSelectedIndex(i);
                break;
            }
        }
    }

    @Override
    public String toString() {
        return "NamedWarmthBrightnessOptions{" +
                "available=" + Arrays.toString(available) +
                ", selectedIndex=" + selectedIndex +
                '}';
    }
}

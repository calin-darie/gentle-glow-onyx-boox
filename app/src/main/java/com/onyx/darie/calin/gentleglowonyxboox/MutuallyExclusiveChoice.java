package com.onyx.darie.calin.gentleglowonyxboox;

import java.util.Arrays;

public class MutuallyExclusiveChoice<T> {
    T[] choices;
    int selectedIndex;
    private T defaultValue;
    int NoChoice = -1;

    public MutuallyExclusiveChoice(T[] choices, int selectedIndex, T defaultValue
    ) {
        this.choices = choices;
        this.selectedIndex = selectedIndex;
        this.defaultValue = defaultValue;
    }

    public T getSelected() {
        return selectedIndex == NoChoice? defaultValue: choices[selectedIndex];
    }

    public MutuallyExclusiveChoice<T> cloneAndReplaceSelected(T replacement) {
        if (selectedIndex == NoChoice) return this;
        MutuallyExclusiveChoice<T> clone = Clone();
        clone.selectedIndex = selectedIndex;
        clone.choices[selectedIndex] = replacement;
        return clone;
    }

    public MutuallyExclusiveChoice<T> cloneAndSelect(Integer index) {
        MutuallyExclusiveChoice<T> clone = Clone();
        clone.selectedIndex = index;
        return clone;
    }

    public MutuallyExclusiveChoice<T> cloneAndClearChoice() {
        if (selectedIndex == NoChoice) return this;
        MutuallyExclusiveChoice<T> clone = Clone();
        clone.selectedIndex = NoChoice;
        return clone;
    }

    public boolean hasChoice() {
        return selectedIndex != NoChoice;
    }

    private MutuallyExclusiveChoice<T> Clone() {
        MutuallyExclusiveChoice<T> clone = new MutuallyExclusiveChoice<T>(Arrays.copyOf(choices, choices.length), selectedIndex, defaultValue);
        return clone;
    }
}

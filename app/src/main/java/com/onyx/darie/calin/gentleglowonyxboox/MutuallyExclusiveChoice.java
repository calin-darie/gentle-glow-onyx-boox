package com.onyx.darie.calin.gentleglowonyxboox;

import java.util.Arrays;

public class MutuallyExclusiveChoice<T> {
    T[] choices;
    int selectedIndex;

    public MutuallyExclusiveChoice(T[] choices, int selectedIndex) {
        this.choices = choices;
        this.selectedIndex = selectedIndex;
    }

    public T getSelected() {
        return choices[selectedIndex];
    }

    public MutuallyExclusiveChoice<T> cloneAndReplaceSelected(T replacement) {
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

    private MutuallyExclusiveChoice<T> Clone() {
        MutuallyExclusiveChoice<T> clone = new MutuallyExclusiveChoice<T>(Arrays.copyOf(choices, choices.length), selectedIndex);
        return clone;
    }
}

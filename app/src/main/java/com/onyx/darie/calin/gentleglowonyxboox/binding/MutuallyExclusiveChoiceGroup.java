package com.onyx.darie.calin.gentleglowonyxboox.binding;

import android.widget.RadioButton;

import java.util.ArrayList;
import java.util.concurrent.Callable;

public class MutuallyExclusiveChoiceGroup {
    ArrayList<RadioButton> buttons = new ArrayList<>();
    private Callable onChoiceChanged;
    private final int NoChoice = -1;

    public void add(final RadioButton radioButton) {
        buttons.add(radioButton);
        int index = buttons.size() - 1;
        if (radioButton.isChecked()) {
            chosenIndex = index;
        }
        radioButton.setOnClickListener(v -> {
            setChosenIndex(index);
            if (onChoiceChanged != null) {
                try {
                    Object _ignore = onChoiceChanged.call();
                } catch (Exception e) {
                }
            }
        });
    }


    public void setChosenIndex(int index) {
        uncheckLightConfigurationRadioButton();
        chosenIndex = index;
        if (chosenIndex != NoChoice)
            getCheckedButton().setChecked(true);
    }

    private void uncheckLightConfigurationRadioButton() {
        if (getChosenIndex() != NoChoice) {
            getCheckedButton().setChecked(false);
        }
    }

    public Integer getChosenIndex() {
        return chosenIndex;
    }

    public void setOnChoiceChanged(Callable callable) {
        onChoiceChanged = callable;
    }

    public void clearChoice() {
        uncheckLightConfigurationRadioButton();
        chosenIndex = NoChoice;
    }

    public void setTextForIndex(int index, String name) {
        buttons.get(index).setText(name);
    }

    int chosenIndex = NoChoice;

    private RadioButton getCheckedButton() {
        return buttons.get(chosenIndex);
    }
}
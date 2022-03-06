package com.onyx.darie.calin.gentleglowonyxboox;

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
                    Object _ = onChoiceChanged.call();
                } catch (Exception e) {
                }
            }
        });
    }


    public void setChosenIndex(int index) {
        if (getChosenIndex() != NoChoice) {
            getCheckedButton().setChecked(false);
        }
        chosenIndex = index; // todo check?
        getCheckedButton().setChecked(true);
    }

    public Integer getChosenIndex() {
        return chosenIndex;
    }

    public void setOnChoiceChanged(Callable callable) {
        onChoiceChanged = callable;
    }

    public void clearChoice() {
        getCheckedButton().setChecked(false);
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
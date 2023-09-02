package com.onyx.darie.calin.gentleglowonyxboox;

import android.widget.RadioButton;

import java.util.ArrayList;
import java.util.concurrent.Callable;

public class MutuallyExclusiveChoiceGroup {
    ArrayList<RadioButton> buttons = new ArrayList<>();
    private Callable onChoiceChanged;

    public void add(final RadioButton radioButton) {
        buttons.add(radioButton);
        if (radioButton.isChecked()) {
            checkedRadioButtonId = radioButton.getId();
        }
        radioButton.setOnClickListener(v -> {
            setCheckedRadioButtonNoEvent(radioButton);
            if (onChoiceChanged != null) {
                try {
                    Object _ignore = onChoiceChanged.call();
                } catch (Exception e) {
                }
            }
        });
    }

    public void setCheckedRadioButtonNoEvent(RadioButton radioButton) {
        checkedRadioButtonId = radioButton.getId();
        for (RadioButton otherButton : buttons) {
            if (otherButton == radioButton) continue;
            otherButton.setChecked(false);
        }
    }

    int checkedRadioButtonId = -1;
    public int getCheckedRadioButtonId() {
        return checkedRadioButtonId;
    }

    public void setOnChoiceChanged(Callable callable) {
        onChoiceChanged = callable;
    }
}
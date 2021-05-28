package com.onyx.darie.calin.gentleglowonyxboox;

import android.view.View;
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
        radioButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                checkedRadioButtonId = radioButton.getId();
                if (onChoiceChanged != null) {
                    try {
                        Object _ = onChoiceChanged.call();
                    } catch (Exception e) {
                    }
                }
                for (RadioButton otherButton : buttons) {
                    if (otherButton == radioButton) continue;
                    otherButton.setChecked(false);
                }
            }
        });
    }

    int checkedRadioButtonId = -1;
    public int getCheckedRadioButtonId() {
        return checkedRadioButtonId;
    }

    public void setOnChoiceChanged(Callable callable) {
        onChoiceChanged = callable;
    }
}
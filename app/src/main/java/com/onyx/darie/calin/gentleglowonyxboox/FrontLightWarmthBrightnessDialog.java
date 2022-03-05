package com.onyx.darie.calin.gentleglowonyxboox;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.SeekBar;
import android.widget.Switch;
import android.widget.TextView;

import com.google.android.flexbox.FlexboxLayout;

import java.util.ArrayList;

import butterknife.ButterKnife;
import butterknife.BindView;
import io.reactivex.rxjava3.functions.Consumer;

public class FrontLightWarmthBrightnessDialog extends Activity {

    @BindView(R.id.status_textview)
    TextView status;

    @BindView(R.id.brightness_slider)
    SeekBar brightness;

    @BindView(R.id.warmth_slider)
    SeekBar warmth;

    @BindView(R.id.warmth_value_label)
    TextView warmthValue;

    @BindView(R.id.brightness_value_label)
    TextView brightnessValue;

    @BindView(R.id.decrease_brightness_by_1)
    Button decreaseBrightnessButton;

    @BindView(R.id.increase_brightness_by_1)
    Button increaseBrightnessButton;

    @BindView(R.id.decrease_warmth_by_1)
    Button decreaseWarmthButton;

    @BindView(R.id.increase_warmth_by_1)
    Button increaseWarmthButton;

    @BindView(R.id.name_edit)
    EditText name;

    @BindView(R.id.replace_with_preset_button)
    Button replaceWithPreset;

    @BindView(R.id.permissions_button)
    Button goToPermissions;

    MutuallyExclusiveChoiceGroup namedSettingsGroup = new MutuallyExclusiveChoiceGroup();

    private Light light;

    private LightConfigurationEditor lightConfigurationEditor;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        light = ((GentleGlowApplication)getApplication()).getDependencies().getOnyxLight();
        lightConfigurationEditor = ((GentleGlowApplication)getApplication()).getDependencies().getOnyxLightConfigurationEditor();

        setContentView(R.layout.activity_front_light_warmth_brightness_dialog);

        ButterKnife.bind(this);

        if (!light.isDeviceSupported()) {
            disableControls();
            final Switch light = findViewById(R.id.light_switch);
            light.setEnabled(false);
            status.setText(getText(R.string.device_not_supported));
             return;
        }

        if (Frontlight.hasPermissions()) {
            bindControls();
        } else {
            disableControls();
            final Switch light = findViewById(R.id.light_switch);
            light.setEnabled(false);
            status.setText(R.string.GentleGlowNeedsPermission);
            goToPermissions.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    showPermissionDialog();
                }
            });
            goToPermissions.setVisibility(View.VISIBLE);
        }
    }

    private void disableControls() {
        brightness.setEnabled(false);
        warmth.setEnabled(false);
        name.setEnabled(false);
        replaceWithPreset.setVisibility(View.GONE);
    }

    private void enableControls() {
        brightness.setEnabled(true);
        warmth.setEnabled(true);
        name.setEnabled(true);
        replaceWithPreset.setVisibility(View.VISIBLE);
    }

    private void bindControls() {
        Frontlight.ensureTurnedOn();

        enableControls();
        final Switch light = findViewById(R.id.light_switch);
        light.setEnabled(true);

        bindLightSwitch();

        bindSliders();

        bindName();

        bindNamedSettingsRadioGroup();

        bindResetSpinner();

        listenForExternalLightChanges();

        lightConfigurationEditor.startEditingCurrentLightConfigurationByBindingToCurrentBrightnessAndWarmthRequest$.onNext(0);
    }

    private final static int GET_PERMISSIONS_REQUEST = 1;
    private  void showPermissionDialog() {
        Intent intent = Frontlight.getPermissionsIntent();
        startActivityForResult(intent, GET_PERMISSIONS_REQUEST);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == GET_PERMISSIONS_REQUEST) {
            if (Frontlight.hasPermissions()) {
                goToPermissions.setVisibility(View.GONE);
                status.setText("");
                bindControls();
            }
        }
    }

    private void listenForExternalLightChanges() {
        Consumer<Boolean> checkForLightSwitchChange = new Consumer<Boolean>() {
            @Override
            public void accept(Boolean lightSwitchState) {
                checkForLightSwitchChange(lightSwitchState);
            }
        };
        final LifecycleAwareSubscription<Boolean> switchSubscription =
                new LifecycleAwareSubscription<>(this,
                        Frontlight.getLightSwitchState$(),
                        checkForLightSwitchChange);
        getApplication().registerActivityLifecycleCallbacks(switchSubscription);
    }

    private void checkForLightSwitchChange(Boolean lightSwitchState) {
        final Switch light = findViewById(R.id.light_switch);
        if (light.isChecked() != lightSwitchState)
            light.setChecked(lightSwitchState);
    }

    private void bindLightSwitch() {
        final Switch light = findViewById(R.id.light_switch);
        light.setChecked(Frontlight.isOn());
        light.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean isChecked) {
                if (isChecked) {
                    Frontlight.ensureTurnedOn();
                    findViewById(R.id.namedSettingsLayout).setVisibility(View.VISIBLE);
                    findViewById(R.id.named_settings_editor).setVisibility(View.VISIBLE);
                    ((View)replaceWithPreset.getParent()).setVisibility(View.VISIBLE);
                    status.setText("");
                } else {
                    Frontlight.turnOff();
                    findViewById(R.id.namedSettingsLayout).setVisibility(View.INVISIBLE);
                    findViewById(R.id.named_settings_editor).setVisibility(View.INVISIBLE);
                    ((View)replaceWithPreset.getParent()).setVisibility(View.GONE);
                    status.setText(R.string.LightsOff);
                }
            }
        });

    }

    private void bindResetSpinner() {
        final Context context = this;

        final ArrayAdapter<SelectItem> arrayAdapter = new ArrayAdapter<>(context, android.R.layout.select_dialog_item);
        final AlertDialog.Builder alertDialog = new AlertDialog.Builder(context);
        alertDialog.setTitle(getText(R.string.replace_with_preset));
        alertDialog.setSingleChoiceItems(arrayAdapter, -1, (dialog, position) -> {
            SelectItem item = arrayAdapter.getItem(position);

            lightConfigurationEditor.replaceCurrentLightConfigurationRequest$.onNext(item.item);

            dialog.dismiss();
        });

        replaceWithPreset.setOnClickListener(v -> {
            final ArrayList<SelectItem> items = new ArrayList<>();
            lightConfigurationEditor.getPresetsToReplaceCurrent().map(preset -> new SelectItem(preset));

            arrayAdapter.clear();
            arrayAdapter.addAll(items);

            alertDialog.show();
        });
    }

    private void bindName() {
        name.addTextChangedListener(new TextWatcher(){

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {/* required by contract, not needed */}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {/* required by contract, not needed */}

            @Override
            public void afterTextChanged(Editable s) {
                lightConfigurationEditor.renameCurrentLightConfigurationRequest$.onNext(name.getText().toString());
            }
        });
    }

    private void bindSliders() {
        getApplication().registerActivityLifecycleCallbacks(new LifecycleAwareSubscription<>(this,light.getBrightnessAndWarmthState$(), brightnessAndWarmthState -> {
            BrightnessAndWarmth bw = brightnessAndWarmthState.brightnessAndWarmth;
            if (brightnessAndWarmthState.isExternalChange) {
                lightConfigurationEditor.stopEditingCurrentLightConfigurationByBindingToCurrentBrightnessAndWarmthRequest$.onNext(0);
                namedSettingsGroup.clearChoice();
                disableControls(); // todo leave them enabled
                // todo clear selection in editor?
            }
            onBrightnessAndWarmthChanged(brightnessAndWarmthState.brightnessAndWarmth);
        }));
        SeekBar.OnSeekBarChangeListener seekBarChangeListener = new SeekBar.OnSeekBarChangeListener() {
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (!fromUser) return;
                light.setBrightnessAndWarmthRequest$.onNext(new BrightnessAndWarmth(
                        new Brightness(brightness.getProgress()),
                        new Warmth(warmth.getProgress())));
            }

            public void onStartTrackingTouch(SeekBar seekBar) {
                lightConfigurationEditor.stopEditingCurrentLightConfigurationByBindingToCurrentBrightnessAndWarmthRequest$.onNext(0);
                status.setText("");
            }

            public void onStopTrackingTouch(SeekBar seekBar) {
                lightConfigurationEditor.startEditingCurrentLightConfigurationByBindingToCurrentBrightnessAndWarmthRequest$.onNext(0);
            }
        };
        brightness.setOnSeekBarChangeListener(seekBarChangeListener);
        warmth.setOnSeekBarChangeListener(seekBarChangeListener);

        decreaseBrightnessButton.setOnClickListener(v -> light.applyDeltaBrightnessRequest$.onNext(-1));
        increaseBrightnessButton.setOnClickListener(v -> light.applyDeltaBrightnessRequest$.onNext(+1));

        decreaseWarmthButton.setOnClickListener(v -> light.applyDeltaWarmthRequest$.onNext(-1));
        increaseWarmthButton.setOnClickListener(v -> light.applyDeltaWarmthRequest$.onNext(+1));
    }

    private void onBrightnessAndWarmthChanged(BrightnessAndWarmth brightnessAndWarmth) {
        updateValues(brightnessAndWarmth);
        updateSliders(brightnessAndWarmth);
    }

    private void updateSliders(BrightnessAndWarmth brightnessAndWarmth) {
        warmth.setProgress(brightnessAndWarmth.warmth.value);
        brightness.setProgress(brightnessAndWarmth.brightness.value);
    }

    private void updateValues(BrightnessAndWarmth brightnessAndWarmth) {
        brightnessValue.setText(brightnessAndWarmth.brightness.value + " / 100");
        warmthValue.setText(brightnessAndWarmth.warmth.value + " / 100");
    }

    private void bindNamedSettingsRadioGroup() {
        initRadioButtons();
        namedSettingsGroup.setOnChoiceChanged(() -> {
            lightConfigurationEditor.chooseCurrentLightConfigurationRequest$.onNext(namedSettingsGroup.getChosenIndex());
            lightConfigurationEditor.startEditingCurrentLightConfigurationByBindingToCurrentBrightnessAndWarmthRequest$.onNext(0);
            return null;
        });
        final LifecycleAwareSubscription<MutuallyExclusiveChoice<LightConfiguration>> subscription =
                new LifecycleAwareSubscription<>(this,
                        lightConfigurationEditor.getLightConfigurationChoices$(),
                        lightConfigurationMutuallyExclusiveChoice -> {
                            status.setText(getText(R.string.saved)); // todo move to own subscription, or rename the method?
                            updateRadioButtons(lightConfigurationMutuallyExclusiveChoice);
                        });
        getApplication().registerActivityLifecycleCallbacks(subscription);
    }

    private void updateRadioButtons(MutuallyExclusiveChoice<LightConfiguration> lightConfigurationMutuallyExclusiveChoice) {
        final LightConfiguration[] choices = lightConfigurationMutuallyExclusiveChoice.choices;
        for (int index = 0; index < choices.length; index++) {
            final LightConfiguration choice = choices[index];
            namedSettingsGroup.setTextForIndex(index, choice.name);
            if (index == lightConfigurationMutuallyExclusiveChoice.selectedIndex) {
                namedSettingsGroup.setChosenIndex(index);
            }
        }
    }

    private void initRadioButtons() {
        for (LightConfiguration ignored : lightConfigurationEditor.getLightConfigurationChoice().choices) {
            final RadioButton radioButton = new RadioButton(this);
            radioButton.setId(View.generateViewId());
            radioButton.setLayoutParams(new RadioGroup.LayoutParams(FlexboxLayout.LayoutParams.WRAP_CONTENT, RadioGroup.LayoutParams.WRAP_CONTENT, 1));
            namedSettingsGroup.add(radioButton);
            ((FlexboxLayout) findViewById(R.id.namedSettingsLayout)).addView(radioButton);
        }
        updateRadioButtons(lightConfigurationEditor.getLightConfigurationChoice());
    }


    private class SelectItem {
        private final LightConfiguration item;

        private SelectItem(LightConfiguration item) {
            this.item = item;
        }

        @Override
        public String toString() {
            return item.name;
        }
    }
}
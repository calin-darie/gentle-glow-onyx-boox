package com.onyx.darie.calin.gentleglowonyxboox;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Gravity;
import android.view.MenuInflater;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.PopupMenu;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.SeekBar;
import android.widget.Switch;
import android.widget.TextView;

import com.google.android.flexbox.FlexboxLayout;

import java.util.stream.Collectors;

import butterknife.BindView;
import butterknife.ButterKnife;
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

    @BindView(R.id.open_profiles_more_menu_button)
    ImageButton openProfilesMoreMenu;

    MutuallyExclusiveChoiceGroup lightConfigurations = new MutuallyExclusiveChoiceGroup();

    private Light light;

    private LightConfigurationEditor lightConfigurationEditor;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setShowWhenLocked(true);

        light = ((GentleGlowApplication)getApplication()).getDependencies().getOnyxLight();
        lightConfigurationEditor = ((GentleGlowApplication)getApplication()).getDependencies().getLightConfigurationEditor();

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
        light.turnOn();

        enableControls();
        final Switch light = findViewById(R.id.light_switch);
        light.setEnabled(true);

        bindLightSwitch();

        bindLightConfigurations();
        bindStatus();

        bindSliders();

        bindResetSpinner();


        listenForExternalLightChanges();
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
                        this.light.isOn$(),
                        checkForLightSwitchChange);
        getApplication().registerActivityLifecycleCallbacks(switchSubscription);
    }

    private void checkForLightSwitchChange(Boolean lightSwitchState) {
        final Switch light = findViewById(R.id.light_switch);
        if (light.isChecked() != lightSwitchState)
            light.setChecked(lightSwitchState);
    }

    private void bindLightSwitch() {
        final Switch lightSwitch = findViewById(R.id.light_switch);
        lightSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean isChecked) {
                if (isChecked) {
                    light.turnOn();
                    findViewById(R.id.namedSettingsLayout).setVisibility(View.VISIBLE);
                    findViewById(R.id.named_settings_editor).setVisibility(View.VISIBLE);
                    ((View)replaceWithPreset.getParent()).setVisibility(View.VISIBLE);
                } else {
                    light.turnOff();
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
            arrayAdapter.clear();
            arrayAdapter.addAll(lightConfigurationEditor.getPresetsToReplaceCurrent().map(preset -> new SelectItem(preset)).collect(Collectors.toList()));

            alertDialog.show();
        });
    }

    private void bindNameViewToEditor() {
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
            if (brightnessAndWarmthState.isExternalChange) {
                lightConfigurationEditor.stopEditingCurrentLightConfigurationByBindingToCurrentBrightnessAndWarmthRequest$.onNext(0);
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

    private void bindStatus() {
        final LifecycleAwareSubscription<Integer> subscription =
                new LifecycleAwareSubscription<>(this,
                        lightConfigurationEditor.getStatus$(),
                        statusStringId -> status.setText(getText(statusStringId))
                        );
        getApplication().registerActivityLifecycleCallbacks(subscription);
    }

    private void bindLightConfigurations() {
        bindNameViewToEditor();

        final Context context = this;
        final LifecycleAwareSubscription<MutuallyExclusiveChoice<LightConfiguration>> subscription =
                new LifecycleAwareSubscription<>(this,
                        lightConfigurationEditor.getLightConfigurationChoices$(),
                        new Consumer<MutuallyExclusiveChoice<LightConfiguration>>() {
                            boolean isFirstTime = true;
                            @Override
                            public void accept(MutuallyExclusiveChoice<LightConfiguration> lightConfigurationChoice) {
                                if (!name.isFocused()) {
                                    name.setText(lightConfigurationChoice.getSelected().name);
                                }

                                if (lightConfigurationChoice.hasChoice()) {
                                    replaceWithPreset.setVisibility(View.VISIBLE);
                                    name.setEnabled(true);
                                } else {
                                    replaceWithPreset.setVisibility(View.GONE);
                                    name.setEnabled(false);
                                }

                                if (!isFirstTime) {
                                    FrontLightWarmthBrightnessDialog.this.updateRadioButtons(lightConfigurationChoice);
                                    return;
                                }
                                isFirstTime = false;
                                FrontLightWarmthBrightnessDialog.this.initRadioButtons();
                                lightConfigurationEditor.startEditingCurrentLightConfigurationByBindingToCurrentBrightnessAndWarmthRequest$.onNext(0);

                                lightConfigurations.setOnChoiceChanged(() -> {
                                    name.clearFocus();
                                    InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
                                    imm.hideSoftInputFromWindow(name.getWindowToken(), 0);
                                    lightConfigurationEditor.chooseCurrentLightConfigurationRequest$.onNext(lightConfigurations.getChosenIndex());
                                    lightConfigurationEditor.startEditingCurrentLightConfigurationByBindingToCurrentBrightnessAndWarmthRequest$.onNext(0);
                                    return null;
                                });
                                openProfilesMoreMenu.setOnClickListener(view -> {
                                    PopupMenu popup = new PopupMenu(context, view);
                                    popup.setGravity(Gravity.END);
                                    popup.setOnMenuItemClickListener(item -> {
                                        switch (item.getItemId()) {
                                            case R.id.restore_onyx_sliders:
                                                light.restoreExternallySetLedOutput$.onNext(0);
                                                return true;
                                            default:
                                                return false;
                                        }
                                    });
                                    MenuInflater inflater = popup.getMenuInflater();
                                    inflater.inflate(R.menu.profiles_more, popup.getMenu());
                                    popup.show();
                                });
                            }
                        });
        getApplication().registerActivityLifecycleCallbacks(subscription);
    }

    private void updateRadioButtons(MutuallyExclusiveChoice<LightConfiguration> lightConfigurationMutuallyExclusiveChoice) {
        final LightConfiguration[] choices = lightConfigurationMutuallyExclusiveChoice.choices;
        for (int index = 0; index < choices.length; index++) {
            final LightConfiguration choice = choices[index];
            lightConfigurations.setTextForIndex(index, choice.name);
        }
        lightConfigurations.setChosenIndex(lightConfigurationMutuallyExclusiveChoice.selectedIndex);
    }

    private void initRadioButtons() {
        for (int index = 0; index < LightConfiguration.getPresets().length; index++) {
            final RadioButton radioButton = new RadioButton(this);
            radioButton.setId(View.generateViewId());
            RadioGroup.LayoutParams layout = new RadioGroup.LayoutParams(FlexboxLayout.LayoutParams.WRAP_CONTENT, RadioGroup.LayoutParams.WRAP_CONTENT, 1);
            layout.setMargins(-10, 16, 10,  16);
            radioButton.setLayoutParams(layout);
            lightConfigurations.add(radioButton);
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
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
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.PopupMenu;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.SeekBar;
import android.widget.Switch;
import android.widget.TextView;

import com.google.android.flexbox.FlexboxLayout;
import com.onyx.darie.calin.gentleglowonyxboox.binding.LifecycleAwareSubscription;
import com.onyx.darie.calin.gentleglowonyxboox.binding.MutuallyExclusiveChoiceGroup;
import com.onyx.darie.calin.gentleglowonyxboox.light.Brightness;
import com.onyx.darie.calin.gentleglowonyxboox.light.BrightnessAndWarmth;
import com.onyx.darie.calin.gentleglowonyxboox.light.Light;
import com.onyx.darie.calin.gentleglowonyxboox.light.LightConfiguration;
import com.onyx.darie.calin.gentleglowonyxboox.light.LightConfigurationEditor;
import com.onyx.darie.calin.gentleglowonyxboox.light.Warmth;
import com.onyx.darie.calin.gentleglowonyxboox.onyx.Frontlight;
import com.onyx.darie.calin.gentleglowonyxboox.schedule.LightScheduler;
import com.onyx.darie.calin.gentleglowonyxboox.schedule.ScheduleActivity;
import com.onyx.darie.calin.gentleglowonyxboox.setup.GentleGlowApplication;
import com.onyx.darie.calin.gentleglowonyxboox.util.MutuallyExclusiveChoice;

import java.util.stream.Collectors;

import io.reactivex.rxjava3.functions.Consumer;

public class FrontLightWarmthBrightnessDialog extends Activity {

    TextView status;

    SeekBar brightness;
    SeekBar warmth;
    TextView warmthValue;
    TextView brightnessValue;
    Button decreaseBrightnessButton;
    Button increaseBrightnessButton;
    Button decreaseWarmthButton;
    Button increaseWarmthButton;
    EditText name;
    Button replaceWithPreset;
    Button goToPermissions;
    ImageButton openProfilesMoreMenu;
    Switch lightSwitch;

    MutuallyExclusiveChoiceGroup lightConfigurations = new MutuallyExclusiveChoiceGroup();

    private Light light;

    private LightConfigurationEditor lightConfigurationEditor;
    private LightScheduler lightScheduler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setShowWhenLocked(true);

        light = ((GentleGlowApplication)getApplication()).getDependencies().getLight();
        lightConfigurationEditor = ((GentleGlowApplication)getApplication()).getDependencies().getLightConfigurationEditor();
        lightScheduler = ((GentleGlowApplication)getApplication()).getScheduleDependencies().getLightScheduler();

        setContentView(R.layout.activity_front_light_warmth_brightness_dialog);

        initializeControls();

        if (!light.isDeviceSupported()) {
            disableControls();
            lightSwitch.setEnabled(false);
            status.setText(getText(R.string.device_not_supported));
            return;
        }

        if (Frontlight.hasPermissions()) {
            light.turnOn();
            bindControls();
        } else {
            disableControls();

            lightSwitch.setEnabled(false);
            status.setText(R.string.GentleGlowNeedsPermission);
            goToPermissions.setOnClickListener(view -> showPermissionDialog());
            goToPermissions.setVisibility(View.VISIBLE);
        }
    }

    private void initializeControls() {
        status = findViewById(R.id.status_textview);
        brightness = findViewById(R.id.brightness_slider);
        warmth = findViewById(R.id.warmth_slider);
        brightnessValue = findViewById(R.id.brightness_value_label);
        warmthValue = findViewById(R.id.warmth_value_label);
        decreaseBrightnessButton = findViewById(R.id.decrease_brightness_by_1);
        increaseBrightnessButton = findViewById(R.id.increase_brightness_by_1);
        decreaseWarmthButton = findViewById(R.id.decrease_warmth_by_1);
        increaseWarmthButton = findViewById(R.id.increase_warmth_by_1);
        name = findViewById(R.id.name_edit);
        replaceWithPreset = findViewById(R.id.replace_with_preset_button);
        openProfilesMoreMenu = findViewById(R.id.open_profiles_more_menu_button);
        goToPermissions = findViewById(R.id.permissions_button);
        lightSwitch = findViewById(R.id.light_switch);
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
        enableControls();

        bindLightConfigurations();
        bindStatus();

        bindSliders();

        bindResetSpinner();

        listenForExternalLightChanges();

        bindLightSwitch();

        bindScheduleSwitch();
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
        final LifecycleAwareSubscription<Boolean> switchSubscription =
                new LifecycleAwareSubscription<>(this,
                        this.light.isOn$(),
                        this::checkForLightSwitchChange);
        getApplication().registerActivityLifecycleCallbacks(switchSubscription);
    }

    private void checkForLightSwitchChange(Boolean lightSwitchState) {
        if (lightSwitch.isChecked() != lightSwitchState) {
            lightSwitch.setChecked(lightSwitchState);
        }
        if (lightSwitchState) {
            findViewById(R.id.namedSettingsLayout).setVisibility(View.VISIBLE);
            findViewById(R.id.named_settings_editor).setVisibility(View.VISIBLE);
            ((View)replaceWithPreset.getParent()).setVisibility(View.VISIBLE);
        } else {
            findViewById(R.id.namedSettingsLayout).setVisibility(View.INVISIBLE);
            findViewById(R.id.named_settings_editor).setVisibility(View.INVISIBLE);
            ((View)replaceWithPreset.getParent()).setVisibility(View.GONE);
            status.setText(R.string.LightsOff);
        }
    }

    private void bindLightSwitch() {
        checkForLightSwitchChange(light.isOn());
        lightSwitch.setOnCheckedChangeListener((compoundButton, isChecked) -> {
            if (isChecked) {
                light.turnOn();
            } else {
                light.turnOff();
            }
        });

    }

    private void bindScheduleSwitch() {
        final Button scheduleButton = findViewById(R.id.schedule_button);
        scheduleButton.setOnClickListener(b -> {
            light.turnOn();
            Intent myIntent = new Intent(
                    FrontLightWarmthBrightnessDialog.this,
                    ScheduleActivity.class);
            FrontLightWarmthBrightnessDialog.this.startActivity(myIntent);
        });
    }

    private void bindResetSpinner() {
        final Context context = this;

        final ArrayAdapter<SelectItem> arrayAdapter = new ArrayAdapter<>(context, android.R.layout.select_dialog_item);

        final AlertDialog.Builder alertDialog = new AlertDialog.Builder(context);
        alertDialog.setTitle(getText(R.string.replace_with_preset));
        alertDialog.setSingleChoiceItems(arrayAdapter, -1, (dialog, position) -> {
            SelectItem item = arrayAdapter.getItem(position);

            lightConfigurationEditor.getReplaceCurrentLightConfigurationRequest$().onNext(item.item);

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
                lightConfigurationEditor.getRenameCurrentLightConfigurationRequest$().onNext(name.getText().toString());
            }
        });
    }

    private void bindSliders() {
        getApplication().registerActivityLifecycleCallbacks(new LifecycleAwareSubscription<>(this,light.getBrightnessAndWarmthState$(), brightnessAndWarmthState -> {
            if (brightnessAndWarmthState.isExternalChange) {
                lightConfigurationEditor.getStopEditingCurrentLightConfigurationByBindingToCurrentBrightnessAndWarmthRequest$().onNext(0);
            }
            onBrightnessAndWarmthChanged(brightnessAndWarmthState.brightnessAndWarmth);
        }));
        SeekBar.OnSeekBarChangeListener seekBarChangeListener = new SeekBar.OnSeekBarChangeListener() {
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (!fromUser) return;
                light.getSetBrightnessAndWarmthRequest$().onNext(new BrightnessAndWarmth(
                        new Brightness(brightness.getProgress()),
                        new Warmth(warmth.getProgress())));
            }

            public void onStartTrackingTouch(SeekBar seekBar) {
                lightConfigurationEditor.getStopEditingCurrentLightConfigurationByBindingToCurrentBrightnessAndWarmthRequest$().onNext(0);
            }

            public void onStopTrackingTouch(SeekBar seekBar) {
                lightConfigurationEditor.getStartEditingCurrentLightConfigurationByBindingToCurrentBrightnessAndWarmthRequest$().onNext(0);
            }
        };
        brightness.setOnSeekBarChangeListener(seekBarChangeListener);
        warmth.setOnSeekBarChangeListener(seekBarChangeListener);

        decreaseBrightnessButton.setOnClickListener(v -> light.getApplyDeltaBrightnessRequest$().onNext(-1));
        increaseBrightnessButton.setOnClickListener(v -> light.getApplyDeltaBrightnessRequest$().onNext(+1));

        decreaseWarmthButton.setOnClickListener(v -> light.getApplyDeltaWarmthRequest$().onNext(-1));
        increaseWarmthButton.setOnClickListener(v -> light.getApplyDeltaWarmthRequest$().onNext(+1));
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
                                lightConfigurationEditor.getStartEditingCurrentLightConfigurationByBindingToCurrentBrightnessAndWarmthRequest$().onNext(0);

                                lightConfigurations.setOnChoiceChanged(() -> {
                                    name.clearFocus();
                                    InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
                                    imm.hideSoftInputFromWindow(name.getWindowToken(), 0);
                                    lightConfigurationEditor.getChooseCurrentLightConfigurationRequest$().onNext(lightConfigurations.getChosenIndex());
                                    lightConfigurationEditor.getStartEditingCurrentLightConfigurationByBindingToCurrentBrightnessAndWarmthRequest$().onNext(0);
                                    return null;
                                });
                                openProfilesMoreMenu.setOnClickListener(view -> {
                                    PopupMenu popup = new PopupMenu(context, view);
                                    popup.setGravity(Gravity.END);
                                    popup.setOnMenuItemClickListener(item -> {
                                        if (item.getItemId() == R.id.restore_onyx_sliders) {
                                            light.getRestoreExternallySetLedOutput$().onNext(0);
                                            return true;
                                        }
                                        else {
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
        final LightConfiguration[] choices = lightConfigurationMutuallyExclusiveChoice.getChoices();
        for (int index = 0; index < choices.length; index++) {
            final LightConfiguration choice = choices[index];
            lightConfigurations.setTextForIndex(index, choice.name);
        }
        lightConfigurations.setChosenIndex(lightConfigurationMutuallyExclusiveChoice.getSelectedIndex());
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
package com.onyx.darie.calin.gentleglowonyxboox;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
import android.util.Log;
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
import com.google.gson.Gson;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.concurrent.Callable;

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

    WarmColdToWarmthBrightnessAdapter adapter;
    private NamedWarmthBrightnessOptions namedWarmthBrightnessOptions;
    private Light light;
    private PublishSubject<BrightnessAndWarmth> brightnessAndWarmthChangeRequest$ = PublishSubject.create();
    private final PublishSubject restoreExternallySetLedOutputRequest$ = PublishSubject.create();
    private final PublishSubject applyDeltaBrightnessRequest$ = PublishSubject.create();
    private final PublishSubject applyDeltaWarmthRequest$ = PublishSubject.create();
    private boolean slidingInProgress;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        light = ((GentleGlowApplication)getApplication()).getDependencies().getOnyxLight();

        migrateSavedSettings();

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

        adapter = Frontlight.getWarmColdToWarmthBrightnessAdapter();

        namedWarmthBrightnessOptions = getNamedWarmthBrightnessOptions();

        light.setCommandSource(new LightCommandSource() {
            @Override
            public Flowable<BrightnessAndWarmth> getBrightnessAndWarmthChangeRequest$() {
                return brightnessAndWarmthChangeRequest$.toFlowable(BackpressureStrategy.LATEST); // todo accept observable and move transformation to Light.java?
            }

            @Override
            public Observable<Integer> getApplyDeltaBrightnessRequest$() {
                return applyDeltaBrightnessRequest$;
            }

            @Override
            public Observable<Integer> getApplyDeltaWarmthRequest$() {
                return applyDeltaWarmthRequest$;
            }


            @Override
            public Observable getRestoreExternalSettingRequest$() {
                return restoreExternallySetLedOutputRequest$;
            }

            @Override
            public Observable<BrightnessAndWarmth> getBrightnessAndWarmthRestoreFromStorageRequest$() {
                WarmthBrightnessSetting setting = namedWarmthBrightnessOptions.getSelected().setting;
                return Observable.just(new BrightnessAndWarmth(new Brightness(setting.brightness), new Warmth(setting.warmth)));
            }
        });

        enableControls();
        final Switch light = findViewById(R.id.light_switch);
        light.setEnabled(true);

        bindLightSwitch();

        initNamedWarmthBrightness();

        bindSliders();

        bindName();

        bindNamedSettingsRadioGroup();

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
                        Frontlight.getLightSwitchState$(),
                        checkForLightSwitchChange);
        getApplication().registerActivityLifecycleCallbacks(switchSubscription);
    }

    private void checkForLightSwitchChange(Boolean lightSwitchState) {
        final Switch light = findViewById(R.id.light_switch);
        if (light.isChecked() != lightSwitchState)
            light.setChecked(lightSwitchState);
    }

    private int getRadioButtonIdBySetting(NamedWarmthBrightnessSetting oldSetting) {
        for (Integer key : namedSettingByRadioButtonId.keySet()) {
            if (namedSettingByRadioButtonId.get(key).equals(oldSetting)) {
                return key;
            }
        }
        throw new Error("this should never happen");
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
        alertDialog.setSingleChoiceItems(arrayAdapter, -1, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int position) {
                SelectItem item = (SelectItem)arrayAdapter.getItem(position);

                replaceNamedWarmthBrightness(item.item);
                brightnessAndWarmthChangeRequest$.onNext(new BrightnessAndWarmth(
                        new Brightness(item.item.setting.brightness),
                        new Warmth(item.item.setting.warmth)
                ));
                saveNamedSettings();

                dialog.dismiss();
            }
        });

        replaceWithPreset.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                final ArrayList<SelectItem> items = new ArrayList<>();
                for (NamedWarmthBrightnessSetting preset : NamedWarmthBrightnessSetting.presets) {
                    if (preset.isForOnyxCompatibility)
                        continue;

                    if (preset.equals(namedWarmthBrightnessOptions.getSelected()))
                        continue;

                    items.add(new SelectItem(preset));
                }
                arrayAdapter.clear();
                arrayAdapter.addAll(items);

                alertDialog.show();
            }
        });
    }

    private NamedWarmthBrightnessOptions getNamedWarmthBrightnessOptions() {
        final NamedWarmthBrightnessSetting[] savedSettings = loadNamedSettings();
        final int selectedIndex = loadSelectedIndex();
        if (savedSettings.length == 0) {
            return NamedWarmthBrightnessSetting.getPresetNamedSettings();
        }
        return getNamedWarmthBrightnessOptions(initialWarmColdSetting, savedSettings, selectedIndex);
    }

    private NamedWarmthBrightnessOptions getNamedWarmthBrightnessOptions(WarmColdSetting initialWarmColdSetting, NamedWarmthBrightnessSetting[] savedSettings, int selectedIndex) {
        final NamedWarmthBrightnessSetting savedWarmthBrightness = savedSettings[selectedIndex];
        final WarmColdSetting savedWarmCold = adapter.convertWarmthBrightnessToWarmCold(savedWarmthBrightness.setting);
        final boolean isSavedWarmColdStillCurrent = initialWarmColdSetting.equals(savedWarmCold);

        return  isSavedWarmColdStillCurrent?
                NamedWarmthBrightnessSetting.getNamedSettings(savedSettings, selectedIndex):
                NamedWarmthBrightnessSetting.getNamedSettingsWithOnyxSliderSelected(savedSettings, adapter.findWarmthBrightnessApproximationForWarmCold(initialWarmColdSetting));
    }

    private void bindName() {
        name.addTextChangedListener(new TextWatcher(){

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {/* required by contract, not needed */}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {/* required by contract, not needed */}

            @Override
            public void afterTextChanged(Editable s) {
                changeName(name.getText().toString());
                
                // todo move to binding of radiobuttons
                RadioButton selectedRadio = findViewById(namedSettingsGroup.getCheckedRadioButtonId());
                selectedRadio.setText(name.getText().toString());
            }
        });
    }

    private void changeName(String newName) {
        replaceNamedWarmthBrightness(
                new NamedWarmthBrightnessSetting(
                        newName,
                        namedWarmthBrightnessOptions.getSelected().setting,
                        namedWarmthBrightnessOptions.getSelected().isForOnyxCompatibility)
        );
        saveNamedSettings();
    }

    private void bindSliders() {
        getApplication().registerActivityLifecycleCallbacks(new LifecycleAwareSubscription<>(this,light.getBrightnessAndWarmthState$(), brightnessAndWarmthState -> {
            BrightnessAndWarmth bw = brightnessAndWarmthState.brightnessAndWarmth;
            if (brightnessAndWarmthState.isExternalChange) {
                namedWarmthBrightnessOptions.setSelectedIndex(namedWarmthBrightnessOptions.getAvailable().length - 1);

                int newCheckedRadioButtonId = getRadioButtonIdBySetting(namedWarmthBrightnessOptions.getSelected());
                RadioButton newCheckedRadioButton = (RadioButton) findViewById(newCheckedRadioButtonId);
                newCheckedRadioButton.setChecked(true);
                namedSettingsGroup.setCheckedRadioButtonNoEvent(newCheckedRadioButton);

                replaceNamedWarmthBrightness(new NamedWarmthBrightnessSetting(
                        namedWarmthBrightnessOptions.getSelected().name,
                        new WarmthBrightnessSetting(bw.warmth.value, bw.brightness.value),
                        namedWarmthBrightnessOptions.getSelected().isForOnyxCompatibility));
                disableControls(); // todo leave them enabled
                saveSelectedIndex();
            } else {
                replaceNamedWarmthBrightness(new NamedWarmthBrightnessSetting(
                        namedWarmthBrightnessOptions.getSelected().name,
                        new WarmthBrightnessSetting(bw.warmth.value, bw.brightness.value),
                        namedWarmthBrightnessOptions.getSelected().isForOnyxCompatibility));
                if (!slidingInProgress) {
                    saveNamedSettings();
                }
            }
        }));
        warmth.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (!fromUser) return;
                brightnessAndWarmthChangeRequest$.onNext(new BrightnessAndWarmth(
                        new Brightness(namedWarmthBrightnessOptions.getSelected().setting.brightness),
                        new Warmth(progress)));
            }

            public void onStartTrackingTouch(SeekBar seekBar) {
                slidingInProgress = true;
                status.setText("");
            }

            public void onStopTrackingTouch(SeekBar seekBar) {
                slidingInProgress = false;
                saveNamedSettings();
            }
        });

        brightness.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (!fromUser) return;
                brightnessAndWarmthChangeRequest$.onNext(new BrightnessAndWarmth(
                        new Brightness(progress),
                        new Warmth(namedWarmthBrightnessOptions.getSelected().setting.warmth)));
            }

            public void onStartTrackingTouch(SeekBar seekBar) {
                slidingInProgress = true;
                status.setText("");
            }

            public void onStopTrackingTouch(SeekBar seekBar) {
                slidingInProgress = false;
                saveNamedSettings();
            }
        });

        decreaseBrightnessButton.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                applyDeltaBrightnessRequest$.onNext(-1);
            }
        });
        increaseBrightnessButton.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                applyDeltaBrightnessRequest$.onNext(+1);
            }
        });

        decreaseWarmthButton.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                applyDeltaWarmthRequest$.onNext(-1);
            }
        });
        increaseWarmthButton.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                applyDeltaWarmthRequest$.onNext(+1);
            }
        });
    }

    private void initNamedWarmthBrightness() {
        onWarmthBrightnessChanged();
    }

    private void replaceNamedWarmthBrightness(NamedWarmthBrightnessSetting namedWarmthBrightnessSetting) {
        NamedWarmthBrightnessSetting oldSetting = namedWarmthBrightnessOptions.getSelected();

        if (oldSetting.equals(namedWarmthBrightnessSetting)) {
            onWarmthBrightnessChanged();
            return;
        }

        namedWarmthBrightnessOptions.replaceAndSelect(
                oldSetting,
                namedWarmthBrightnessSetting);

        namedSettingByRadioButtonId.remove(namedSettingsGroup.getCheckedRadioButtonId());
        namedSettingByRadioButtonId.put(namedSettingsGroup.getCheckedRadioButtonId(), namedWarmthBrightnessOptions.getSelected());

        onWarmthBrightnessChanged();
    }
    private void selectNamedWarmthBrightness(NamedWarmthBrightnessSetting namedWarmthBrightnessSetting) {
        namedWarmthBrightnessOptions.select(namedWarmthBrightnessSetting);

        if (namedWarmthBrightnessSetting.isForOnyxCompatibility) {
            disableControls();
            restoreExternallySetLedOutputRequest$.onNext(0);
        } else {
            enableControls();
            brightnessAndWarmthChangeRequest$.onNext(new BrightnessAndWarmth(
                    new Brightness(namedWarmthBrightnessSetting.setting.brightness),
                    new Warmth(namedWarmthBrightnessSetting.setting.warmth)
            ));
        }
    }

    private void onWarmthBrightnessChanged() {
        updateValues();
        updateSliders();
        updateName();
    }

    private void updateSliders() {
        boolean canEdit = namedWarmthBrightnessOptions.getSelected().canEdit();
        warmth.setEnabled(canEdit);
        brightness.setEnabled(canEdit);
        decreaseBrightnessButton.setEnabled(canEdit);
        increaseBrightnessButton.setEnabled(canEdit);
        decreaseWarmthButton.setEnabled(canEdit);
        increaseWarmthButton.setEnabled(canEdit);
        replaceWithPreset.setEnabled(canEdit);

        warmth.setProgress(namedWarmthBrightnessOptions.getSelected().setting.warmth);
        brightness.setProgress(namedWarmthBrightnessOptions.getSelected().setting.brightness);
    }

    Hashtable<Integer, NamedWarmthBrightnessSetting> namedSettingByRadioButtonId = new Hashtable<>();

    private void bindNamedSettingsRadioGroup() {
        for (NamedWarmthBrightnessSetting namedSetting : namedWarmthBrightnessOptions.getAvailable()) {
            final RadioButton radioButton = new RadioButton(this);
            radioButton.setText(namedSetting.name);
            if (namedSetting == namedWarmthBrightnessOptions.getSelected()) {
                radioButton.setChecked(true);
            }
            radioButton.setId(View.generateViewId());
            namedSettingByRadioButtonId.put(radioButton.getId(), namedSetting);
            radioButton.setLayoutParams(new RadioGroup.LayoutParams(FlexboxLayout.LayoutParams.WRAP_CONTENT, RadioGroup.LayoutParams.WRAP_CONTENT, 1));

            namedSettingsGroup.add(radioButton);
            ((FlexboxLayout)findViewById(R.id.namedSettingsLayout)).addView(radioButton);
        }
        namedSettingsGroup.setOnChoiceChanged(() -> {
            selectNamedWarmthBrightness(namedSettingByRadioButtonId.get(namedSettingsGroup.getCheckedRadioButtonId()));
            saveSelectedIndex();
            return null;
        });
    }

    private void updateName() {
        name.setInputType(namedWarmthBrightnessOptions.getSelected().canEdit() ? InputType.TYPE_CLASS_TEXT : InputType.TYPE_NULL);
        if (!name.getText().toString().equals(namedWarmthBrightnessOptions.getSelected().name)) {
            name.setText(namedWarmthBrightnessOptions.getSelected().name);
        }
    }

    File namedSettingsFile()  {
        return new File(getFilesDir(), "namedSettings.json");
    }

    Gson json = new Gson();
    private void migrateSavedSettings() {
        NamedWarmthBrightnessSetting[] savedSettings = loadNamedSettings();
        boolean changed = false;
        for (int i = 0; i< savedSettings.length; i++) {
            if (savedSettings[i].setting.brightness == 0) {
                savedSettings[i] = NamedWarmthBrightnessSetting.presets[i];
                changed = true;
            }
        }
        if (changed) {
            saveNamedSettings(savedSettings);
        }
    }

    private NamedWarmthBrightnessSetting[] loadNamedSettings() {
        File namedSettingsFile = namedSettingsFile();
        if (! namedSettingsFile.exists())
            return new NamedWarmthBrightnessSetting[0];

        byte[] bytes;
        try {
            bytes = Files.readAllBytes(namedSettingsFile.toPath());
        } catch (IOException e) {
            status.setText("Error reading presets. This should never happen.");
            return new NamedWarmthBrightnessSetting[0];
        }
        String namedSettingsAsJson = new String(bytes);
        return json.fromJson(namedSettingsAsJson, NamedWarmthBrightnessSetting[].class);
    }

    private void saveNamedSettings() {
        NamedWarmthBrightnessSetting[] namedSettingsToSave = namedWarmthBrightnessOptions.getAvailable();

        saveNamedSettings(namedSettingsToSave);

        status.setText(getText(R.string.saved));
    }

    private void saveNamedSettings(NamedWarmthBrightnessSetting[] namedSettingsToSave) {
        try {
            writeFile(namedSettingsFile(), json.toJson(namedSettingsToSave));
        }
        catch (IOException e){
            status.setText(getString(R.string.could_not_save, e.getMessage()));
        }
    }

    File selectionFile()  {
        return new File(getFilesDir(), "selectedIndex.txt");
    }

    private void saveSelectedIndex() {
        try {
            writeFile(selectionFile(), String.valueOf(namedWarmthBrightnessOptions.getSelectedIndex()));
            status.setText(getText(R.string.saved));
        }
        catch (IOException e){
            status.setText(getString(R.string.could_not_save, e.getMessage()));
        }
    }

    private int loadSelectedIndex() {
        File file = selectionFile();
        if (! file.exists())
            return 0;

        byte[] bytes;
        try {
            bytes = Files.readAllBytes(file.toPath());
        } catch (IOException e) {
            status.setText("Error reading selection. This should never happen.");
            return 0;
        }
        String selectedAsString = new String(bytes);
        return Integer.parseInt(selectedAsString);
    }

    void writeFile(File file, String data) throws IOException {
        try (FileOutputStream out = new FileOutputStream(file, false)) {
            byte[] contents = data.getBytes();
            out.write(contents);
            out.flush();
        }
    }

    private void updateValues() {
        brightnessValue.setText(namedWarmthBrightnessOptions.getSelected().setting.brightness + " / 100");
        warmthValue.setText(namedWarmthBrightnessOptions.getSelected().setting.warmth + " / 100");
    }


    private class SelectItem {
        private final NamedWarmthBrightnessSetting item;

        private SelectItem(NamedWarmthBrightnessSetting item) {
            this.item = item;
        }

        @Override
        public String toString() {
            return item.name;
        }
    }
}
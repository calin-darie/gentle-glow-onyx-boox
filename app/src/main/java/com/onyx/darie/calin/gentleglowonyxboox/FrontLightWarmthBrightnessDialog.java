package com.onyx.darie.calin.gentleglowonyxboox;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.SeekBar;
import android.widget.TextView;

import com.google.gson.Gson;

import org.apache.commons.lang3.ArrayUtils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Hashtable;

import butterknife.Bind;
import butterknife.ButterKnife;

public class FrontLightWarmthBrightnessDialog extends Activity {
    @Bind(R.id.status_textview)
    TextView status;

    @Bind(R.id.brightness_slider)
    SeekBar brightness;

    @Bind(R.id.warmth_slider)
    SeekBar warmth;

    @Bind(R.id.warmth_value_label)
    TextView warmthValue;

    @Bind(R.id.brightness_value_label)
    TextView brightnessValue;

    @Bind(R.id.decrease_brightness_by_1)
    Button decreaseBrightnessButton;

    @Bind(R.id.increase_brightness_by_1)
    Button increaseBrightnessButton;

    @Bind(R.id.decrease_warmth_by_1)
    Button decreaseWarmthButton;

    @Bind(R.id.increase_warmth_by_1)
    Button increaseWarmthButton;

    @Bind(R.id.namedSettings)
    RadioGroup namedSettings;

    @Bind(R.id.name_edit)
    EditText name;

    @Bind(R.id.replace_with_preset_button)
    Button replaceWithPreset;


    WarmColdToWarmthBrightnessAdapter adapter;
    private NamedWarmthBrightnessOptions namedWarmthBrightnessOptions;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_front_light_warmth_brightness_dialog);

        ButterKnife.bind(this);

        if (!Frontlight.hasDualFrontlight(this)) {
            status.setText(getText(R.string.device_not_supported));
            namedSettings.setEnabled(false);
            brightness.setEnabled(false);
            warmth.setEnabled(false);
            name.setEnabled(false);
            return;
        }

        Frontlight.ensureTurnedOn(this);

        adapter = Frontlight.getWarmColdToWarmthBrightnessAdapter(this);

        final WarmColdSetting initialWarmColdSetting = Frontlight.getWarmCold(this);

        namedWarmthBrightnessOptions = getNamedWarmthBrightnessOptions(initialWarmColdSetting);

        if (namedWarmthBrightnessOptions.getSelected().isForOnyxCompatibility) {
            saveOnyxSliderWarmCold(initialWarmColdSetting);
        }

        initNamedWarmthBrightness();

        bindSliders();

        bindName();;

        bindNamedSettingsRadioGroup();

        bindResetSpinner();
    }

    private void bindResetSpinner() {

        final Context context = this;

        final ArrayAdapter arrayAdapter = new ArrayAdapter(context, android.R.layout.select_dialog_item);
        final AlertDialog.Builder alertDialog = new AlertDialog.Builder(context);
        alertDialog.setTitle(getText(R.string.replace_with_preset));
        alertDialog.setSingleChoiceItems(arrayAdapter, -1, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int position) {
                SelectItem item = (SelectItem)arrayAdapter.getItem(position);

                replaceNamedWarmthBrightness(item.item);
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

    private NamedWarmthBrightnessOptions getNamedWarmthBrightnessOptions(WarmColdSetting initialWarmColdSetting) {
        final NamedWarmthBrightnessSetting[] savedSettings = loadNamedSettings();
        final int selectedIndex = loadSelectedIndex();
        if (savedSettings.length == 0) {
            return NamedWarmthBrightnessSetting.getPresetNamedSettings(adapter.findWarmthBrightnessApproximationForWarmCold(initialWarmColdSetting));
        }
        final NamedWarmthBrightnessSetting savedWarmthBrightness = savedSettings[selectedIndex];
        final WarmColdSetting savedWarmCold = adapter.convertWarmthBrightnessToWarmCold(savedWarmthBrightness.setting);
        final boolean isSavedWarmColdStillCurrent = initialWarmColdSetting.equals(savedWarmCold);

        return  isSavedWarmColdStillCurrent?
                NamedWarmthBrightnessSetting.getNamedSettings(savedSettings, selectedIndex):
                NamedWarmthBrightnessSetting.getNamedSettings(savedSettings, adapter.findWarmthBrightnessApproximationForWarmCold(initialWarmColdSetting));
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

                RadioButton selectedRadio = namedSettings.findViewById(namedSettings.getCheckedRadioButtonId());
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
        // todo throttling?
        warmth.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                replaceNamedWarmthBrightness(new NamedWarmthBrightnessSetting(
                        namedWarmthBrightnessOptions.getSelected().name,
                        new WarmthBrightnessSetting(progress, namedWarmthBrightnessOptions.getSelected().setting.brightness),
                        namedWarmthBrightnessOptions.getSelected().isForOnyxCompatibility));
            }

            public void onStartTrackingTouch(SeekBar seekBar) {
                status.setText("");
            }

            public void onStopTrackingTouch(SeekBar seekBar) {
                saveNamedSettings();
            }
        });

        brightness.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                replaceNamedWarmthBrightness(new NamedWarmthBrightnessSetting(
                        namedWarmthBrightnessOptions.getSelected().name,
                        new WarmthBrightnessSetting(namedWarmthBrightnessOptions.getSelected().setting.warmth, progress),
                        namedWarmthBrightnessOptions.getSelected().isForOnyxCompatibility));
            }

            public void onStartTrackingTouch(SeekBar seekBar) {
                status.setText("");
            }

            public void onStopTrackingTouch(SeekBar seekBar) {
                saveNamedSettings();
            }
        });

        decreaseBrightnessButton.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                decreaseBrightness();
            }
        });
        increaseBrightnessButton.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                increaseBrightness();
            }
        });

        decreaseWarmthButton.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                decreaseWarmth();
            }
        });
        increaseWarmthButton.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                increaseWarmth();
            }
        });
    }

    private void initNamedWarmthBrightness() {
        onWarmthBrightnessChanged();
    }

    private void replaceNamedWarmthBrightness(NamedWarmthBrightnessSetting namedWarmthBrightnessSetting) {
        NamedWarmthBrightnessSetting oldSetting = namedWarmthBrightnessOptions.getSelected();

        if (oldSetting.equals(namedWarmthBrightnessSetting))
            return;

        namedWarmthBrightnessOptions.replaceAndSelect(
                oldSetting,
                namedWarmthBrightnessSetting);

        namedSettingByRadioButtonId.remove(namedSettings.getCheckedRadioButtonId());
        namedSettingByRadioButtonId.put(namedSettings.getCheckedRadioButtonId(), namedWarmthBrightnessOptions.getSelected());

        onWarmthBrightnessChanged();
    }
    private void selectNamedWarmthBrightness(NamedWarmthBrightnessSetting namedWarmthBrightnessSetting) {
        namedWarmthBrightnessOptions.select(namedWarmthBrightnessSetting);

        onWarmthBrightnessChanged();
    }

    private void onWarmthBrightnessChanged() {
        updateValues();
        updateSliders();
        updateName();
        updateFrontLight();
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

    Hashtable<Integer, NamedWarmthBrightnessSetting> namedSettingByRadioButtonId = new Hashtable<>();;

    private void bindNamedSettingsRadioGroup() {
        int nextId = 15000;
        for (NamedWarmthBrightnessSetting namedSetting : namedWarmthBrightnessOptions.getAvailable()) {
            final RadioButton radioButton = new RadioButton(this);
            final WarmthBrightnessSetting setting = namedSetting.setting;
            radioButton.setTextSize(20); // todo can we use style for this?
            radioButton.setText(namedSetting.name); // todo translate
            if (namedSetting == namedWarmthBrightnessOptions.getSelected()) {
                radioButton.setChecked(true);
            }
            final int id = nextId;
            nextId ++;
            radioButton.setId(id);
            namedSettingByRadioButtonId.put(id, namedSetting);
            radioButton.setLayoutParams(new RadioGroup.LayoutParams(RadioGroup.LayoutParams.WRAP_CONTENT, RadioGroup.LayoutParams.WRAP_CONTENT, 1));
            radioButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    selectNamedWarmthBrightness(namedSettingByRadioButtonId.get(id));
                    saveSelectedIndex();
                }
            });
            namedSettings.addView(radioButton);
        }
    }

    private void updateName() {
        name.setInputType(namedWarmthBrightnessOptions.getSelected().canEdit() ? InputType.TYPE_CLASS_TEXT : InputType.TYPE_NULL);
        if (!name.getText().toString().equals(namedWarmthBrightnessOptions.getSelected().name)) {
            name.setText(namedWarmthBrightnessOptions.getSelected().name);
        }
    }

    private void updateFrontLight() {
        WarmColdSetting warmCold = namedWarmthBrightnessOptions.getSelected().isForOnyxCompatibility?
                loadOnyxSliderWarmCold() :
                adapter.convertWarmthBrightnessToWarmCold(namedWarmthBrightnessOptions.getSelected().setting);
        Frontlight.setWarmCold(warmCold, this);
    }

    File namedSettingsFile()  {
        return new File(getFilesDir(), "namedSettings.json");
    }

    Gson json = new Gson();
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
        try {
            NamedWarmthBrightnessSetting[] namedSettingsToSave =
                    namedSettingByRadioButtonId.values().toArray(new NamedWarmthBrightnessSetting[0]);
            ArrayUtils.reverse(namedSettingsToSave);
            writeFile(namedSettingsFile(), json.toJson(namedSettingsToSave));
            status.setText(getText(R.string.saved));
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
        return Integer.valueOf(selectedAsString);
    }

    File onyxSliderFile()  {
        return new File(getFilesDir(), "onyxSlider.json");
    }
    private void saveOnyxSliderWarmCold (WarmColdSetting onyxSliderWarmCold) {
        try {
            writeFile(onyxSliderFile(), json.toJson(onyxSliderWarmCold));
        }
        catch (IOException e){
            Log.e("autosave", "could not save onyx slider");
        }
    }

    private WarmColdSetting loadOnyxSliderWarmCold() {
        File namedSettingsFile = onyxSliderFile();
        if (! namedSettingsFile.exists())
            return adapter.convertWarmthBrightnessToWarmCold(NamedWarmthBrightnessSetting.onyxSliderPreset().setting);

        byte[] bytes;
        try {
            bytes = Files.readAllBytes(namedSettingsFile.toPath());
        } catch (IOException e) {
            Log.e("load", "Error reading presets. This should never happen.");
            return adapter.convertWarmthBrightnessToWarmCold(NamedWarmthBrightnessSetting.onyxSliderPreset().setting);
        }
        String settingAsJson = new String(bytes);
        return json.fromJson(settingAsJson, WarmColdSetting.class);
    }

    void writeFile(File file, String data) throws IOException {
        FileOutputStream out = new FileOutputStream(file, false);
        try {
            byte[] contents = data.getBytes();
            out.write(contents);
            out.flush();
        }
        finally {
            out.close();
        }
    }

    private void updateValues() {
        brightnessValue.setText(namedWarmthBrightnessOptions.getSelected().setting.brightness + " / 100");
        warmthValue.setText(namedWarmthBrightnessOptions.getSelected().setting.warmth + " / 100");
    }

    public void decreaseBrightness() {
        if (namedWarmthBrightnessOptions.getSelected().setting.brightness > 0) {
            replaceNamedWarmthBrightness(new NamedWarmthBrightnessSetting(
                    namedWarmthBrightnessOptions.getSelected().name,
                    new WarmthBrightnessSetting(namedWarmthBrightnessOptions.getSelected().setting.warmth, namedWarmthBrightnessOptions.getSelected().setting.brightness - 1),
                    namedWarmthBrightnessOptions.getSelected().isForOnyxCompatibility));
            saveNamedSettings();
        }
    }
    public void increaseBrightness() {
        if (namedWarmthBrightnessOptions.getSelected().setting.brightness < 100) {
            replaceNamedWarmthBrightness(new NamedWarmthBrightnessSetting(
                    namedWarmthBrightnessOptions.getSelected().name,
                    new WarmthBrightnessSetting(namedWarmthBrightnessOptions.getSelected().setting.warmth, namedWarmthBrightnessOptions.getSelected().setting.brightness + 1),
                    namedWarmthBrightnessOptions.getSelected().isForOnyxCompatibility));
            saveNamedSettings();
        }
    }

    public void decreaseWarmth() {
        if (namedWarmthBrightnessOptions.getSelected().setting.warmth > 0) {
            replaceNamedWarmthBrightness(new NamedWarmthBrightnessSetting(
                    namedWarmthBrightnessOptions.getSelected().name,
                    new WarmthBrightnessSetting(namedWarmthBrightnessOptions.getSelected().setting.warmth -1, namedWarmthBrightnessOptions.getSelected().setting.brightness) ,
                    namedWarmthBrightnessOptions.getSelected().isForOnyxCompatibility));
            saveNamedSettings();
        }
    }
    public void increaseWarmth() {
        if (namedWarmthBrightnessOptions.getSelected().setting.warmth < 100) {
            replaceNamedWarmthBrightness(new NamedWarmthBrightnessSetting(
                    namedWarmthBrightnessOptions.getSelected().name,
                    new WarmthBrightnessSetting(namedWarmthBrightnessOptions.getSelected().setting.warmth + 1, namedWarmthBrightnessOptions.getSelected().setting.brightness) ,
                    namedWarmthBrightnessOptions.getSelected().isForOnyxCompatibility));
            saveNamedSettings();
        }
    }

    private class SelectItem {
        private final NamedWarmthBrightnessSetting item;

        private SelectItem(NamedWarmthBrightnessSetting item) {
            this.item = item;
        }

        public NamedWarmthBrightnessSetting getItem() {
            return item;
        }

        @Override
        public String toString() {
            return item.name;
        }
    }
}
package com.onyx.darie.calin.gentleglowonyxboox;

import android.app.Activity;
import android.os.Bundle;
import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.SeekBar;
import android.widget.TextView;

import com.google.gson.Gson;
import com.onyx.android.sdk.api.device.FrontLightController;

import org.apache.commons.lang3.ArrayUtils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Hashtable;

import butterknife.Bind;
import butterknife.ButterKnife;

public class FrontLightWarmthBrightnessDialog extends Activity {

    private NamedWarmthBrightnessSetting[] availableNamedSettings;

    private class WarmColdSetting {
        public final int warm;
        public final int cold;

        public WarmColdSetting(int warm, int cold) {
            this.warm = warm;
            this.cold = cold;
        }
    }

    private class WarmColdToWarmthBrightnessAdapter {
        private final WarmColdSetting maxWarmColdSetting;
        private final int MAX_BRIGHTNESS_LUX = 112;

        public WarmColdToWarmthBrightnessAdapter(WarmColdSetting maxWarmColdSetting) {
            this.maxWarmColdSetting = maxWarmColdSetting;
        }

        public WarmColdSetting convertWarmthBrightnessToWarmCold (NamedWarmthBrightnessSetting warmthBrightnessSetting) {
            final double desiredBrightnessLux = convertBrightnessSettingToLux(warmthBrightnessSetting.setting.brightness);

            final double warmBrightnessLux = (double)desiredBrightnessLux * warmthBrightnessSetting.setting.warmth / 100;
            final int warmSetting = convertLuxToWarmOrColdSetting(warmBrightnessLux, maxWarmColdSetting.warm);

            final double coldBrightnessLux = desiredBrightnessLux - warmBrightnessLux;
            final int coldSetting = convertLuxToWarmOrColdSetting(coldBrightnessLux, maxWarmColdSetting.cold);

            return warmthBrightnessSetting.isForOnyxCompatibility?
                    new WarmColdSetting((int)Math.round((double)warmSetting / 5) * 5, (int)Math.round((double)coldSetting / 5) * 5):
                    new WarmColdSetting(warmSetting, coldSetting);
        }
        
        public WarmthBrightnessSetting convertWarmColdToWarmthBrightness (WarmColdSetting warmCold) {
            final double warmBrightnessLux = convertWarmOrColdSettingToLux(warmCold.warm);
            final double coldBrightnessLux = convertWarmOrColdSettingToLux(warmCold.cold);

            final double brightnessLux = warmBrightnessLux + coldBrightnessLux;

            if (brightnessLux == 0)
                return new WarmthBrightnessSetting(50, 0);

            final int warmthPercent = (int)Math.round(Math.min(100, warmBrightnessLux * 100 / brightnessLux));
            final int brightness = convertLuxToBrigthnessSetting(brightnessLux);
            
            return new WarmthBrightnessSetting(warmthPercent, brightness);
        }// todo multiple conversions caused by simply opening & closing the dialog should not result in changes


        private int convertLuxToWarmOrColdSetting(double brightnessLux, int maxResult) {
            if (brightnessLux == 0) return 0;
            final int assumedMinResult = 0;
            return Math.max(assumedMinResult, Math.min(maxResult, (int) Math.round(34 * Math.log(17 * brightnessLux))));
        }

        private double convertWarmOrColdSettingToLux(int setting) {
            if (setting == 0) return 0;
            return Math.pow(Math.E, (double)setting/34)/17;
        }

        private double convertBrightnessSettingToLux(int slider) {
            if (slider == 0) return 0;

            return Math.min(MAX_BRIGHTNESS_LUX, 0.501717 * Math.pow(Math.E, (0.0545382 * slider)));
        }

        private int convertLuxToBrigthnessSetting (double lux) {
            if (lux == 0) return 0;
            final double MAX_BRIGHTNESS_SETTING = 100;
            return (int)Math.round(Math.max(1, Math.min(MAX_BRIGHTNESS_SETTING, Math.round(18.3358 * Math.log(1.993155503999267 * lux)))));
        }
    }

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

    private NamedWarmthBrightnessSetting namedWarmthBrightnessSetting;

    WarmColdToWarmthBrightnessAdapter adapter;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_front_light_warmth_brightness_dialog);

        ButterKnife.bind(this);

        if (!FrontLightController.hasCTMBrightness(this)) {
            status.setText(getText(R.string.device_not_supported));
            namedSettings.setEnabled(false);
            brightness.setEnabled(false);
            warmth.setEnabled(false);
            name.setEnabled(false);
            return;
        }

        final WarmthBrightnessSetting initialSetting = getInitialWarmthBrightnessSettingFromCurrentWarmColdValues();
        final NamedWarmthBrightnessSetting[] savedSettings = loadNamedSettings();
        availableNamedSettings = NamedWarmthBrightnessSetting.getNamedSettings(savedSettings, initialSetting);

        NamedWarmthBrightnessSetting initialNamedSetting = availableNamedSettings[0];
        for (NamedWarmthBrightnessSetting namedSetting : availableNamedSettings) {
            if (initialSetting.equals(namedSetting.setting)) {
                initialNamedSetting = namedSetting;
                break;
            }
        }

        initNamedWarmthBrightness(initialNamedSetting);

        bindSliders();

        bindName();;

        bindNamedSettingsRadioGroup();
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
        setNamedWarmthBrightness(
                new NamedWarmthBrightnessSetting(
                        newName,
                        namedWarmthBrightnessSetting.setting,
                        namedWarmthBrightnessSetting.isForOnyxCompatibility)
        );
        saveNanedSettings();
    }

    private WarmthBrightnessSetting getInitialWarmthBrightnessSettingFromCurrentWarmColdValues() {
        adapter = new WarmColdToWarmthBrightnessAdapter(new WarmColdSetting(
               max(FrontLightController.getWarmLightValues(this)),
               max(FrontLightController.getColdLightValues(this))
       ));

        final WarmthBrightnessSetting initialSetting = adapter.convertWarmColdToWarmthBrightness(
            new WarmColdSetting(
                FrontLightController.isWarmLightOn(this)?  FrontLightController.getWarmLightConfigValue(this): 0,
                FrontLightController.isColdLightOn(this)?  FrontLightController.getColdLightConfigValue(this): 0
            )
        );
        return initialSetting;
    }

    private void bindSliders() {
        // todo throttling?
        NamedWarmthBrightnessSetting namedSetting = namedSettingByRadioButtonId.get(namedSettings.getCheckedRadioButtonId());
        warmth.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                setNamedWarmthBrightness(new NamedWarmthBrightnessSetting(
                        namedWarmthBrightnessSetting.name,
                        new WarmthBrightnessSetting(progress, namedWarmthBrightnessSetting.setting.brightness),
                        namedWarmthBrightnessSetting.isForOnyxCompatibility));
            }

            public void onStartTrackingTouch(SeekBar seekBar) {/* required by contract, not needed */}

            public void onStopTrackingTouch(SeekBar seekBar) {
                saveNanedSettings();
            }
        });

        brightness.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                setNamedWarmthBrightness(new NamedWarmthBrightnessSetting(
                        namedWarmthBrightnessSetting.name,
                        new WarmthBrightnessSetting(namedWarmthBrightnessSetting.setting.warmth, progress),
                        namedWarmthBrightnessSetting.isForOnyxCompatibility));
            }

            public void onStartTrackingTouch(SeekBar seekBar) {/* required by contract, not needed */}

            public void onStopTrackingTouch(SeekBar seekBar) {
                saveNanedSettings();
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

    private void initNamedWarmthBrightness(NamedWarmthBrightnessSetting namedWarmthBrightnessSetting) {
        this.namedWarmthBrightnessSetting = namedWarmthBrightnessSetting;

        updateFrontLight();
        updateValues();
        updateSliders();
        updateName();
    }

    private void setNamedWarmthBrightness(NamedWarmthBrightnessSetting namedWarmthBrightnessSetting) {
        NamedWarmthBrightnessSetting oldSetting = this.namedWarmthBrightnessSetting;

        if (oldSetting.equals(namedWarmthBrightnessSetting))
            return;

        this.namedWarmthBrightnessSetting = namedWarmthBrightnessSetting;

        namedSettingByRadioButtonId.remove(namedSettings.getCheckedRadioButtonId());
        namedSettingByRadioButtonId.put(namedSettings.getCheckedRadioButtonId(), namedWarmthBrightnessSetting);

        updateFrontLight();
        updateValues();
        updateSliders();
        updateName();
    }

    private void updateSliders() {
        warmth.setEnabled(namedWarmthBrightnessSetting.canEdit());
        brightness.setEnabled(namedWarmthBrightnessSetting.canEdit());
        warmth.setProgress(namedWarmthBrightnessSetting.setting.warmth);
        brightness.setProgress(namedWarmthBrightnessSetting.setting.brightness);
    }

    Hashtable<Integer, NamedWarmthBrightnessSetting> namedSettingByRadioButtonId = new Hashtable<>();;

    private void bindNamedSettingsRadioGroup() {
        int nextId = 15000;
        for (NamedWarmthBrightnessSetting namedSetting : availableNamedSettings) {
            final RadioButton radioButton = new RadioButton(this);
            final WarmthBrightnessSetting setting = namedSetting.setting;
            radioButton.setTextSize(20); // todo can we use style for this?
            radioButton.setText(namedSetting.name); // todo translate
            if (namedSetting == namedWarmthBrightnessSetting) {
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
                    setNamedWarmthBrightness(namedSettingByRadioButtonId.get(id));
                    status.setText("");
                }
            });
            namedSettings.addView(radioButton);
        }
    }

    private void updateName() {
        name.setInputType(namedWarmthBrightnessSetting.canEdit() ? InputType.TYPE_CLASS_TEXT : InputType.TYPE_NULL);
        if (!name.getText().toString().equals(namedWarmthBrightnessSetting.name)) {
            name.setText(namedWarmthBrightnessSetting.name);
        }
    }

    File namedSettingsFile()  {
        return new File(getFilesDir(), "namedSettings.json");
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

    Gson json = new Gson();
    private void updateFrontLight() {
        WarmColdSetting setting = adapter.convertWarmthBrightnessToWarmCold(namedWarmthBrightnessSetting);

        FrontLightController.setWarmLightDeviceValue(this, setting.warm);
        FrontLightController.setColdLightDeviceValue(this, setting.cold);
    }

    private void saveNanedSettings() {
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
        brightnessValue.setText(namedWarmthBrightnessSetting.setting.brightness + " / 100");
        warmthValue.setText(namedWarmthBrightnessSetting.setting.warmth + " / 100");
    }

    private static Integer max(Integer[] values) {
        if (values.length == 0) return Integer.MIN_VALUE;
        Integer max = values[0];
        for (int i = 1; i < values.length; i++) {
            if (values[i] > max) {
                max = values[i];
            }
        }
        return max;
    }

    //todo act by changing the mdoel, not the sliders
    public void decreaseBrightness() {
        if (namedWarmthBrightnessSetting.setting.brightness > 0) {
            brightness.setProgress(namedWarmthBrightnessSetting.setting.brightness - 1);
            saveNanedSettings();
        }
    }
    public void increaseBrightness() {
        if (namedWarmthBrightnessSetting.setting.brightness < 100) {
            brightness.setProgress(namedWarmthBrightnessSetting.setting.brightness + 1);
            saveNanedSettings();
        }
    }

    public void decreaseWarmth() {
        if (namedWarmthBrightnessSetting.setting.warmth > 0) {
            warmth.setProgress(namedWarmthBrightnessSetting.setting.warmth - 1);
            saveNanedSettings();
        }
    }
    public void increaseWarmth() {
        if (namedWarmthBrightnessSetting.setting.warmth < 100) {
            warmth.setProgress(namedWarmthBrightnessSetting.setting.warmth + 1);
            saveNanedSettings();
        }
    }
}


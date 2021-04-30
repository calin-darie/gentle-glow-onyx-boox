package com.onyx.darie.calin.gentleglowonyxboox;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.SeekBar;
import android.widget.TextView;

import com.onyx.android.sdk.api.device.FrontLightController;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class FrontLightWarmthBrightnessDialog extends Activity {
    private class WarmColdSetting {
        public final int warm;
        public final int cold;

        public WarmColdSetting(int warm, int cold) {
            this.warm = warm;
            this.cold = cold;
        }
    }

    public class WarmthBrightnessSetting {
        public final int warmth;
        public final int brightness;

        public WarmthBrightnessSetting(int warmthPercent, int brightnessSetting) {
            this.warmth = warmthPercent;
            this.brightness = brightnessSetting;
        }
    }
    
    private class WarmColdToWarmthBrightnessAdapter {
        private final WarmColdSetting maxWarmColdSetting;
        private final int MAX_BRIGHTNESS_LUX = 112;

        public WarmColdToWarmthBrightnessAdapter(WarmColdSetting maxWarmColdSetting) {
            this.maxWarmColdSetting = maxWarmColdSetting;
        }

        public WarmColdSetting convertWarmthBrightnessToWarmCold (WarmthBrightnessSetting warmthBrightnessSetting) {
            final double desiredBrightnessLux = convertBrightnessSettingToLux(warmthBrightnessSetting.brightness);

            final double warmBrightnessLux = (double)desiredBrightnessLux * warmthBrightnessSetting.warmth / 100;
            final int warmSetting = convertLuxToWarmOrColdSetting(warmBrightnessLux, maxWarmColdSetting.warm);

            final double coldBrightnessLux = desiredBrightnessLux - warmBrightnessLux;
            final int coldSetting = convertLuxToWarmOrColdSetting(coldBrightnessLux, maxWarmColdSetting.cold);
            return new WarmColdSetting(warmSetting, coldSetting);
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
            return (int)Math.round(Math.min(MAX_BRIGHTNESS_SETTING, Math.round(18.3358 * Math.log(1.993155503999267 * lux))));
        }
    }

    @Bind(R.id.summary_textview)
    TextView summary;

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

    private WarmthBrightnessSetting warmthBrightnessSetting;

    WarmColdToWarmthBrightnessAdapter adapter;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_front_light_warmth_brightness_dialog);

        ButterKnife.bind(this);

        if (!FrontLightController.hasCTMBrightness(this)) {
            summary.setText(getText(R.string.device_not_supported));
            brightness.setEnabled(false);
            warmth.setEnabled(false);
            return;
        }

         adapter = new WarmColdToWarmthBrightnessAdapter(new WarmColdSetting(
                max(FrontLightController.getWarmLightValues(this)),
                max(FrontLightController.getColdLightValues(this))
        ));
        warmthBrightnessSetting = adapter.convertWarmColdToWarmthBrightness(
            new WarmColdSetting(
                FrontLightController.isWarmLightOn(this)?  FrontLightController.getWarmLightConfigValue(this): 0,
                FrontLightController.isColdLightOn(this)?  FrontLightController.getColdLightConfigValue(this): 0
            )
        );

        warmth.setProgress(warmthBrightnessSetting.warmth);
        brightness.setProgress(warmthBrightnessSetting.brightness);

        // todo throttling?
        warmth.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                warmthBrightnessSetting = new WarmthBrightnessSetting(progress, warmthBrightnessSetting.brightness);
                updateFrontLight();
            }

            public void onStartTrackingTouch(SeekBar seekBar) {
                // TODO Auto-generated method stub
            }

            public void onStopTrackingTouch(SeekBar seekBar) {
            }
        });

        brightness.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                warmthBrightnessSetting = new WarmthBrightnessSetting(warmthBrightnessSetting.warmth, progress);
                updateFrontLight();
            }

            public void onStartTrackingTouch(SeekBar seekBar) {
                // TODO Auto-generated method stub
            }

            public void onStopTrackingTouch(SeekBar seekBar) {
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

        updateValues();
    }

    private void updateFrontLight() {
        WarmColdSetting setting = adapter.convertWarmthBrightnessToWarmCold(warmthBrightnessSetting);

        setWarmBrightness(setting.warm);
        setColdBrightness(setting.cold);
    }

    private void updateValues() {
        brightnessValue.setText(warmthBrightnessSetting.brightness + " / 100");
        warmthValue.setText(warmthBrightnessSetting.warmth + " / 100");
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

    private void setColdBrightness(int brightness) {
        FrontLightController.setColdLightDeviceValue(this, brightness);
        updateValues();
    }
    private void setWarmBrightness(int brightness) {
        FrontLightController.setWarmLightDeviceValue(this, brightness);
        updateValues();
    }

    public void decreaseBrightness() {
        if (warmthBrightnessSetting.brightness > 0) {
            brightness.setProgress(warmthBrightnessSetting.brightness - 1);
        }
    }
    public void increaseBrightness() {
        if (warmthBrightnessSetting.brightness < 100) {
            brightness.setProgress(warmthBrightnessSetting.brightness + 1);
        }
    }

    public void decreaseWarmth() {
        if (warmthBrightnessSetting.warmth > 0) {
            warmth.setProgress(warmthBrightnessSetting.warmth - 1);
        }
    }
    public void increaseWarmth() {
        if (warmthBrightnessSetting.warmth < 100) {
            warmth.setProgress(warmthBrightnessSetting.warmth + 1);
        }
    }
}

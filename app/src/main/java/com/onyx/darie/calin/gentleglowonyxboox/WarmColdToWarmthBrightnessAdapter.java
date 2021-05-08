package com.onyx.darie.calin.gentleglowonyxboox;


public class WarmColdToWarmthBrightnessAdapter {
    private final WarmColdSetting maxWarmColdSetting;
    private final int MAX_BRIGHTNESS_LUX = 112;
    private final Integer[] onyxWarmValues;
    private final Integer[] onyxColdValues;

    public WarmColdToWarmthBrightnessAdapter(Integer[] warmValues, Integer[] coldValues) {
        this.maxWarmColdSetting = new WarmColdSetting(max(warmValues), max(coldValues));
        this.onyxWarmValues = warmValues;
        this.onyxColdValues = coldValues;
    }

    public WarmColdSetting convertWarmthBrightnessToWarmCold (NamedWarmthBrightnessSetting warmthBrightnessSetting) {
        final double desiredBrightnessLux = convertBrightnessSettingToLux(warmthBrightnessSetting.setting.brightness);

        final double warmBrightnessLux = (double)desiredBrightnessLux * warmthBrightnessSetting.setting.warmth / 100;
        final int warmSetting = convertLuxToWarmOrColdSetting(warmBrightnessLux, maxWarmColdSetting.warm);

        final double coldBrightnessLux = desiredBrightnessLux - warmBrightnessLux;
        final int coldSetting = convertLuxToWarmOrColdSetting(coldBrightnessLux, maxWarmColdSetting.cold);

        return warmthBrightnessSetting.isForOnyxCompatibility?
                new WarmColdSetting(closest(warmSetting, onyxWarmValues), closest(coldSetting, onyxColdValues)):
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
    }// todo unit test multiple conversions caused by simply opening & closing the dialog should not result in changes


    private int convertLuxToWarmOrColdSetting(double brightnessLux, int maxResult) {
        if (brightnessLux == 0) return 0;
        final int assumedMinResult = 1;
        return Math.max(assumedMinResult, Math.min(maxResult, (int) Math.round(34 * Math.log(17 * brightnessLux))));
    }

    public double convertWarmOrColdSettingToLux(int setting) {
        if (setting == 0) return 0;
        return Math.pow(Math.E, (double)setting/34)/17;
    }

    private double convertBrightnessSettingToLux(int slider) {
        if (slider == 0)
            return 0;

        if (slider <= 5) {
            return 0.0223609 * Math.pow(slider, 2) - 0.0406061 * slider + 0.0861964;
        }

        return Math.min(MAX_BRIGHTNESS_LUX, 0.501717 * Math.pow(Math.E, (0.0545382 * slider)));
    }

    private int convertLuxToBrigthnessSetting (double lux) {
        if (lux == 0)
            return 0;

        if (lux <= 0.5) {
            return (int)Math.round(0.90797 + 0.214277 * Math.sqrt(Math.max(0, 974 * lux - 66)));
        }

        final double MAX_BRIGHTNESS_SETTING = 100;
        return (int)Math.round(Math.max(1, Math.min(MAX_BRIGHTNESS_SETTING, Math.round(18.3358 * Math.log(1.993155503999267 * lux)))));
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

    private static Integer closest(double target, Integer[] values) {
        if (values.length == 0) return Integer.MIN_VALUE;
        double minDiff = Math.abs(target - values[0]);
        int closest = values [0];
        for (int i = 1; i < values.length; i++) {
            final double diff = Math.abs(target - values[i]);
            if (diff < minDiff) {
                minDiff = diff;
                closest = values[i];
            }
        }
        return closest;
    }
}

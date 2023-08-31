package com.onyx.darie.calin.gentleglowonyxboox;

/** to be replaced by {@link BrightnessAndWarmthToWarmAndColdLedOutputAdapter} **/
@Deprecated
public class WarmColdToWarmthBrightnessAdapter {
    private final BrightnessAndWarmthToWarmAndColdLedOutputAdapter newAdapter;

    public WarmColdToWarmthBrightnessAdapter(Integer[] warmValues, Integer[] coldValues) {
        newAdapter = new BrightnessAndWarmthToWarmAndColdLedOutputAdapter(new Range<>(coldValues[1], coldValues[coldValues.length - 1]));
    }

    public WarmColdSetting convertWarmthBrightnessToWarmCold (WarmthBrightnessSetting warmthBrightness) {
        final WarmAndColdLedOutput result = newAdapter.toWarmCold(new BrightnessAndWarmth(new Brightness(warmthBrightness.brightness), new Warmth(warmthBrightness.warmth)));

        return new WarmColdSetting(result.warm, result.cold);
    }

    public WarmthBrightnessSetting findWarmthBrightnessApproximationForWarmCold(WarmColdSetting warmCold) {
        final double warmBrightnessLux = convertWarmOrColdSettingToLux(warmCold.warm);
        final double coldBrightnessLux = convertWarmOrColdSettingToLux(warmCold.cold);

        final double brightnessLux = warmBrightnessLux + coldBrightnessLux;

        if (brightnessLux == 0)
            return new WarmthBrightnessSetting(50, 0);

        final int warmthPercent = (int)Math.round(Math.min(100, warmBrightnessLux * 100 / brightnessLux));
        final int brightness = convertLuxToBrigthnessSetting(brightnessLux);

        return new WarmthBrightnessSetting(warmthPercent, brightness);
    }

    private double convertWarmOrColdSettingToLux(int setting) {
        if (setting == 0) return 0;
        return Math.pow(Math.E, (double)setting/34)/17;
    }

    private int convertLuxToBrigthnessSetting (double lux) {
        if (lux == 0)
            return 0;

        if (lux <= 0.5) {
            return (int)Math.round(0.90797 + 0.214277 * Math.sqrt(Math.max(0, 974 * lux - 66)));
        }

        final double MAX_BRIGHTNESS_SETTING = 100;
        return (int)Math.round(Math.max(1, Math.min(MAX_BRIGHTNESS_SETTING, Math.round(18.6916 * Math.log(2 * lux)))));
    }
}

package com.onyx.darie.calin.gentleglowonyxboox;

public class BrightnessAndWarmthToWarmAndColdLedOutputAdapter {
    private final int MAX_BRIGHTNESS_LUX = 112;
    private Range<Integer> ledOutputRange;

    public BrightnessAndWarmthToWarmAndColdLedOutputAdapter(Range<Integer> ledOutputRange) {
        this.ledOutputRange = ledOutputRange;
    }

    public WarmAndColdLedOutput toWarmCold (BrightnessAndWarmth brightnessAndWarmth) {
        final double desiredBrightnessLux = convertBrightnessSettingToLux(brightnessAndWarmth.brightness.value);

        final double warmBrightnessLux = desiredBrightnessLux * brightnessAndWarmth.warmth.value / 100;
        final int warmLedOutput = convertLuxToLedOutput(warmBrightnessLux);

        final double coldBrightnessLux = desiredBrightnessLux - warmBrightnessLux;
        final int coldLedOutput = convertLuxToLedOutput(coldBrightnessLux);

        return new WarmAndColdLedOutput(warmLedOutput, coldLedOutput);
    }

    public BrightnessAndWarmth findBrightnessAndColdApproximationForWarmCold(WarmAndColdLedOutput warmCold) {
        final double warmBrightnessLux = convertLedOutputToLux(warmCold.warm);
        final double coldBrightnessLux = convertLedOutputToLux(warmCold.cold);

        final double brightnessLux = warmBrightnessLux + coldBrightnessLux;

        if (brightnessLux == 0)
            return new BrightnessAndWarmth(new Brightness(0), new Warmth(50));

        final int warmthPercent = (int)Math.round(Math.min(100, warmBrightnessLux * 100 / brightnessLux));
        final int brightnessSetting = convertLuxToBrightnessSetting(brightnessLux);

        return new BrightnessAndWarmth(new Brightness(brightnessSetting), new Warmth(warmthPercent));
    }


    private int convertLuxToLedOutput(double brightnessLux) {
        if (brightnessLux <= 0.05) return 0;
        final int minNonZeroResult = ledOutputRange.getLower();
        int ledOutput =  Math.max(minNonZeroResult, Math.min(ledOutputRange.getUpper(),
                (int) Math.round(34 * Math.log(17 * brightnessLux))));
        return ledOutput;
    }

    private double convertLedOutputToLux(int ledOutput) {
        if (ledOutput == 0) return 0;
        return Math.pow(Math.E, (double)ledOutput/34)/17;
    }

    private double convertBrightnessSettingToLux(int slider) {
        if (slider == 0)
            return 0;

        if (slider <= 5) {
            return Math.max(0, 0.0223609 * Math.pow(slider, 2) - 0.0406061 * slider + 0.0861964);
        }

        return Math.min(MAX_BRIGHTNESS_LUX, 0.5 * Math.pow(Math.E, (0.0535 * slider)));
    }

    private int convertLuxToBrightnessSetting(double lux) {
        if (lux == 0)
            return 0;

        if (lux <= 0.5) {
            return (int)Math.round(0.90797 + 0.214277 * Math.sqrt(Math.max(0, 974 * lux - 66)));
        }

        final double MAX_BRIGHTNESS_PERCENT = 100;
        return (int)Math.round(Math.max(1, Math.min(MAX_BRIGHTNESS_PERCENT, Math.round(18.6916 * Math.log(2 * lux)))));
    }
}


package com.onyx.darie.calin.gentleglowonyxboox.onyx.brightnessandtemperature;

import com.onyx.darie.calin.gentleglowonyxboox.light.Brightness;
import com.onyx.darie.calin.gentleglowonyxboox.light.BrightnessAndWarmth;
import com.onyx.darie.calin.gentleglowonyxboox.light.BrightnessAndWarmthToNativeOutputAdapter;
import com.onyx.darie.calin.gentleglowonyxboox.light.Warmth;
import com.onyx.darie.calin.gentleglowonyxboox.util.Range;

public class BrightnessAndWarmthToBrightnessAndTemperatureOutputAdapter implements BrightnessAndWarmthToNativeOutputAdapter<BrightnessAndTemperatureOutput> {
    private Range<Integer> brightnessRange;
    private Range<Integer> temperatureRange;

    @Override
    public BrightnessAndTemperatureOutput toNativeOutput(BrightnessAndWarmth brightnessAndWarmth) {
        return new BrightnessAndTemperatureOutput(
                Math.max(brightnessRange.getLower(), brightnessRange.getUpper() * brightnessAndWarmth.brightness.value / 100),
                temperatureRange.getUpper() * brightnessAndWarmth.warmth.value / 100
                );
    }
    @Override
    public BrightnessAndWarmth findBrightnessAndWarmthApproximationForNativeOutput(BrightnessAndTemperatureOutput output) {
        return new BrightnessAndWarmth(
                new Brightness(Math.max(1, 100 * output.brightness / brightnessRange.getUpper())),
                new Warmth(100 * output.temperature / temperatureRange.getUpper()));
    }

    public BrightnessAndWarmthToBrightnessAndTemperatureOutputAdapter(
            Range<Integer> brightnessRange,
            Range<Integer> temperatureRange
            ) {
        this.brightnessRange = brightnessRange;
        this.temperatureRange = temperatureRange;
    }


}


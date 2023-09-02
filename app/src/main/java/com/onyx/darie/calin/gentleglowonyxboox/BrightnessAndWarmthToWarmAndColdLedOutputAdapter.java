package com.onyx.darie.calin.gentleglowonyxboox;

public interface BrightnessAndWarmthToWarmAndColdLedOutputAdapter {
    WarmAndColdLedOutput toWarmAndColdLedOutput(BrightnessAndWarmth brightnessAndWarmth);

    BrightnessAndWarmth findBrightnessAndWarmthApproximationForWarmAndColdLedOutput(WarmAndColdLedOutput warmCold);
}

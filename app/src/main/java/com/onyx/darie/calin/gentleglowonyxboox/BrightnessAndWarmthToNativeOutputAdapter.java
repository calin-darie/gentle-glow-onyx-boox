package com.onyx.darie.calin.gentleglowonyxboox;

public interface BrightnessAndWarmthToNativeOutputAdapter<TNativeOutput> {
    TNativeOutput toNativeOutput(BrightnessAndWarmth brightnessAndWarmth);

    BrightnessAndWarmth findBrightnessAndWarmthApproximationForNativeOutput(TNativeOutput nativeOutput);
}

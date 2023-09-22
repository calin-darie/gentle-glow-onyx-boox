package com.onyx.darie.calin.gentleglowonyxboox.light;

public interface BrightnessAndWarmthToNativeOutputAdapter<TNativeOutput> {
    TNativeOutput toNativeOutput(BrightnessAndWarmth brightnessAndWarmth);

    BrightnessAndWarmth findBrightnessAndWarmthApproximationForNativeOutput(TNativeOutput nativeOutput);
}

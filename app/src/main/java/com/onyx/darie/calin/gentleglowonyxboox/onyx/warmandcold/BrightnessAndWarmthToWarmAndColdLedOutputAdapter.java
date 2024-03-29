package com.onyx.darie.calin.gentleglowonyxboox.onyx.warmandcold;

import com.onyx.darie.calin.gentleglowonyxboox.light.BrightnessAndWarmthToNativeOutputAdapter;
import com.onyx.darie.calin.gentleglowonyxboox.util.Range;
import com.onyx.darie.calin.gentleglowonyxboox.light.Brightness;
import com.onyx.darie.calin.gentleglowonyxboox.light.BrightnessAndWarmth;
import com.onyx.darie.calin.gentleglowonyxboox.light.Warmth;

public class BrightnessAndWarmthToWarmAndColdLedOutputAdapter implements BrightnessAndWarmthToNativeOutputAdapter<WarmAndColdLedOutput> {
    @Override
    public WarmAndColdLedOutput toNativeOutput(BrightnessAndWarmth brightnessAndWarmth) {
        final double desiredBrightnessLux = convertBrightnessSettingToLux(brightnessAndWarmth.brightness.value);

        final double desiredWarmBrightnessLux = desiredBrightnessLux * brightnessAndWarmth.warmth.value / 100;
        final int warmLedOutput = convertLuxToLedOutput(desiredWarmBrightnessLux);

        final double desiredColdBrightnessLux = desiredBrightnessLux - desiredWarmBrightnessLux;
        final int coldLedOutput = convertLuxToLedOutput(desiredColdBrightnessLux);

        WarmAndColdLedOutput proposedOutput = new WarmAndColdLedOutput(warmLedOutput, coldLedOutput);
        WarmAndColdLedOutput correctedOutput = correctWarmAndColdLedOutput(brightnessAndWarmth, desiredBrightnessLux, proposedOutput);

        return correctedOutput;
    }

    private WarmAndColdLedOutput correctWarmAndColdLedOutput(BrightnessAndWarmth brightnessAndWarmth, double desiredBrightnessLux, WarmAndColdLedOutput proposedOutput) {
        if (brightnessAndWarmth.brightness.value == 1) {
            return brightnessAndWarmth.warmth.value >= 50? new WarmAndColdLedOutput( ledOutputRange.getLower(), 0) : new WarmAndColdLedOutput(0, ledOutputRange.getLower());
        }

        double midwayToPreviousBrightnessIncrement = (desiredBrightnessLux + (brightnessAndWarmth.brightness.value == 1 ? 0 : convertBrightnessSettingToLux(brightnessAndWarmth.brightness.value - 1))) / 2;
        double midwayToNextBrightnessIncrement = brightnessAndWarmth.brightness.value == 100 ? MAX_BRIGHTNESS_LUX : (desiredBrightnessLux + convertBrightnessSettingToLux(brightnessAndWarmth.brightness.value + 1)) / 2;
        Range<Double> luxRange = new Range<>(midwayToPreviousBrightnessIncrement, midwayToNextBrightnessIncrement);

        double bestBrightnessDiffOutsideRange = Double.MAX_VALUE;
        WarmAndColdLedOutput bestCandidateOutsideRange = null;

        double bestWarmthDiff = Double.MAX_VALUE;
        double bestBrightnessDiff = Double.MAX_VALUE;
        WarmAndColdLedOutput bestCandidate = null;
        for (int deltaWarm = -1; deltaWarm <=1; deltaWarm ++)
        for (int deltaCold = -1; deltaCold <=1; deltaCold ++) {
            WarmAndColdLedOutput candidate = new WarmAndColdLedOutput(
                    constrainLedOutput(proposedOutput.warm + deltaWarm),
                    constrainLedOutput(proposedOutput.cold + deltaCold));

            Double brightnessLux = getBrightnessLux(candidate);
            double brightnessDiff = Math.abs(brightnessLux - desiredBrightnessLux);
            if (!luxRange.containsInclusive(brightnessLux)) {
                if (brightnessDiff < bestBrightnessDiffOutsideRange) {
                    bestBrightnessDiffOutsideRange = brightnessDiff;
                    bestCandidateOutsideRange = candidate;
                }
            }

            double warmthPercentDiff = Math.abs(getWarmPercent(candidate) - brightnessAndWarmth.warmth.value);
            if (warmthPercentDiff <= bestWarmthDiff) {
                if (warmthPercentDiff < bestWarmthDiff) {
                    bestBrightnessDiff = Double.MAX_VALUE;
                }
                if (brightnessDiff < bestBrightnessDiff) {
                    if (warmthPercentDiff < bestWarmthDiff) {
                        bestWarmthDiff = warmthPercentDiff;
                    }
                    bestBrightnessDiff = brightnessDiff;
                    bestCandidate = candidate;
                }
            }
        }

        return bestCandidate == null? bestCandidateOutsideRange : bestCandidate;
    }

    private double getWarmPercent(WarmAndColdLedOutput candidate) {
        double warmLux = convertLedOutputToLux(candidate.warm);
        double coldLux = convertLedOutputToLux(candidate.cold);
        return warmLux + coldLux == 0? 0 : 100 * warmLux / (warmLux + coldLux);
    }

    private double getBrightnessLux(WarmAndColdLedOutput candidate) {
        double warmLux = convertLedOutputToLux(candidate.warm);
        double coldLux = convertLedOutputToLux(candidate.cold);
        return warmLux + coldLux;
    }

    @Override
    public BrightnessAndWarmth findBrightnessAndWarmthApproximationForNativeOutput(WarmAndColdLedOutput warmCold) {
        if (brightnessAndWarmthByWarmAndCold == null)
            brightnessAndWarmthByWarmAndCold = getBrightnessAndWarmthByWarmAndCold();

        return brightnessAndWarmthByWarmAndCold[warmCold.warm][warmCold.cold];
    }

    private BrightnessAndWarmth[][] getBrightnessAndWarmthByWarmAndCold() {
        BrightnessAndWarmth[][] brightnessAndWarmthByWarmAndCold = new BrightnessAndWarmth[ledOutputRange.getUpper() + 1][ledOutputRange.getUpper() + 1];

        brightnessAndWarmthByWarmAndCold[0][0] = new BrightnessAndWarmth(new Brightness(0), new Warmth(50));

        for (int warm = 0; warm <= 255; warm++)
            for (int cold = 0; cold <= 255; cold++){
                if (brightnessAndWarmthByWarmAndCold[warm][cold] != null) {continue;}
                BrightnessAndWarmth firstApproximation = findFirstApproximationOfBrightnessAndWarmthForWarmAndColdLedOutput(new WarmAndColdLedOutput(warm, cold));
                brightnessAndWarmthByWarmAndCold[warm][cold] = firstApproximation.brightness.value == 0? firstApproximation.withDeltaBrightness(+1).value: firstApproximation;
            }

        for (int brightness = 100; brightness >= 1; brightness--)
            for (int warmth = 0; warmth <= 100; warmth++) {
                BrightnessAndWarmth brightnessAndWarmth = new BrightnessAndWarmth(new Brightness(brightness), new Warmth(warmth));
                WarmAndColdLedOutput output = toNativeOutput(brightnessAndWarmth);
                brightnessAndWarmthByWarmAndCold[output.warm][output.cold] = brightnessAndWarmth;
            }

        return brightnessAndWarmthByWarmAndCold;
    }

    public BrightnessAndWarmthToWarmAndColdLedOutputAdapter(Range<Integer> ledOutputRange) {
        this.ledOutputRange = ledOutputRange;
    }

    private final int MAX_BRIGHTNESS_LUX = 112;
    private Range<Integer> ledOutputRange;
    private BrightnessAndWarmth[][] brightnessAndWarmthByWarmAndCold;

    private int convertLuxToLedOutput(double brightnessLux) {
        if (brightnessLux <= 0.05) return 0;
        int unconstrainedLedOutput = (int) Math.round(34 * Math.log(17 * brightnessLux));
        return constrainLedOutput(unconstrainedLedOutput);
    }

    private int constrainLedOutput(int unconstrainedLedOutput) {
        if (unconstrainedLedOutput <= 0) return 0;
        final int minNonZeroResult = ledOutputRange.getLower();
        int ledOutput =  Math.max(minNonZeroResult, Math.min(ledOutputRange.getUpper(),
                unconstrainedLedOutput));
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

        if (lux <= convertBrightnessSettingToLux(5)) {
            return (int)Math.round(0.90797 + 0.214277 * Math.sqrt(Math.max(0, 974 * lux - 66)));
        }

        final double MAX_BRIGHTNESS_PERCENT = 100;
        return (int)Math.round(Math.max(5, Math.min(MAX_BRIGHTNESS_PERCENT, Math.round(18.6916 * Math.log(2 * lux)))));
    }

    private BrightnessAndWarmth findFirstApproximationOfBrightnessAndWarmthForWarmAndColdLedOutput(WarmAndColdLedOutput warmCold) {
        final double warmBrightnessLux = convertLedOutputToLux(warmCold.warm);
        final double coldBrightnessLux = convertLedOutputToLux(warmCold.cold);

        final double brightnessLux = warmBrightnessLux + coldBrightnessLux;

        if (brightnessLux == 0)
            return new BrightnessAndWarmth(new Brightness(0), new Warmth(50));

        final int warmthPercent = (int)Math.round(Math.min(100, warmBrightnessLux * 100 / brightnessLux));
        final int brightnessSetting = convertLuxToBrightnessSetting(brightnessLux);

        return new BrightnessAndWarmth(new Brightness(brightnessSetting), new Warmth(warmthPercent));
    }
}


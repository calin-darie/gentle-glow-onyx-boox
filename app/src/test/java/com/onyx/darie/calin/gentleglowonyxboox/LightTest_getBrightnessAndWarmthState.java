package com.onyx.darie.calin.gentleglowonyxboox;

import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

public class LightTest_getBrightnessAndWarmthState {
    @Test
    public void whenSettingBrightnessAndWarmthManuallyFromGentleGlow_isNotExternalChangeAndBrightnessWarmthEqualsLastSet() {
        BrightnessAndWarmth brightnessAndWarmth = new BrightnessAndWarmth(new Brightness(30), new Warmth(80));
        fixture.setBrightnessAndWarmth(brightnessAndWarmth);
        assertFalse(fixture.getBrightnessAndWarmthState().isExternalChange);
        assertEquals(brightnessAndWarmth, fixture.getBrightnessAndWarmthState().brightnessAndWarmth);
    }

    @Test
    public void whenSettingBrightnessAndWarmthFromOnyxSlider_isExternalChangeAndBrightnessWarmthDoesNotEqualLastSet() {
        BrightnessAndWarmth brightnessAndWarmth = new BrightnessAndWarmth(new Brightness(30), new Warmth(80));
        fixture.setBrightnessAndWarmth(brightnessAndWarmth);
        fixture.simulateOnyxSliderChange(new WarmAndColdLedOutput(255, 0));
        assertTrue(fixture.getBrightnessAndWarmthState().isExternalChange);
        assertNotEquals(brightnessAndWarmth, fixture.getBrightnessAndWarmthState().brightnessAndWarmth);
    }

    @Test
    public void whenExternalChange_approximatedBrightnessIncreasesAlongMainDiagonal() {
        fixture.setSavedBrightnessAndWarmth(new BrightnessAndWarmth(new Brightness(100), new Warmth(100)));
        int failCount = 0;
        for (int diagonal = 0; diagonal <= fixture.ledOutputRange.getUpper(); diagonal++) {
            failCount += countAndLogApproximatedBrightnessDecreasesAlongMainDiagonal(new WarmAndColdLedOutput(0, diagonal), +1, +1);
            failCount += countAndLogApproximatedBrightnessDecreasesAlongMainDiagonal(new WarmAndColdLedOutput(diagonal, 0), -1, +1);
        }
        if (failCount > 0) fail(failCount + " failures");
    }

    private int countAndLogApproximatedBrightnessDecreasesAlongMainDiagonal(WarmAndColdLedOutput initialOutput, int warmIncrement, int coldIncrement) {
        int failCount = 0;
        Brightness oldBrightness = null;
        for (
                WarmAndColdLedOutput ledOutput = initialOutput;
                ledOutput.cold <= fixture.ledOutputRange.getUpper();
                ledOutput = new WarmAndColdLedOutput(ledOutput.warm + warmIncrement, ledOutput.cold + coldIncrement)
        ) {
            if (0 < ledOutput.warm && ledOutput.warm < fixture.ledOutputRange.getLower() ||
                    0 < ledOutput.cold && ledOutput.cold < fixture.ledOutputRange.getLower())
                continue;

            fixture.simulateOnyxSliderChange(ledOutput);
            BrightnessAndWarmth approximatedBrightnessAndWarmth = fixture.getBrightnessAndWarmthState().brightnessAndWarmth;
            if (oldBrightness != null && approximatedBrightnessAndWarmth.brightness.value < oldBrightness.value) {
                failCount++;
                System.out.println(ledOutput + ": " + approximatedBrightnessAndWarmth.brightness + " < " + oldBrightness);
            }
            oldBrightness = approximatedBrightnessAndWarmth.brightness;
        }
        return failCount;
    }

    @Test
    public void whenExternalChange_approximatedWarmthIncreasesAlongSecondaryDiagonal() {
        fixture.setSavedBrightnessAndWarmth(new BrightnessAndWarmth(new Brightness(100), new Warmth(100)));
        int failCount = 0;
        for (int diagonal = 0; diagonal <= fixture.ledOutputRange.getUpper(); diagonal++) {
            failCount += countAndLogApproximatedWarmthDecreasesAlongSecondaryDiagonal(new WarmAndColdLedOutput(diagonal, 0), -1, -1);
            failCount += countAndLogApproximatedWarmthDecreasesAlongSecondaryDiagonal(new WarmAndColdLedOutput(fixture.ledOutputRange.getUpper(), diagonal), +1, -1);
        }
        if (failCount > 0) fail(failCount + " failures");
    }

    private int countAndLogApproximatedWarmthDecreasesAlongSecondaryDiagonal(WarmAndColdLedOutput initialOutput, int warmIncrement, int coldIncrement) {
        int failCount = 0;
        Warmth oldWarmth = null;
        for (
                WarmAndColdLedOutput ledOutput = initialOutput;
                0 <= ledOutput.cold && ledOutput.cold <= fixture.ledOutputRange.getUpper() &&
                0 <= ledOutput.warm && ledOutput.warm <= fixture.ledOutputRange.getUpper();
                ledOutput = new WarmAndColdLedOutput(ledOutput.warm + warmIncrement, ledOutput.cold + coldIncrement)
        ) {
            if (0 < ledOutput.warm && ledOutput.warm < fixture.ledOutputRange.getLower() ||
                    0 < ledOutput.cold && ledOutput.cold < fixture.ledOutputRange.getLower())
                continue;

            fixture.simulateOnyxSliderChange(ledOutput);
            BrightnessAndWarmth approximatedBrightnessAndWarmth = fixture.getBrightnessAndWarmthState().brightnessAndWarmth;
            if (oldWarmth != null && approximatedBrightnessAndWarmth.warmth.value < oldWarmth.value) {
                failCount++;
                System.out.println(ledOutput + ": " + approximatedBrightnessAndWarmth.warmth + " < " + oldWarmth);
            }
            oldWarmth = approximatedBrightnessAndWarmth.warmth;
        }
        return failCount;
    }

    @Test
    public void whenExternalChangeThenIncreaseBrightnessOrIncreaseWarmth_brightnessOrWarmthDoNotDecrease() {
        BrightnessAndWarmth brightnessAndWarmth = new BrightnessAndWarmth(new Brightness(100), new Warmth(100));
        fixture.setBrightnessAndWarmth(brightnessAndWarmth);
        int failCount = 0;
        for (int warm = fixture.ledOutputRange.getLower(); warm <= fixture.ledOutputRange.getUpper(); warm ++)
        for (int cold = fixture.ledOutputRange.getLower(); cold <= fixture.ledOutputRange.getUpper(); cold ++) {
            WarmAndColdLedOutput output = new WarmAndColdLedOutput(warm, cold);
            fixture.simulateOnyxSliderChange(output);

            Result<BrightnessAndWarmth> brightnessIncrease = brightnessAndWarmth.withDeltaBrightness(+1);
            if (!brightnessIncrease.hasError()) {
                WarmAndColdLedOutput higherBrightnessOutput = fixture.setBrightnessAndWarmth(brightnessIncrease.value);
                if(fixture.getTotalLux(higherBrightnessOutput) < fixture.getTotalLux(output)) {
                    failCount++;
                    System.out.println(output + " brightness");
                }
            }
            Result<BrightnessAndWarmth> warmthIncrease = brightnessAndWarmth.withDeltaWarmth(+1);
            if (!warmthIncrease.hasError()) {
                WarmAndColdLedOutput higherWarmthOutput = fixture.setBrightnessAndWarmth(warmthIncrease.value);
                if (fixture.getWarmthPercentLux(higherWarmthOutput) < fixture.getWarmthPercentLux(output))
                failCount++;
                System.out.println(output + " warmth");
            }
        }

        if(failCount > 0) {
            fail(failCount + " failures.");
        }
    }

    private LightTestFixture fixture;


    @Before
    public void beforeEach() {
        fixture = new LightTestFixture();
    }
}

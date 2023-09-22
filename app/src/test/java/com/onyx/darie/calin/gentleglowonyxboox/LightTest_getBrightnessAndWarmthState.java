package com.onyx.darie.calin.gentleglowonyxboox;

import com.onyx.darie.calin.gentleglowonyxboox.light.Brightness;
import com.onyx.darie.calin.gentleglowonyxboox.light.BrightnessAndWarmth;
import com.onyx.darie.calin.gentleglowonyxboox.light.Warmth;
import com.onyx.darie.calin.gentleglowonyxboox.onyx.warmandcold.WarmAndColdLedOutput;

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
    public void whenExternalChange_anyBrightnessAndWarmthIsApproximatedBackToItself() {
        for (int brightness = 1; brightness <= 100; brightness ++)
        for (int warmth = 0; warmth <= 100; warmth ++) {
            BrightnessAndWarmth brightnessAndWarmth = new BrightnessAndWarmth(new Brightness(brightness), new Warmth(warmth));
            WarmAndColdLedOutput output = fixture.setBrightnessAndWarmth(brightnessAndWarmth);
            WarmAndColdLedOutput differentOutput = output.cold == 100? new WarmAndColdLedOutput(100, 0) : new WarmAndColdLedOutput(100, 100);
            fixture.simulateOnyxSliderChange(differentOutput);

            fixture.simulateOnyxSliderChange(output);

            assertEquals(brightnessAndWarmth, fixture.getBrightnessAndWarmthState().brightnessAndWarmth);
        }
    }

    private LightTestFixture fixture;


    @Before
    public void beforeEach() {
        fixture = new LightTestFixture();
    }
}

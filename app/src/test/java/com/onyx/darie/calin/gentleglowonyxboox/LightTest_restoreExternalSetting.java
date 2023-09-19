package com.onyx.darie.calin.gentleglowonyxboox;

import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class LightTest_restoreExternalSetting {
    @Test
    public void whenLightExternallyChanged_savesLedOutput() {
        fixture.setBrightnessAndWarmth(new BrightnessAndWarmth(
                new Brightness(30), new Warmth(80)));
        WarmAndColdLedOutput ledOutput = new WarmAndColdLedOutput(255, 0);

        fixture.simulateOnyxSliderChange(ledOutput);

        fixture.setBrightnessAndWarmth(new BrightnessAndWarmth(
                new Brightness(30), new Warmth(80)));
        fixture.restoreExternallySetLedOutput();

        WarmAndColdLedOutput restoredOutput = fixture.captureChangedLedOutput();
        assertEquals(ledOutput, restoredOutput);
    }

    @Test
    public void whenExternallySetLedOutputRestored_brightnessAndWarmthUpdated() {
        fixture.setBrightnessAndWarmth(new BrightnessAndWarmth(
                new Brightness(30), new Warmth(80)));
        WarmAndColdLedOutput ledOutput = new WarmAndColdLedOutput(255, 0);

        fixture.simulateOnyxSliderChange(ledOutput);

        fixture.setBrightnessAndWarmth(new BrightnessAndWarmth(
                new Brightness(30), new Warmth(80)));
        fixture.restoreExternallySetLedOutput();

        assertEquals(
                new BrightnessAndWarmthState(true, new BrightnessAndWarmth(
                        new Brightness(100), new Warmth(100))),
                fixture.getBrightnessAndWarmthState());
    }

    @Test
    public void afterExternallySetLedOutputRestored_brightnessAndWarmthCanBeRevertedToPreviousValue() {
        fixture.setBrightnessAndWarmth(new BrightnessAndWarmth(
                new Brightness(30), new Warmth(80)));
        WarmAndColdLedOutput ledOutput = new WarmAndColdLedOutput(255, 0);

        fixture.simulateOnyxSliderChange(ledOutput);

        fixture.setBrightnessAndWarmth(new BrightnessAndWarmth(
                new Brightness(30), new Warmth(80)));
        fixture.restoreExternallySetLedOutput();

        fixture.setBrightnessAndWarmth(new BrightnessAndWarmth(
                new Brightness(30), new Warmth(80)));

        assertEquals(
                new BrightnessAndWarmthState(false, new BrightnessAndWarmth(
                        new Brightness(30), new Warmth(80))),
                fixture.getBrightnessAndWarmthState());
    }


    @Test
    public void whenOnyxSliderUsed_setOutputNotCalled() {
        fixture.setBrightnessAndWarmth(new BrightnessAndWarmth(
                new Brightness(30), new Warmth(80)));
        WarmAndColdLedOutput ledOutput = new WarmAndColdLedOutput(255, 0);

        fixture.resetLedOutputMocks();
        fixture.simulateOnyxSliderChange(ledOutput);
        fixture.assertNoChange();
    }

    @Before
    public void beforeEach() {
        fixture = new LightTestFixture();
    }

    private LightTestFixture fixture;
}

package com.onyx.darie.calin.gentleglowonyxboox;

import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;

public class LightTest_setBrightnessAndWarmth_backpressure {
    @Test
    public void secondAndThirdCallBeforeFirstOneCompletes_secondAndThirdCallsDoNotCompleteBeforeFirst() {
        fixture.setBrightnessAndWarmthAndAssertNoChange(new BrightnessAndWarmth(new Brightness(50), new Warmth(100)));
        fixture.setBrightnessAndWarmthAndAssertNoChange(new BrightnessAndWarmth(new Brightness(100), new Warmth(100)));
    }

    @Test
    public void secondAndThirdCallBeforeFirstOneCompletes_whenFirstCallCompletes_secondCallThrottledOutAndThirdCallAffectsLedOutput() {
        fixture.setBrightnessAndWarmthAndAssertNoChange(new BrightnessAndWarmth(new Brightness(50), new Warmth(100)));
        fixture.setBrightnessAndWarmthAndAssertNoChange(new BrightnessAndWarmth(new Brightness(100), new Warmth(100)));
        ledOutput3 = fixture.completeAndCaptureNewLedOutput(ledOutput1);
        assertNotEquals(ledOutput3.cold, ledOutput3.warm);
    }

    @Test
    public void secondAndThirdCallBeforeFirstOneCompletes_whenFirstCallCompletes_externalChangeNotReported() {
        fixture.setBrightnessAndWarmthAndAssertNoChange(new BrightnessAndWarmth(new Brightness(50), new Warmth(100)));
        fixture.setBrightnessAndWarmthAndAssertNoChange(new BrightnessAndWarmth(new Brightness(100), new Warmth(100)));
        ledOutput3 = fixture.completeAndCaptureNewLedOutput(ledOutput1);
        assertFalse(fixture.getBrightnessAndWarmthState().isExternalChange);
    }

    private LightTestFixture fixture;

    private WarmAndColdLedOutput ledOutput1;
    private WarmAndColdLedOutput ledOutput3;

    @Before
    public void beforeEach() {
        fixture = new LightTestFixture();

        ledOutput1 = fixture.captureWarmAndColdLedOutputWithoutCompleting(new BrightnessAndWarmth(new Brightness(1), new Warmth(100)));
    }
}

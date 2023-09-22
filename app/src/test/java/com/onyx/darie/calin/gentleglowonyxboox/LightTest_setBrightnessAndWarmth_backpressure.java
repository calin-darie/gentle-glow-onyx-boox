package com.onyx.darie.calin.gentleglowonyxboox;

import com.onyx.darie.calin.gentleglowonyxboox.light.Brightness;
import com.onyx.darie.calin.gentleglowonyxboox.light.BrightnessAndWarmth;
import com.onyx.darie.calin.gentleglowonyxboox.light.Warmth;
import com.onyx.darie.calin.gentleglowonyxboox.onyx.warmandcold.WarmAndColdLedOutput;

import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

public class LightTest_setBrightnessAndWarmth_backpressure {
    @Test
    public void secondAndThirdCallBeforeFirstOneCompletes_secondAndThirdCallsDoNotCompleteBeforeFirst() {
        fixture.setBrightnessAndWarmthAndAssertNoChange(new BrightnessAndWarmth(new Brightness(50), new Warmth(100)));
        fixture.setBrightnessAndWarmthAndAssertNoChange(new BrightnessAndWarmth(new Brightness(100), new Warmth(100)));
    }

    @Test
    public void secondAndThirdCallBeforeFirstOneCompletes_whenFirstCallCompletes_secondCallThrottledOutAndThirdCallAffectsLedOutput() {
        fixture.setBrightnessAndWarmthAndAssertNoChange(new BrightnessAndWarmth(new Brightness(50), new Warmth(50)));
        fixture.setBrightnessAndWarmthAndAssertNoChange(new BrightnessAndWarmth(new Brightness(100), new Warmth(100)));
        fixture.complete(ledOutput1);
        ledOutput3 = fixture.captureChangedLedOutput();
        assertEquals(new WarmAndColdLedOutput(fixture.ledOutputRange.getUpper(), 0), ledOutput3);
    }

    @Test
    public void secondAndThirdCallBeforeFirstOneCompletes_whenFirstCallCompletes_externalChangeNotReported() {
        fixture.setBrightnessAndWarmthAndAssertNoChange(new BrightnessAndWarmth(new Brightness(50), new Warmth(100)));
        fixture.setBrightnessAndWarmthAndAssertNoChange(new BrightnessAndWarmth(new Brightness(100), new Warmth(100)));
        fixture.complete(ledOutput1);
        //ledOutput3 = fixture.captureChangedLedOutput();
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

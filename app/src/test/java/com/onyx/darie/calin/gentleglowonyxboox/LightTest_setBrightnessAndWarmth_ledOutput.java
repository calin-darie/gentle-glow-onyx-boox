package com.onyx.darie.calin.gentleglowonyxboox;

import org.junit.Before;
import org.junit.Test;
import org.junit.experimental.theories.DataPoints;
import org.junit.experimental.theories.FromDataPoints;
import org.junit.experimental.theories.Theories;
import org.junit.experimental.theories.Theory;
import org.junit.runner.RunWith;

import java.util.stream.IntStream;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

// todo https://github.com/junit-team/junit4/wiki/Continuous-testing
@RunWith(Theories.class)
public class LightTest_setBrightnessAndWarmth_ledOutput {
    @DataPoints("canLinkWarmAndColdInNonEqualProportion_warmths")
    public static int[] canLinkWarmAndColdInNonEqualProportion_warmths() {
        return IntStream.rangeClosed(10, 90).toArray();
    }
    @DataPoints("canLinkWarmAndColdInNonEqualProportion_brightnesses")
    public static int[] canLinkWarmAndColdInNonEqualProportion_brightnesses() {
        return IntStream.rangeClosed(6, 94).toArray();
    }

    @Theory
    public void canLinkWarmAndColdInNonEqualProportion(
            @FromDataPoints("canLinkWarmAndColdInNonEqualProportion_warmths") int warmthPercent,
            @FromDataPoints("canLinkWarmAndColdInNonEqualProportion_brightnesses") int brightness
    ) {
        WarmAndColdLedOutput ledOutput = fixture.setBrightnessAndWarmth(new BrightnessAndWarmth(new Brightness(brightness), new Warmth(warmthPercent)));

        double warmPercentLuxScale = fixture.getWarmthPercentLux(ledOutput);
        assertEquals(warmthPercent, warmPercentLuxScale, 0.5);
    }

    @Test
    public void anyWarmth_setsIncreasingBrightness() {
        for (int warmthPercent = 0; warmthPercent <= 100; warmthPercent ++) {
            double oldTotalLuxScale = 0;
            for (int brightness = 1; brightness <= 100; brightness++) {
                double newTotalLuxScale = setAndCaptureTotalLuxScale(brightness, warmthPercent);
                if (newTotalLuxScale < oldTotalLuxScale) {
                    fail("warmth= " + warmthPercent + ", brightness = " + brightness + "; brighntess dropped from " + oldTotalLuxScale + " to " + newTotalLuxScale);
                }
                oldTotalLuxScale = newTotalLuxScale;
            }
        }
    }

    @Test
    public void anyBrightness_setsIncreasingWarmth() {
        for (int brightness = 1; brightness <= 100; brightness++) {
            double oldWarmthPercent = 0;
            for (int warmthPercent = 0; warmthPercent <= 100; warmthPercent ++) {

                WarmAndColdLedOutput ledOutput = fixture.setBrightnessAndWarmth(new BrightnessAndWarmth(new Brightness(brightness), new Warmth(warmthPercent)));

                double warmthPercentLuxScale = fixture.getWarmthPercentLux(ledOutput);
                if (warmthPercentLuxScale < oldWarmthPercent - 0.01) {
                    fail("brightness = " + brightness + ", warmth= " + warmthPercent + "; warmth dropped from " + oldWarmthPercent + " to " + warmthPercentLuxScale);
                }
                oldWarmthPercent = warmthPercentLuxScale;
            }
        }
    }

    @Test
    public void setToColdestBrightness1_setsLedOutputToMinForWarmAndZeroForCold() {
        for(int warmth = 0; warmth <= 49; warmth++) {
            WarmAndColdLedOutput ledOutput = fixture.setBrightnessAndWarmth(new BrightnessAndWarmth(new Brightness(1), new Warmth(warmth)));
            assertEquals(new WarmAndColdLedOutput(0, fixture.ledOutputRange.getLower()), ledOutput);
        }
    }

    @Test
    public void setToWarmestBrightness1_setsLedOutputToMinForWarmAndZeroForCold() {
        for(int warmth = 51; warmth <= 100; warmth++) {
            WarmAndColdLedOutput ledOutput = fixture.setBrightnessAndWarmth(new BrightnessAndWarmth(new Brightness(1), new Warmth(warmth)));
            assertEquals(new WarmAndColdLedOutput(fixture.ledOutputRange.getLower(), 0), ledOutput);
        }
    }

    @Test
    public void setToWarmestAndBrightest_setsLedOutputToMaxWarmAndZeroCold() {
        WarmAndColdLedOutput ledOutput = fixture.setBrightnessAndWarmth(new BrightnessAndWarmth(new Brightness(100), new Warmth(100)));
        assertEquals(new WarmAndColdLedOutput(fixture.ledOutputRange.getUpper(), 0), ledOutput);
    }

    @Test
    public void setToColdestAndBrightest_setsLedOutputToZeroWarmAndMaxCold() {
        WarmAndColdLedOutput ledOutput = fixture.setBrightnessAndWarmth(new BrightnessAndWarmth(new Brightness(100), new Warmth(0)));
        assertEquals(new WarmAndColdLedOutput(0, fixture.ledOutputRange.getUpper()), ledOutput);
    }

    @Test
    public void whenSettingSameWarmColdOutput_isNotBlocked() {
        fixture.setBrightnessAndWarmth(new BrightnessAndWarmth(new Brightness(1), new Warmth(99)));
        fixture.setBrightnessAndWarmthAndAssertNoChange(new BrightnessAndWarmth(new Brightness(1), new Warmth(100)));

        fixture.captureWarmAndColdLedOutputWithoutCompleting(new BrightnessAndWarmth(new Brightness(100), new Warmth(99)));
    }

    private double setAndCaptureTotalLuxScale(int brightness, int warmthPercent) {
        WarmAndColdLedOutput ledOutput = fixture.setBrightnessAndWarmth(new BrightnessAndWarmth(new Brightness(brightness), new Warmth(warmthPercent)));

        double totalLuxScale = fixture.getTotalLux(ledOutput);
        return totalLuxScale;
    }

    LightTestFixture fixture;

    @Before
    public void beforeEach() {
        fixture = new LightTestFixture();
    }
}


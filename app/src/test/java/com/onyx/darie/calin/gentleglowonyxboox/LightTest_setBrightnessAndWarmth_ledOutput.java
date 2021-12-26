package com.onyx.darie.calin.gentleglowonyxboox;

import org.junit.Before;
import org.junit.Test;
import org.junit.experimental.theories.DataPoints;
import org.junit.experimental.theories.FromDataPoints;
import org.junit.experimental.theories.Theories;
import org.junit.experimental.theories.Theory;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import java.util.stream.IntStream;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

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
        ArgumentCaptor<WarmAndColdLedOutput> ledOutputCaptor = ArgumentCaptor.forClass(WarmAndColdLedOutput.class);

        light.setBrightnessAndWarmth(new BrightnessAndWarmth(new Brightness(brightness), new Warmth(warmthPercent)));

        verify(nativeLight).setLedOutput(ledOutputCaptor.capture());
        WarmAndColdLedOutput ledOutput = ledOutputCaptor.getValue();
        double warmPercentLuxScale = getWarmPercentLuxScale(ledOutput);
        assertEquals(warmthPercent, warmPercentLuxScale, 0.5);
    }

    private double getWarmPercentLuxScale(WarmAndColdLedOutput ledOutput) {
        final double warmLuxScale = toLuxBrightnessScale(ledOutput.warm);
        final double coldLuxScale = toLuxBrightnessScale(ledOutput.cold);
        double warmPercentLuxScale = 100 * warmLuxScale / (warmLuxScale + coldLuxScale);
        return warmPercentLuxScale;
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

                Mockito.reset(nativeLight);
                ArgumentCaptor<WarmAndColdLedOutput> ledOutputCaptor = ArgumentCaptor.forClass(WarmAndColdLedOutput.class);
                light.setBrightnessAndWarmth(new BrightnessAndWarmth(new Brightness(brightness), new Warmth(warmthPercent)));
                verify(nativeLight).setLedOutput(ledOutputCaptor.capture());

                double warmthPercentLuxScale = getWarmPercentLuxScale(ledOutputCaptor.getValue());
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
            Mockito.reset(nativeLight);
            light.setBrightnessAndWarmth(new BrightnessAndWarmth(new Brightness(1), new Warmth(warmth)));
            try {
                verify(nativeLight).setLedOutput(new WarmAndColdLedOutput(
                        0, ledOutputRange.getLower()));
            } catch (Error e) {
                System.out.println("failed for warmth = " + warmth);
                throw e;
            }
        }
    }

    @Test
    public void setToWarmestBrightness1_setsLedOutputToMinForWarmAndZeroForCold() {
        for(int warmth = 51; warmth <= 100; warmth++) {
            Mockito.reset(nativeLight);
            light.setBrightnessAndWarmth(new BrightnessAndWarmth(new Brightness(1), new Warmth(warmth)));
            try {
                verify(nativeLight).setLedOutput(new WarmAndColdLedOutput(
                        ledOutputRange.getLower(), 0));
            } catch (Error e) {
                System.out.println("failed for warmth = " + warmth);
                throw e;
            }
        }
    }

    @Test
    public void setToWarmestAndBrightest_setsLedOutputToMaxWarmAndZeroCold() {
        light.setBrightnessAndWarmth(new BrightnessAndWarmth(new Brightness(100), new Warmth(100)));

        verify(nativeLight).setLedOutput(new WarmAndColdLedOutput(
                ledOutputRange.getUpper(), 0));
    }

    @Test
    public void setToColdestAndBrightest_setsLedOutputToZeroWarmAndMaxCold() {
        light.setBrightnessAndWarmth(new BrightnessAndWarmth(new Brightness(100), new Warmth(0)));

        verify(nativeLight).setLedOutput(new WarmAndColdLedOutput(
                0, ledOutputRange.getUpper()));
    }

    private double setAndCaptureTotalLuxScale(int brightness, int warmthPercent) {
        Mockito.reset(nativeLight);
        ArgumentCaptor<WarmAndColdLedOutput> ledOutputCaptor = ArgumentCaptor.forClass(WarmAndColdLedOutput.class);

        light.setBrightnessAndWarmth(new BrightnessAndWarmth(new Brightness(brightness), new Warmth(warmthPercent)));

        verify(nativeLight).setLedOutput(ledOutputCaptor.capture());
        final double warmLuxScale = toLuxBrightnessScale(ledOutputCaptor.getValue().warm);
        final double coldLuxScale = toLuxBrightnessScale(ledOutputCaptor.getValue().cold);
        final double totalLuxScale = warmLuxScale + coldLuxScale;
        return totalLuxScale;
    }

    private double toLuxBrightnessScale(int ledOutput) {
        if (ledOutput == 0) return 0;
        return Math.pow(Math.E, (double)ledOutput/34)/17;
    }

    @Mock
    private NativeWarmColdLightController nativeLight;

    private Light light;

    Range<Integer> ledOutputRange = new Range<>(5, 255);

    @Before
    public void beforeEach() {
        MockitoAnnotations.openMocks(this);
        OnyxBrightnessAndWarmthToWarmAndColdLedOutputAdapter adapter =
                new OnyxBrightnessAndWarmthToWarmAndColdLedOutputAdapter(ledOutputRange);
        light = new Light(nativeLight, adapter);
    }
}


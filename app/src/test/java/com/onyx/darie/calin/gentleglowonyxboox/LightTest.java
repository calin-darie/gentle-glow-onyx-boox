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

import java.util.HashMap;
import java.util.Map;
import java.util.stream.IntStream;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

// todo https://github.com/junit-team/junit4/wiki/Continuous-testing
@RunWith(Theories.class)
public class LightTest {
    @DataPoints("setBrightnessAndWarmth_canLinkWarmAndColdInNonEqualProportion_warmths")
    public static int[] setBrightnessAndWarmth_canLinkWarmAndColdInNonEqualProportion_warmths() {
        return IntStream.rangeClosed(10, 90).toArray();
    }
    @DataPoints("setBrightnessAndWarmth_canLinkWarmAndColdInNonEqualProportion_brightnesses")
    public static int[] setBrightnessAndWarmth_canLinkWarmAndColdInNonEqualProportion_brightnesses() {
        return IntStream.rangeClosed(6, 94).toArray();
    }

    @Theory
    public void setBrightnessAndWarmth_canLinkWarmAndColdInNonEqualProportion(
            @FromDataPoints("setBrightnessAndWarmth_canLinkWarmAndColdInNonEqualProportion_warmths") int warmthPercent,
            @FromDataPoints("setBrightnessAndWarmth_canLinkWarmAndColdInNonEqualProportion_brightnesses") int brightness
    ) {
        ArgumentCaptor<WarmAndColdLedOutput> ledOutputCaptor = ArgumentCaptor.forClass(WarmAndColdLedOutput.class);

        light.setBrightnessAndWarmth(new BrightnessAndWarmth(new Brightness(brightness), new Warmth(warmthPercent)));

        verify(nativeLight).setLedOutput(ledOutputCaptor.capture());
        final double warmLuxScale = toLuxBrightnessScale(ledOutputCaptor.getValue().warm);
        final double coldLuxScale = toLuxBrightnessScale(ledOutputCaptor.getValue().cold);
        double warmPercentLuxScale = 100 * warmLuxScale / (warmLuxScale + coldLuxScale);
        if (Math.abs(warmPercentLuxScale - warmthPercent) > 0.49) {
            System.out.println("warmth = " + warmthPercent + ", brightness = " + brightness);
            System.out.println("warm percent in lux scale = " + warmPercentLuxScale);
        }
        assertEquals(warmthPercent, warmPercentLuxScale, 0.7);
    }

    @DataPoints("setBrightnessAndWarmth_whenChangingWarmth_keepsBrightnessConstant_brightnesses")
    public static int[] setBrightnessAndWarmth_whenChangingWarmth_keepsBrightnessConstant_brightnesses() {
        return IntStream.rangeClosed(7, 93).toArray();
    }
    @DataPoints("setBrightnessAndWarmth_whenChangingWarmth_keepsBrightnessConstant_warmths")
    public static int[] setBrightnessAndWarmth_whenChangingWarmth_keepsBrightnessConstant_warmths() {
        return IntStream.rangeClosed(7, 93).toArray();
    }

    static Map<Integer, Range<Double>> luxScaleRangeByBrightness = new HashMap<>();
    @Theory
    public void setBrightnessAndWarmth_whenChangingWarmth_keepsBrightnessConstant(
            @FromDataPoints("setBrightnessAndWarmth_whenChangingWarmth_keepsBrightnessConstant_brightnesses") int brightness,
            @FromDataPoints("setBrightnessAndWarmth_whenChangingWarmth_keepsBrightnessConstant_warmths") int warmthPercent
    ) {
        final double totalLuxScale = getTotalLuxScale(brightness, warmthPercent);

        if (luxScaleRangeByBrightness.containsKey(brightness)) {
            Range<Double> range = luxScaleRangeByBrightness.get(brightness);
            Range<Double> newRange = getNewRange(totalLuxScale, range);
            boolean isRangeWithinSevenPercentOfLower = (newRange.getUpper() - newRange.getLower()) / newRange.getLower() < 0.07;
            if (!isRangeWithinSevenPercentOfLower) {
                fail("brightness = " + brightness + ", warmth = " + warmthPercent + "% : " + newRange);
            }
        } else {
            luxScaleRangeByBrightness.put(brightness, new Range(totalLuxScale, totalLuxScale));
        }
    }

    private Range<Double> getNewRange(double totalLuxScale, Range<Double> range) {
        Range<Double> newRange;
        if (totalLuxScale < range.getLower()) {
            newRange = new Range(totalLuxScale, range.getUpper());
        } else if (totalLuxScale > range.getUpper()) {
            newRange = new Range(range.getLower(), totalLuxScale);
        } else {
            newRange = range;
        }
        return newRange;
    }

    @Test
    public void setBrightnessAndWarmth_lowWarmth_setsIncreasingBrightness() {
        for (int warmthPercent = 0; warmthPercent <= 10; warmthPercent ++) {
            double oldTotalLuxScale = 0;
            for (int brightness = 1; brightness <= 100; brightness++) {
                double newTotalLuxScale = getTotalLuxScale(brightness, warmthPercent);
                assertTrue(newTotalLuxScale > oldTotalLuxScale);
            }
        }
    }

    @Test
    public void setBrightnessAndWarmth_highWarmth_setsIncreasingBrightness() {
        for (int warmthPercent = 90; warmthPercent <= 100; warmthPercent ++) {
            double oldTotalLuxScale = 0;
            for (int brightness = 1; brightness <= 100; brightness++) {
                double newTotalLuxScale = getTotalLuxScale(brightness, warmthPercent);
                assertTrue(newTotalLuxScale > oldTotalLuxScale);
                oldTotalLuxScale = newTotalLuxScale;
            }
        }
    }

    private double getTotalLuxScale(int brightness, int warmthPercent) {
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
        return Math.pow(Math.E, (double)ledOutput/34);
    }

    @Test
    public void setBrightnessAndWarmth_increaseWarmth_setsIncreasingWarmOutputAndDecreasingColdOutput() {
        for (int brightness = 1; brightness <= 100; brightness++) {
            WarmAndColdLedOutput oldLedOutput = new WarmAndColdLedOutput(0, ledOutputRange.getUpper());
            for (int warmthPercent = 0; warmthPercent <= 100; warmthPercent ++) {
                Mockito.reset(nativeLight);
                ArgumentCaptor<WarmAndColdLedOutput> ledOutputCaptor = ArgumentCaptor.forClass(WarmAndColdLedOutput.class);

                light.setBrightnessAndWarmth(new BrightnessAndWarmth(new Brightness(brightness), new Warmth(warmthPercent)));

                verify(nativeLight).setLedOutput(ledOutputCaptor.capture());
                assertTrue(ledOutputCaptor.getValue().warm >= oldLedOutput.warm);
                assertTrue(ledOutputCaptor.getValue().cold <= oldLedOutput.cold);
                oldLedOutput = ledOutputCaptor.getValue();
            }
        }
    }

    @Test
    public void setBrightnessAndWarmth_toColdestBrightness1_setsLedOutputToMinForWarmAndZeroForCold() {
        for(int warmth = 0; warmth <= 26; warmth++) { // todo 50
            Mockito.reset(nativeLight);
            light.setBrightnessAndWarmth(new BrightnessAndWarmth(new Brightness(1), new Warmth(warmth)));
            verify(nativeLight).setLedOutput(new WarmAndColdLedOutput(
                    0, ledOutputRange.getLower()));
        }
    }

    @Test
    public void setBrightnessAndWarmth_toWarmestBrightness1_setsLedOutputToMinForWarmAndZeroForCold() {
        for(int warmth = 74; warmth <= 100; warmth++) { // todo 50
            Mockito.reset(nativeLight);
            light.setBrightnessAndWarmth(new BrightnessAndWarmth(new Brightness(1), new Warmth(warmth)));
            verify(nativeLight).setLedOutput(new WarmAndColdLedOutput(
                    ledOutputRange.getLower(),0));
        }
    }

    @Test
    public void setBrightnessAndWarmth_toWarmestAndBrightest_setsLedOutputToMaxWarmAndZeroCold() {
        light.setBrightnessAndWarmth(new BrightnessAndWarmth(new Brightness(100), new Warmth(100)));

        verify(nativeLight).setLedOutput(new WarmAndColdLedOutput(
                ledOutputRange.getUpper(), 0));
    }

    @Test
    public void setBrightnessAndWarmth_toColdestAndBrightest_setsLedOutputToZeroWarmAndMaxCold() {
        light.setBrightnessAndWarmth(new BrightnessAndWarmth(new Brightness(100), new Warmth(0)));

        verify(nativeLight).setLedOutput(new WarmAndColdLedOutput(
                0, ledOutputRange.getUpper()));
    }

    @Mock
    private NativeWarmColdLightController nativeLight;

    private OnyxBrightnessAndWarmthLightController brightnessAndWarmthLightController;

    private Light light;

    Range<Integer> ledOutputRange = new Range<>(5, 255);; // todo isolate onyx specific

    @Before
    public void beforeEach() {
        MockitoAnnotations.openMocks(this);
        when(nativeLight.getWarmAndColdLedOutputRange())
                .thenReturn(ledOutputRange);
        brightnessAndWarmthLightController = new OnyxBrightnessAndWarmthLightController(nativeLight);
        light = new Light(brightnessAndWarmthLightController);
    }
}
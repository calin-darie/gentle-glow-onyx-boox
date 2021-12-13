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
import org.mockito.MockitoAnnotations;

import java.util.stream.IntStream;

import static org.junit.Assert.*;
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
        ArgumentCaptor<Integer> warmLedOutputCaptor = ArgumentCaptor.forClass(Integer.class);
        ArgumentCaptor<Integer> coldLedOutputCaptor = ArgumentCaptor.forClass(Integer.class);

        light.setBrightnessAndWarmth(new BrightnessAndWarmth(new Brightness(brightness), new Warmth(warmthPercent)));

        verify(warmLeds).setOutput(warmLedOutputCaptor.capture());
        verify(coldLeds).setOutput(coldLedOutputCaptor.capture());
        final double warmLuxScale = toLuxBrightnessScale(warmLedOutputCaptor.getValue());
        final double coldLuxScale = toLuxBrightnessScale(coldLedOutputCaptor.getValue());
        double warmPercentLuxScale = 100 * warmLuxScale / (warmLuxScale + coldLuxScale);
        if (Math.abs(warmPercentLuxScale - warmthPercent) > 0.49) {
            System.out.println("warmth = " + warmthPercent + ", brightness = " + brightness);
            System.out.println("warm percent in lux scale = " + warmPercentLuxScale);
        }
        assertEquals(warmthPercent, warmPercentLuxScale, 0.7);
    }

    // theory about brightness being constant when varying warmth

    // towards end of range (1 - 5, 95 - 99):
    // if warmth fixed, theory: lux brightness / led output monotonous and bound between range.low and output(6)
    // if brightness fixed:
    //   for brightness 1: if warmth 0-50: led output (0, min); if warmth 51 - 100: led output (min, 0)
    //   theory: warmth 0-50 => warm output monotonous increasing, cold output monotonous decreasing; warmth 51-100 symmetrical


    private double toLuxBrightnessScale(int ledOutput) {
        if (ledOutput == 0) return 0;
        return Math.pow(Math.E, (double)ledOutput/34);
    }

    @Test
    public void setBrightnessAndWarmth_toColdestAndBrightest_setsLedOutputToMaxForWarmAndZeroForCold() {
        light.setBrightnessAndWarmth(new BrightnessAndWarmth(new Brightness(100), new Warmth(100)));

        verify(warmLeds).setOutput(ledOutputRange.getUpper());
        verify(coldLeds).setOutput(0);
    }

    @Test
    public void setBrightnessAndWarmth_toWarmestAndBrightest_setsLedOutputToMaxForColdAndZeroForWarm() {
        light.setBrightnessAndWarmth(new BrightnessAndWarmth(new Brightness(100), new Warmth(0)));

        verify(warmLeds).setOutput(0);
        verify(coldLeds).setOutput(ledOutputRange.getUpper());
    }

    @Test
    public void setBrightnessAndWarmth_toWarmestBrightness1_setsLedOutputToMinForWarmAndZeroForCold() {
        light.setBrightnessAndWarmth(new BrightnessAndWarmth(new Brightness(1), new Warmth(100)));

        verify(warmLeds).setOutput(ledOutputRange.getLower());
        verify(coldLeds).setOutput(0);
    }

    @Test
    public void setBrightnessAndWarmth_canSetMinVoltageForCold() {
        light.setBrightnessAndWarmth(new BrightnessAndWarmth(new Brightness(100), new Warmth(0)));

        verify(warmLeds).setOutput(0);
        verify(coldLeds).setOutput(ledOutputRange.getUpper());
    }

    @Mock
    private LedGroup warmLeds;
    @Mock
    private LedGroup coldLeds;
    @Mock
    private NativeWarmColdLightController nativeWarmColdLightController;

    private OnyxBrightnessAndWarmthLightController brightnessAndWarmthLightController;

    private Light light;

    Range<Integer> ledOutputRange = new Range<>(5, 255);; // todo isolate onyx specific

    @Before
    public void beforeEach() {
        MockitoAnnotations.openMocks(this);
        when(nativeWarmColdLightController.getWarmAndColdBrightnessRange())
                .thenReturn(ledOutputRange);
        when(nativeWarmColdLightController.warm())
                .thenReturn(warmLeds);
        when(nativeWarmColdLightController.cold())
                .thenReturn(coldLeds);
        brightnessAndWarmthLightController = new OnyxBrightnessAndWarmthLightController(nativeWarmColdLightController);
        light = new Light(brightnessAndWarmthLightController);
    }
}
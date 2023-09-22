package com.onyx.darie.calin.gentleglowonyxboox;

import com.onyx.darie.calin.gentleglowonyxboox.light.BrightnessAndWarmthState;
import com.onyx.darie.calin.gentleglowonyxboox.light.LightConfiguration;
import com.onyx.darie.calin.gentleglowonyxboox.onyx.warmandcold.WarmAndColdLedOutput;
import com.onyx.darie.calin.gentleglowonyxboox.util.MutuallyExclusiveChoice;

import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.assertEquals;

public class LightConfigurationEditorTest {

    @Test
    public void whenSubscribingToConfigurationChanges_restoresBrightnessAndWarmth() {
        fixture.configurationEditor.getLightConfigurationChoices$().subscribe();

        BrightnessAndWarmthState state = fixture.lightTestFixture.getBrightnessAndWarmthState();
        assertEquals(LightConfiguration.getPresets()[0].brightnessAndWarmth, state.brightnessAndWarmth);
    }

    @Test
    public void whenSelectedConfigurationChanges_lightEmitsBrightnessAndWarmth() {
        fixture.configurationEditor.getLightConfigurationChoices$().subscribe();
        int indexToSelect = 1;
        fixture.configurationEditor.getChooseCurrentLightConfigurationRequest$().onNext(indexToSelect);
        fixture.lightTestFixture.captureLedOutputAndComplete();

        BrightnessAndWarmthState state = fixture.lightTestFixture.getBrightnessAndWarmthState();
        assertEquals(LightConfiguration.getPresets()[indexToSelect].brightnessAndWarmth, state.brightnessAndWarmth);
    }

    @Before
    public void beforeEach() { fixture= new LightConfigurationEditorTestFixture(); }

    @Test
    public void whenResumingAfterExternalChange_lightEmitsBrightnessAndWarmth() {
        fixture.configurationEditor.getLightConfigurationChoices$().subscribe();
        MutuallyExclusiveChoice<LightConfiguration> oldConfiguration =
                fixture.configurationEditor.getLightConfigurationChoice();

        fixture.lightTestFixture.simulateOnyxSliderChange(new WarmAndColdLedOutput(255, 255));

        fixture.configurationEditor.getChooseCurrentLightConfigurationRequest$().onNext(
                oldConfiguration.getSelectedIndex());
        fixture.lightTestFixture.captureLedOutputAndComplete();

        BrightnessAndWarmthState state = fixture.lightTestFixture.getBrightnessAndWarmthState();
        assertEquals(oldConfiguration.getSelected().brightnessAndWarmth, state.brightnessAndWarmth);
    }

    LightConfigurationEditorTestFixture fixture;
}

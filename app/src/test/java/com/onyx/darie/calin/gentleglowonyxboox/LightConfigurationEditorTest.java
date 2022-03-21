package com.onyx.darie.calin.gentleglowonyxboox;

import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.assertEquals;

public class LightConfigurationEditorTest {
    @Test
    public void whenSubscribingToConfigurationChanges_restoresBrightnessAndWarmth() {
        fixture.configurationEditor.getLightConfigurationChoices$().subscribe();
        fixture.lightTestFixture.captureLedOutputAndComplete();

        BrightnessAndWarmthState state = fixture.lightTestFixture.getBrightnessAndWarmthState();
        assertEquals(LightConfiguration.getPresets()[0].brightnessAndWarmth, state.brightnessAndWarmth);
    }

    @Test
    public void whenSelectedConfigurationChanges_lightEmitsBrightnessAndWarmth() {
        fixture.configurationEditor.getLightConfigurationChoices$().subscribe();
        fixture.lightTestFixture.captureLedOutputAndComplete();
        int indexToSelect = 1;
        fixture.configurationEditor.chooseCurrentLightConfigurationRequest$.onNext(indexToSelect);
        fixture.lightTestFixture.captureLedOutputAndComplete();

        BrightnessAndWarmthState state = fixture.lightTestFixture.getBrightnessAndWarmthState();
        assertEquals(LightConfiguration.getPresets()[indexToSelect].brightnessAndWarmth, state.brightnessAndWarmth);
    }

    @Before
    public void beforeEach() { fixture= new LightConfigurationEditorTestFixture(); }

    LightConfigurationEditorTestFixture fixture;
}

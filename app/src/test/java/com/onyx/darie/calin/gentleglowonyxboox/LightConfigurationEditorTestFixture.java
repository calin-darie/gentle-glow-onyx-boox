package com.onyx.darie.calin.gentleglowonyxboox;

import com.onyx.darie.calin.gentleglowonyxboox.light.LightConfigurationEditor;
import com.onyx.darie.calin.gentleglowonyxboox.light.LightConfigurationEditorImpl;

public class LightConfigurationEditorTestFixture {
    public final LightTestFixture lightTestFixture = new LightTestFixture();
    public LightConfigurationEditor configurationEditor = new LightConfigurationEditorImpl(
            lightTestFixture.light,
            new FakeStorage<>()
            );

}

package com.onyx.darie.calin.gentleglowonyxboox;

public class LightConfigurationEditorTestFixture {
    public final LightTestFixture lightTestFixture = new LightTestFixture();
    public LightConfigurationEditor configurationEditor = new LightConfigurationEditorImpl(
            lightTestFixture.light,
            new FakeStorage<>()
            );

}

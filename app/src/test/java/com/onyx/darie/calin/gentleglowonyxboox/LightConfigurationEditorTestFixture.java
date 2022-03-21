package com.onyx.darie.calin.gentleglowonyxboox;

public class LightConfigurationEditorTestFixture {
    public final LightTestFixture lightTestFixture = new LightTestFixture();
    public LightConfigurationEditor configurationEditor = new LightConfigurationEditor(
            lightTestFixture.light,
            new FakeStorage<>()
            );

}

package com.onyx.darie.calin.gentleglowonyxboox.setup;

import com.onyx.darie.calin.gentleglowonyxboox.light.Light;
import com.onyx.darie.calin.gentleglowonyxboox.light.LightConfigurationEditor;

public interface Dependencies {

    Light getLight();

    LightConfigurationEditor getLightConfigurationEditor();
}
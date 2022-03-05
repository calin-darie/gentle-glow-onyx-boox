package com.onyx.darie.calin.gentleglowonyxboox;

import io.reactivex.rxjava3.core.Observable;

public interface LightConfigurationEditorCommandSource {
    Observable<Integer> getChooseCurrentLightConfigurationRequest$();
    Observable getStartEditingCurrentLightConfigurationByBindingToCurrentBrightnessAndWarmthRequest$();
    Observable getStopEditingCurrentLightConfigurationByBindingToCurrentBrightnessAndWarmthRequest$();
    Observable<String> getRenameCurrentLightConfigurationRequest$();
    Observable<LightConfiguration> getReplaceCurrentLightConfigurationRequest$();
}

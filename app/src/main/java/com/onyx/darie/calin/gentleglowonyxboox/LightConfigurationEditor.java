package com.onyx.darie.calin.gentleglowonyxboox;

import java.util.stream.Stream;

import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.subjects.PublishSubject;

public interface LightConfigurationEditor {
    PublishSubject<Integer> getChooseCurrentLightConfigurationRequest$();

    PublishSubject<Integer> getStartEditingCurrentLightConfigurationByBindingToCurrentBrightnessAndWarmthRequest$();

    PublishSubject<Integer> getStopEditingCurrentLightConfigurationByBindingToCurrentBrightnessAndWarmthRequest$();

    PublishSubject<String> getRenameCurrentLightConfigurationRequest$();

    PublishSubject<LightConfiguration> getReplaceCurrentLightConfigurationRequest$();

    MutuallyExclusiveChoice<LightConfiguration> getLightConfigurationChoice();

    Stream<LightConfiguration> getPresetsToReplaceCurrent();

    Observable<MutuallyExclusiveChoice<LightConfiguration>> getLightConfigurationChoices$();
    Observable<Integer> getStatus$();


}

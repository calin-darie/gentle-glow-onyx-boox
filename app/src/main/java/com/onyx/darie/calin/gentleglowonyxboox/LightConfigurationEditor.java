package com.onyx.darie.calin.gentleglowonyxboox;

import androidx.core.util.ObjectsCompat;

import java.util.Arrays;
import java.util.stream.Stream;

import io.reactivex.rxjava3.annotations.NonNull;
import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.subjects.PublishSubject;

public class LightConfigurationEditor {
    public final PublishSubject<Integer> chooseCurrentLightConfigurationRequest$ = PublishSubject.create();
    public final PublishSubject<Integer> startEditingCurrentLightConfigurationByBindingToCurrentBrightnessAndWarmthRequest$ = PublishSubject.create();
    private Observable<Integer> editResumed$;
    public final PublishSubject<Integer> stopEditingCurrentLightConfigurationByBindingToCurrentBrightnessAndWarmthRequest$ = PublishSubject.create();
    public final PublishSubject<String> renameCurrentLightConfigurationRequest$ = PublishSubject.create();
    public final PublishSubject<LightConfiguration> replaceCurrentLightConfigurationRequest$ = PublishSubject.create();
    private final PublishSubject<Integer> status$ = PublishSubject.create();
    private MutuallyExclusiveChoice<LightConfiguration> lightConfigurationChoice;

    public MutuallyExclusiveChoice<LightConfiguration> getLightConfigurationChoice() {
        return lightConfigurationChoice;
    }

    public Stream<LightConfiguration> getPresetsToReplaceCurrent() {
        return Arrays.stream(LightConfiguration.getPresets())
                .filter(preset -> !preset.equals(getLightConfigurationChoice().getSelected()));
    }

    public Observable<MutuallyExclusiveChoice<LightConfiguration>> getLightConfigurationChoices$() {
        return configurationChoice$;
    }

    public Observable<Integer> getStatus$() {
        return status$.distinctUntilChanged();
    }

    public LightConfigurationEditor(
            Light light,
            Storage<LightConfigurationChoice> storage) {
        this.light = light;
        this.storage = storage;
        subscribeStartStopCurrentConfigurationBindingToBrightnessAndWarmth();
        this.configurationChoice$ = Observable.defer(() -> Observable
                .merge(
                        setupBrightnessAndWarmthBinding(),
                        setupChooseCurrentLightConfiguration(),
                        setupRenameCurrentLightConfiguration(),
                        setupReplaceCurrentLightConfiguration()
                )
                .startWithItem(restoreState())
                .distinctUntilChanged()
                .doOnNext(configuration -> setConfiguration(configuration))
        );
    }

    private void setConfiguration(MutuallyExclusiveChoice<LightConfiguration> configuration) {
        this.lightConfigurationChoice = configuration;

        if(!configuration.hasChoice()) {
            status$.onNext(R.string.external_change_status);
            return;
        }

        status$.onNext(R.string.empty);
        storage.save(new LightConfigurationChoice(configuration.choices, configuration.selectedIndex));
        status$.onNext(R.string.saved);
    }

    private MutuallyExclusiveChoice<LightConfiguration> restoreState() {
        LightConfigurationChoice defaultValue = new LightConfigurationChoice(LightConfiguration.getPresets(), 0);
        Result<LightConfigurationChoice> result = storage.loadOrDefault(defaultValue);

        LightConfigurationChoice restoredState = result.hasError()? defaultValue :result.value;

        BrightnessAndWarmth brightnessAndWarmth = restoredState
                .getSelected().brightnessAndWarmth;
        light.restoreBrightnessAndWarmthRequest$.onNext(brightnessAndWarmth);
        return restoredState;
    }
    private BrightnessAndWarmth latestBrightnessAndWarmth;

    private @NonNull Observable<MutuallyExclusiveChoice<LightConfiguration>> setupBrightnessAndWarmthBinding() {
        return Observable.merge(
                light.getBrightnessAndWarmthState$()
                        .filter(s -> s.isExternalChange)
                        .map(s -> getLightConfigurationChoice().cloneAndClearChoice()),
                Observable.merge(
                        light.getBrightnessAndWarmthState$()
                                .filter(s -> !s.isExternalChange)
                                .doOnNext(s -> {
                                    MutuallyExclusiveChoice<LightConfiguration> choice = getLightConfigurationChoice();
                                    if (choice.hasChoice() &&
                                            ! s.brightnessAndWarmth.equals(choice.getSelected().brightnessAndWarmth)) {
                                        status$.onNext(R.string.empty);
                                    }
                                })
                                .doOnNext(s -> latestBrightnessAndWarmth = s.brightnessAndWarmth)
                                .filter(s -> isCurrentLightConfigurationBoundToBrightnessAndWarmth)
                                .map(s -> s.brightnessAndWarmth),
                        editResumed$
                                .filter(ignore -> latestBrightnessAndWarmth != null)
                                .map(ignore -> latestBrightnessAndWarmth)
                        )
                        .filter(brightnessAndWarmth -> !brightnessAndWarmth.equals(getLightConfigurationChoice().getSelected().brightnessAndWarmth))
                        .map(brightnessAndWarmth ->
                                getLightConfigurationChoice().cloneAndReplaceSelected(
                                        getLightConfigurationChoice().getSelected().cloneWithBrightnessAndWarmth(brightnessAndWarmth)))
        );
    }

    private @NonNull Observable<MutuallyExclusiveChoice<LightConfiguration>> setupReplaceCurrentLightConfiguration() {
        return replaceCurrentLightConfigurationRequest$
                .map(configuration -> getLightConfigurationChoice().cloneAndReplaceSelected(configuration))
                .doAfterNext(configuration ->
                        light.setBrightnessAndWarmthRequest$.onNext(
                                configuration.getSelected().brightnessAndWarmth));
    }

    private @NonNull Observable<MutuallyExclusiveChoice<LightConfiguration>> setupChooseCurrentLightConfiguration() {
        return chooseCurrentLightConfigurationRequest$
                .map(index -> getLightConfigurationChoice().cloneAndSelect(index))
                .doAfterNext(configuration ->
                        light.setBrightnessAndWarmthRequest$.onNext(
                                configuration.getSelected().brightnessAndWarmth));
    }

    private @NonNull Observable<MutuallyExclusiveChoice<LightConfiguration>> setupRenameCurrentLightConfiguration() {
        return renameCurrentLightConfigurationRequest$
                .filter(name -> !ObjectsCompat.equals(name, getLightConfigurationChoice().getSelected().name))
                .map(name ->
                        getLightConfigurationChoice().cloneAndReplaceSelected(
                                getLightConfigurationChoice().getSelected().cloneAndRename(name)));
    }

    private void subscribeStartStopCurrentConfigurationBindingToBrightnessAndWarmth() {
        editResumed$ = startEditingCurrentLightConfigurationByBindingToCurrentBrightnessAndWarmthRequest$
                .filter(_ignore -> !isCurrentLightConfigurationBoundToBrightnessAndWarmth)
                .doAfterNext(_ignore -> isCurrentLightConfigurationBoundToBrightnessAndWarmth = true);
        stopEditingCurrentLightConfigurationByBindingToCurrentBrightnessAndWarmthRequest$
                .subscribe(_ignore -> isCurrentLightConfigurationBoundToBrightnessAndWarmth = false);
    }
    private final Light light;
    private final Storage<LightConfigurationChoice> storage;
    private Observable<MutuallyExclusiveChoice<LightConfiguration>> configurationChoice$;

    private boolean isCurrentLightConfigurationBoundToBrightnessAndWarmth;
}


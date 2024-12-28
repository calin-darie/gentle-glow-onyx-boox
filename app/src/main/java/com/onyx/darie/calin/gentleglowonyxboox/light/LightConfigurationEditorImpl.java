package com.onyx.darie.calin.gentleglowonyxboox.light;

import androidx.core.util.ObjectsCompat;

import com.onyx.darie.calin.gentleglowonyxboox.R;
import com.onyx.darie.calin.gentleglowonyxboox.storage.Storage;
import com.onyx.darie.calin.gentleglowonyxboox.util.MutuallyExclusiveChoice;
import com.onyx.darie.calin.gentleglowonyxboox.util.Result;

import java.util.Arrays;
import java.util.stream.Stream;

import io.reactivex.rxjava3.annotations.NonNull;
import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.subjects.PublishSubject;

public class LightConfigurationEditorImpl<TNativeOutput> implements LightConfigurationEditor{
    @Override
    public PublishSubject<Integer> getChooseCurrentLightConfigurationRequest$() {
        return chooseCurrentLightConfigurationRequest$;
    }

    @Override
    public PublishSubject<Integer> getStartEditingCurrentLightConfigurationByBindingToCurrentBrightnessAndWarmthRequest$() {
        return startEditingCurrentLightConfigurationByBindingToCurrentBrightnessAndWarmthRequest$;
    }

    @Override
    public PublishSubject<Integer> getStopEditingCurrentLightConfigurationByBindingToCurrentBrightnessAndWarmthRequest$() {
        return stopEditingCurrentLightConfigurationByBindingToCurrentBrightnessAndWarmthRequest$;
    }

    @Override
    public PublishSubject<String> getRenameCurrentLightConfigurationRequest$() {
        return renameCurrentLightConfigurationRequest$;
    }

    @Override
    public PublishSubject<LightConfiguration> getReplaceCurrentLightConfigurationRequest$() {
        return replaceCurrentLightConfigurationRequest$;
    }

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

    public LightConfigurationEditorImpl(
            LightImpl<TNativeOutput> light,
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
        storage.save(new LightConfigurationChoice(configuration.getChoices(), configuration.getSelectedIndex()));
        status$.onNext(R.string.saved);
    }

    private MutuallyExclusiveChoice<LightConfiguration> restoreState() {
        LightConfigurationChoice defaultValue = new LightConfigurationChoice(LightConfiguration.getPresets(), 0);
        Result<LightConfigurationChoice> result = storage.loadOrDefault(defaultValue);

        LightConfigurationChoice restoredState = result.hasError()? defaultValue :result.value;

        BrightnessAndWarmth brightnessAndWarmth = restoredState
                .getSelected().brightnessAndWarmth;
        light.getRestoreBrightnessAndWarmthRequest$().onNext(brightnessAndWarmth);
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
                        light.getSetBrightnessAndWarmthRequest$().onNext(
                                configuration.getSelected().brightnessAndWarmth));
    }

    private @NonNull Observable<MutuallyExclusiveChoice<LightConfiguration>> setupChooseCurrentLightConfiguration() {
        return chooseCurrentLightConfigurationRequest$
                .map(index -> getLightConfigurationChoice().cloneAndSelect(index))
                .doAfterNext(configuration ->
                        light.getSetBrightnessAndWarmthRequest$().onNext(
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

    private final PublishSubject<Integer> chooseCurrentLightConfigurationRequest$ = PublishSubject.create();
    private final PublishSubject<Integer> startEditingCurrentLightConfigurationByBindingToCurrentBrightnessAndWarmthRequest$ = PublishSubject.create();
    private final PublishSubject<Integer> stopEditingCurrentLightConfigurationByBindingToCurrentBrightnessAndWarmthRequest$ = PublishSubject.create();
    private final PublishSubject<String> renameCurrentLightConfigurationRequest$ = PublishSubject.create();
    private final PublishSubject<LightConfiguration> replaceCurrentLightConfigurationRequest$ = PublishSubject.create();

    private final LightImpl<TNativeOutput> light;
    private final Storage<LightConfigurationChoice> storage;
    private Observable<MutuallyExclusiveChoice<LightConfiguration>> configurationChoice$;
    private Observable<Integer> editResumed$;
    private MutuallyExclusiveChoice<LightConfiguration> lightConfigurationChoice;
    private final PublishSubject<Integer> status$ = PublishSubject.create();

    private boolean isCurrentLightConfigurationBoundToBrightnessAndWarmth;
}


package com.onyx.darie.calin.gentleglowonyxboox;

import java.util.Arrays;
import java.util.stream.Stream;

import io.reactivex.rxjava3.annotations.NonNull;
import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.subjects.PublishSubject;

// set to preview changes, read to update current config
// 1. load state of MutuallyExclusiveChoice<>
// 2. init desired brightness and warmth and let Light emit an externalChange?
//
// todo migrateSavedSettings()
public class LightConfigurationEditor {
    public final PublishSubject<Integer> chooseCurrentLightConfigurationRequest$ = PublishSubject.create();
    public final PublishSubject<Integer> startEditingCurrentLightConfigurationByBindingToCurrentBrightnessAndWarmthRequest$ = PublishSubject.create();
    public final PublishSubject<Integer> stopEditingCurrentLightConfigurationByBindingToCurrentBrightnessAndWarmthRequest$ = PublishSubject.create();
    public final PublishSubject<String> renameCurrentLightConfigurationRequest$ = PublishSubject.create();
    public final PublishSubject<LightConfiguration> replaceCurrentLightConfigurationRequest$ = PublishSubject.create();
    private MutuallyExclusiveChoice<LightConfiguration> lightConfigurationChoice;

    public MutuallyExclusiveChoice<LightConfiguration> getLightConfigurationChoice() {
        return lightConfigurationChoice;
    }

    public Stream<LightConfiguration> getPresetsToReplaceCurrent() {
        return Arrays.stream(LightConfiguration.getPresets())
                .filter(preset -> preset.equals(getLightConfigurationChoice()));
    }

    public Observable<MutuallyExclusiveChoice<LightConfiguration>> getLightConfigurationChoices$() {
        return configurationChoice$;
    }

    public LightConfigurationEditor(Light light, Storage<MutuallyExclusiveChoice<LightConfiguration>> storage) {
        this.light = light;
        this.storage = storage;
        this.configurationChoice$ = Observable.merge(
                subscribeBrightnessAndWarmthBinding(),
                subscribeChooseCurrentLightConfiguration(),
                subscribeRenameCurrentLightConfiguration(),
                subscribeReplaceCurrentLightConfiguration()
        )
                .startWith(Observable.just(restoreState()))
                .doOnEach(configurationNotification -> this.lightConfigurationChoice = configurationNotification.getValue());
        subscribeStartStopCurrentConfigurationBindingToBrightnessAndWarmth();
        setBrightnessAndWamrthWhenConfigurationChanges();
    }

    private void setBrightnessAndWamrthWhenConfigurationChanges() {
        configurationChoice$.subscribe(configuration -> light.setBrightnessAndWarmthRequest$.onNext(configuration.getSelected().brightnessAndWarmth));
    }

    private MutuallyExclusiveChoice<LightConfiguration> restoreState() {
        Result<MutuallyExclusiveChoice<LightConfiguration>> result = storage.loadOrDefault(
                new MutuallyExclusiveChoice<LightConfiguration>(LightConfiguration.getPresets(), 0));
                // todo handle error
        return result.value;
    }

    private BrightnessAndWarmth latestBrightnessAndWarmth;
    private @NonNull Observable<MutuallyExclusiveChoice<LightConfiguration>> subscribeBrightnessAndWarmthBinding() {
        @NonNull Observable<BrightnessAndWarmth> x = light.getBrightnessAndWarmthState$()
                .filter(s -> !s.isExternalChange)
                .doOnEach(n -> latestBrightnessAndWarmth = n.getValue().brightnessAndWarmth)
                .filter(s -> isCurrentLightConfigurationBoundToBrightnessAndWarmth)
                .map(s -> s.brightnessAndWarmth);
        return Observable.merge(
                light.getBrightnessAndWarmthState$()
                        .filter(s -> !s.isExternalChange)
                        .doOnEach(n -> latestBrightnessAndWarmth = n.getValue().brightnessAndWarmth)
                        .filter(s -> isCurrentLightConfigurationBoundToBrightnessAndWarmth)
                        .map(s -> s.brightnessAndWarmth),
                startEditingCurrentLightConfigurationByBindingToCurrentBrightnessAndWarmthRequest$
                        .filter(ignore -> latestBrightnessAndWarmth != null)
                        .map(ignore -> latestBrightnessAndWarmth)
                )
                .map(brightnessAndWarmth -> getLightConfigurationChoice().cloneAndReplaceSelected(
                        getLightConfigurationChoice().getSelected().cloneWithBrightnessAndWarmth(brightnessAndWarmth)));
    }

    private @NonNull Observable<MutuallyExclusiveChoice<LightConfiguration>> subscribeReplaceCurrentLightConfiguration() {
        return replaceCurrentLightConfigurationRequest$
            .map(configuration -> getLightConfigurationChoice().cloneAndReplaceSelected(configuration));
    }

    private @NonNull Observable<MutuallyExclusiveChoice<LightConfiguration>> subscribeChooseCurrentLightConfiguration() {
        return chooseCurrentLightConfigurationRequest$
                .map(index -> getLightConfigurationChoice().cloneAndSelect(index));
    }

    private @NonNull Observable<MutuallyExclusiveChoice<LightConfiguration>> subscribeRenameCurrentLightConfiguration() {
        return renameCurrentLightConfigurationRequest$
                .map(name ->
                        getLightConfigurationChoice().cloneAndReplaceSelected(
                                getLightConfigurationChoice().getSelected().cloneAndRename(name)));
    }

    private void subscribeStartStopCurrentConfigurationBindingToBrightnessAndWarmth() {
        startEditingCurrentLightConfigurationByBindingToCurrentBrightnessAndWarmthRequest$
                .subscribe(_ignore -> {
                    isCurrentLightConfigurationBoundToBrightnessAndWarmth = true;
                });
        stopEditingCurrentLightConfigurationByBindingToCurrentBrightnessAndWarmthRequest$
                .subscribe(_ignore -> isCurrentLightConfigurationBoundToBrightnessAndWarmth = false);
    }

    private final Light light;
    private final Storage<MutuallyExclusiveChoice<LightConfiguration>> storage;
    private final Observable<MutuallyExclusiveChoice<LightConfiguration>> configurationChoice$;
    private boolean isCurrentLightConfigurationBoundToBrightnessAndWarmth;
}

//    File namedSettingsFile()  {
//        return new File(getFilesDir(), "namedSettings.json");
//    }
//
//    Gson json = new Gson();
//    private void migrateSavedSettings() { // todo
//        NamedWarmthBrightnessSetting[] savedSettings = loadNamedSettings();
//        boolean changed = false;
//        for (int i = 0; i< savedSettings.length; i++) {
//            if (savedSettings[i].setting.brightness == 0) {
//                savedSettings[i] = NamedWarmthBrightnessSetting.presets[i];
//                changed = true;
//            }
//        }
//        if (changed) {
//            saveNamedSettings(savedSettings);
//        }
//    }

//    private NamedWarmthBrightnessSetting[] loadNamedSettings() {
//        File namedSettingsFile = namedSettingsFile();
//        if (! namedSettingsFile.exists())
//            return new NamedWarmthBrightnessSetting[0];
//
//        byte[] bytes;
//        try {
//            bytes = Files.readAllBytes(namedSettingsFile.toPath());
//        } catch (IOException e) {
//            status.setText("Error reading presets. This should never happen.");
//            return new NamedWarmthBrightnessSetting[0];
//        }
//        String namedSettingsAsJson = new String(bytes);
//        return json.fromJson(namedSettingsAsJson, NamedWarmthBrightnessSetting[].class);
//    }
//
//    // todo bind to configuration saved event from LightConfigurationEditor
//    // todo status.setText(getText(R.string.saved));
//
//    File selectionFile()  {
//        return new File(getFilesDir(), "selectedIndex.txt");
//    }
//
//    private int loadSelectedIndex() {
//        File file = selectionFile();
//        if (! file.exists())
//            return 0;
//
//        byte[] bytes;
//        try {
//            bytes = Files.readAllBytes(file.toPath());
//        } catch (IOException e) {
//            status.setText("Error reading selection. This should never happen.");
//            return 0;
//        }
//        String selectedAsString = new String(bytes);
//        return Integer.parseInt(selectedAsString);
//    }
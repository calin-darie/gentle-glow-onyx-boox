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

    public LightConfigurationEditor(Light light, Storage<LightConfigurationChoice> storage) {
        this.light = light;
        this.storage = storage;
        subscribeStartStopCurrentConfigurationBindingToBrightnessAndWarmth();
        this.configurationChoice$ = Observable.defer(() -> Observable
                .merge(
                        subscribeBrightnessAndWarmthBinding(),
                        subscribeChooseCurrentLightConfiguration(),
                        subscribeRenameCurrentLightConfiguration(),
                        subscribeReplaceCurrentLightConfiguration()
                )
                .startWithItem(restoreState())
                .doOnNext(configuration -> setConfiguration(configuration))
        );
    }

    private void setConfiguration(MutuallyExclusiveChoice<LightConfiguration> configuration) {
        this.lightConfigurationChoice = configuration;
        storage.save(new LightConfigurationChoice(configuration.choices, configuration.selectedIndex));
    }

    private MutuallyExclusiveChoice<LightConfiguration> restoreState() {
        Result<LightConfigurationChoice> result = storage.loadOrDefault(
                new LightConfigurationChoice(LightConfiguration.getPresets(), 0));
        // todo handle error
        BrightnessAndWarmth brightnessAndWarmth = result.value.getSelected().brightnessAndWarmth;
        light.restoreBrightnessAndWarmthRequest$.onNext(brightnessAndWarmth);
        return result.value;
    }

    private BrightnessAndWarmth latestBrightnessAndWarmth;
    private @NonNull Observable<MutuallyExclusiveChoice<LightConfiguration>> subscribeBrightnessAndWarmthBinding() {
        return Observable.merge(
                light.getBrightnessAndWarmthState$()
                        .filter(s -> !s.isExternalChange)
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
                                getLightConfigurationChoice().getSelected().cloneWithBrightnessAndWarmth(brightnessAndWarmth)));
    }

    private @NonNull Observable<MutuallyExclusiveChoice<LightConfiguration>> subscribeReplaceCurrentLightConfiguration() {
        return replaceCurrentLightConfigurationRequest$
                .map(configuration -> getLightConfigurationChoice().cloneAndReplaceSelected(configuration))
                .doAfterNext(configuration ->
                        light.setBrightnessAndWarmthRequest$.onNext(
                                configuration.getSelected().brightnessAndWarmth));
    }

    private @NonNull Observable<MutuallyExclusiveChoice<LightConfiguration>> subscribeChooseCurrentLightConfiguration() {
        return chooseCurrentLightConfigurationRequest$
                .map(index -> getLightConfigurationChoice().cloneAndSelect(index))
                .doAfterNext(configuration ->
                        light.setBrightnessAndWarmthRequest$.onNext(
                                configuration.getSelected().brightnessAndWarmth));
    }

    private @NonNull Observable<MutuallyExclusiveChoice<LightConfiguration>> subscribeRenameCurrentLightConfiguration() {
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
// todo
// restore onyx slider UI
// migrateSavedSettings()
// todo test
// emit & handle first light
// bind config to trackbar`
// bind config to +/-
// save on release track bar
// circular events
// fix reset

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
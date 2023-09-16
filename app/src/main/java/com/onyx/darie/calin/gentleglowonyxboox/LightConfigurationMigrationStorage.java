package com.onyx.darie.calin.gentleglowonyxboox;

import java.io.File;
import java.util.Arrays;

public class LightConfigurationMigrationStorage implements Storage<LightConfigurationChoice>{
    public LightConfigurationMigrationStorage(File directory) {
        storage = new FileStorage<>(directory, "lightConfigurations.json");
        legacySettingsStorage = new FileStorage<>(directory, "namedSettings.json");
        legacySettingsIndexStorage = new FileStorage<>(directory, "selectedIndex.txt");
    }

    private final Storage<LightConfigurationChoice> storage;
    private final Storage<NamedWarmthBrightnessSetting[]> legacySettingsStorage;
    private final Storage<Integer> legacySettingsIndexStorage;

    private LightConfigurationChoice migrateSavedSettings() {
        Result<NamedWarmthBrightnessSetting[]> legacySettingsResult = legacySettingsStorage.loadOrDefault(new NamedWarmthBrightnessSetting[0]);
        if (legacySettingsResult.hasError())
            return null;
        Result<Integer> legacySettingsIndexResult = legacySettingsIndexStorage.loadOrDefault(0);
        if (legacySettingsIndexResult.hasError())
            return null;

        LightConfiguration[] choices = Arrays.stream(legacySettingsResult.value)
                .filter(setting -> !setting.isForOnyxCompatibility)
                .map(setting -> new LightConfiguration(setting.name,
                        new BrightnessAndWarmth(new Brightness(setting.setting.brightness), new Warmth(setting.setting.warmth))))
                .toArray(size -> new LightConfiguration[size]);

        int index = legacySettingsIndexResult.value >= choices.length? 0 : legacySettingsIndexResult.value;

        LightConfigurationChoice configuration = new LightConfigurationChoice(choices, index);
        // todo test return Integer.parseInt(selectedAsString);
        return configuration;
    }

    @Override
    public Result save(LightConfigurationChoice data) {
        return storage.save(data);
    }

    @Override
    public Result<LightConfigurationChoice> loadOrDefault(LightConfigurationChoice defaultValue) {
        Result<LightConfigurationChoice> rawResult = storage.loadOrDefault(null);
        if (rawResult.hasError() || rawResult.value != null)
            return rawResult;

        LightConfigurationChoice migratedChoice = migrateSavedSettings();
        if (migratedChoice == null) {
            migratedChoice = defaultValue;
        }

        save(migratedChoice);

        return Result.success(migratedChoice);
    }
}

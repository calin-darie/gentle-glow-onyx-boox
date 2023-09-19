package com.onyx.darie.calin.gentleglowonyxboox;

import java.io.File;
import java.util.Arrays;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Stream;

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

        LightConfiguration[] choices =
            Stream.concat(
                    Arrays.stream(legacySettingsResult.value)
                    .filter(setting -> !setting.isForOnyxCompatibility)
                    .map(setting -> new LightConfiguration(setting.name,
                            new BrightnessAndWarmth(new Brightness(setting.setting.brightness), new Warmth(setting.setting.warmth)))),

                    (Arrays.stream(LightConfiguration.getPresets()).skip(LightConfiguration.getPresets().length - 1))
                    )
                    .toArray(size -> new LightConfiguration[size]);

        int index = legacySettingsIndexResult.value >= choices.length? 0 : legacySettingsIndexResult.value;

        LightConfigurationChoice configuration = new LightConfigurationChoice(choices, index);
        Logger.getLogger("x").log(Level.SEVERE, "selected index: " + index);
        return configuration;
    }

    @Override
    public Result save(LightConfigurationChoice data) {
        return storage.save(data);
    }

    @Override
    public Result<LightConfigurationChoice> loadOrDefault(LightConfigurationChoice defaultValue) {
        Result<LightConfigurationChoice> rawResult = storage.loadOrDefault(new LightConfigurationChoice(new LightConfiguration[0], -1));
        if (rawResult.hasError() || rawResult.value.selectedIndex != -1)
            return rawResult;

        LightConfigurationChoice migratedChoice = migrateSavedSettings();
        if (migratedChoice == null) {
            migratedChoice = defaultValue;
        }

        save(migratedChoice);

        return Result.success(migratedChoice);
    }
}

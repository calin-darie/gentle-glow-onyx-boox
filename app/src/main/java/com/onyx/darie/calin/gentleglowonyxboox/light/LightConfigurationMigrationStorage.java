package com.onyx.darie.calin.gentleglowonyxboox.light;

import com.onyx.darie.calin.gentleglowonyxboox.storage.FileStorage;
import com.onyx.darie.calin.gentleglowonyxboox.onyx.warmandcold.NamedWarmthBrightnessSetting;
import com.onyx.darie.calin.gentleglowonyxboox.util.Result;
import com.onyx.darie.calin.gentleglowonyxboox.storage.Storage;

import java.io.File;
import java.util.Arrays;
import java.util.stream.Stream;

public class LightConfigurationMigrationStorage implements Storage<LightConfigurationChoice> {
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
        if (legacySettingsResult.hasError() || legacySettingsResult.value.length == 0)
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

        int index = legacySettingsIndexResult.value >= choices.length ? 0 : legacySettingsIndexResult.value;

        LightConfigurationChoice configuration = new LightConfigurationChoice(choices, index);
        return configuration;
    }

    @Override
    public Result save(LightConfigurationChoice data) {
        return storage.save(data);
    }

    @Override
    public Result<LightConfigurationChoice> loadOrDefault(LightConfigurationChoice defaultValue) {
        Result<LightConfigurationChoice> rawResult = storage.loadOrDefault(new LightConfigurationChoice(new LightConfiguration[0], -1));
        if (rawResult.hasError() || rawResult.value.getChoices().length != 0)
            return rawResult;

        LightConfigurationChoice migratedChoice = migrateSavedSettings();
        if (migratedChoice == null) {
            migratedChoice = defaultValue;
        }

        save(migratedChoice);

        return Result.success(migratedChoice);
    }
}

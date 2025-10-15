package com.jtprince.coordinateoffset.config;

import com.jtprince.coordinateoffset.CoordinateOffsetCore;
import de.exlll.configlib.ConfigLib;
import de.exlll.configlib.YamlConfigurationProperties;
import de.exlll.configlib.YamlConfigurations;
import org.jspecify.annotations.Nullable;

import java.nio.file.Path;

public class ConfigHolder {
    static YamlConfigurationProperties properties =
        ConfigLib.BUKKIT_DEFAULT_PROPERTIES.toBuilder()
            .header("""
                    CoordinateOffset Configuration File
                    See https://github.com/joshuaprince/CoordinateOffset/wiki/Configuration-Guide
                    Do not leave comments in this file! They will be removed when the file is loaded.
                    """)
            .build();

    private final CoordinateOffsetCore core;
    private @Nullable CoordinateOffsetConfigBase config;

    public ConfigHolder(CoordinateOffsetCore core) {
        this.core = core;
        loadConfig();
    }

    public CoordinateOffsetConfigBase getConfig() {
        if (config == null) {
            throw new IllegalStateException("CoordinateOffset config is not yet loaded.");
        }
        return config;
    }

    public CoordinateOffsetConfigFull getProviderConfig() {
        if (!(config instanceof CoordinateOffsetConfigFull fullConfig)) {
            throw new IllegalStateException("CoordinateOffset Offset Provider config is not yet loaded.");
        }
        return fullConfig;
    }

    private void loadConfig() {
        Path configPath = core.getAdapter().getConfigPath();

        if (!core.areAllProvidersLoaded() && configPath.toFile().exists()) {
            config = YamlConfigurations.load(configPath, CoordinateOffsetConfigBase.class, properties);
        } else {
            config = YamlConfigurations.update(configPath, CoordinateOffsetConfigFull.class, properties);
        }
    }

    public void reload(boolean logMessage) {
        loadConfig();
        if (logMessage) {
            CoordinateOffsetCore.get().getLogger().info("Config reloaded.");
        }
    }
}

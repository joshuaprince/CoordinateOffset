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
                    Comments left in this file will be deleted when configuration is loaded.
                    """)
            .build();

    private final CoordinateOffsetCore core;
    private @Nullable CoordinateOffsetConfigBase config;
    private boolean safeToWriteToConfigFile = false;

    public ConfigHolder(CoordinateOffsetCore core) {
        this.core = core;
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

    public void loadBaseConfig() {
        Path configPath = core.getAdapter().getConfigPath();

        if (configPath.toFile().exists()) {
            config = YamlConfigurations.load(configPath, CoordinateOffsetConfigBase.class, properties);
            if (ConfigVersion.onLoadBaseConfig(configPath, config)) {
                safeToWriteToConfigFile = true;
            }
        } else {
            config = new CoordinateOffsetConfigBase();
            safeToWriteToConfigFile = true;
            loadFullConfig();
        }
    }

    /** @return true if the config was successfully loaded, false if errors occurred. */
    public boolean loadFullConfig() {
        Path configPath = core.getAdapter().getConfigPath();

        if (configPath.toFile().exists()) {
            try {
                config = YamlConfigurations.load(configPath, CoordinateOffsetConfigFull.class, properties);
            } catch (Exception e) {
                e.printStackTrace();
                return false;
            }
        } else {
            config = new CoordinateOffsetConfigFull();
        }

        if (safeToWriteToConfigFile) {
            config.configVersion = ConfigVersion.CURRENT;
            YamlConfigurations.save(configPath, CoordinateOffsetConfigFull.class, (CoordinateOffsetConfigFull) config, properties);
        }

        return true;
    }

    public void reload(boolean logMessage) {
        if (loadFullConfig() && logMessage) {
            CoordinateOffsetCore.get().getLogger().info("Config reloaded.");
        }
    }
}

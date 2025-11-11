package com.jtprince.coordinateoffset.config;

import com.jtprince.coordinateoffset.CoordinateOffsetCore;
import de.exlll.configlib.ConfigLib;
import de.exlll.configlib.NameFormatters;
import de.exlll.configlib.YamlConfigurationProperties;
import de.exlll.configlib.YamlConfigurations;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

import java.nio.file.Path;

@NullMarked
public class ConfigHolder {
    private static final YamlConfigurationProperties properties =
        ConfigLib.BUKKIT_DEFAULT_PROPERTIES.toBuilder()
            .header("""
                    CoordinateOffset Configuration File
                    https://github.com/joshuaprince/CoordinateOffset/wiki/Configuration-Guide
                    Comments left in this file will be deleted when configuration is loaded.
                    """)
            .build();

    private static final YamlConfigurationProperties propertiesMessages =
        ConfigLib.BUKKIT_DEFAULT_PROPERTIES.toBuilder()
            .addSerializer(MessagesConfig.Message.class, new MessagesConfig.Serializer())
            .setNameFormatter(NameFormatters.LOWER_UNDERSCORE)
            .header("""
                    CoordinateOffset Language/Messages File
                    Formatting help: https://webui.advntr.dev/
                    Comments and extra keys left in this file will be deleted when configuration is loaded.
                    """)
            .build();

    private final CoordinateOffsetCore core;
    private @Nullable CoordinateOffsetConfigBase runningConfig;
    private @Nullable MessagesConfig messagesConfig;
    private boolean safeToWriteToConfigFile = false;

    public ConfigHolder(CoordinateOffsetCore core) {
        this.core = core;
    }

    public CoordinateOffsetConfigBase getConfig() {
        if (runningConfig == null) {
            throw new IllegalStateException("CoordinateOffset config is not yet loaded.");
        }
        return runningConfig;
    }

    public CoordinateOffsetConfigFull getProviderConfig() {
        if (!(runningConfig instanceof CoordinateOffsetConfigFull fullConfig)) {
            throw new IllegalStateException("CoordinateOffset Offset Provider config is not yet loaded.");
        }
        return fullConfig;
    }

    public MessagesConfig getMessagesConfig() {
        if (messagesConfig == null) {
            throw new IllegalStateException("CoordinateOffset message config is not yet loaded.");
        }
        return messagesConfig;
    }

    public void loadMessagesConfig() {
        Path messagesPath = core.getAdapter().getConfigDir().resolve("messages.yml");
        messagesConfig = YamlConfigurations.update(messagesPath, MessagesConfig.class, propertiesMessages);
    }

    public void loadBaseConfig() {
        Path configPath = core.getAdapter().getConfigDir().resolve("config.yml");
        if (configPath.toFile().exists()) {
            runningConfig = YamlConfigurations.load(configPath, CoordinateOffsetConfigBase.class, properties);
            if (ConfigVersion.onLoadBaseConfig(configPath, runningConfig)) {
                safeToWriteToConfigFile = true;
            }
        } else {
            runningConfig = new CoordinateOffsetConfigBase();
            safeToWriteToConfigFile = true;
            loadFullConfig();
        }
    }

    /** @return true if the config was successfully loaded and validated, false if errors occurred. */
    public boolean loadFullConfig() {
        Path configPath = core.getAdapter().getConfigDir().resolve("config.yml");

        if (configPath.toFile().exists()) {
            try {
                CoordinateOffsetConfigFull c = YamlConfigurations.load(configPath, CoordinateOffsetConfigFull.class, properties);
                if (!c.validateBaseAndFullConfig()) {
                    return false;
                }
                runningConfig = c;
            } catch (Exception e) {
                e.printStackTrace();
                return false;
            }
        } else {
            runningConfig = new CoordinateOffsetConfigFull();
        }

        if (safeToWriteToConfigFile) {
            runningConfig.configVersion = ConfigVersion.CURRENT;
            YamlConfigurations.save(configPath, CoordinateOffsetConfigFull.class, (CoordinateOffsetConfigFull) runningConfig, properties);
        }

        return true;
    }

    public boolean reload() {
        boolean success = false;
        try {
            loadMessagesConfig();
            success = loadFullConfig();
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (success) {
            CoordinateOffsetCore.get().getLogger().info("Config reloaded.");
        } else {
            CoordinateOffsetCore.get().getLogger().warning("Failed to reload config. Running configuration has not been changed.");
        }
        return success;
    }
}

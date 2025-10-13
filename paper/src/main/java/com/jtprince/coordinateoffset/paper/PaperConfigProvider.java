package com.jtprince.coordinateoffset.paper;

import com.jtprince.coordinateoffset.config.CoordinateOffsetConfigImpl;
import de.exlll.configlib.ConfigLib;
import de.exlll.configlib.YamlConfigurationProperties;
import de.exlll.configlib.YamlConfigurations;
import org.jspecify.annotations.NullMarked;

@NullMarked
public class PaperConfigProvider {
    static YamlConfigurationProperties properties =
        ConfigLib.BUKKIT_DEFAULT_PROPERTIES.toBuilder()
            // TODO: Add version to comment, mention comments being overwritten on run
            .header("""
                    CoordinateOffset Configuration File
                    See https://github.com/joshuaprince/CoordinateOffset/wiki/Configuration-Guide
                    """)
            .build();

    private final CoordinateOffsetPaperPlugin plugin;
    private CoordinateOffsetConfigImpl currentConfig;

    public PaperConfigProvider(CoordinateOffsetPaperPlugin plugin) {
        this.plugin = plugin;
        loadConfig();
    }

    public CoordinateOffsetConfigImpl get() {
        return currentConfig;
    }

    private void loadConfig() {
        currentConfig = YamlConfigurations.update(
            plugin.getDataFolder().toPath().resolve("config-new.yml"), // TODO
            CoordinateOffsetConfigImpl.class,
            properties
        );
    }

    public void reload() {
        loadConfig();
    }
}

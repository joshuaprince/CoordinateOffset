package com.jtprince.coordinateoffset.paper;

import de.exlll.configlib.YamlConfigurations;
import org.jspecify.annotations.NullMarked;

@NullMarked
public class PaperConfigProvider {
    private final CoordinateOffsetPaperPlugin plugin;
    private PaperConfig currentConfig;

    public PaperConfigProvider(CoordinateOffsetPaperPlugin plugin) {
        this.plugin = plugin;
        loadConfig();
    }

    public PaperConfig get() {
        return currentConfig;
    }

    private void loadConfig() {
        currentConfig = YamlConfigurations.update(
            plugin.getDataFolder().toPath().resolve("config-new.yml"), // TODO
            PaperConfig.class,
            PaperConfig.properties
        );
    }

    public void reload() {
        loadConfig();
    }
}

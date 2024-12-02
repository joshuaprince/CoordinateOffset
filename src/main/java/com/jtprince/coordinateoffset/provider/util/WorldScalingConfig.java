package com.jtprince.coordinateoffset.provider.util;

import com.jtprince.coordinateoffset.Offset;
import org.bukkit.configuration.ConfigurationSection;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;

public class WorldScalingConfig {
    private final @NotNull Map<String, Double> scalesPerWorld = new HashMap<>();

    private WorldScalingConfig() {}

    public static WorldScalingConfig EMPTY = new WorldScalingConfig();

    public @NotNull Offset scale(@NotNull Offset offset, @NotNull String worldName) {
        Double scaling = scalesPerWorld.get(worldName);
        if (scaling != null) {
            return offset.scaleByDouble(scaling);
        }
        return offset;
    }

    public static WorldScalingConfig fromConfig(ConfigurationSection configSection) {
        WorldScalingConfig config = new WorldScalingConfig();

        for (Map.Entry<String, Object> entry : configSection.getValues(false).entrySet()) {
            if (entry.getValue() instanceof Number n) {
                config.scalesPerWorld.put(entry.getKey(), n.doubleValue());
            } else {
                throw new IllegalArgumentException(configSection.getCurrentPath() + "\": World scaling for world \"" + entry.getKey() + "\" must be a number!");
            }
        }

        return config;
    }
}

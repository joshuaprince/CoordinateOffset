package com.jtprince.coordinateoffset.provider;

import com.jtprince.coordinateoffset.CoordinateOffset;
import com.jtprince.coordinateoffset.Offset;
import com.jtprince.coordinateoffset.OffsetProvider;
import com.jtprince.coordinateoffset.OffsetProviderContext;
import org.bukkit.configuration.ConfigurationSection;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class ConstantOffsetProvider extends OffsetProvider {
    final Offset offset;
    final Map<String, Double> worldScaling;

    private ConstantOffsetProvider(String name, Offset offset, Map<String, Double> worldScaling) {
        super(name);
        this.offset = offset;
        this.worldScaling = worldScaling;
    }

    @Override
    public @NotNull Offset getOffset(@NotNull OffsetProviderContext context) {
        Double scaling = worldScaling.get(context.world().getName());
        if (scaling != null) {
            return offset.scaleByDouble(scaling);
        }
        return offset;
    }

    public static class ConfigFactory implements OffsetProvider.ConfigurationFactory<ConstantOffsetProvider> {
        @Override
        public @NotNull ConstantOffsetProvider createProvider(
                @NotNull String name, @NotNull CoordinateOffset plugin, @NotNull ConfigurationSection providerConfig
        ) throws IllegalArgumentException {
            if (!providerConfig.isInt("offsetX")) {
                throw new IllegalArgumentException("Missing field offsetX for ConstantOffsetProvider.");
            }
            if (!providerConfig.isInt("offsetZ")) {
                throw new IllegalArgumentException("Missing field offsetZ for ConstantOffsetProvider.");
            }

            int offsetX = providerConfig.getInt("offsetX");
            int offsetZ = providerConfig.getInt("offsetZ");

            if (offsetX > OffsetProvider.OFFSET_MAX) {
                throw new IllegalArgumentException("Provider \"" + name + "\": offsetX is too large! (Max 30M)");
            }
            if (offsetZ > OffsetProvider.OFFSET_MAX) {
                throw new IllegalArgumentException("Provider \"" + name + "\": offsetZ is too large! (Max 30M)");
            }

            Map<String, Double> worldScaling = new HashMap<>();
            if (providerConfig.isConfigurationSection("worldScaling")) {
                var section = Objects.requireNonNull(providerConfig.getConfigurationSection("worldScaling"));
                for (Map.Entry<String, Object> entry : section.getValues(false).entrySet()) {
                    if (entry.getValue() instanceof Number n) {
                        worldScaling.put(entry.getKey(), n.doubleValue());
                    } else {
                        throw new IllegalArgumentException("Provider \"" + name + "\": World scaling for world \"" + entry.getKey() + "\" must be a number!");
                    }
                }
            }

            return new ConstantOffsetProvider(name, new Offset(offsetX, offsetZ), worldScaling);
        }
    }
}

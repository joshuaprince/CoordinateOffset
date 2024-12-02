package com.jtprince.coordinateoffset.provider;

import com.jtprince.coordinateoffset.CoordinateOffset;
import com.jtprince.coordinateoffset.Offset;
import com.jtprince.coordinateoffset.OffsetProvider;
import com.jtprince.coordinateoffset.OffsetProviderContext;
import com.jtprince.coordinateoffset.provider.util.WorldScalingConfig;
import org.bukkit.configuration.ConfigurationSection;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

public class ConstantOffsetProvider extends OffsetProvider {
    final Offset offset;
    final @NotNull WorldScalingConfig worldScalingConfig;

    private ConstantOffsetProvider(String name, Offset offset, @NotNull WorldScalingConfig worldScalingConfig) {
        super(name);
        this.offset = offset;
        this.worldScalingConfig = worldScalingConfig;
    }

    @Override
    public @NotNull Offset getOffset(@NotNull OffsetProviderContext context) {
        return worldScalingConfig.scale(offset, context.world().getName());
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

            WorldScalingConfig worldScalingConfig;
            if (providerConfig.isConfigurationSection("worldScaling")) {
                var section = Objects.requireNonNull(providerConfig.getConfigurationSection("worldScaling"));
                worldScalingConfig = WorldScalingConfig.fromConfig(section);
            } else {
                worldScalingConfig = WorldScalingConfig.EMPTY;
            }

            return new ConstantOffsetProvider(name, new Offset(offsetX, offsetZ), worldScalingConfig);
        }
    }
}

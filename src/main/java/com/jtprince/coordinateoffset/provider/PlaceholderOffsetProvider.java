package com.jtprince.coordinateoffset.provider;

import com.jtprince.coordinateoffset.CoordinateOffset;
import com.jtprince.coordinateoffset.Offset;
import com.jtprince.coordinateoffset.OffsetProvider;
import com.jtprince.coordinateoffset.OffsetProviderContext;
import com.jtprince.coordinateoffset.provider.util.WorldScalingConfig;
import me.clip.placeholderapi.PlaceholderAPI;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class PlaceholderOffsetProvider extends OffsetProvider {
    private @Nullable String placeholderXZ;
    private @Nullable String placeholderOtherProvider;
    private @Nullable String fallbackProvider;
    private @NotNull WorldScalingConfig worldScalingConfig = WorldScalingConfig.EMPTY;

    private PlaceholderOffsetProvider(String name) {
        super(name);
    }

    @Override
    public @NotNull Offset getOffset(@NotNull OffsetProviderContext context) {
        Player player = context.player();

        if (placeholderXZ != null && !placeholderXZ.isBlank()) {
            String resolved = PlaceholderAPI.setPlaceholders(player, placeholderXZ).trim();

            if (PlaceholderAPI.containsPlaceholders(resolved)) {
                // Only a verbose info message because we'll try moving to the next placeholder resolution method.
                if (context.plugin().isVerboseLoggingEnabled()) {
                    context.plugin().getLogger().info("Could not resolve all placeholders for X/Z offset: " + resolved);
                }
            } else {
                Matcher matcher = Pattern.compile("(-?\\d+),(-?\\d+)").matcher(resolved);
                if (!matcher.matches()) {
                    context.plugin().getLogger().warning("Invalid placeholder X/Z for offset provider " + this.name + ": " + resolved);
                } else {
                    int x = Integer.parseInt(matcher.group(1));
                    int z = Integer.parseInt(matcher.group(2));

                    return worldScalingConfig.scale(new Offset(x, z), context.world().getName());
                }
            }
        }

        throw new IllegalArgumentException("Failed to resolve placeholders in an offset for " + context.player().getName());
    }

    public static class ConfigFactory implements OffsetProvider.ConfigurationFactory<PlaceholderOffsetProvider> {
        @Override
        public @NotNull PlaceholderOffsetProvider createProvider(
                @NotNull String name, @NotNull CoordinateOffset plugin, @NotNull ConfigurationSection providerConfig
        ) throws IllegalArgumentException {
            PlaceholderOffsetProvider p = new PlaceholderOffsetProvider(name);
            p.placeholderXZ = providerConfig.getString("placeholderXZ");
            p.placeholderOtherProvider = providerConfig.getString("placeholderOtherProvider");
            p.fallbackProvider = providerConfig.getString("fallbackProvider");

            if (providerConfig.isConfigurationSection("worldScaling")) {
                var section = Objects.requireNonNull(providerConfig.getConfigurationSection("worldScaling"));
                p.worldScalingConfig = WorldScalingConfig.fromConfig(section);
            }

            return p;
        }
    }
}

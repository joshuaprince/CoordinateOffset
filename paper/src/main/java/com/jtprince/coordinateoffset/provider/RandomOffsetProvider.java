package com.jtprince.coordinateoffset.provider;

import com.jtprince.coordinateoffset.CoordinateOffset;
import com.jtprince.coordinateoffset.Offset;
import com.jtprince.coordinateoffset.OffsetProvider;
import com.jtprince.coordinateoffset.OffsetProviderContext;
import com.jtprince.coordinateoffset.provider.util.PerWorldOffsetStore;
import com.jtprince.coordinateoffset.provider.util.ResetConfig;
import com.jtprince.coordinateoffset.provider.util.WorldAlignmentConfig;
import org.bukkit.NamespacedKey;
import org.bukkit.configuration.ConfigurationSection;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;

public class RandomOffsetProvider extends OffsetProvider {
    private static final String DEFAULT_PERSISTENCE_KEY = "default";

    private ResetConfig resetConfig;
    private int randomBound;
    private WorldAlignmentConfig worldAlignment;

    private final PerWorldOffsetStore perWorldOffsetStore;

    private RandomOffsetProvider(String name, @Nullable String persistenceKey, CoordinateOffset plugin) {
        super(name);
        if (persistenceKey == null) {
            this.perWorldOffsetStore = new PerWorldOffsetStore.Cached();
        } else {
            NamespacedKey key = new NamespacedKey(plugin, "random-persistence." + persistenceKey);
            this.perWorldOffsetStore = new PerWorldOffsetStore.Persistent(key);
        }
    }

    @Override
    public @NotNull Offset getOffset(@NotNull OffsetProviderContext context) {
        //noinspection DuplicatedCode (with ZeroAtLocationOffsetProvider)
        if (resetConfig.resetOn(context.reason())) {
            perWorldOffsetStore.reset(context.player().getUniqueId());
        }

        // Check if this world already has an offset calculated
        Offset offset = perWorldOffsetStore.get(context.player(), context.world().getName());
        if (offset != null) {
            return offset;
        }

        // Check if we need to align to an offset we already generated for this player in another world
        WorldAlignmentConfig.QueryResult alignment = worldAlignment.findAlignment(context.world().getName());
        if (alignment != null) {
            Offset alignedWorldOffset = perWorldOffsetStore.get(context.player(), alignment.targetWorldName());
            if (alignedWorldOffset != null) {
                offset = alignedWorldOffset.scale(alignment.rightShiftAmount());
                if (context.plugin().isVerboseLoggingEnabled()) {
                    String scaleStr;
                    if (alignment.rightShiftAmount() == 0) scaleStr = ".";
                    else if (alignment.rightShiftAmount() < 0) scaleStr = " (scaled up by " + (1 << -alignment.rightShiftAmount()) + ").";
                    else scaleStr = " (scaled down by " + (1 << alignment.rightShiftAmount()) + ").";
                    context.plugin().getLogger().info("Provider \"" + name + "\": Aligning new offset for world \"" +
                            context.world().getName() + "\" to existing " + alignedWorldOffset + " from world \"" +
                            alignment.targetWorldName() + "\"" + scaleStr);
                }
            }
        }

        // Generate a new offset if nothing else matched
        if (offset == null) {
            offset = Offset.random(randomBound);
        }

        perWorldOffsetStore.put(context.player(), context.world().getName(), offset);
        return offset;
    }

    @Override
    public void onPlayerDisconnect(@NotNull UUID playerUuid) {
        if (perWorldOffsetStore instanceof PerWorldOffsetStore.Cached) {
            perWorldOffsetStore.reset(playerUuid);
        }
    }

    public static class ConfigFactory implements OffsetProvider.ConfigurationFactory<RandomOffsetProvider> {
        @Override
        public @NotNull RandomOffsetProvider createProvider(
                @NotNull String name, @NotNull CoordinateOffset plugin, @NotNull ConfigurationSection providerConfig
        ) throws IllegalArgumentException {
            if (!providerConfig.isInt("randomBound")) {
                throw new IllegalArgumentException("Missing field randomBound for RandomOffsetProvider.");
            }

            boolean persistent = providerConfig.getBoolean("persistent");
            String persistenceKey = providerConfig.getString("persistenceKey", DEFAULT_PERSISTENCE_KEY);
            if (!persistent && !persistenceKey.equals(DEFAULT_PERSISTENCE_KEY)) {
                plugin.getLogger().warning("Provider \"" + name + "\": Defined a persistence key when persistence is disabled!");
            }

            RandomOffsetProvider p = new RandomOffsetProvider(name, persistent ? persistenceKey : null, plugin);
            p.resetConfig = ResetConfig.fromConfigSection(providerConfig);
            p.randomBound = providerConfig.getInt("randomBound");
            if (p.randomBound > OffsetProvider.OFFSET_MAX) {
                throw new IllegalArgumentException("Provider \"" + name + "\": randomBound is too large! (Max 30M)");
            }
            p.worldAlignment = WorldAlignmentConfig.fromConfig(providerConfig.getStringList("worldAlignment"));
            return p;
        }
    }

    public boolean isPersistent() {
        return perWorldOffsetStore instanceof PerWorldOffsetStore.Persistent;
    }

    public ResetConfig getResetConfig() {
        return resetConfig;
    }
}

package com.jtprince.coordinateoffset.provider;

import com.jtprince.coordinateoffset.CoordinateOffset;
import com.jtprince.coordinateoffset.Offset;
import com.jtprince.coordinateoffset.OffsetProvider;
import com.jtprince.coordinateoffset.OffsetProviderContext;
import com.jtprince.coordinateoffset.provider.util.PerWorldOffsetStore;
import com.jtprince.coordinateoffset.provider.util.ResetConfig;
import com.jtprince.coordinateoffset.provider.util.WorldAlignmentConfig;
import org.bukkit.Location;
import org.bukkit.configuration.ConfigurationSection;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;

public class ZeroAtLocationOffsetProvider extends OffsetProvider {
    private ResetConfig resetConfig;
    private WorldAlignmentConfig worldAlignment;

    private final PerWorldOffsetStore perWorldOffsetStore = new PerWorldOffsetStore.Cached();

    private ZeroAtLocationOffsetProvider(String name) {
        super(name);
    }

    @Override
    public @NotNull Offset getOffset(@NotNull OffsetProviderContext context) {
        //noinspection DuplicatedCode (with RandomOffsetProvider)
        if (resetConfig.resetOn(context.reason())) {
            perWorldOffsetStore.reset(context.player());
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
            }
        }

        // Generate a new offset if nothing else matched
        if (offset == null) {
            Location loc = context.playerLocation();
            int alignmentPower = worldAlignment.greatestPossibleRightShiftForWorld(context.world().getName());
            offset = Offset.align(loc.getBlockX(), loc.getBlockZ(), alignmentPower);
        }

        perWorldOffsetStore.put(context.player(), context.world().getName(), offset);
        return offset;
    }

    @Override
    public void onPlayerDisconnect(@NotNull UUID playerUuid) {
        perWorldOffsetStore.reset(playerUuid);
    }

    public static class ConfigFactory implements OffsetProvider.ConfigurationFactory<ZeroAtLocationOffsetProvider> {
        @Override
        public @NotNull ZeroAtLocationOffsetProvider createProvider(
                @NotNull String name, @NotNull CoordinateOffset plugin, @NotNull ConfigurationSection providerConfig
        ) throws IllegalArgumentException {
            ZeroAtLocationOffsetProvider p = new ZeroAtLocationOffsetProvider(name);
            p.resetConfig = ResetConfig.fromConfigSection(providerConfig);
            p.worldAlignment = WorldAlignmentConfig.fromConfig(providerConfig.getStringList("worldAlignment"));
            return p;
        }
    }

    public ResetConfig getResetConfig() {
        return resetConfig;
    }
}

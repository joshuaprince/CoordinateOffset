package com.jtprince.coordinateoffset.provider;

import com.jtprince.coordinateoffset.CoordinateOffsetCore;
import com.jtprince.coordinateoffset.Offset;
import com.jtprince.coordinateoffset.adapter.OffsetLocation;
import com.jtprince.coordinateoffset.provider.util.PerWorldOffsetStore;
import com.jtprince.coordinateoffset.provider.util.RegenerateConfig;
import com.jtprince.coordinateoffset.provider.util.WorldAlignmentConfig;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

import java.util.*;

@NullMarked
public final class ZeroAtLocationOffsetProvider extends CoreOffsetProvider {
    private final RegenerateConfig regenerateConfig;
    private final @Nullable WorldAlignmentConfig worldAlignmentConfig;

    private final PerWorldOffsetStore perWorldOffsetStore = new PerWorldOffsetStore.Cached();

    ZeroAtLocationOffsetProvider(
        String name,
        RegenerateConfig regenerateConfig,
        @Nullable WorldAlignmentConfig worldAlignmentConfig
    ) {
        super(name);
        this.regenerateConfig = regenerateConfig;
        this.worldAlignmentConfig = worldAlignmentConfig;
    }

    @Override
    public @Nullable Offset provideOffset(OffsetProviderContext context) {
        //noinspection DuplicatedCode (with RandomOffsetProvider)
        boolean willRegenerate = false;
        switch (context.reason()) {
            case JOIN -> {}
            case DEATH_RESPAWN -> { if (regenerateConfig.isRegenOnDeath()) willRegenerate = true; }
            case WORLD_CHANGE -> { if (regenerateConfig.isRegenOnWorldChange()) willRegenerate = true; }
            /* Always regenerate when explicitly called */
            case COMMAND_REGENERATE, PLUGIN_REGENERATE -> willRegenerate = true;
            case TELEPORT -> {
                Objects.requireNonNull(context.previousLocation());
                Double distanceTeleported = context.playerLocation().getDistance(context.previousLocation());
                Objects.requireNonNull(distanceTeleported);
                if (regenerateConfig.isRegenOnDistantTeleport(distanceTeleported)) {
                    willRegenerate = true;
                } else {
                    // Special case to avoid log spam: Returning null means "no offset change" with no log message
                    return null;
                }
            }
        }
        if (willRegenerate) {
            perWorldOffsetStore.reset(context.player());
        }

        // Check if this world already has an offset calculated
        Offset offset = perWorldOffsetStore.get(context.player(), context.worldName());
        if (offset != null) {
            return offset;
        }

        // Check if we need to align to an offset we already generated for this player in another world
        WorldAlignmentConfig.QueryResult alignment = null;
        if (worldAlignmentConfig != null) {
            alignment = worldAlignmentConfig.findAlignment(context.worldName());
        }
        if (alignment != null) {
            Offset alignedWorldOffset = perWorldOffsetStore.get(context.player(), alignment.targetWorldName());
            if (alignedWorldOffset != null) {
                offset = alignedWorldOffset.scale(alignment.rightShiftAmount());
                if (CoordinateOffsetCore.get().getConfig().getVerbose()) {
                    String scaleStr;
                    if (alignment.rightShiftAmount() == 0) scaleStr = ".";
                    else if (alignment.rightShiftAmount() < 0)
                        scaleStr = " (scaled up by " + (1 << -alignment.rightShiftAmount()) + ").";
                    else scaleStr = " (scaled down by " + (1 << alignment.rightShiftAmount()) + ").";
                    CoordinateOffsetCore.get().getLogger().info("Provider \"" + name + "\": Aligning new offset for world \"" +
                        context.worldName() + "\" to offset from world \"" + alignment.targetWorldName() + "\"" + scaleStr);
                }
            }
        }

        // Generate a new offset if nothing else matched
        if (offset == null) {
            OffsetLocation loc = context.playerLocation();
            int alignmentPower = 0;
            if (worldAlignmentConfig != null) {
                alignmentPower = worldAlignmentConfig.greatestPossibleRightShiftForWorld(context.worldName());
            }
            offset = Offset.align((int) loc.getX(), (int) loc.getZ(), alignmentPower);
        }

        perWorldOffsetStore.put(context.player(), context.worldName(), offset);
        return offset;
    }

    @Override
    public void onPlayerDisconnect(UUID playerUuid) {
        if (perWorldOffsetStore instanceof PerWorldOffsetStore.Cached) {
            perWorldOffsetStore.reset(playerUuid);
        }
    }

    @Override
    public SequencedMap<String, ?> serialize() {
        SequencedMap<String, Object> map = new LinkedHashMap<>();

        regenerateConfig.serializeTo(map);

        if (worldAlignmentConfig != null) {
            worldAlignmentConfig.serializeTo(map);
        }

        return map;
    }

    public static ZeroAtLocationOffsetProvider deserialize(OffsetProviderConfig config) throws IllegalArgumentException {
        SequencedMap<String, Object> s = config.getConfigSection();

        RegenerateConfig regenerateConfig = RegenerateConfig.deserialize(s); // nullable

        WorldAlignmentConfig worldAlignment = null;
        if (s.containsKey("worldAlignment")) {
            if (!(s.get("worldAlignment") instanceof List<?> worldAlignmentList)) {
                throw new IllegalArgumentException("Provider \"" + config.getUserDefinedProviderName() +
                    ": Field `worldAlignment` for ZeroAtLocationOffsetProvider is not a list.");
            }
            worldAlignment = WorldAlignmentConfig.deserialize(worldAlignmentList.stream().map(Object::toString).toList());
        }

        return new ZeroAtLocationOffsetProvider(config.getUserDefinedProviderName(), regenerateConfig, worldAlignment);
    }

    @Override
    public String getMetricsClassName() {
        return "ZeroAtLocationOffsetProvider";
    }

    @Override
    public String getMetricsDetails() {
        return "Reset " + regenerateConfig.getMetricsCharacterString();
    }
}

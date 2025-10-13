package com.jtprince.coordinateoffset.provider;

import com.jtprince.coordinateoffset.CoordinateOffset;
import com.jtprince.coordinateoffset.Offset;
import com.jtprince.coordinateoffset.OffsetProvider;
import com.jtprince.coordinateoffset.OffsetProviderContext;
import com.jtprince.coordinateoffset.adapter.OffsetLocation;
import com.jtprince.coordinateoffset.provider.util.PerWorldOffsetStore;
import com.jtprince.coordinateoffset.provider.util.ResetConfig;
import com.jtprince.coordinateoffset.provider.util.WorldAlignmentConfig;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

import java.util.*;

@NullMarked
public class ZeroAtLocationOffsetProvider extends OffsetProvider {
    @Nullable ResetConfig resetConfig;
    @Nullable WorldAlignmentConfig worldAlignmentConfig;

    private final PerWorldOffsetStore perWorldOffsetStore = new PerWorldOffsetStore.Cached();

    ZeroAtLocationOffsetProvider(String name) {
        super(name);
    }

    @Override
    public Offset provideOffset(OffsetProviderContext context) {
        //noinspection DuplicatedCode (with RandomOffsetProvider)
        if (resetConfig != null && resetConfig.resetOn(context.reason())) {
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
                if (CoordinateOffset.get().getConfig().getVerbose()) {
                    String scaleStr;
                    if (alignment.rightShiftAmount() == 0) scaleStr = ".";
                    else if (alignment.rightShiftAmount() < 0)
                        scaleStr = " (scaled up by " + (1 << -alignment.rightShiftAmount()) + ").";
                    else scaleStr = " (scaled down by " + (1 << alignment.rightShiftAmount()) + ").";
                    CoordinateOffset.get().getLogger().info("Provider \"" + name + "\": Aligning new offset for world \"" +
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

        if (resetConfig != null) {
            resetConfig.serializeTo(map);
        }

        if (worldAlignmentConfig != null) {
            worldAlignmentConfig.serializeTo(map);
        }

        return map;
    }

    public static class ConfigFactory implements ConfigurationFactory<ZeroAtLocationOffsetProvider> {
        @Override
        public ZeroAtLocationOffsetProvider deserialize(String name, Map<String, ?> element) throws IllegalArgumentException {
            ResetConfig resetConfig = ResetConfig.deserialize(element); // nullable

            WorldAlignmentConfig worldAlignment = null;
            if (element.containsKey("worldAlignment")) {
                if (!(element.get("worldAlignment") instanceof List<?> worldAlignmentList)) {
                    throw new IllegalArgumentException("Provider \"" + name + ": Field `worldAlignment` for ZeroAtLocationOffsetProvider is not a list.");
                }
                worldAlignment = WorldAlignmentConfig.deserialize(worldAlignmentList.stream().map(Object::toString).toList());
            }

            ZeroAtLocationOffsetProvider provider = new ZeroAtLocationOffsetProvider(name);
            provider.resetConfig = resetConfig;
            provider.worldAlignmentConfig = worldAlignment;
            return provider;
        }
    }
}

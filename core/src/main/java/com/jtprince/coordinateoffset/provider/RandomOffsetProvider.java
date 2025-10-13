package com.jtprince.coordinateoffset.provider;

import com.jtprince.coordinateoffset.CoordinateOffset;
import com.jtprince.coordinateoffset.Offset;
import com.jtprince.coordinateoffset.OffsetProvider;
import com.jtprince.coordinateoffset.OffsetProviderContext;
import com.jtprince.coordinateoffset.provider.util.PerWorldOffsetStore;
import com.jtprince.coordinateoffset.provider.util.PlayerOffsetPersistence;
import com.jtprince.coordinateoffset.provider.util.ResetConfig;
import com.jtprince.coordinateoffset.provider.util.WorldAlignmentConfig;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

import java.util.*;

@NullMarked
public class RandomOffsetProvider extends OffsetProvider {
    public static final String PERSISTENCE_KEY_CLASS_KEY = "random-persistence";
    public static final String DEFAULT_PERSISTENCE_KEY = "default";

    final int randomBound;

    @Nullable ResetConfig resetConfig;
    @Nullable WorldAlignmentConfig worldAlignmentConfig;
    @Nullable Boolean isPersistentConfig;
    @Nullable String persistenceKeyConfig;

    private final PerWorldOffsetStore perWorldOffsetStore;

    RandomOffsetProvider(String name, int randomBound, PlayerOffsetPersistence.@Nullable Key persistenceKey) {
        super(name);
        this.randomBound = randomBound;
        if (persistenceKey != null) {
            this.perWorldOffsetStore = new PerWorldOffsetStore.Persistent(persistenceKey);
        } else {
            this.perWorldOffsetStore = new PerWorldOffsetStore.Cached();
        }
    }

    @Override
    public Offset provideOffset(OffsetProviderContext context) {
        //noinspection DuplicatedCode (with ZeroAtLocationOffsetProvider)
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
            offset = Offset.random(randomBound);
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

        map.put("randomBound", (long) randomBound);

        if (resetConfig != null) {
            resetConfig.serializeTo(map);
        }

        if (isPersistentConfig != null) {
            map.put("persistent", isPersistentConfig);
        }
        if (persistenceKeyConfig != null) {
            map.put("persistenceKey", persistenceKeyConfig);
        }

        if (worldAlignmentConfig != null) {
            worldAlignmentConfig.serializeTo(map);
        }

        return map;
    }

    public static class ConfigFactory implements ConfigurationFactory<RandomOffsetProvider> {
        @Override
        public RandomOffsetProvider deserialize(String name, Map<String, ?> element) throws IllegalArgumentException {
            if (!element.containsKey("randomBound") || !(element.get("randomBound") instanceof Number randomBoundNum)) {
                throw new IllegalArgumentException("Provider \"" + name + ": Required key `randomBound` for RandomOffsetProvider is missing or invalid.");
            }
            int randomBound = randomBoundNum.intValue();

            ResetConfig resetConfig = ResetConfig.deserialize(element); // nullable

            Boolean persistent = null;
            if (element.containsKey("persistent")) {
                if (!(element.get("persistent") instanceof Boolean)) {
                    throw new IllegalArgumentException("Provider \"" + name + ": Field `persistent` for RandomOffsetProvider is not a boolean.");
                }
                persistent = (Boolean) element.get("persistent");
            }
            String persistenceKeyConfig = null;
            if (element.containsKey("persistenceKey")) {
                persistenceKeyConfig = element.get("persistenceKey").toString();
            }
            PlayerOffsetPersistence.Key persistenceKey = null;
            if (persistent != null && persistent) {
                persistenceKey = new PlayerOffsetPersistence.Key(
                    PERSISTENCE_KEY_CLASS_KEY,
                    persistenceKeyConfig != null ? persistenceKeyConfig : DEFAULT_PERSISTENCE_KEY
                );
            }

            WorldAlignmentConfig worldAlignment = null;
            if (element.containsKey("worldAlignment")) {
                if (!(element.get("worldAlignment") instanceof List<?> worldAlignmentList)) {
                    throw new IllegalArgumentException("Provider \"" + name + ": Field `worldAlignment` for RandomOffsetProvider is not a list.");
                }
                worldAlignment = WorldAlignmentConfig.deserialize(worldAlignmentList.stream().map(Object::toString).toList());
            }

            RandomOffsetProvider provider = new RandomOffsetProvider(name, randomBound, persistenceKey);
            provider.resetConfig = resetConfig;
            provider.isPersistentConfig = persistent;
            provider.persistenceKeyConfig = persistenceKeyConfig;
            provider.worldAlignmentConfig = worldAlignment;
            return provider;
        }
    }
}

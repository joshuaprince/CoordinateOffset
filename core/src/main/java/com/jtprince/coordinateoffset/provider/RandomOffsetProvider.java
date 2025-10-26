package com.jtprince.coordinateoffset.provider;

import com.jtprince.coordinateoffset.CoordinateOffsetCore;
import com.jtprince.coordinateoffset.Offset;
import com.jtprince.coordinateoffset.provider.util.PerWorldOffsetStore;
import com.jtprince.coordinateoffset.provider.util.PlayerOffsetPersistence;
import com.jtprince.coordinateoffset.provider.util.RegenerateConfig;
import com.jtprince.coordinateoffset.provider.util.WorldAlignmentConfig;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

import java.util.*;

@NullMarked
public final class RandomOffsetProvider extends CoreOffsetProvider {
    public static final String PERSISTENCE_KEY_CLASS_KEY = "random-persistence";
    public static final String DEFAULT_PERSISTENCE_KEY = "default";

    private final int randomBound;
    private final RegenerateConfig regenerateConfig;
    private final @Nullable Boolean isPersistentConfig;
    private final @Nullable String persistenceKeyConfig;
    private final @Nullable WorldAlignmentConfig worldAlignmentConfig;

    private final PerWorldOffsetStore perWorldOffsetStore;

    RandomOffsetProvider(
        String name,
        int randomBound,
        RegenerateConfig regenerateConfig,
        @Nullable Boolean isPersistentConfig,
        @Nullable String persistenceKeyConfig,
        @Nullable WorldAlignmentConfig worldAlignmentConfig
    ) {
        super(name);
        this.randomBound = randomBound;
        this.regenerateConfig = regenerateConfig;
        this.isPersistentConfig = isPersistentConfig;
        this.persistenceKeyConfig = persistenceKeyConfig;
        this.worldAlignmentConfig = worldAlignmentConfig;

        if (isPersistentConfig != null && isPersistentConfig) {
            if (persistenceKeyConfig == null) {
                throw new IllegalArgumentException("Provider \"" + name +
                    ": Field `persistenceKey` for RandomOffsetProvider is required when `persistent` is true.");
            }
            this.perWorldOffsetStore = new PerWorldOffsetStore.Persistent(
                new PlayerOffsetPersistence.Key(PERSISTENCE_KEY_CLASS_KEY, persistenceKeyConfig));
        } else {
            this.perWorldOffsetStore = new PerWorldOffsetStore.Cached();
        }
    }

    @Override
    public @Nullable Offset provideOffset(OffsetProviderContext context) {
        //noinspection DuplicatedCode (with ZeroAtLocationOffsetProvider)
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
        Offset offset = perWorldOffsetStore.get(context.player(), context.playerLocation().getWorld().getName());
        if (offset != null) {
            return offset;
        }

        // Check if we need to align to an offset we already generated for this player in another world
        WorldAlignmentConfig.QueryResult alignment = null;
        if (worldAlignmentConfig != null) {
            alignment = worldAlignmentConfig.findAlignment(context.playerLocation().getWorld().getName());
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
                        context.playerLocation().getWorld().getName() + "\" to offset from world \"" + alignment.targetWorldName() + "\"" + scaleStr);
                }
            }
        }

        // Generate a new offset if nothing else matched
        if (offset == null) {
            offset = Offset.random(randomBound);
        }

        perWorldOffsetStore.put(context.player(), context.playerLocation().getWorld().getName(), offset);
        return offset;
    }

    @Override
    public void onPlayerDisconnect(UUID playerUuid) {
        if (perWorldOffsetStore instanceof PerWorldOffsetStore.Cached) {
            perWorldOffsetStore.reset(playerUuid);
        }
    }

    public boolean isPersistent() {
        return perWorldOffsetStore instanceof PerWorldOffsetStore.Persistent;
    }

    public @Nullable WorldAlignmentConfig getWorldAlignmentConfig() {
        return worldAlignmentConfig;
    }

    @Override
    public SequencedMap<String, ?> serialize() {
        SequencedMap<String, Object> map = new LinkedHashMap<>();

        map.put("randomBound", (long) randomBound);

        regenerateConfig.serializeTo(map);

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

    public static RandomOffsetProvider deserialize(OffsetProviderConfig config) throws IllegalArgumentException {
        SequencedMap<String, Object> s = config.getConfigSection();

        if (!s.containsKey("randomBound") || !(s.get("randomBound") instanceof Number randomBoundNum)) {
            throw new IllegalArgumentException("Provider \"" + config.getUserDefinedProviderName() +
                ": Required key `randomBound` for RandomOffsetProvider is missing or invalid.");
        }
        int randomBound = randomBoundNum.intValue();

        RegenerateConfig regenerateConfig = RegenerateConfig.deserialize(s);

        Boolean isPersistentConfig = null;
        if (s.containsKey("persistent")) {
            if (!(s.get("persistent") instanceof Boolean)) {
                throw new IllegalArgumentException("Provider \"" + config.getUserDefinedProviderName() +
                    ": Field `persistent` for RandomOffsetProvider is not a boolean.");
            }
            isPersistentConfig = (Boolean) s.get("persistent");
        }
        String persistenceKeyConfig = null;
        if (s.containsKey("persistenceKey")) {
            persistenceKeyConfig = s.get("persistenceKey").toString();
        } else if (isPersistentConfig != null && isPersistentConfig) {
            // Write default persistenceKey if persistence is enabled but the key isn't present in config
            persistenceKeyConfig = DEFAULT_PERSISTENCE_KEY;
        }

        WorldAlignmentConfig worldAlignment = null;
        if (s.containsKey("worldAlignment")) {
            if (!(s.get("worldAlignment") instanceof List<?> worldAlignmentList)) {
                throw new IllegalArgumentException("Provider \"" + config.getUserDefinedProviderName() +
                    ": Field `worldAlignment` for RandomOffsetProvider is not a list.");
            }
            worldAlignment = WorldAlignmentConfig.deserialize(worldAlignmentList.stream().map(Object::toString).toList());
        }

        return new RandomOffsetProvider(
            config.getUserDefinedProviderName(),
            randomBound,
            regenerateConfig,
            isPersistentConfig,
            persistenceKeyConfig,
            worldAlignment
        );
    }

    @Override
    public String getMetricsClassName() {
        return "RandomOffsetProvider";
    }

    @Override
    public String getMetricsDetails() {
        return ((isPersistentConfig != null && isPersistentConfig) ? "Persistent" : "Not Persistent")
            + " | Reset " + regenerateConfig.getMetricsCharacterString();
    }
}

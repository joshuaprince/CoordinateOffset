package com.jtprince.coordinateoffset.provider;

import com.jtprince.coordinateoffset.CoordinateOffsetCore;
import com.jtprince.coordinateoffset.Offset;
import com.jtprince.coordinateoffset.provider.util.CoordinateScaleUtils;
import com.jtprince.coordinateoffset.provider.util.PlayerOffsetPersistence;
import com.jtprince.coordinateoffset.provider.util.ProviderOffsetStore;
import com.jtprince.coordinateoffset.provider.util.RegenerateConfig;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

import java.util.LinkedHashMap;
import java.util.Objects;
import java.util.SequencedMap;
import java.util.UUID;

@NullMarked
public final class RandomOffsetProvider extends CoreOffsetProvider {
    public static final String PERSISTENCE_KEY_CLASS_KEY = "provider.persistence";
    public static final String DEFAULT_PERSISTENCE_KEY = "default";

    private final int randomBound;
    private final RegenerateConfig regenerateConfig;
    private final @Nullable Boolean isPersistentConfig;
    private final @Nullable String persistenceKeyConfig;

    private final ProviderOffsetStore offsetStore;

    RandomOffsetProvider(
        String name,
        int randomBound,
        RegenerateConfig regenerateConfig,
        @Nullable Boolean isPersistentConfig,
        @Nullable String persistenceKeyConfig
    ) {
        super(name);
        this.randomBound = randomBound;
        this.regenerateConfig = regenerateConfig;
        this.isPersistentConfig = isPersistentConfig;
        this.persistenceKeyConfig = persistenceKeyConfig;

        if (isPersistentConfig != null && isPersistentConfig) {
            if (persistenceKeyConfig == null) {
                throw new IllegalArgumentException("Provider \"" + name +
                    ": Field `persistenceKey` for RandomOffsetProvider is required when `persistent` is true.");
            }
            this.offsetStore = new ProviderOffsetStore.Persistent(
                CoordinateOffsetCore.get().getAdapter().getPlayerOffsetPersistence(),
                new PlayerOffsetPersistence.Key(PERSISTENCE_KEY_CLASS_KEY, persistenceKeyConfig));
        } else {
            this.offsetStore = new ProviderOffsetStore.Cached();
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
            offsetStore.clear(context.player());
        }

        // Check if the provider already has an offset calculated that was not cleared for a regenerate
        Offset offset = offsetStore.get(context.player());
        boolean isReusedOffset = true;
        if (offset == null) {
            // Generate a new offset if we don't already have one for this player
            offset = Offset.random(randomBound);
            offsetStore.put(context.player(), offset);
            isReusedOffset = false;
        }

        return CoordinateScaleUtils.scaleVerbosely(
            offset,
            context.playerLocation().getWorld(),
            this,
            isReusedOffset ? "stored" : "new"
        );
    }

    @Override
    public void onPlayerDisconnect(UUID playerUuid) {
        if (offsetStore instanceof ProviderOffsetStore.Cached) {
            offsetStore.clear(playerUuid);
        }
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

        return new RandomOffsetProvider(
            config.getUserDefinedProviderName(),
            randomBound,
            regenerateConfig,
            isPersistentConfig,
            persistenceKeyConfig
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

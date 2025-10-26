package com.jtprince.coordinateoffset.provider;

import com.jtprince.coordinateoffset.Offset;
import com.jtprince.coordinateoffset.provider.util.CoordinateScaleUtils;
import com.jtprince.coordinateoffset.provider.util.ProviderOffsetStore;
import com.jtprince.coordinateoffset.provider.util.RegenerateConfig;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

import java.util.LinkedHashMap;
import java.util.Objects;
import java.util.SequencedMap;
import java.util.UUID;

@NullMarked
public final class ZeroAtLocationOffsetProvider extends CoreOffsetProvider {
    private final RegenerateConfig regenerateConfig;

    private final ProviderOffsetStore.Cached offsetStore = new ProviderOffsetStore.Cached();

    ZeroAtLocationOffsetProvider(String name, RegenerateConfig regenerateConfig) {
        super(name);
        this.regenerateConfig = regenerateConfig;
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
            offsetStore.clear(context.player());
        }

        // Check if the provider already has an offset calculated that was not cleared for a regenerate
        Offset offset = offsetStore.get(context.player());
        double coordinateScale = context.playerLocation().getWorld().getCoordinateScale(); // 8 for nether e.g.
        boolean isReusedOffset = true;
        if (offset == null) {
            // Generate a new offset if we don't already have one for this player
            offset = Offset.align(
                (int) (context.playerLocation().getX() * coordinateScale),
                (int) (context.playerLocation().getZ() * coordinateScale)
            );
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
        offsetStore.clear(playerUuid);
    }

    @Override
    public SequencedMap<String, ?> serialize() {
        SequencedMap<String, Object> map = new LinkedHashMap<>();

        regenerateConfig.serializeTo(map);

        return map;
    }

    public static ZeroAtLocationOffsetProvider deserialize(OffsetProviderConfig config) throws IllegalArgumentException {
        SequencedMap<String, Object> s = config.getConfigSection();

        RegenerateConfig regenerateConfig = RegenerateConfig.deserialize(s); // nullable

        return new ZeroAtLocationOffsetProvider(config.getUserDefinedProviderName(), regenerateConfig);
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

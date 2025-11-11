package com.jtprince.coordinateoffset.provider;

import com.jtprince.coordinateoffset.CoordinateOffsetCore;
import com.jtprince.coordinateoffset.Offset;
import com.jtprince.coordinateoffset.ScalableOffset;
import com.jtprince.coordinateoffset.adapter.OffsetPlayer;
import com.jtprince.coordinateoffset.command.OffsetSetCommand;
import com.jtprince.coordinateoffset.provider.util.ProviderOffsetStore;
import com.jtprince.coordinateoffset.provider.util.RegenerateConfig;
import org.jspecify.annotations.NullMarked;

import java.util.LinkedHashMap;
import java.util.Objects;
import java.util.SequencedMap;

@NullMarked
public final class ZeroAtLocationOffsetProvider extends CoreOffsetProvider {
    private final RegenerateConfig regenerateConfig;
    private final ProviderOffsetStore offsetStore;

    ZeroAtLocationOffsetProvider(String name, RegenerateConfig regenerateConfig) {
        super(name);
        this.regenerateConfig = regenerateConfig;
        this.offsetStore = new ProviderOffsetStore(
            CoordinateOffsetCore.get().getAdapter().getPersistenceAdapter(),
            name,
            regenerateConfig.persistenceKeyOverride()
        );
    }

    @Override
    public Offset provideOffset(OffsetProviderContext context) {
        //noinspection DuplicatedCode (with RandomOffsetProvider)
        boolean willRegenerate = switch (context.reason()) {
            case JOIN -> regenerateConfig.isRegenOnJoin();
            case DEATH_RESPAWN -> regenerateConfig.isRegenOnDeath();
            case WORLD_CHANGE -> regenerateConfig.isRegenOnWorldChange();
            case COMMAND_REGENERATE, PLUGIN_REGENERATE -> true; /* Always regenerate when explicitly called */
            case TELEPORT -> {
                Objects.requireNonNull(context.previousLocation());
                Double distanceTeleported = context.playerLocation().getDistance(context.previousLocation());
                Objects.requireNonNull(distanceTeleported);
                yield regenerateConfig.isRegenOnDistantTeleport(distanceTeleported);
            }
            case COMMAND_SET, PLUGIN_SET -> false; /* Should be unreachable - offset providers are not called for this reason */
        };
        if (willRegenerate) {
            offsetStore.clear(context.player().getUuid());
        }

        // Check if the provider already has an offset calculated that was not cleared for a regenerate
        ScalableOffset offset = offsetStore.get(context.player());
        double coordinateScale = context.playerLocation().getWorld().getCoordinateScale(); // 8 for nether e.g.
        if (offset == null) {
            // Generate a new offset if we don't already have one for this player
            offset = Offset.align(
                (int) (context.playerLocation().getX() * coordinateScale),
                (int) (context.playerLocation().getZ() * coordinateScale)
            );
            offsetStore.put(context.player(), offset);
        }

        return offset;
    }

    @Override
    public void onOffsetSetByCommand(OffsetSetCommand command, OffsetPlayer target) {
        if (CoordinateOffsetCore.get().getConfig().getVerbose()) {
            CoordinateOffsetCore.get().getLogger().info("Provider \"" + name + "\": Updating offset " +
                "for " + target.getName() + " to " + command.getOffset());
        }
        offsetStore.put(target, command.getOffset());
    }


    @Override
    public SequencedMap<String, ?> serialize() {
        SequencedMap<String, Object> map = new LinkedHashMap<>();

        regenerateConfig.serializeTo(map);

        return map;
    }

    public static ZeroAtLocationOffsetProvider deserialize(OffsetProviderConfig config) throws IllegalArgumentException {
        SequencedMap<String, Object> s = config.getConfigSection();

        RegenerateConfig regenerateConfig = RegenerateConfig.deserialize(config.getUserDefinedProviderName(), s);

        return new ZeroAtLocationOffsetProvider(config.getUserDefinedProviderName(), regenerateConfig);
    }

    @Override
    public String getMetricsClassName() {
        return "ZeroAtLocationOffsetProvider";
    }

    @Override
    public String getMetricsDetails() {
        return "Regenerate on " + regenerateConfig.getMetricsCharacterString();
    }
}

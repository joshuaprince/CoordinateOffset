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
public final class RandomOffsetProvider extends CoreOffsetProvider {
    private final int randomBound;
    private final RegenerateConfig regenerateConfig;
    private final ProviderOffsetStore offsetStore;

    RandomOffsetProvider(
        String name,
        int randomBound,
        RegenerateConfig regenerateConfig
    ) {
        super(name);
        this.randomBound = randomBound;
        this.regenerateConfig = regenerateConfig;
        this.offsetStore = new ProviderOffsetStore(
            CoordinateOffsetCore.get().getAdapter().getPersistenceAdapter(),
            name,
            regenerateConfig.persistenceKeyOverride()
        );
    }

    @Override
    public Offset provideOffset(OffsetProviderContext context) {
        //noinspection DuplicatedCode (with ZeroAtLocationOffsetProvider)
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
        if (offset == null) {
            // Generate a new offset if we don't already have one for this player
            offset = Offset.random(randomBound);
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

        map.put("randomBound", (long) randomBound);

        regenerateConfig.serializeTo(map);

        return map;
    }

    public static RandomOffsetProvider deserialize(OffsetProviderConfig config) throws IllegalArgumentException {
        SequencedMap<String, Object> s = config.getConfigSection();

        if (!s.containsKey("randomBound") || !(s.get("randomBound") instanceof Number randomBoundNum)) {
            throw new IllegalArgumentException("Provider \"" + config.getUserDefinedProviderName() +
                ": Required key randomBound for RandomOffsetProvider is missing or invalid.");
        }
        int randomBound = randomBoundNum.intValue();

        RegenerateConfig regenerateConfig = RegenerateConfig.deserialize(config.getUserDefinedProviderName(), s);

        return new RandomOffsetProvider(
            config.getUserDefinedProviderName(),
            randomBound,
            regenerateConfig
        );
    }

    @Override
    public String getMetricsClassName() {
        return "RandomOffsetProvider";
    }

    @Override
    public String getMetricsDetails() {
        return "Regenerate on " + regenerateConfig.getMetricsCharacterString();
    }
}

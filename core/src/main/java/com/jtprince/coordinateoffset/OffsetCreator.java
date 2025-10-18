package com.jtprince.coordinateoffset;

import com.jtprince.coordinateoffset.config.OffsetProviderOverrideConfig;
import com.jtprince.coordinateoffset.provider.OffsetProvider;
import com.jtprince.coordinateoffset.provider.OffsetProviderContext;
import org.jspecify.annotations.NullMarked;

import java.util.Optional;

@NullMarked
class OffsetCreator {
    private final CoordinateOffsetCore core;

    OffsetCreator(CoordinateOffsetCore core) {
        this.core = core;
    }

    Offset createOffset(OffsetProviderContext context) {
        OffsetProvider provider = null;
        ProviderSource providerSource = null;

        Offset savedOffsetInWorld = core.getOffsetHolder().getSavedWorldOffset(context.player(), context.worldName());

        // Priority 0: Permission-based bypass
        if (core.getConfig().getBypassByPermission() &&
                context.player().hasPermission(CoordinateOffsetPermission.BYPASS.node)) {
            if (core.getConfig().getVerbose()) {
                core.getLogger().info("Bypassing offset with permission for player " + context.player().getName() + ".");
            }
            return Offset.ZERO;
        }

        // Priority 1: Config override rule
        //noinspection ConstantValue
        if (provider == null) {
            Optional<OffsetProviderOverrideConfig> appliedOverride =
                core.getProviderConfig().getOffsetProviderOverrides().stream()
                    .filter(o -> providerOverrideAppliesTo(context, o)).findFirst();
            if (appliedOverride.isPresent()) {
                provider = appliedOverride.get().getOffsetProvider();
                providerSource = ProviderSource.OVERRIDE;
            }
        }

        // Priority 2: Default provider
        if (provider == null) {
            provider = core.getProviderConfig().getDefaultOffsetProviderConfig();
            providerSource = ProviderSource.DEFAULT;
        }

        // With provider selected, get the offset.
        Offset offset = provider.provideOffset(context);
        if (core.getConfig().getVerbose()) {
            String usingOrReusing;
            if (offset.equals(savedOffsetInWorld)) {
                usingOrReusing = "Reusing";
            } else {
                usingOrReusing = "Using";
            }

            String reasonStr = null;
            switch (context.reason()) {
                case JOIN -> reasonStr = "player joined";
                case DEATH_RESPAWN -> reasonStr = "player respawned";
                case WORLD_CHANGE -> reasonStr = "player changed worlds";
                case DISTANT_TELEPORT -> reasonStr = "player teleported";
            }

            String sourceStr = null;
            switch (providerSource) {
                case DEFAULT -> sourceStr = "default provider";
                case OVERRIDE -> sourceStr = "config.yml override";
            }

            core.getLogger().info(
                usingOrReusing + " " + offset + " from provider \"" + provider.name + "\" (" + sourceStr + ") " +
                        "for player " + context.player().getName() + " in world \"" + context.worldName() +
                        "\" (" + reasonStr + ").");
        }
        return offset;
    }

    enum ProviderSource {
        DEFAULT, OVERRIDE
    }

    private boolean providerOverrideAppliesTo(OffsetProviderContext context, OffsetProviderOverrideConfig override) {
        if (override.getPlayerUuid() != null && !override.getPlayerUuid().equals(context.player().getUuid())) return false;
        if (override.getWorld() != null && !override.getWorld().equals(context.worldName())) return false;
        if (override.getPermission() != null && !context.player().hasPermission(override.getPermission())) return false;

        return true;
    }
}

package com.jtprince.coordinateoffset;

import com.jtprince.coordinateoffset.config.OffsetProviderOverrideConfig;
import com.jtprince.coordinateoffset.provider.OffsetProvider;
import com.jtprince.coordinateoffset.provider.OffsetProviderContext;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

import java.util.Optional;

@NullMarked
public class OffsetCreator {
    private final CoordinateOffsetCore core;

    OffsetCreator(CoordinateOffsetCore core) {
        this.core = core;
    }

    /**
     * Select and query an offset provider to generate a new offset for a player.
     *
     * @param context Context containing player, world, location, reason for offset change, etc.
     * @return A new offset to apply (which may be the same as the current offset), or null if the player's offset
     * should remain the same as it is now.
     */
    @Nullable CreatedOffset createOffset(OffsetProviderContext context) {
        OffsetProvider provider = null;
        boolean providerIsOverride = false;

        // Priority 0: Permission-based bypass
        if (core.getConfig().getBypassByPermission() &&
                context.player().hasPermission(CoordinateOffsetPermission.BYPASS.node)) {
            return new CreatedOffset(
                Offset.ZERO,
                new CreatedOffset.Source.PermissionBypass(CoordinateOffsetPermission.BYPASS),
                context.player(), context.playerLocation().getWorld(), context.reason());
        }

        // Priority 1: Config override rule
        //noinspection ConstantValue
        if (provider == null) {
            Optional<OffsetProviderOverrideConfig> appliedOverride =
                core.getProviderConfig().getOffsetProviderOverrides().stream()
                    .filter(o -> providerOverrideAppliesTo(context, o)).findFirst();
            if (appliedOverride.isPresent()) {
                provider = appliedOverride.get().getOffsetProvider();
                providerIsOverride = true;
            }
        }

        // Priority 2: Default provider
        if (provider == null) {
            provider = core.getProviderConfig().getDefaultOffsetProviderConfig();
        }

        // With provider selected, get the offset.
        Offset offset = provider.provideOffset(context);
        if (offset == null) {
            return null;
        }
        return new CreatedOffset(
            offset,
            new CreatedOffset.Source.Provider(provider, providerIsOverride),
            context.player(), context.playerLocation().getWorld(), context.reason());
    }

    private boolean providerOverrideAppliesTo(OffsetProviderContext context, OffsetProviderOverrideConfig override) {
        if (override.getPlayerUuid() != null && !override.getPlayerUuid().equals(context.player().getUuid())) return false;
        if (override.getWorld() != null
            && !override.getWorld().equals(context.playerLocation().getWorld().getName())
            && !override.getWorld().equals(context.playerLocation().getWorld().getKey())
            && !override.getWorld().equals(context.playerLocation().getWorld().getUuid().toString())) return false;
        if (override.getPermission() != null && !context.player().hasPermission(override.getPermission())) return false;

        return true;
    }
}

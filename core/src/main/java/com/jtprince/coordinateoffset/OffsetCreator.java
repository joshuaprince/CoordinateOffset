package com.jtprince.coordinateoffset;

import com.jtprince.coordinateoffset.config.OffsetProviderOverrideConfig;
import com.jtprince.coordinateoffset.provider.OffsetProvider;
import com.jtprince.coordinateoffset.provider.OffsetProviderContext;
import org.geysermc.geyser.api.GeyserApi;
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

        // Note for all Priority 0 checks: Be sure to update /offset commands which check for similar things.
        //  (e.g. /offset regenerate has a special response for bypasses by permissions or Bedrock)

        // Priority 0: Permission-based bypass
        if (core.getConfig().getBypassByPermission() &&
                context.player().hasPermission(CoordinateOffsetPermission.BYPASS.node)) {
            return new CreatedOffset(
                Offset.ZERO,
                new CreatedOffset.Source.PermissionBypass(CoordinateOffsetPermission.BYPASS),
                context);
        }

        // Priority 0: Geyser bypass
        try {
            if (GeyserApi.api().isBedrockPlayer(context.player().getUuid())) {
                /* Log a warning only once on join */
                if (context.reason() == OffsetProviderContext.ProvideReason.JOIN) {
                    core.getLogger().warning("Coordinate offsets are disabled for Bedrock player " +
                        context.player().getName() + ". (Give permission coordinateoffset.bypass to disable offsets " +
                        " and hide this warning)");
                }
                return new CreatedOffset(
                    Offset.ZERO,
                    new CreatedOffset.Source.BedrockBypass(),
                    context);
            }
        } catch (Exception ignored) {}

        // Priority 1: Config override rule
        //noinspection ConstantValue
        if (provider == null) {
            Optional<OffsetProviderOverrideConfig> appliedOverride =
                core.getProviderConfig().getOffsetProviderOverrides().stream()
                    .filter(o -> o.appliesTo(context)).findFirst();
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
            context);
    }
}

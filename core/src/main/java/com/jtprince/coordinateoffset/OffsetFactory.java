package com.jtprince.coordinateoffset;

import com.jtprince.coordinateoffset.adapter.OffsetPlayer;
import com.jtprince.coordinateoffset.config.OffsetProviderOverrideConfig;
import com.jtprince.coordinateoffset.provider.OffsetProvider;
import com.jtprince.coordinateoffset.provider.OffsetProviderContext;
import org.geysermc.geyser.api.GeyserApi;
import org.jspecify.annotations.NullMarked;

import java.util.Optional;

@NullMarked
public class OffsetFactory {
    private final CoordinateOffsetCore core;

    OffsetFactory(CoordinateOffsetCore core) {
        this.core = core;
    }

    /**
     * Select and query an offset provider to generate a new offset for a player.
     *
     * @param context Context containing player, world, location, reason for offset change, etc.
     * @return A new offset to apply (which may be the same as the current offset).
     */
    OffsetData createOffset(OffsetProviderContext context) {
        OffsetProvider provider = null;
        boolean providerIsOverride = false;

        // Note for all Priority 0 checks: Be sure to update /offset commands which check for similar things.
        //  (e.g. /offset regenerate has a special response for bypasses by permissions or Bedrock)

        // Priority 0: Permission-based bypass
        if (canBypassByPermission(context.player())) {
            return new OffsetData(
                Offset.ZERO,
                new OffsetData.Source.PermissionBypass(),
                context
            );
        }

        // Priority 0: Bedrock player/Geyser bypass
        if (isBedrockPlayerAndLogOnJoin(context)) {
            return new OffsetData(
                Offset.ZERO,
                new OffsetData.Source.BedrockBypass(),
                context
            );
        }

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
        return new OffsetData(
            offset,
            new OffsetData.Source.Provider(provider, providerIsOverride),
            context);
    }

    /**
     * Create a specific offset for a player, still checking certain critical conditions to ensure no offset is
     * generated in contexts that cannot receive an offset.
     * @param offset Offset to use.
     * @param source Source of the offset.
     * @param context Context to attach to the generated offset.
     * @return A new offset to apply (which may be the same as the current offset).
     */
    public OffsetData createSpecificOffset(Offset offset, OffsetData.Source source, OffsetProviderContext context) {
        // Priority 0: Bedrock player/Geyser bypass
        if (isBedrockPlayerAndLogOnJoin(context)) {
            return new OffsetData(
                Offset.ZERO,
                new OffsetData.Source.BedrockBypass(),
                context
            );
        }

        return new OffsetData(offset, source, context);
    }

    /**
     * Check if a player has permission to bypass standard offset generation. This also requires the plugin to be
     * configured to allow bypassing.
     *
     * @param player Player to check.
     * @return true if bypassing is allowed AND the player has permission to bypass offsets, false otherwise.
     */
    public static boolean canBypassByPermission(OffsetPlayer player) {
        return CoordinateOffsetCore.get().getConfig().getBypassByPermission() &&
            player.hasPermission(CoordinateOffsetPermission.BYPASS.node);
    }

    private boolean isBedrockPlayerAndLogOnJoin(OffsetProviderContext context) {
        try {
            if (GeyserApi.api().isBedrockPlayer(context.player().getUuid())) {
                /* Log a warning only once on join */
                if (context.reason() == OffsetProviderContext.ProvideReason.JOIN) {
                    core.getLogger().warning("Coordinate offsets are disabled for Bedrock player " +
                        context.player().getName() + ". (Give permission coordinateoffset.bypass to disable offsets " +
                        " and hide this warning)");
                }
                return true;
            }
        } catch (Exception ignored) {}
        return false;
    }
}

package com.jtprince.coordinateoffset;

import com.jtprince.coordinateoffset.adapter.OffsetPlayer;
import com.jtprince.coordinateoffset.config.OffsetProviderOverrideConfig;
import com.jtprince.coordinateoffset.provider.OffsetProvider;
import com.jtprince.coordinateoffset.provider.OffsetProviderContext;
import org.geysermc.geyser.api.GeyserApi;
import org.jspecify.annotations.NullMarked;

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
        Integer overrideRuleIndex = null;
        //noinspection ConstantValue
        if (provider == null) {
            for (int i = 0; i < core.getProviderConfig().getOffsetProviderOverrides().size(); i++) {
                OffsetProviderOverrideConfig override = core.getProviderConfig().getOffsetProviderOverrides().get(i);
                if (override.appliesTo(context)) {
                    overrideRuleIndex = i + 1; // 1-index for user readability
                    provider = override.getOffsetProvider();
                    break;
                }
            }
        }

        // Priority 2: Default provider
        if (provider == null) {
            provider = core.getProviderConfig().getDefaultOffsetProviderConfig();
        }

        // With provider selected, get the offset and scale it if needed.
        Offset offset = provider.provideOffset(context);
        FixedOffset fixed = fixOffsetVerbosely(offset, new OffsetData.Source.Provider(provider, overrideRuleIndex), context);
        return new OffsetData(fixed, new OffsetData.Source.Provider(provider, overrideRuleIndex), context);
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

        FixedOffset fixed = fixOffsetVerbosely(offset, source, context);
        return new OffsetData(fixed, source, context);
    }

    private FixedOffset fixOffsetVerbosely(Offset offset, OffsetData.Source source, OffsetProviderContext context) {
        return switch (offset) {
            case FixedOffset f -> f;
            case ScalableOffset s -> {
                double scale = context.playerLocation().getWorld().getCoordinateScale();

                if (offset.isZero() || scale == 1.0 || !core.getConfig().getVerbose()) yield s.scaleDownAndRound(scale);

                String prefix = switch (source) {
                    case OffsetData.Source.SetCommand cmd -> {
                        cmd.command().warnScaling(context.player(), scale);
                        yield "Command from " + cmd.command().getCommandSender().name() + ": ";
                    }
                    case OffsetData.Source.Provider provider -> "Provider \"" + provider.provider().name + "\": ";
                    case OffsetData.Source.BedrockBypass ignored -> "";
                    case OffsetData.Source.PermissionBypass ignored -> "";
                    case OffsetData.Source.PluginSet ignored -> "";
                };
                core.getLogger().info(prefix + "Scaling offset " + s + " by " + scale + " to match coordinate scale of world \"" + context.playerLocation().getWorld().getName() + "\"");

                yield s.scaleDownAndRound(scale);
            }
        };
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
                        "and hide this warning)");
                }
                return true;
            }
        } catch (NoClassDefFoundError ignored) {
            // Geyser not loaded - not an error
        } catch (Throwable t) {
            t.printStackTrace();
        }
        return false;
    }
}

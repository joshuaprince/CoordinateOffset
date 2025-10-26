package com.jtprince.coordinateoffset.config;

import com.jtprince.coordinateoffset.provider.OffsetProvider;
import com.jtprince.coordinateoffset.provider.OffsetProviderContext;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

/**
 * Container for a single line in the <code>offsetProviderOverrides</code> list in the CoordinateOffset configuration.
 *
 * <p>See
 * <a href="https://github.com/joshuaprince/CoordinateOffset/wiki/Configuration-Guide#applying-offset-providers">Applying Offset Providers</a>
 * for more information about how overrides work.</p>
 */
@NullMarked
public interface OffsetProviderOverrideConfig {
    /** The offset provider configuration to use when this override matches. */
    OffsetProvider getOffsetProvider();

    /** The name, key, or UUID of a world to match, or null to match any world. */
    @Nullable String getWorld();

    /** The permission node to match, or null to match any player. */
    @Nullable String getPermission();

    /** The name or UUID of a specific player to match, or null to match any player. */
    @Nullable String getPlayer();

    default boolean appliesTo(OffsetProviderContext context) {
        if (getPlayer() != null
            && !getPlayer().equalsIgnoreCase(context.player().getName())
            && !getPlayer().equalsIgnoreCase(context.player().getUuid().toString())) return false;
        if (getWorld() != null
            && !getWorld().equalsIgnoreCase(context.playerLocation().getWorld().getName())
            && !getWorld().equalsIgnoreCase(context.playerLocation().getWorld().getKey())
            && !getWorld().equalsIgnoreCase(context.playerLocation().getWorld().getUuid().toString())) return false;
        if (getPermission() != null && !context.player().hasPermission(getPermission())) return false;

        return true;
    }
}

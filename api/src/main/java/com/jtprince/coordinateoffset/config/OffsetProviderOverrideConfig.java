package com.jtprince.coordinateoffset.config;

import com.jtprince.coordinateoffset.provider.OffsetProvider;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

import java.util.UUID;

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

    /** The world name to match, or null to match any world. */
    @Nullable String getWorld();

    /** The permission node to match, or null to match any player. */
    @Nullable String getPermission();

    /** The UUID of a specific player to match, or null to match any player. */
    @Nullable UUID getPlayerUuid();
}

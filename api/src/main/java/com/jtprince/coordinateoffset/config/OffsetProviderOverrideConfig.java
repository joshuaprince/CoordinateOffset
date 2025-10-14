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
 *
 * @param provider   The offset provider configuration to use when this override matches.
 * @param world      The world name to match, or null to match any world.
 * @param permission The permission node to match, or null to match any player.
 * @param playerUuid The UUID of a specific player to match, or null to match any player.
 */
@NullMarked
public record OffsetProviderOverrideConfig(
    OffsetProvider provider,
    @Nullable String world,
    @Nullable String permission,
    @Nullable UUID playerUuid
) {}

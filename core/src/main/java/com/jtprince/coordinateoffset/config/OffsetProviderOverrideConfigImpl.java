package com.jtprince.coordinateoffset.config;

import com.jtprince.coordinateoffset.CoordinateOffsetCore;
import com.jtprince.coordinateoffset.provider.OffsetProvider;
import de.exlll.configlib.Configuration;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

import java.util.Objects;
import java.util.SequencedMap;
import java.util.UUID;

@NullMarked
@Configuration
public class OffsetProviderOverrideConfigImpl implements OffsetProviderOverrideConfig {
    private @Nullable String provider;
    @Override
    public OffsetProvider getOffsetProvider() {
        CoordinateOffsetProviderConfig providers = CoordinateOffsetCore.get().getProviderConfig();
        return Objects.requireNonNull(providers.getAllOffsetProviderConfigs().get(Objects.requireNonNull(provider)));
    }

    private @Nullable String world;
    @Override
    public @Nullable String getWorld() {
        return world;
    }

    private @Nullable String permission;
    @Override
    public @Nullable String getPermission() {
        return permission;
    }

    private @Nullable String player;
    @Override
    public @Nullable String getPlayer() {
        return player;
    }

    @Deprecated
    private @Nullable UUID playerUuid;

    /**
     * Validate that the override is valid. Logs warning messages for any errors found.
     *
     * @param allProviders All known offset providers mapped by user-defined name.
     * @param logWarning If true, logs a warning to the logger indicating why.
     *
     * @return true if the configuration is acceptable to use, false if the configuration is invalid and should be
     *         ignored.
     */
    public boolean validate(SequencedMap<String, OffsetProvider> allProviders, boolean logWarning) {
        if (playerUuid != null) {
            // Migrate old `playerUuid` field (UUID only) to new `player` field (supports player names and UUIDs)
            if (player == null) {
                player = playerUuid.toString();
            }
            playerUuid = null;
        }

        StringBuilder b = new StringBuilder();
        if (provider != null) b.append(" provider=").append(provider);
        if (world != null) b.append(" world=").append(world);
        if (permission != null) b.append(" permission=").append(permission);
        if (player != null) b.append(" player=").append(player);

        if (provider == null) {
            if (logWarning) {
                CoordinateOffsetCore.get().getLogger().warning("Ignoring an offset provider override with no provider set:" + b);
            }
            return false;
        }

        if (!allProviders.containsKey(provider)) {
            if (logWarning) {
                CoordinateOffsetCore.get().getLogger().warning("Ignoring an offset provider override with unknown provider:" + b);
            }
            return false;
        }

        return true;
    }
}

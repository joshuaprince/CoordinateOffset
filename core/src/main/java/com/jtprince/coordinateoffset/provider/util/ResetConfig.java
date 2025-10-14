package com.jtprince.coordinateoffset.provider.util;

import com.jtprince.coordinateoffset.provider.OffsetProviderContext;
import org.jspecify.annotations.Nullable;

import java.util.Map;
import java.util.SequencedMap;

public record ResetConfig(
    @Nullable Boolean resetOnDeath,
    @Nullable Boolean resetOnWorldChange,
    @Nullable Boolean resetOnDistantTeleport
) {
    public boolean resetOn(OffsetProviderContext.ProvideReason reason) {
        switch (reason) {
            case DEATH_RESPAWN -> { return Boolean.TRUE.equals(resetOnDeath); }
            case WORLD_CHANGE -> { return Boolean.TRUE.equals(resetOnWorldChange); }
            case DISTANT_TELEPORT -> { return Boolean.TRUE.equals(resetOnDistantTeleport); }
            default -> { return false; }
        }
    }

    public void serializeTo(SequencedMap<String, Object> map) {
        if (resetOnDeath != null) {
            map.put("resetOnDeath", resetOnDeath);
        }
        if (resetOnWorldChange != null) {
            map.put("resetOnWorldChange", resetOnWorldChange);
        }
        if (resetOnDistantTeleport != null) {
            map.put("resetOnDistantTeleport", resetOnDistantTeleport);
        }
    }

    public static @Nullable ResetConfig deserialize(Map<String, ?> providerConfig) throws IllegalArgumentException {
        boolean anySet = false;

        Boolean resetOnDeath = null;
        if (providerConfig.get("resetOnDeath") != null) {
            if (!(providerConfig.get("resetOnDeath") instanceof Boolean b)) {
                throw new IllegalArgumentException("resetOnDeath must be a boolean");
            }
            resetOnDeath = b;
            anySet = true;
        }
        Boolean resetOnWorldChange = null;
        if (providerConfig.get("resetOnWorldChange") != null) {
            if (!(providerConfig.get("resetOnWorldChange") instanceof Boolean b)) {
                throw new IllegalArgumentException("resetOnWorldChange must be a boolean");
            }
            resetOnWorldChange = b;
            anySet = true;
        }
        Boolean resetOnDistantTeleport = null;
        if (providerConfig.get("resetOnDistantTeleport") != null) {
            if (!(providerConfig.get("resetOnDistantTeleport") instanceof Boolean b)) {
                throw new IllegalArgumentException("resetOnDistantTeleport must be a boolean");
            }
            resetOnDistantTeleport = b;
            anySet = true;
        }

        if (!anySet) {
            return null;
        }
        return new ResetConfig(resetOnDeath, resetOnWorldChange, resetOnDistantTeleport);
    }
}

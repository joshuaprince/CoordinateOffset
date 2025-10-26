package com.jtprince.coordinateoffset.provider.util;

import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

import java.util.Map;
import java.util.Objects;
import java.util.SequencedMap;

@NullMarked
public record ResetConfig(
    Boolean resetOnDeath,
    Boolean resetOnWorldChange,
    Boolean resetOnDistantTeleport,
    @Nullable Double minimumTeleportDistance
) {
    public static final double DEFAULT_MINIMUM_TELEPORT_DISTANCE = 256.0;

    public boolean isResetOnDeath() {
        return resetOnDeath;
    }
    public boolean isResetOnWorldChange() {
        return resetOnWorldChange;
    }
    public boolean isResetOnDistantTeleport(double distance) {
        return resetOnDistantTeleport
            && distance > Objects.requireNonNullElse(minimumTeleportDistance, DEFAULT_MINIMUM_TELEPORT_DISTANCE);
    }

    public void serializeTo(SequencedMap<String, Object> map) {
        map.put("resetOnDeath", resetOnDeath);
        map.put("resetOnWorldChange", resetOnWorldChange);
        map.put("resetOnDistantTeleport", resetOnDistantTeleport);
        if (minimumTeleportDistance != null) {
            map.put("minimumTeleportDistance", minimumTeleportDistance);
        }
    }

    public static ResetConfig deserialize(Map<String, ?> providerConfig) throws IllegalArgumentException {
        boolean resetOnDeath = false;
        if (providerConfig.get("resetOnDeath") != null) {
            if (!(providerConfig.get("resetOnDeath") instanceof Boolean b)) {
                throw new IllegalArgumentException("resetOnDeath must be a boolean");
            }
            resetOnDeath = b;
        }
        boolean resetOnWorldChange = false;
        if (providerConfig.get("resetOnWorldChange") != null) {
            if (!(providerConfig.get("resetOnWorldChange") instanceof Boolean b)) {
                throw new IllegalArgumentException("resetOnWorldChange must be a boolean");
            }
            resetOnWorldChange = b;
        }
        boolean resetOnDistantTeleport = false;
        if (providerConfig.get("resetOnDistantTeleport") != null) {
            if (!(providerConfig.get("resetOnDistantTeleport") instanceof Boolean b)) {
                throw new IllegalArgumentException("resetOnDistantTeleport must be a boolean");
            }
            resetOnDistantTeleport = b;
        }
        Double minimumTeleportDistance = null;
        if (providerConfig.get("minimumTeleportDistance") != null) {
            if (!(providerConfig.get("minimumTeleportDistance") instanceof Number l)) {
                throw new IllegalArgumentException("minimumTeleportDistance must be a number");
            }
            minimumTeleportDistance = l.doubleValue();
        } else if (resetOnDistantTeleport) {
            // Write default min distance if reset on teleport is enabled but the key isn't present in config
            minimumTeleportDistance = DEFAULT_MINIMUM_TELEPORT_DISTANCE;
        }

        return new ResetConfig(resetOnDeath, resetOnWorldChange, resetOnDistantTeleport, minimumTeleportDistance);
    }

    /**
     * Example metrics strings: "DWT", "Dxx", "xxx"
     */
    public String getMetricsCharacterString() {
        @SuppressWarnings("StringBufferReplaceableByString")
        StringBuilder sb = new StringBuilder();
        sb.append(resetOnDeath ? "D" : "x");
        sb.append(resetOnWorldChange ? "W" : "x");
        sb.append(resetOnDistantTeleport ? "T" : "x");
        return sb.toString();
    }
}

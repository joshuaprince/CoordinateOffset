package com.jtprince.coordinateoffset.provider.util;

import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

import java.util.Map;
import java.util.Objects;
import java.util.SequencedMap;

@NullMarked
public record RegenerateConfig(
    Boolean regenerateOnDeath,
    Boolean regenerateOnWorldChange,
    Boolean regenerateOnTeleport,
    @Nullable Double minimumTeleportDistance
) {
    public static final double DEFAULT_MINIMUM_TELEPORT_DISTANCE = 256.0;

    public boolean isRegenOnDeath() {
        return regenerateOnDeath;
    }
    public boolean isRegenOnWorldChange() {
        return regenerateOnWorldChange;
    }
    public boolean isRegenOnDistantTeleport(double distance) {
        return regenerateOnTeleport
            && distance > Objects.requireNonNullElse(minimumTeleportDistance, DEFAULT_MINIMUM_TELEPORT_DISTANCE);
    }

    public void serializeTo(SequencedMap<String, Object> map) {
        map.put("regenerateOnDeath", regenerateOnDeath);
        map.put("regenerateOnWorldChange", regenerateOnWorldChange);
        map.put("regenerateOnTeleport", regenerateOnTeleport);
        if (minimumTeleportDistance != null) {
            map.put("minimumTeleportDistance", minimumTeleportDistance);
        }
    }

    public static RegenerateConfig deserialize(Map<String, ?> providerConfig) throws IllegalArgumentException {
        boolean regenerateOnDeath = false;
        if (providerConfig.get("regenerateOnDeath") != null) {
            if (!(providerConfig.get("regenerateOnDeath") instanceof Boolean b)) {
                throw new IllegalArgumentException("regenerateOnDeath must be a boolean");
            }
            regenerateOnDeath = b;
        } else if (providerConfig.get("resetOnDeath") != null
            && providerConfig.get("resetOnDeath") instanceof Boolean b) {
            // v5 and below compatibility
            regenerateOnDeath = b;
        }

        boolean regenerateOnWorldChange = false;
        if (providerConfig.get("regenerateOnWorldChange") != null) {
            if (!(providerConfig.get("regenerateOnWorldChange") instanceof Boolean b)) {
                throw new IllegalArgumentException("regenerateOnWorldChange must be a boolean");
            }
            regenerateOnWorldChange = b;
        } else if (providerConfig.get("resetOnWorldChange") != null
            && providerConfig.get("resetOnWorldChange") instanceof Boolean b) {
            // v5 and below compatibility
            regenerateOnWorldChange = b;
        }

        boolean regenerateOnTeleport = false;
        if (providerConfig.get("regenerateOnTeleport") != null) {
            if (!(providerConfig.get("regenerateOnTeleport") instanceof Boolean b)) {
                throw new IllegalArgumentException("regenerateOnTeleport must be a boolean");
            }
            regenerateOnTeleport = b;
        } else if (providerConfig.get("resetOnDistantTeleport") != null
            && providerConfig.get("resetOnDistantTeleport") instanceof Boolean b) {
            // v5 and below compatibility
            regenerateOnTeleport = b;
        }

        Double minimumTeleportDistance = null;
        if (providerConfig.get("minimumTeleportDistance") != null) {
            if (!(providerConfig.get("minimumTeleportDistance") instanceof Number l)) {
                throw new IllegalArgumentException("minimumTeleportDistance must be a number");
            }
            minimumTeleportDistance = l.doubleValue();
        } else if (regenerateOnTeleport) {
            // Write default min distance if regen on teleport is enabled but the key isn't present in config
            minimumTeleportDistance = DEFAULT_MINIMUM_TELEPORT_DISTANCE;
        }

        return new RegenerateConfig(regenerateOnDeath, regenerateOnWorldChange, regenerateOnTeleport, minimumTeleportDistance);
    }

    /**
     * Example metrics strings: "DWT", "Dxx", "xxx"
     */
    public String getMetricsCharacterString() {
        @SuppressWarnings("StringBufferReplaceableByString")
        StringBuilder sb = new StringBuilder();
        sb.append(regenerateOnDeath ? "D" : "x");
        sb.append(regenerateOnWorldChange ? "W" : "x");
        sb.append(regenerateOnTeleport ? "T" : "x");
        return sb.toString();
    }
}

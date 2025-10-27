package com.jtprince.coordinateoffset.provider.util;

import com.jtprince.coordinateoffset.CoordinateOffsetCore;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

import java.util.Map;
import java.util.Objects;
import java.util.SequencedMap;

@NullMarked
public record RegenerateConfig(
    Boolean regenerateOnJoin,
    Boolean regenerateOnDeath,
    Boolean regenerateOnWorldChange,
    Boolean regenerateOnTeleport,
    @Nullable Double minimumTeleportDistance,
    @Nullable String persistenceKeyOverride
) {
    public static final double DEFAULT_MINIMUM_TELEPORT_DISTANCE = 256.0;
    public static final String LEGACY_DEFAULT_PERSISTENCE_KEY = "default";

    public boolean isRegenOnJoin() {
        return regenerateOnJoin;
    }

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
        map.put("regenerateOnJoin", regenerateOnJoin);
        map.put("regenerateOnDeath", regenerateOnDeath);
        map.put("regenerateOnWorldChange", regenerateOnWorldChange);
        map.put("regenerateOnTeleport", regenerateOnTeleport);
        if (minimumTeleportDistance != null) {
            map.put("minimumTeleportDistance", minimumTeleportDistance);
        }
        if (persistenceKeyOverride != null) {
            map.put("persistenceKeyOverride", persistenceKeyOverride);
        }
    }

    public static RegenerateConfig deserialize(String providerName, Map<String, ?> providerConfig) throws IllegalArgumentException {
        boolean regenerateOnJoin = false;
        if (providerConfig.get("regenerateOnJoin") != null) {
            if (!(providerConfig.get("regenerateOnJoin") instanceof Boolean b)) {
                throw new IllegalArgumentException("regenerateOnJoin must be a boolean");
            }
            regenerateOnJoin = b;
        } else if (providerConfig.get("persistent") != null) {
            // v5 and below compatibility - used to be "persistent", which is the inverse of regenerateOnJoin
            if (!(providerConfig.get("persistent") instanceof Boolean persistent)) {
                throw new IllegalArgumentException("persistent must be a boolean");
            }
            CoordinateOffsetCore.get().getLogger().info("Provider \"" + providerName + "\": " +
                "Migrating legacy key persistent to regenerateOnJoin");
            regenerateOnJoin = !persistent;
        }

        String persistenceKeyOverride = null;
        if (providerConfig.containsKey("persistenceKeyOverride")) {
            persistenceKeyOverride = providerConfig.get("persistenceKeyOverride").toString();
        } else if (providerConfig.containsKey("persistenceKey")) {
            if (!(providerConfig.get("persistenceKey").toString().equals(LEGACY_DEFAULT_PERSISTENCE_KEY))) {
                CoordinateOffsetCore.get().getLogger().info("Provider \"" + providerName + "\": " +
                    "Migrating legacy key persistenceKey to persistenceKeyOverride");
                persistenceKeyOverride = providerConfig.get("persistenceKey").toString();
            }
        }

        boolean regenerateOnDeath = false;
        if (providerConfig.get("regenerateOnDeath") != null) {
            if (!(providerConfig.get("regenerateOnDeath") instanceof Boolean b)) {
                throw new IllegalArgumentException("regenerateOnDeath must be a boolean");
            }
            regenerateOnDeath = b;
        } else if (providerConfig.get("resetOnDeath") != null
            && providerConfig.get("resetOnDeath") instanceof Boolean b) {
            // v5 and below compatibility - used to be called "resetOnDeath"
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
            // v5 and below compatibility - used to be called "resetOnWorldChange"
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
            // v5 and below compatibility - used to be called "resetOnDistantTeleport"
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

        return new RegenerateConfig(
            regenerateOnJoin, regenerateOnDeath, regenerateOnWorldChange,
            regenerateOnTeleport, minimumTeleportDistance, persistenceKeyOverride);
    }

    /**
     * Example metrics strings: "DWT", "Dxx", "xxx"
     */
    public String getMetricsCharacterString() {
        @SuppressWarnings("StringBufferReplaceableByString")
        StringBuilder sb = new StringBuilder();
        sb.append(regenerateOnJoin ? "J" : "x");
        sb.append(regenerateOnDeath ? "D" : "x");
        sb.append(regenerateOnWorldChange ? "W" : "x");
        sb.append(regenerateOnTeleport ? "T" : "x");
        return sb.toString();
    }
}

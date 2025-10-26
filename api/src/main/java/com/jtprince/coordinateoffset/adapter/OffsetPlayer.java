package com.jtprince.coordinateoffset.adapter;

import org.jspecify.annotations.NullMarked;

import java.util.UUID;

/**
 * Adapter interface representing a player in a Minecraft server.
 */
@NullMarked
public interface OffsetPlayer {
    UUID getUuid();
    String getName();
    boolean hasPermission(String permission);
    OffsetLocation getLocation();

    /**
     * Get the underlying platform-specific player object, for example a Bukkit Player.
     */
    Object getPlatformPlayerObject();
}

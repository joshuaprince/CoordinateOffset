package com.jtprince.coordinateoffset.adapter;

import java.util.UUID;

/**
 * Adapter interface representing a player in a Minecraft server.
 */
public interface OffsetPlayer {
    UUID getUuid();
    String getName();
    boolean hasPermission(String permission);
}

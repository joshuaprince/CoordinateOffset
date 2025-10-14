package com.jtprince.coordinateoffset.adapter;

import org.jspecify.annotations.NullMarked;

/**
 * Adapter interface representing a location in a Minecraft world with X, Y, Z coordinates.
 *
 * <p>The name does NOT imply that an offset was already applied. It refers to any Location object in the different
 * platforms.</p>
 */
@NullMarked
public interface OffsetLocation {
    String getWorldName();
    double getX();
    double getY();
    double getZ();

    /**
     * Get the underlying platform-specific location object, for example a Bukkit Location.
     */
    Object getPlatformLocationObject();
}

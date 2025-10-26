package com.jtprince.coordinateoffset.adapter;

import com.jtprince.coordinateoffset.Offset;
import org.checkerframework.dataflow.qual.Pure;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

/**
 * Adapter interface representing a location in a Minecraft world with X, Y, Z coordinates.
 *
 * <p>The name does NOT imply that an offset was already applied. It refers to any Location object in the different
 * platforms.</p>
 */
@NullMarked
public interface OffsetLocation {
    @Nullable String getWorldName();
    double getX();
    double getY();
    double getZ();

    /**
     * Apply an offset to this location. This location should refer to coordinates in "real" space, i.e. not yet
     * transformed by an offset.
     *
     * <p>Be careful not to use the returned Location for anything internal to the server, such as getting the Block
     * at that Location. The returned Location is primarily intended to be sent to a Player who this Offset is applied
     * to, such as in a message.</p>
     *
     * @param offset The offset to apply.
     * @return A new {@code OffsetLocation} with the offset applied and all other data matching the original.
     */
    @Pure
    OffsetLocation apply(Offset offset);

    /**
     * Reverses the application of the given offset from this location, effectively restoring the "real" coordinates
     * before the offset was applied. This location should refer to coordinates in a player's offsetted space.
     *
     * @param offset The offset to reverse.
     * @return A new {@code OffsetLocation} with the offset unapplied and all other data matching the original.
     */
    @Pure
    OffsetLocation unapply(Offset offset);

    /**
     * Get the distance between two locations.
     * @param other Second location to compare against.
     * @return A distance in blocks, or null if the locations are in different worlds.
     */
    default @Nullable Double getDistance(OffsetLocation other) {
        if (getWorldName() != null || other.getWorldName() != null) {
            if (getWorldName() == null ||
                other.getWorldName() == null ||
                !getWorldName().equals(other.getWorldName())) {
                return null;
            }
        }
        return Math.sqrt(Math.pow(getX() - other.getX(), 2) + Math.pow(getY() - other.getY(), 2) + Math.pow(getZ() - other.getZ(), 2));
    }

    /**
     * Get the underlying platform-specific location object, for example a Bukkit Location.
     */
    Object getPlatformLocationObject();
}

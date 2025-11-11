package com.jtprince.coordinateoffset;

import com.jtprince.coordinateoffset.adapter.OffsetLocation;
import com.jtprince.coordinateoffset.api.CoordinateOffset;
import org.checkerframework.dataflow.qual.Pure;
import org.jspecify.annotations.NullMarked;

/**
 * Amount by which a player's clientside X and Z coordinates will appear shifted compared to their real position in a
 * world.
 *
 * <p>Fixed offsets are absolute in any coordinate space. For example, a fixed offset of <code>(800, 800)</code>
 * will <i>always</i> subtract 800 from the player's coordinates. This may break coordinate-based alignment between
 * nether portals <b>and make it possible to reverse-engineer offsets</b> through clever use of nether portals.</p>
 *
 * <p>A fixed offset is fully resolved and may be applied to coordinates directly.</p>
 *
 * @param x X offset value in blocks. Will be subtracted from the player's real X coordinate.
 * @param z Z offset value in blocks. Will be subtracted from the player's real Z coordinate.
 */
@NullMarked
public record FixedOffset(int x, int z) implements Offset {
    public FixedOffset {
        if (x % 16 != 0) {
            throw new IllegalArgumentException("Offset x=" + x + " is not chunk-aligned! (must be a multiple of 16)");
        }
        if (z % 16 != 0) {
            throw new IllegalArgumentException("Offset z=" + z + " is not chunk-aligned! (must be a multiple of 16)");
        }
    }

    @Override
    public String toString() {
        return "[x=" + x + ", z=" + z + "]";
    }

    @Override
    public FixedOffset negate() {
        return new FixedOffset(-x, -z);
    }

    @Override
    public boolean isZero() {
        return x == 0 && z == 0;
    }

    public int chunkX() {
        return x >> 4;
    }

    public int chunkZ() {
        return z >> 4;
    }

    /**
     * Apply this offset to a location.
     *
     * @param location Location to apply the offset to. This may either be a {@link OffsetLocation} or a
     *                 platform-specific location object, such as a Bukkit Location.
     * @return A new Location object with the offset applied and all other data (including type) matching the original.
     * @param <T> Either {@link OffsetLocation} or a platform-specific location object.
     * @throws ClassCastException if the provided object is not of an acceptable type for the running platform.
     */
    @Pure
    public <T> T apply(T location) throws ClassCastException {
        OffsetLocation l;
        if (location instanceof OffsetLocation) {
            l = (OffsetLocation) location;
        } else {
            l = CoordinateOffset.api().adaptLocation(location);
        }

        OffsetLocation applied = l.apply(this);

        if (location instanceof OffsetLocation) {
            return (T) applied;
        } else {
            return (T) applied.getPlatformLocationObject();
        }
    }

    /**
     * Unapply this offset from a location.
     *
     * @param location Location to unapply the offset from. This may either be a {@link OffsetLocation} or a
     *                 platform-specific location object, such as a Bukkit Location.
     * @return A new Location object with the offset unapplied and all other data (including type) matching the original.
     * @param <T> Either {@link OffsetLocation} or a platform-specific location object.
     * @throws ClassCastException if the provided object is not of an acceptable type for the running platform.
     */
    @Pure
    public <T> T unapply(T location) throws ClassCastException {
        OffsetLocation l;
        if (location instanceof OffsetLocation) {
            l = (OffsetLocation) location;
        } else {
            l = CoordinateOffset.api().adaptLocation(location);
        }

        OffsetLocation unapplied = l.unapply(this);

        if (location instanceof OffsetLocation) {
            return (T) unapplied;
        } else {
            return (T) unapplied.getPlatformLocationObject();
        }
    }
}

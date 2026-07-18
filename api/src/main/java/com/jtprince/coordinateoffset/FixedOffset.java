package com.jtprince.coordinateoffset;

import com.jtprince.coordinateoffset.adapter.OffsetLocation;
import com.jtprince.coordinateoffset.api.CoordinateOffset;
import org.jspecify.annotations.NullMarked;

/**
 * Amount by which a player's clientside X, Y, and Z coordinates will appear shifted compared to their real
 * position in a world.
 *
 * <p>Fixed offsets are absolute in any coordinate space. For example, a fixed offset of <code>(800, 800)</code>
 * will <i>always</i> subtract 800 from the player's coordinates. This may break coordinate-based alignment between
 * nether portals <b>and make it possible to reverse-engineer offsets</b> through clever use of nether portals.</p>
 *
 * <p>A fixed offset is fully resolved and may be applied to coordinates directly.</p>
 *
 * @param x X offset value in blocks. Must be a multiple of 16. Will be subtracted from the player's real X coordinate.
 * @param y Y offset value in blocks. Will be subtracted from the player's real Y coordinate.
 * @param z Z offset value in blocks. Must be a multiple of 16. Will be subtracted from the player's real Z coordinate.
 */
@NullMarked
public record FixedOffset(int x, int y, int z) implements Offset {
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
        return "[x=" + x + ", y=" + y + ", z=" + z + "]";
    }

    @Override
    public FixedOffset negate() {
        return new FixedOffset(-x, -y, -z);
    }

    @Override
    public boolean isZero() {
        return x == 0 && y == 0 && z == 0;
    }

    public int chunkX() {
        return x >> 4;
    }

    public int chunkZ() {
        return z >> 4;
    }

    @Override
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

    @Override
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

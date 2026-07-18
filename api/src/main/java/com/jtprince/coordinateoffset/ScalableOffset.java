package com.jtprince.coordinateoffset;

import com.jtprince.coordinateoffset.adapter.OffsetLocation;
import com.jtprince.coordinateoffset.adapter.OffsetWorld;
import com.jtprince.coordinateoffset.api.CoordinateOffset;
import org.jspecify.annotations.NullMarked;

import java.util.Random;

/**
 * Amount by which a player's clientside X, Y, and Z coordinates will appear shifted compared to their real
 * position in a world.
 *
 * <p>A scalable offset is scaled based on the coordinate system of the world the offset is applied in. For example,
 * a scalable offset of <code>(800, 800)</code> may subtract 800 blocks from the player's coordinates in the
 * overworld and subtract 100 blocks in the nether.</p>
 *
 * <p>Scalable offsets cannot be applied to coordinates directly. They must first be scaled to a {@link FixedOffset}
 * with the context of a world's coordinate scale. See {@link #scaleDownAndRound}.</p>
 *
 * @param x X offset value in blocks. Will be scaled based on world, then subtracted from the player's real X coordinate.
 * @param y Y offset value in blocks. Will be subtracted from the player's real Y coordinate.
 * @param z Z offset value in blocks. Will be scaled based on world, then subtracted from the player's real Z coordinate.
 */
@NullMarked
public record ScalableOffset(int x, int y, int z) implements Offset {
    static final Random RANDOM = new Random();

    @Override
    public String toString() {
        return "[x=" + x + ", y=" + y + ", z=" + z + "]";
    }

    @Override
    public <T> T apply(T location) throws ClassCastException {
        OffsetLocation l;
        if (location instanceof OffsetLocation) {
            l = (OffsetLocation) location;
        } else {
            l = CoordinateOffset.api().adaptLocation(location);
        }

        OffsetLocation applied = l.apply(this.scaleToWorld(l.getWorld()));

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

        OffsetLocation unapplied = l.unapply(this.scaleToWorld(l.getWorld()));

        if (location instanceof OffsetLocation) {
            return (T) unapplied;
        } else {
            return (T) unapplied.getPlatformLocationObject();
        }
    }

    @Override
    public ScalableOffset negate() {
        return new ScalableOffset(-x, -y, -z);
    }

    @Override
    public boolean isZero() {
        return x == 0 && y == 0 && z == 0;
    }

    /**
     * Scale the components of this offset down by the specified divisor, then round to the nearest configured
     * <code>offsetsAreMultiplesOfBlocks</code> value, returning a {@link FixedOffset}.
     *
     * @param divisor Amount by which to scale down the components of this offset.
     * @return A new {@link FixedOffset} with scaled components.
     */
    public FixedOffset scaleDownAndRound(double divisor) {
        return new FixedOffset(
            Offset.alignComponentToConfiguredMultiple((int) (x / divisor)),
            y,
            Offset.alignComponentToConfiguredMultiple((int) (z / divisor))
        );
    }

    /**
     * Scale the components of this offset down by the specified divisor, returning a {@link FixedOffset}.
     *
     * @param divisor Amount by which to scale the components of this offset.
     * @return A new {@link FixedOffset} with scaled components.
     * @deprecated Use {@link #scaleDownAndRound(double)} instead to ensure offsets are always multiples of the
     *             configured <code>offsetsAreMultiplesOfBlocks</code> value.
     */
    @Deprecated(forRemoval = true)
    public FixedOffset scaleDownBy(double divisor) {
        if (divisor == 0.0) {
            // Special case - 0 would naturally result in a NaN/infinity offset, but interpret 0 scale as 0 offset
            return new FixedOffset(0, y, 0);
        }
        return new FixedOffset(
            Offset.alignComponent((int) (x / divisor), 0),
            y,
            Offset.alignComponent((int) (z / divisor), 0)
        );
    }

    /**
     * Scale the components of this offset to the coordinate scale of the specified world.
     *
     * @param world World to scale the offset to.
     * @return A new {@link FixedOffset} with scaled components.
     */
    public FixedOffset scaleToWorld(OffsetWorld world) {
        return this.scaleDownAndRound(world.getCoordinateScale());
    }
}

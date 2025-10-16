package com.jtprince.coordinateoffset;

import com.jtprince.coordinateoffset.adapter.OffsetLocation;
import com.jtprince.coordinateoffset.api.CoordinateOffset;
import org.checkerframework.dataflow.qual.Pure;
import org.jspecify.annotations.NullMarked;

import java.util.Random;

/**
 * Represents the amount by which a player's clientside X and Z coordinates will appear shifted compared to their real
 * position in a world.
 *
 * <p>Offsets are <b>subtracted</b> from real coordinates. An offset of <code>(16, 16)</code> would result in a player
 * seeing themselves at <code>(0, 0)</code> when they are standing at <code>(16, 16)</code> in the Overworld, and
 * seeing themselves standing at <code>(-16, -16)</code> when they are standing at the real origin.</p>
 *
 * @param x Offset amount for the X coordinate. Must be a multiple of 16 to align with chunk boundaries.
 * @param z Offset amount for the Z coordinate. Must be a multiple of 16 to align with chunk boundaries.
 */
@NullMarked
public record Offset (int x, int z) {
    /**
     * The "zero" or identity Offset, which results in no transformation from real-world coordinates.
     */
    public static final Offset ZERO = new Offset(0, 0);

    /**
     * Argument for the {@code toChunksPower} parameter of {@link #align(int, int, int)} that results in an Overworld
     * offset that will cleanly translate to a Nether offset.
     */
    public static final int ALIGN_OVERWORLD = 3;

    public Offset {
        if (x % 16 != 0) {
            throw new IllegalArgumentException("Offset x=" + x + " is not chunk-aligned! (must be a multiple of 16)");
        }
        if (z % 16 != 0) {
            throw new IllegalArgumentException("Offset z=" + z + " is not chunk-aligned! (must be a multiple of 16)");
        }
    }

    /**
     * Get a random Offset, with X and Z in the range <code>(-bound, bound)</code>.
     *
     * @param bound Maximum absolute value of each offset component.
     * @return A new Offset with values that are multiples of 128 blocks.
     */
    public static Offset random(int bound) {
        Random random = new Random();
        return align(random.nextInt(-bound, bound), random.nextInt(-bound, bound), ALIGN_OVERWORLD);
    }

    /**
     * Get a new Offset closest to the specified offset that is aligned to chunk borders.
     *
     * <p>Offsets MUST be aligned with chunk borders, meaning each component is divisible by 16.</p>
     * @param x X offset
     * @param z Z offset
     * @param toChunksPower Value used to perform extra alignment with chunks. The input x/z will be rounded to the
     *                      nearest <code>2^toChunksPower</code> chunks. This is useful for making Nether translations
     *                      predictable: we want Overworld offsets to still align to chunk boundaries even after
     *                      dividing them by 8. Therefore, we would use {@value ALIGN_OVERWORLD} as the value here when
     *                      aligning the Overworld offset (since 2^3 == 8).
     * @return A new Offset.
     */
    public static Offset align(int x, int z, int toChunksPower) {
        int shift = toChunksPower + 4;

        // Add half of the divisor so that the output is rounded instead of just floored
        x += 1 << (shift - 1);
        z += 1 << (shift - 1);

        return new Offset(x >> shift << shift, z >> shift << shift);
    }

    public static Offset align(int x, int z) {
        return Offset.align(x, z, 0);
    }

    public int chunkX() {
        return x >> 4;
    }

    public int chunkZ() {
        return z >> 4;
    }

    /**
     * Get a new Offset with the components of this offset scaled by a power of two.
     *
     * @param rightShiftAmount The amount to right-shift this Offset's components. A negative value will make the offset
     *                         larger (e.g. -3 would multiply the components by 8). A positive value will make the
     *                         offset smaller (e.g. 5 would divide the components by 32).
     * @return A new Offset aligned to 1 chunk.
     */
    @Pure
    public Offset scale(int rightShiftAmount) {
        if (rightShiftAmount <= 0) {
            return new Offset(x << -rightShiftAmount, z << -rightShiftAmount);
        } else {
            // When scaling the offset down, ensure that the new offset is also divisible by 16.
            return Offset.align(x >> rightShiftAmount, z >> rightShiftAmount);
        }
    }

    /**
     * Get a new Offset with the components of this offset multiplied by an arbitrary number and rounded.
     *
     * @param scaleFactor The factor to multiply this offset by.
     * @return A new Offset aligned to 1 chunk.
     */
    @Pure
    public Offset scaleByDouble(double scaleFactor) {
        return Offset.align((int) Math.round(x * scaleFactor), (int) Math.round(z * scaleFactor));
    }

    /**
     * Get a new Offset with the inverse components as this one (x -> -x, z -> -z).
     * @return A new Offset.
     */
    @Pure
    public Offset negate() {
        return new Offset(-x, -z);
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

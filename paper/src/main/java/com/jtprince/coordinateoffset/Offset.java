package com.jtprince.coordinateoffset;

import com.jeff_media.morepersistentdatatypes.DataType;
import com.jeff_media.morepersistentdatatypes.datatypes.GenericDataType;
import org.bukkit.Location;
import org.bukkit.persistence.PersistentDataType;
import org.checkerframework.dataflow.qual.Pure;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.util.Random;

/**
 * Represents the amount by which a player's clientside X and Z coordinates will appear shifted compared to their real
 * position in a world.
 *
 * <p>An offset of <code>(16, 16)</code> would result in a player seeing themselves at <code>(0, 0)</code> when they are
 * standing at <code>(16, 16)</code> in the Overworld, and seeing themselves standing at <code>(-16, -16)</code> when
 * they are standing at the real origin.</p>
 *
 * @param x Offset amount for the X coordinate. Must be a multiple of 16 to align with chunk boundaries.
 * @param z Offset amount for the Z coordinate. Must be a multiple of 16 to align with chunk boundaries.
 */
public record Offset (int x, int z) {
    /**
     * The "zero" or identity Offset, which results in no transformation from real-world coordinates.
     */
    public static final Offset ZERO = new Offset(0, 0);

    /**
     * Type for storing Offsets in Persistent Data Containers (PDC).
     */
    public static final PersistentDataType<int[], Offset> PDT_TYPE =
            new GenericDataType<>(DataType.INTEGER_ARRAY.getPrimitiveType(), Offset.class, Offset::fromPdt, Offset::toPdt);

    /**
     * Argument for the {@code toChunksPower} parameter of {@link #align(int, int, int)} that results in an Overworld
     * offset that will cleanly translate to a Nether offset.
     */
    public static final int ALIGN_OVERWORLD = 3;

    public Offset {
        if (x % 16 != 0) {
            throw new IllegalArgumentException("Offset x=" + x + " is not aligned with the chunks! (must be a multiple of 16)");
        }
        if (z % 16 != 0) {
            throw new IllegalArgumentException("Offset z=" + z + " is not aligned with the chunks! (must be a multiple of 16)");
        }
    }

    /**
     * Get a random Offset, with X and Z in the range <code>(-bound, bound)</code>.
     *
     * @param bound Maximum absolute value of each offset component.
     * @return A new Offset with values that are multiples of 128 blocks.
     */
    public static @NotNull Offset random(int bound) {
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
    public static @NotNull Offset align(int x, int z, int toChunksPower) {
        int shift = toChunksPower + 4;

        // Add half of the divisor so that the output is rounded instead of just floored
        x += 1 << (shift - 1);
        z += 1 << (shift - 1);

        return new Offset(x >> shift << shift, z >> shift << shift);
    }

    public static @NotNull Offset align(int x, int z) {
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
    public @NotNull Offset scale(int rightShiftAmount) {
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
    public @NotNull Offset scaleByDouble(double scaleFactor) {
        return Offset.align((int) Math.round(x * scaleFactor), (int) Math.round(z * scaleFactor));
    }

    /**
     * Get a new Offset with the inverse components as this one (x -> -x, z -> -z).
     * @return A new Offset.
     */
    @Pure
    public @NotNull Offset negate() {
        return new Offset(-x, -z);
    }

    /**
     * Apply this Offset to a Bukkit Location, resulting in the Location that a player who has this Offset would see
     * if they were at that Location.
     *
     * <p>Care should be taken not to use the returned Location for anything internal to the server, such as getting the
     * Block at that Location. The returned Location is primarily intended to be sent to a Player who this Offset is
     * applied to, such as in a message.</p>
     *
     * @param realLocation A Location on the server, in real coordinate space.
     * @return A new Location object that represents the coordinates that the player will see.
     */
    @Pure
    @Contract("null -> null; !null -> !null")
    public Location apply(Location realLocation) {
        if (realLocation == null) return null;
        return realLocation.clone().subtract(this.x, 0, this.z);
    }

    /**
     * Apply the inverse of this Offset to a Bukkit Location, resulting in a real server Location.
     *
     * @param offsettedLocation An offsetted Location coming from a Player who has this offset.
     * @return A new Location object that represents the real Location for the server to use.
     */
    @Pure
    @Contract("null -> null; !null -> !null")
    public Location unapply(Location offsettedLocation) {
        if (offsettedLocation == null) return null;
        return offsettedLocation.clone().add(this.x, 0, this.z);
    }

    private static Offset fromPdt(int[] arr) {
        return new Offset(arr[0], arr[1]);
    }

    private int[] toPdt() {
        return new int[] { x, z };
    }
}

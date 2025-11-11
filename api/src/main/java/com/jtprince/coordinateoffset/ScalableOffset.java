package com.jtprince.coordinateoffset;

import org.jspecify.annotations.NullMarked;

import java.util.Random;

/**
 * Amount by which a player's clientside X and Z coordinates will appear shifted compared to their real position in a
 * world.
 *
 * <p>A scalable offset is scaled based on the coordinate system of the world the offset is applied in. For example,
 * a scalable offset of <code>(800, 800)</code> may subtract 800 blocks from the player's coordinates in the
 * overworld and subtract 100 blocks in the nether.</p>
 *
 * <p>Scalable offsets cannot be applied to coordinates directly. They must first be scaled to a {@link FixedOffset}
 * with the context of a world's coordinate scale. See {@link #scaleDownBy}.</p>
 *
 * @param x X offset value in blocks. Will be scaled based on world, then subtracted from the player's real X
 *          coordinate.
 * @param z Z offset value in blocks. Will be scaled based on world, then subtracted from the player's real Z
 *          coordinate.
 */
@NullMarked
public record ScalableOffset(int x, int z) implements Offset {
    static final Random RANDOM = new Random();

    @Override
    public String toString() {
        return "[x=" + x + ", z=" + z + "]";
    }

    @Override
    public ScalableOffset negate() {
        return new ScalableOffset(-x, -z);
    }

    public boolean isZero() {
        return x == 0 && z == 0;
    }

    public FixedOffset scaleDownBy(double divisor) {
        return new FixedOffset(
            Offset.alignComponent((int) (x / divisor), 0),
            Offset.alignComponent((int) (z / divisor), 0)
        );
    }
}

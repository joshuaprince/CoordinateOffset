package com.jtprince.coordinateoffset;

import com.jtprince.coordinateoffset.adapter.OffsetLocation;
import org.checkerframework.dataflow.qual.Pure;
import org.jspecify.annotations.NullMarked;

/**
 * An offset is the amount by which a player's clientside X and Z coordinates will appear shifted compared to their real
 * position in a world.
 *
 * <p>Offsets are <b>subtracted</b> from real coordinates. An offset of <code>(16, 16)</code> would result in a player
 * seeing themselves at <code>(0, 0)</code> when they are standing at <code>(16, 16)</code> in the Overworld, and
 * seeing themselves standing at <code>(-16, -16)</code> when they are standing at the real origin.</p>
 *
 * There are two types of offsets: {@link FixedOffset} and {@link ScalableOffset}. The difference relates to how they
 * handle coordinate scaling for some worlds, such as the nether. To apply an offset that is absolute in any coordinate
 * space, use a {@link FixedOffset}. To apply an offset that automatically scales to match the coordinate scale of the
 * world, use a {@link ScalableOffset}. If unsure, {@link ScalableOffset} is generally preferred so that you don't have
 * to worry about world alignment.
 */
@NullMarked
public sealed interface Offset permits FixedOffset, ScalableOffset {
    /**
     * Create a new fixed offset with the given components.
     *
     * <p>Fixed offsets are absolute in any coordinate space. For example, a fixed offset of <code>(800, 800)</code>
     * will <i>always</i> subtract 800 from the player's coordinates. This may break coordinate-based alignment between
     * nether portals <b>and make it possible to reverse-engineer offsets</b> through clever use of nether portals.</p>
     *
     * <p>{@link ScalableOffset} is recommended for most use cases. {@link FixedOffset} is only recommended if vanilla
     * nether portal travel is disabled, or if the offset provider is manually performing coordinate scaling.</p>
     *
     * @param x X offset value in blocks. Will be subtracted from the player's real X coordinate.
     * @param z Z offset value in blocks. Will be subtracted from the player's real Z coordinate.
     * @return A new FixedOffset.
     */
    static FixedOffset fixed(int x, int z) {
        return new FixedOffset(x, z);
    }

    /**
     * Create a new scalable offset with the given components.
     *
     * <p>A scalable offset is scaled based on the coordinate system of the world the offset is applied in. For example,
     * a scalable offset of <code>(800, 800)</code> may subtract 800 blocks from the player's coordinates in the
     * overworld and subtract 100 blocks in the nether.</p>
     *
     * <p>This is the ideal method of applying offsets because it ensures that coordinates still align across worlds.
     * Players expect that entering a nether portal they see at <code>(-4000, 4000)</code> will bring them to
     * <code>(-500, 500)</code> in the nether.</p>
     *
     * @param x X offset value in blocks. Will be scaled based on world, then subtracted from the player's real X
     *          coordinate.
     * @param z Z offset value in blocks. Will be scaled based on world, then subtracted from the player's real Z
     *          coordinate.
     * @return A new ScalableOffset.
     */
    static ScalableOffset scalable(int x, int z) {
        return new ScalableOffset(x, z);
    }

    /**
     * The "zero" or identity offset. This offset results in no transformation from real-world coordinates.
     */
    FixedOffset ZERO = new FixedOffset(0, 0);

    /**
     * Align offset components to the nearest 8 chunks. This number is selected because the default nether has a
     * coordinate scale of 1/8th of the overworld, so aligning an overworld offset to the nearest 8 chunks guarantees
     * that it will divide evenly to create an offset in the nether.
     */
    int ALIGN_DEFAULT_OVERWORLD = 3;

    /**
     * Align offset components to the nearest 8 chunks and create a new {@link ScalableOffset}.
     *
     * <p>Offset components must be multiples of 16 to align to 1 chunk. This function rounds each component to the
     * nearest multiple of 16*8 because the default nether's coordinate scale is 1/8th of the overworld. Using multiples
     * of 8 chunks ensures that the resulting offset in the overworld will divide evenly to create an offset in the
     * nether.</p>
     *
     * @param x X offset value in blocks. Will be scaled based on world, then subtracted from the player's real X
     *          coordinate.
     * @param z Z offset value in blocks. Will be scaled based on world, then subtracted from the player's real Z
     *          coordinate.
     * @return A new ScalableOffset with provided X and Z components rounded to the nearest multiple of 8*16.
     */
    static ScalableOffset align(int x, int z) {
        return Offset.align(x, z, ALIGN_DEFAULT_OVERWORLD);
    }

    /**
     * Align offset components to the nearest 2^N chunks and create a new {@link ScalableOffset}.
     *
     * <p>Offset components must be multiples of 16 to align to 1 chunk. For minimal rounding, use
     * <code>toChunksPower=0</code>. For ideal behavior, round to a number of chunks such that the resulting offset in
     * each world will evenly divide into offsets in other worlds. For a default set of vanilla Minecraft worlds,
     * {@link Offset#ALIGN_DEFAULT_OVERWORLD} aligns offsets to 8 chunks since the nether's coordinate scale is 1/8th
     * of the overworld.</p>
     *
     * @param x X offset value in blocks. Will be scaled based on world, then subtracted from the player's real X
     *          coordinate.
     * @param z Z offset value in blocks. Will be scaled based on world, then subtracted from the player's real Z
     *          coordinate.
     * @param toChunksPower Power of 2 to round each component to. For example, <code>toChunksPower=3</code> will round
     *                      each component to the nearest <code>(2^3)=8</code> chunks (128 blocks).
     * @return A new ScalableOffset with provided X and Z components rounded to the nearest multiple of
     *         <code>16*(2^p)</code>.
     */
    static ScalableOffset align(int x, int z, int toChunksPower) {
        return new ScalableOffset(alignComponent(x, toChunksPower), alignComponent(z, toChunksPower));
    }

    /**
     * Create a new random {@link ScalableOffset} with components between negative and positive values of the given
     * bound.
     *
     * @param bound Maximum absolute value of the generated offset's X and Z components.
     * @return A new ScalableOffset with components divisible by 128 blocks (to ensure vanilla overworld/nether
     *         alignment; see {@link Offset#ALIGN_DEFAULT_OVERWORLD})
     */
    static ScalableOffset random(int bound) {
        return random(bound, ALIGN_DEFAULT_OVERWORLD);
    }

    /**
     * Create a new random {@link ScalableOffset} with components between negative and positive values of the given
     * bound.
     *
     * <p>Components are rounded to the nearest multiple of 2^N chunks</p>. For minimal rounding, use
     * <code>toChunksPower=0</code>. For ideal behavior, round to a number of chunks such that the resulting offset in
     * each world will evenly divide into offsets in other worlds. For a default set of vanilla Minecraft worlds,
     * {@link Offset#ALIGN_DEFAULT_OVERWORLD} aligns offsets to 8 chunks since the nether's coordinate scale is 1/8th
     * of the overworld.</p>
     *
     * @param bound Maximum absolute value of the generated offset's X and Z components.
     * @param alignToChunksPower Power of 2 to round each component to. For example, <code>toChunksPower=3</code> will
     *                           round each component to the nearest <code>(2^3)=8</code> chunks (128 blocks).
     * @return A new ScalableOffset with components divisible by 128 blocks (to ensure vanilla overworld/nether
     *         alignment; see {@link Offset#ALIGN_DEFAULT_OVERWORLD})
     */
    static ScalableOffset random(int bound, int alignToChunksPower) {
        return new ScalableOffset(
            alignComponent(ScalableOffset.RANDOM.nextInt(bound), alignToChunksPower),
            alignComponent(ScalableOffset.RANDOM.nextInt(bound), alignToChunksPower)
        );
    }

    /**
     * Align a single component (X- or Z-value) to the nearest multiple of 16*2^N chunks.
     *
     * @param component X- or Z-value to align.
     * @param alignToChunksPower Power of 2 to round the component to. For example, <code>toChunksPower=3</code> will
     *                           round each component to the nearest <code>(2^3)=8</code> chunks (128 blocks).
     * @return The aligned component.
     */
    static int alignComponent(int component, int alignToChunksPower) {
        return Math.round((float) component / (1 << alignToChunksPower + 4)) * (1 << alignToChunksPower + 4);
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
    <T> T apply(T location) throws ClassCastException;

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
    <T> T unapply(T location) throws ClassCastException;

    /**
     * Get a new Offset with the inverse components as this one (x -> -x, z -> -z).
     * @return A new Offset.
     */
    @Pure
    Offset negate();

    /**
     * Check if both components of this offset are zero, resulting in no coordinate offset.
     */
    boolean isZero();
}

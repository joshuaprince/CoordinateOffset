package com.jtprince.coordinateoffset;

import com.jtprince.coordinateoffset.adapter.OffsetLocation;
import com.jtprince.coordinateoffset.api.CoordinateOffset;
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
 * handle coordinate rounding and scaling for some worlds (such as the nether). To apply a specific offset that will not
 * be rounded or scaled, use a {@link FixedOffset}. To apply an offset that automatically scales to match the coordinate
 * scale of the world and rounds the components as needed, use a {@link ScalableOffset}. If unsure,
 * {@link ScalableOffset} is generally preferred so that you don't have to worry about world alignment or rounding.
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
     * <p>Fixed offsets also do not verify that they are multiples of the <code>offsetsAreMultiplesOfBlocks</code>
     * configuration option. This may result in unexpected behavior, especially if a plugin like Distant Horizons
     * is installed (DHSupport relies on all offsets being a multiple of a larger value like 64).</p>
     *
     * <p>{@link ScalableOffset} is recommended for most use cases. {@link FixedOffset} is only recommended if vanilla
     * nether portal travel is disabled, or if the offset provider is manually performing coordinate scaling.</p>
     *
     * @param x X offset value in blocks. Must be a multiple of 16. Will be subtracted from the player's real X
     *          coordinate.
     * @param z Z offset value in blocks. Must be a multiple of 16. Will be subtracted from the player's real Z
     *          coordinate.
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
     * <p>After scaling to a world, scalable offsets are rounded to the nearest configured
     * <code>offsetsAreMultiplesOfBlocks</code> value, which defaults to 16 but may be higher if a plugin like Distant
     * Horizons is installed.</p>
     *
     * <p>This is the preferred method of applying offsets because it ensures that coordinates still align across
     * worlds. Players expect that entering a nether portal they see at <code>(-4000, 4000)</code> will bring them to
     * <code>(-500, 500)</code> in the nether.</p>
     *
     * @param x X offset value in blocks. Will be scaled based on world, rounded to the nearest
     *          <code>offsetsAreMultiplesOfBlocks</code> (likely 16) blocks, then subtracted from the player's real X
     *          coordinate.
     * @param z Z offset value in blocks. Will be scaled based on world, rounded to the nearest
     *          <code>offsetsAreMultiplesOfBlocks</code> (likely 16) blocks, then subtracted from the player's real Z
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
     *
     * @deprecated Alignment is now a server configuration, <code>offsetsAreMultiplesOfBlocks</code>. Check the
     * configured value with <code>CoordinateOffset.api().getConfig().getOffsetsAreMultiplesOf();</code> instead of
     * using this hardcoded constant.
     */
    @Deprecated(forRemoval = true)
    int ALIGN_DEFAULT_OVERWORLD = 3;

    /**
     * Align offset components to the nearest N chunks and create a new {@link ScalableOffset}.
     *
     * <p>N is derived from the server configuration <code>offsetsAreMultiplesOfBlocks</code>. In most cases, this
     * method rounds offset components to the nearest 1 chunk; however, higher values of N may be used for compatibility
     * with plugins like Distant Horizons.</p>
     *
     * @param x X offset value in blocks. Will be scaled based on world, rounded to the nearest
     *          <code>offsetsAreMultiplesOfBlocks</code> (likely 16) blocks, then subtracted from the player's real X
     *          coordinate.
     * @param z Z offset value in blocks. Will be scaled based on world, rounded to the nearest
     *          <code>offsetsAreMultiplesOfBlocks</code> (likely 16) blocks, then subtracted from the player's real Z
     *          coordinate.
     * @return A new ScalableOffset with provided X and Z components rounded to the nearest multiple of 8*16.
     */
    static ScalableOffset align(int x, int z) {
        return new ScalableOffset(
            alignComponentToConfiguredMultiple(x),
            alignComponentToConfiguredMultiple(z)
        );
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
     * <p>Results are guaranteed to be multiples of the globally configured <code>offsetsAreMultiplesOfBlocks</code>
     * value.</p>
     *
     * @param bound Maximum absolute value of the generated offset's X and Z components.
     * @return A new ScalableOffset with components divisible by <code>offsetsAreMultiplesOfBlocks</code>.
     */
    static ScalableOffset random(int bound) {
        return new ScalableOffset(
            Offset.alignComponentToConfiguredMultiple(ScalableOffset.RANDOM.nextInt(bound)),
            Offset.alignComponentToConfiguredMultiple(ScalableOffset.RANDOM.nextInt(bound))
        );
    }

    /**
     * Create a new random {@link ScalableOffset} with components between negative and positive values of the given
     * bound.
     *
     * <p>Components are rounded to the nearest multiple of 2^N chunks. For minimal rounding, use
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
     * @deprecated Configuration now allows the user to specify the desired chunk alignment with
     * <code>offsetsAreMultiplesOfBlocks</code>. Use {@link #random(int)} instead.
     */
    @Deprecated(forRemoval = true)
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
        return Math.round((float) component / (1 << (alignToChunksPower + 4))) * (1 << (alignToChunksPower + 4));
    }

    /**
     * Align a single component (X- or Z-value) to the nearest multiple of the <code>offsetsAreMultiplesOfBlocks</code>
     * value that has been configured globally for the server.
     *
     * @param component X- or Z-value to align.
     * @return The aligned component.
     */
    static int alignComponentToConfiguredMultiple(int component) {
        int configuredMultiple = CoordinateOffset.api().getConfig().getOffsetsAreMultiplesOfBlocks();
        return Math.round((float) component / configuredMultiple) * configuredMultiple;
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

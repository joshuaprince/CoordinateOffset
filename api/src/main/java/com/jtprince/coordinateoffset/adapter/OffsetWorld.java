package com.jtprince.coordinateoffset.adapter;

import org.jspecify.annotations.NullMarked;

import java.util.UUID;

/**
 * Adapter interface representing a world in a Minecraft server.
 *
 * <p>The name does NOT imply that an offset was already applied. It refers to any World object in the different
 * platforms.</p>
 */
@NullMarked
public interface OffsetWorld {
    UUID getUuid();

    /**
     * Get the name of the world.
     * @return Example <code>world</code> or <code>world_nether</code>.
     */
    String getName();

    /**
     * Get the key of the world.
     * @return Example <code>minecraft:overworld</code> or <code>minecraft:the_nether</code>.
     */
    String getKey();

    /**
     * Get the scaling factor for this world. This is used to scale the offset by the world's size.
     * Offsets are <b>divided</b> by this factor. For example, the default nether has a scaling factor of 8.
     * @return Scaling factor for this world.
     */
    Double getCoordinateScale();

    /**
     * Get the underlying platform-specific player object, for example a Bukkit World.
     */
    Object getPlatformPlayerObject();
}

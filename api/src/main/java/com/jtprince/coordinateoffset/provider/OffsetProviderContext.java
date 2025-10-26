package com.jtprince.coordinateoffset.provider;

import com.jtprince.coordinateoffset.Offset;
import com.jtprince.coordinateoffset.adapter.OffsetLocation;
import com.jtprince.coordinateoffset.adapter.OffsetPlayer;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

/**
 * Container for relevant information when calculating a new {@link Offset}.
 *
 * @param player The Player that will receive this new Offset.
 * @param previousLocation The previous location of the Player before this Offset begins to take effect. If the player
 *                         is joining the server or the death location is unknown for a respawn, this will be
 *                         <code>null</code>.
 * @param playerLocation The real location that the Player will be at as soon as this Offset begins to take effect.
 *                       Note that this may be different from <code>player.getLocation()</code> because the Provider is
 *                       called <i>before</i> a teleport completes.
 * @param reason The reason that a new Offset is being requested.
 */
@NullMarked
public record OffsetProviderContext(
    OffsetPlayer player,
    @Nullable OffsetLocation previousLocation,
    OffsetLocation playerLocation,
    @Nullable Offset previousOffset,
    ProvideReason reason
) {
    public enum ProvideReason {
        /**
         * A player's offset is being generated because they are joining the server. They may or may not have played
         * on the server previously.
         */
        JOIN,

        /**
         * A player's offset is being generated because they died and are about to respawn.
         */
        DEATH_RESPAWN,

        /**
         * A player's offset is being generated because they are changing worlds, either through a Nether portal, End
         * portal, or any teleport across worlds.
         */
        WORLD_CHANGE,

        /**
         * A player's offset is being generated because they are teleporting within the same world.
         */
        TELEPORT,

        /**
         * A player's offset is being regenerated immediately because someone executed a command (such as
         * <code>/offset regenerate</code>).
         */
        COMMAND_REGENERATE,

        /**
         * A player's offset is being generated immediately because a third-party plugin requested it.
         */
        PLUGIN_REGENERATE,
    }

    /**
     * @deprecated Use {@link #playerLocation()} - {@link OffsetLocation#getWorld()} instead.
     */
    @Deprecated
    public String worldName() {
        return playerLocation.getWorld().getName();
    }
}

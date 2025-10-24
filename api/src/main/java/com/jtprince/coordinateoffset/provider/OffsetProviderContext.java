package com.jtprince.coordinateoffset.provider;

import com.jtprince.coordinateoffset.Offset;
import com.jtprince.coordinateoffset.adapter.OffsetLocation;
import com.jtprince.coordinateoffset.adapter.OffsetPlayer;
import org.jspecify.annotations.NullMarked;

/**
 * Container for relevant information when calculating a new {@link Offset}.
 *
 * @param player The Player that will receive this new Offset.
 * @param worldName Name of the World this Offset will apply to. Note that this may be different from
 *                  <code>player.getWorld()</code> because the Provider is called <i>before</i> a world change.
 * @param playerLocation The real location that the Player will be at as soon as this Offset begins to take effect.
 *                       Note that this may be different from <code>player.getLocation()</code> because the Provider is
 *                       called <i>before</i> a teleport completes.
 * @param reason The reason that a new Offset is being requested.
 */
@NullMarked
public record OffsetProviderContext(
    OffsetPlayer player,
    String worldName,
    OffsetLocation playerLocation,
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
         * A player's offset is being generated because they are teleporting within the same world. The teleport must
         * be far enough such that there are no chunks visible both before and after the teleport.
         */
        DISTANT_TELEPORT,

        /**
         * A player's offset is being generated immediately because someone executed a command (such as
         * /offset regenerate).
         */
        COMMAND,

        /**
         * A player's offset is being generated immediately because a third-party plugin requested it.
         */
        PLUGIN,
    }
}

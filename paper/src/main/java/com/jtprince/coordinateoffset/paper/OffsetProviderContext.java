package com.jtprince.coordinateoffset.paper;

import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

/** TODO DELETE */
public record OffsetProviderContext(
    @NotNull Player player,
    @NotNull World world,
    @NotNull Location playerLocation,
    @NotNull ProvideReason reason,
    @NotNull CoordinateOffsetPaperPlugin plugin
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
    }
}

package com.jtprince.coordinateoffset;

import org.jspecify.annotations.NullMarked;

@NullMarked
public enum CoordinateOffsetPermission {
    BYPASS("coordinateoffset.bypass",
        "Players with this permission will never have their coordinates offsetted."),
    QUERY_SELF("coordinateoffset.query",
        "Allows use of commands /offset and /offset query, which prints a player's own offset and coordinates."),
    QUERY_OTHERS("coordinateoffset.query.others",
        "Allows use of command /offset query <player>, which prints another player's offset and coordinates."),
    RELOAD("coordinateoffset.reload",
        "Players with this permission can use the /offset reload command to reload the plugin."),
    RESET("coordinateoffset.reset",
        "Allows use of the command /offset reset, which regenerates the player's offset using configured offset providers."),
    RESET_OTHERS("coordinateoffset.reset.others",
        "Allows use of the command /offset reset <player>, which regenerates another player's offset using configured offset providers."),
    SET("coordinateoffset.set",
        "Allows use of the command /offset set, which sets the player's offset to a specific value."),
    SET_OTHERS("coordinateoffset.set.others",
        "Allows use of the command /offset set <player>, which sets another player's offset to a specific value.");

    public final String node;
    public final String description;

    CoordinateOffsetPermission(String node, String description) {
        // All permissions default to Operators only
        this.node = node;
        this.description = description;
    }

    @Override
    public String toString() {
        return node;
    }
}

package com.jtprince.coordinateoffset;

import org.jspecify.annotations.NullMarked;

import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;

@NullMarked
public enum CoordinateOffsetPermission {
    ALL("coordinateoffset.*",
        "Players with this permission will have all other permissions granted."),
    BYPASS("coordinateoffset.bypass",
        "Players with this permission will never have their coordinates offsetted."),
    QUERY_SELF("coordinateoffset.query",
        "Allows use of commands /offset and /offset query, which prints a player's own offset and coordinates."),
    QUERY_OTHERS("coordinateoffset.query.others",
        "Allows use of command /offset query <player>, which prints another player's offset and coordinates."),
    QUERY_VERBOSE("coordinateoffset.query.verbose",
        "Players with this permission see the name of the offset provider which generated an offset in /offset query."),
    RELOAD("coordinateoffset.reload",
        "Players with this permission can use the /offset reload command to reload the plugin."),
    REGENERATE_SELF("coordinateoffset.regenerate",
        "Allows use of the command /offset regenerate, which regenerates the player's offset using configured offset providers."),
    REGENERATE_OTHERS("coordinateoffset.regenerate.others",
        "Allows use of the command /offset regenerate <player>, which regenerates another player's offset using configured offset providers."),
    SET_SELF("coordinateoffset.set",
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

    public Set<CoordinateOffsetPermission> getChildren() {
        return switch (this) {
            case ALL -> Arrays.stream(values()).filter(p -> p != ALL).collect(Collectors.toSet());
            case QUERY_OTHERS -> Set.of(QUERY_SELF);
            case REGENERATE_OTHERS -> Set.of(REGENERATE_SELF);
            case SET_OTHERS -> Set.of(SET_SELF);
            default -> Set.of();
        };
    }

    @Override
    public String toString() {
        return node;
    }
}

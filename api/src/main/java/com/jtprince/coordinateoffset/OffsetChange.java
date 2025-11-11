package com.jtprince.coordinateoffset;

import org.jspecify.annotations.Nullable;

/**
 * Container for all data associated with a player's offset changing.
 *
 * <p>This record is immutable and represents the final result of an offset change; i.e., it is created after the
 * new offset has already been determined.</p>
 *
 * @param previousOffsetData The previous offset data for the player, or null if the player is joining the server.
 * @param newOffsetData The new offset data for the player.
 */
public record OffsetChange(
    @Nullable OffsetData previousOffsetData,
    OffsetData newOffsetData
) {
    public boolean offsetChanged() {
        return previousOffsetData == null || !previousOffsetData.offset().equals(newOffsetData.offset());
    }
}

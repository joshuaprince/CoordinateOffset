package com.jtprince.coordinateoffset;

import org.jspecify.annotations.Nullable;

/**
 * Container for all data associated with a player's offset changing.
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

    public @Nullable String getCommandSenderResponse() {
        return switch (newOffsetData.source()) {
            case OffsetData.Source.BedrockBypass ignored -> "Offsets are not supported for Bedrock players.";
            case OffsetData.Source.PermissionBypass ignored -> "Player has permission to bypass offsets.";
            case OffsetData.Source.Provider ignored -> null;
            case OffsetData.Source.SetCommand ignored -> null;
        };
    }
}

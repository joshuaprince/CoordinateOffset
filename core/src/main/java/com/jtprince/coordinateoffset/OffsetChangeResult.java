package com.jtprince.coordinateoffset;

import org.jspecify.annotations.Nullable;

public record OffsetChangeResult(
    OffsetData offset,
    boolean offsetChanged
) {
    public @Nullable String getCommandSenderResponse() {
        return switch (offset.source()) {
            case OffsetData.Source.BedrockBypass ignored -> "Offsets are not supported for Bedrock players.";
            case OffsetData.Source.PermissionBypass ignored -> "Player has permission to bypass offsets.";
            case OffsetData.Source.Provider ignored -> null;
            case OffsetData.Source.SetCommand ignored -> null;
        };
    }
}

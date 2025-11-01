package com.jtprince.coordinateoffset;

import org.jspecify.annotations.Nullable;

public record OffsetChangeResult(
    CreatedOffset offset,
    boolean offsetChanged
) {
    public @Nullable String getCommandSenderResponse() {
        return switch (offset.source()) {
            case CreatedOffset.Source.BedrockBypass ignored -> "Offsets are not supported for Bedrock players.";
            case CreatedOffset.Source.PermissionBypass ignored -> "Player has permission to bypass offsets.";
            case CreatedOffset.Source.Provider ignored -> null;
            case CreatedOffset.Source.SetCommand ignored -> null;
        };
    }
}

package com.jtprince.coordinateoffset.config;

import com.jtprince.coordinateoffset.OffsetProvider;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

import java.util.UUID;

@NullMarked
public record OffsetProviderOverrideConfig(
    OffsetProvider provider,
    @Nullable String world,
    @Nullable String permission,
    @Nullable UUID playerUuid
) {}

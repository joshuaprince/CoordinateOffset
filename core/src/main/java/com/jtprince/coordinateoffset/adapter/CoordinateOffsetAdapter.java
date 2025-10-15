package com.jtprince.coordinateoffset.adapter;

import com.jtprince.coordinateoffset.config.CoordinateOffsetConfig;
import com.jtprince.coordinateoffset.config.CoordinateOffsetProviderConfig;
import com.jtprince.coordinateoffset.provider.util.PlayerOffsetPersistence;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

import java.util.UUID;
import java.util.logging.Logger;

@NullMarked
public interface CoordinateOffsetAdapter {
    CoordinateOffsetConfig getConfig();
    CoordinateOffsetProviderConfig getProviderConfig();
    void reloadConfig(boolean logMessage);

    Logger getLogger();

    @Nullable OffsetPlayer getPlayer(UUID playerUuid);
    OffsetPlayer adaptPlayer(Object platformPlayerObject);

    PlayerOffsetPersistence getPlayerOffsetPersistence();
}

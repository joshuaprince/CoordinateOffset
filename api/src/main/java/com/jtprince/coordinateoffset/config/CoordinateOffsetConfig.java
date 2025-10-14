package com.jtprince.coordinateoffset.config;

import com.jtprince.coordinateoffset.OffsetProvider;

import java.util.List;
import java.util.Map;

public interface CoordinateOffsetConfig {
    OffsetProvider getDefaultOffsetProviderConfig();
    List<OffsetProviderOverrideConfig> getOffsetProviderOverrides();
    Map<String, OffsetProvider> getAllOffsetProviderConfigs();

    boolean getFixCollisionBamboo();
    boolean getFixCollisionDripstone();
    boolean getBypassByPermission();
    boolean getObfuscateWorldBorder();
    boolean getVerbose();
    boolean getDebugEnable();
    int getDebugPacketHistorySize();

    boolean getUnsafeResetOnDistantTeleport();
}

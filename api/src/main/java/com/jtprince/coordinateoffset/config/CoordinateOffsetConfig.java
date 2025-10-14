package com.jtprince.coordinateoffset.config;

import com.jtprince.coordinateoffset.provider.OffsetProvider;
import org.jspecify.annotations.NullMarked;

import java.util.List;
import java.util.Map;

/**
 * Configuration interface for CoordinateOffset. Provides access to various settings and offset provider configurations
 * known to CoordinateOffset.
 *
 * <p>See <a href="https://github.com/joshuaprince/CoordinateOffset/wiki/Configuration-Guide">Configuration Guide</a>
 * for information about individual settings.</p>
 */
@NullMarked
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

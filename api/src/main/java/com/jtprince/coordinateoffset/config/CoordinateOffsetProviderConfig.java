package com.jtprince.coordinateoffset.config;

import com.jtprince.coordinateoffset.provider.OffsetProvider;
import org.jspecify.annotations.NullMarked;

import java.util.List;
import java.util.Map;

/**
 * Configuration interface for Offset Providers in CoordinateOffset.
 *
 * <p>See {@link CoordinateOffsetConfig} for general configuration access.</p>
 *
 * <p>See <a href="https://github.com/joshuaprince/CoordinateOffset/wiki/Configuration-Guide">Configuration Guide</a>
 * for information about how providers work.</p>
 */
@NullMarked
public interface CoordinateOffsetProviderConfig {
    OffsetProvider getDefaultOffsetProviderConfig();
    List<OffsetProviderOverrideConfig> getOffsetProviderOverrides();
    Map<String, OffsetProvider> getAllOffsetProviderConfigs();
}

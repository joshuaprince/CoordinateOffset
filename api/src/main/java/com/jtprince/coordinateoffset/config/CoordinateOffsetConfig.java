package com.jtprince.coordinateoffset.config;

import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

import java.util.SequencedMap;

/**
 * Configuration interface for CoordinateOffset. Provides access to various settings configured in the CoordinateOffset
 * configuration file.
 *
 * <p>Configured Offset Providers are NOT accessible here because they load after the main CoordinateOffset config.
 * See {@link CoordinateOffsetProviderConfig} for provider-specific configuration access.</p>
 *
 * <p>See <a href="https://github.com/joshuaprince/CoordinateOffset/wiki/Configuration-Guide">Configuration Guide</a>
 * for information about individual settings.</p>
 */
@NullMarked
public interface CoordinateOffsetConfig {
    @Nullable Integer getConfigVersion();
    boolean getBypassByPermission();
    boolean getFixCollisionBamboo();
    boolean getFixCollisionDripstone();
    boolean getObfuscateWorldBorder();
    boolean getObfuscateDebugPropertySubscriptions();
    int getOffsetsAreMultiplesOfBlocks();
    boolean getVerbose();
    SequencedMap<String, Double> getWorldCoordinateScaleOverrides();
}

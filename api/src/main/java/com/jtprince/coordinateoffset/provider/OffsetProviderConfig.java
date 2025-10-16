package com.jtprince.coordinateoffset.provider;

import org.jspecify.annotations.NullMarked;

import java.util.SequencedMap;

@NullMarked
public interface OffsetProviderConfig {
    String getUserDefinedProviderName();

    String getProviderClassName();

    SequencedMap<String, Object> getConfigSection();
}

package com.jtprince.coordinateoffset.config;

import com.jtprince.coordinateoffset.provider.OffsetProviderConfig;
import org.jspecify.annotations.NullMarked;

import java.util.SequencedMap;

@NullMarked
public class OffsetProviderConfigImpl implements OffsetProviderConfig {
    private final String providerName;
    private final String providerClassName;
    private final SequencedMap<String, Object> configSection;

    public OffsetProviderConfigImpl(String providerName, String providerClassName, SequencedMap<String, Object> configSection) {
        this.providerName = providerName;
        this.providerClassName = providerClassName;
        this.configSection = configSection;
    }

    @Override
    public String getUserDefinedProviderName() {
        return providerName;
    }

    @Override
    public String getProviderClassName() {
        return providerClassName;
    }

    @Override
    public SequencedMap<String, Object> getConfigSection() {
        return configSection;
    }
}

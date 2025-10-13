package com.jtprince.coordinateoffset;

import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;

@NullMarked
public class OffsetProviderRegistryImpl implements OffsetProviderRegistry {
    private final Map<String, OffsetProvider.ConfigurationFactory<? extends OffsetProvider>> providers = new HashMap<>();

    @Override
    public void registerProviderClass(String className, OffsetProvider.ConfigurationFactory<? extends OffsetProvider> factory) {
        providers.put(className, factory);
    }

    public OffsetProvider.@Nullable ConfigurationFactory<? extends OffsetProvider> getProviderFactory(String className) {
        return providers.get(className);
    }
}

package com.jtprince.coordinateoffset;

import com.jtprince.coordinateoffset.provider.OffsetProvider;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;

@NullMarked
public class OffsetProviderRegistry {
    private final Map<String, OffsetProvider.ConfigurationFactory<? extends OffsetProvider>> providers = new HashMap<>();

    public void registerProviderClass(String className, OffsetProvider.ConfigurationFactory<? extends OffsetProvider> factory) {
        if (providers.containsKey(className)) {
            throw new IllegalArgumentException("Attempted to re-register Offset provider class: " + className);
        }
        providers.put(className, factory);
    }

    public OffsetProvider.@Nullable ConfigurationFactory<? extends OffsetProvider> getProviderFactory(String className) {
        return providers.get(className);
    }
}

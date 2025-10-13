package com.jtprince.coordinateoffset;

import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

@NullMarked
public interface OffsetProviderRegistry {
    void registerProviderClass(String className, OffsetProvider.ConfigurationFactory<? extends OffsetProvider> factory);
    OffsetProvider.@Nullable ConfigurationFactory<? extends OffsetProvider> getProviderFactory(String className);
}

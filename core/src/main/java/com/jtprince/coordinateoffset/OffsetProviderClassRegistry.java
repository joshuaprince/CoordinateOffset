package com.jtprince.coordinateoffset;

import com.jtprince.coordinateoffset.provider.OffsetProvider;
import com.jtprince.coordinateoffset.provider.OffsetProviderConfig;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

@NullMarked
public class OffsetProviderClassRegistry {
    /**
     * Record for storing a registered provider class.
     * @param className The class name of the provider, e.g. "RandomOffsetProvider".
     * @param isCore If true, the provider is built-in to CoordinateOffset. If false, the provider was added by an
     *               API consumer.
     * @param deserializeFunction A function that can deserialize an OffsetProviderConfig into an OffsetProvider.
     */
    public record RegisteredProviderClass(
        String className,
        boolean isCore,
        Function<OffsetProviderConfig, OffsetProvider> deserializeFunction
    ) {}
    private final Map<String /* className */, RegisteredProviderClass> registeredProviders = new HashMap<>();

    public void registerProviderClass(
        String className,
        boolean isCore,
        Function<OffsetProviderConfig, OffsetProvider> deserializeFunction
    ) {
        if (registeredProviders.containsKey(className)) {
            throw new IllegalArgumentException("Attempted to re-register Offset provider class: " + className);
        }
        registeredProviders.put(className, new RegisteredProviderClass(className, isCore, deserializeFunction));
    }

    public @Nullable RegisteredProviderClass getRegisteredProviderClass(String className) {
        return registeredProviders.get(className);
    }
}

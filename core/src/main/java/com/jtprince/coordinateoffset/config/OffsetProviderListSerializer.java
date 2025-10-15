package com.jtprince.coordinateoffset.config;

import com.jtprince.coordinateoffset.CoordinateOffsetCore;
import com.jtprince.coordinateoffset.provider.OffsetProvider;
import de.exlll.configlib.Serializer;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.SequencedMap;

public class OffsetProviderListSerializer implements Serializer<SequencedMap<String, OffsetProvider>, SequencedMap<String, ?>> {
    @Override
    public SequencedMap<String, ?> serialize(SequencedMap<String, OffsetProvider> map) {
        SequencedMap<String, Object> providers = new LinkedHashMap<>();
        for (Map.Entry<String, OffsetProvider> entry : map.entrySet()) {
            SequencedMap<String, Object> serialized = new LinkedHashMap<>();
            serialized.put("class", entry.getValue().getClass().getSimpleName());
            serialized.putAll(entry.getValue().serialize());
            providers.put(entry.getKey(), serialized);
        }
        return providers;
    }

    @Override
    public SequencedMap<String, OffsetProvider> deserialize(SequencedMap<String, ?> element) {
        SequencedMap<String, OffsetProvider> providers = new LinkedHashMap<>();
        if (!CoordinateOffsetCore.get().areAllProvidersLoaded()) {
            /*
             * Wait until all providers are registered. Until then, other config needs to load, so just return an
             * empty list. The platform will call reload() after all providers are registered.
             */
            return providers;
        }
        for (Map.Entry<String, ?> entry : element.entrySet()) {
            if (!(entry.getValue() instanceof Map<?, ?> providerMap)) {
                throw new IllegalArgumentException("Invalid provider config for key " + entry.getKey());
            }
            if (!providerMap.containsKey("class") || !(providerMap.get("class") instanceof String className)) {
                throw new IllegalArgumentException("Missing or invalid field 'class' for provider " + entry.getKey());
            }
            OffsetProvider.ConfigurationFactory<? extends OffsetProvider> factory =
                CoordinateOffsetCore.get().getProviderRegistry().getProviderFactory(className);
            if (factory == null) {
                throw new IllegalArgumentException("Unknown provider class " + className + " for provider " + entry.getKey());
            }

            OffsetProvider provider = factory.deserialize(entry.getKey(), (Map<String, ?>) providerMap);
            providers.put(entry.getKey(), provider);
        }
        return providers;
    }
}

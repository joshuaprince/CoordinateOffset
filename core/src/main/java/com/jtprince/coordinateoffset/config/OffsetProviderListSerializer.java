package com.jtprince.coordinateoffset.config;

import com.jtprince.coordinateoffset.CoordinateOffset;
import com.jtprince.coordinateoffset.OffsetProvider;
import de.exlll.configlib.Serializer;

import java.util.HashMap;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;

public class OffsetProviderListSerializer implements Serializer<Map<String, OffsetProvider>, Map<String, ?>> {
    @Override
    public Map<String, ?> serialize(Map<String, OffsetProvider> map) {
        HashMap<String, Object> providers = new HashMap<>();
        for (Map.Entry<String, OffsetProvider> entry : map.entrySet()) {
            SortedMap<String, Object> serialized = new TreeMap<>();
            serialized.put("class", entry.getValue().getClass().getSimpleName());
            serialized.putAll(entry.getValue().serialize());
            providers.put(entry.getKey(), serialized);
        }
        return providers;
    }

    @Override
    public Map<String, OffsetProvider> deserialize(Map<String, ?> element) {
        HashMap<String, OffsetProvider> providers = new HashMap<>();
        for (Map.Entry<String, ?> entry : element.entrySet()) {
            if (!(entry.getValue() instanceof Map<?, ?> providerMap)) {
                throw new IllegalArgumentException("Invalid provider config for key " + entry.getKey());
            }
            if (!providerMap.containsKey("class") || !(providerMap.get("class") instanceof String className)) {
                throw new IllegalArgumentException("Missing or invalid field 'class' for provider " + entry.getKey());
            }
            OffsetProvider.ConfigurationFactory<? extends OffsetProvider> factory =
                CoordinateOffset.get().getProviderRegistry().getProviderFactory(className);
            if (factory == null) {
                throw new IllegalArgumentException("Unknown provider class " + className + " for provider " + entry.getKey());
            }

            OffsetProvider provider = factory.createProvider(entry.getKey(), (Map<String, ?>) providerMap);
            providers.put(entry.getKey(), provider);
        }
        return providers;
    }
}

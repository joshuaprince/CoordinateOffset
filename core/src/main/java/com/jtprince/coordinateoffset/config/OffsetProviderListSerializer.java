package com.jtprince.coordinateoffset.config;

import com.jtprince.coordinateoffset.CoordinateOffsetCore;
import com.jtprince.coordinateoffset.OffsetProviderClassRegistry;
import com.jtprince.coordinateoffset.provider.OffsetProvider;
import de.exlll.configlib.Serializer;
import org.jspecify.annotations.NullMarked;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.SequencedMap;

@NullMarked
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
    public SequencedMap<String, OffsetProvider> deserialize(SequencedMap<String, ?> element) throws IllegalArgumentException {
        if (!CoordinateOffsetCore.get().areAllProvidersLoaded()) {
            throw new IllegalStateException("Cannot deserialize OffsetProvider list until all providers are registered.");
        }

        SequencedMap<String, OffsetProvider> providers = new LinkedHashMap<>();
        for (Map.Entry<String, ?> entry : element.entrySet()) {
            if (!(entry.getValue() instanceof Map<?, ?> providerMap)) {
                throw new IllegalArgumentException("Invalid provider config for key " + entry.getKey());
            }
            if (!providerMap.containsKey("class") || !(providerMap.get("class") instanceof String className)) {
                throw new IllegalArgumentException("Missing or invalid field 'class' for provider \"" + entry.getKey() + "\"");
            }

            OffsetProviderClassRegistry.RegisteredProviderClass clazz =
                CoordinateOffsetCore.get().getProviderRegistry().getRegisteredProviderClass(className);
            if (clazz == null) {
                // Unknown provider class
                // TODO: Replace full failure with best-effort loading for the remaining known provider classes
                // Can't do this now because ConfigLib will just delete any unknown sections when doing update()
                throw new IllegalArgumentException("Unknown provider class " + className + " for provider \"" + entry.getKey() + "\"");
            }

            OffsetProviderConfigImpl providerConfig = new OffsetProviderConfigImpl(
                entry.getKey(),
                className,
                new LinkedHashMap<>((Map<String, ?>) providerMap)
            );
            try {
                OffsetProvider provider = clazz.deserializeFunction().apply(providerConfig);
                providers.put(entry.getKey(), provider);
            } catch (IllegalArgumentException e) {
                String msg = "Failed to read configured offset provider \"" + entry.getKey() + "\" with class " +
                    clazz.className() + ". Check your configuration and look at the error below.";
                throw new IllegalArgumentException(msg, e);
            } catch (Exception e) {
                StringBuilder msg = new StringBuilder();
                msg.append("Failed to read configured offset provider \"").append(entry.getKey())
                    .append("\" with class ").append(clazz.className()).append(". ");
                if (clazz.isCore()) {
                    msg.append("This is a built-in offset provider, please report this as a bug.");
                } else {
                    msg.append("This is NOT a CoordinateOffset bug! Check with the author of ")
                        .append(clazz.className()).append(" before reporting this to CoordinateOffset.");
                }
                throw new IllegalArgumentException(msg.toString(), e);
            }
        }
        return providers;
    }
}

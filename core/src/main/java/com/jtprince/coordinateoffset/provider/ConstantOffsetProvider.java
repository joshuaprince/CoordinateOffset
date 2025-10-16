package com.jtprince.coordinateoffset.provider;

import com.jtprince.coordinateoffset.Offset;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.SequencedMap;

@NullMarked
public class ConstantOffsetProvider extends OffsetProvider {
    final Offset offset;
    @Nullable final Map<String, Double> worldScaling;

    ConstantOffsetProvider(String name, Offset offset, @Nullable Map<String, Double> worldScaling) {
        super(name);
        this.offset = offset;
        this.worldScaling = worldScaling;
    }

    @Override
    public Offset provideOffset(OffsetProviderContext context) {
        if (worldScaling != null) {
            Double scaling = worldScaling.get(context.worldName());
            if (scaling != null) {
                return offset.scaleByDouble(scaling);
            }
        }
        return offset;
    }

    @Override
    public SequencedMap<String, ?> serialize() {
        SequencedMap<String, Object> map = new LinkedHashMap<>();
        map.put("offsetX", (long) offset.x());
        map.put("offsetZ", (long) offset.z());
        if (worldScaling != null) {
            map.put("worldScaling", worldScaling);
        }
        return map;
    }

    public static ConstantOffsetProvider deserialize(OffsetProviderConfig config) throws IllegalArgumentException {
        SequencedMap<String, Object> s = config.getConfigSection();

        if (!s.containsKey("offsetX") || !(s.get("offsetX") instanceof Number offsetXNum)) {
            throw new IllegalArgumentException("Provider \"" + config.getUserDefinedProviderName() +
                "\": Required key `offsetX` for ConstantOffsetProvider is missing or invalid.");
        }
        if (!s.containsKey("offsetZ") || !(s.get("offsetZ") instanceof Number offsetZNum)) {
            throw new IllegalArgumentException("Provider \"" + config.getUserDefinedProviderName() +
                "\": Required key `offsetZ` for ConstantOffsetProvider is missing or invalid.");
        }

        int offsetX = offsetXNum.intValue();
        int offsetZ = offsetZNum.intValue();

        if (Math.abs(offsetX) > OffsetProvider.OFFSET_MAX) {
            throw new IllegalArgumentException("Provider \"" + config.getUserDefinedProviderName() +
                "\": `offsetX` value " + offsetX + " is too large! (Max 30M)");
        }
        if (Math.abs(offsetZ) > OffsetProvider.OFFSET_MAX) {
            throw new IllegalArgumentException("Provider \"" + config.getUserDefinedProviderName() +
                "\": `offsetZ` value " + offsetZ + " is too large! (Max 30M)");
        }

        Map<String, Double> worldScaling = null;
        if (s.containsKey("worldScaling")) {
            if (!(s.get("worldScaling") instanceof Map<?, ?> worldScalingMap)) {
                throw new IllegalArgumentException("Provider \"" + config.getUserDefinedProviderName() +
                    "\": `worldScaling` is not a map.");
            }
            worldScaling = new java.util.HashMap<>();
            for (Map.Entry<?, ?> entry : worldScalingMap.entrySet()) {
                String worldName = entry.getKey().toString();
                if (!(entry.getValue() instanceof Number scalingNum)) {
                    throw new IllegalArgumentException("Provider \"" + config.getUserDefinedProviderName() +
                        "\": `worldScaling` value " + entry.getValue() + " for world \"" + worldName + "\" is not a number.");
                }
                worldScaling.put(worldName, scalingNum.doubleValue());
            }
        }

        return new ConstantOffsetProvider(
            config.getUserDefinedProviderName(),
            new Offset(offsetX, offsetZ),
            worldScaling
        );
    }
}

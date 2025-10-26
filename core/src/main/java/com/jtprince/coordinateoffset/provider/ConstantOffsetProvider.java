package com.jtprince.coordinateoffset.provider;

import com.jtprince.coordinateoffset.Offset;
import org.jspecify.annotations.NullMarked;

import java.util.LinkedHashMap;
import java.util.SequencedMap;

@NullMarked
public final class ConstantOffsetProvider extends CoreOffsetProvider {
    final Offset offset;

    ConstantOffsetProvider(String name, Offset offset) {
        super(name);
        this.offset = offset;
    }

    @Override
    public Offset provideOffset(OffsetProviderContext context) {
        return offset.scaleDownBy(context.playerLocation().getWorld().getCoordinateScale());
    }

    @Override
    public SequencedMap<String, ?> serialize() {
        SequencedMap<String, Object> map = new LinkedHashMap<>();
        map.put("offsetX", (long) offset.x());
        map.put("offsetZ", (long) offset.z());
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

        return new ConstantOffsetProvider(
            config.getUserDefinedProviderName(),
            new Offset(offsetX, offsetZ)
        );
    }

    @Override
    public String getMetricsClassName() {
        return "ConstantOffsetProvider";
    }

    @Override
    public String getMetricsDetails() {
        // No details reported for constant providers.
        return getMetricsClassName();
    }
}

package com.jtprince.coordinateoffset.provider;

import com.jtprince.coordinateoffset.CoordinateOffsetCore;
import com.jtprince.coordinateoffset.Offset;
import com.jtprince.coordinateoffset.ScalableOffset;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

import java.util.LinkedHashMap;
import java.util.Objects;
import java.util.SequencedMap;

@NullMarked
public final class ConstantOffsetProvider extends CoreOffsetProvider {
    private final @Nullable ScalableOffset offset;

    ConstantOffsetProvider(String name, @Nullable ScalableOffset offset) {
        super(name);
        this.offset = offset;
    }

    @Override
    public Offset provideOffset(OffsetProviderContext context) {
        return Objects.requireNonNullElse(offset, Offset.ZERO);
    }

    @Override
    public SequencedMap<String, ?> serialize() {
        SequencedMap<String, Object> map = new LinkedHashMap<>();
        if (offset == null) {
            map.put("offsetX", (long) 0);
            map.put("offsetZ", (long) 0);
        } else {
            map.put("offsetX", (long) offset.x());
            map.put("offsetZ", (long) offset.z());
        }
        return map;
    }

    public static ConstantOffsetProvider deserialize(OffsetProviderConfig config) throws IllegalArgumentException {
        SequencedMap<String, Object> s = config.getConfigSection();

        if (!s.containsKey("offsetX") || !(s.get("offsetX") instanceof Number offsetXNum)) {
            throw new IllegalArgumentException("Provider \"" + config.getUserDefinedProviderName() +
                "\": Required key offsetX for ConstantOffsetProvider is missing or invalid.");
        }
        if (!s.containsKey("offsetZ") || !(s.get("offsetZ") instanceof Number offsetZNum)) {
            throw new IllegalArgumentException("Provider \"" + config.getUserDefinedProviderName() +
                "\": Required key offsetZ for ConstantOffsetProvider is missing or invalid.");
        }

        int offsetX = offsetXNum.intValue();
        int offsetZ = offsetZNum.intValue();

        if (Math.abs(offsetX) > OffsetProvider.OFFSET_MAX) {
            throw new IllegalArgumentException("Provider \"" + config.getUserDefinedProviderName() +
                "\": offsetX value " + offsetX + " is too large! (Max 30M)");
        }
        if (Math.abs(offsetZ) > OffsetProvider.OFFSET_MAX) {
            throw new IllegalArgumentException("Provider \"" + config.getUserDefinedProviderName() +
                "\": offsetZ value " + offsetZ + " is too large! (Max 30M)");
        }

        if (offsetX == 0 && offsetZ == 0) {
            return new ConstantOffsetProvider(config.getUserDefinedProviderName(), null);
        } else {
            ScalableOffset directOffset = Offset.scalable(offsetX, offsetZ);
            ScalableOffset alignedOffset = Offset.align(offsetX, offsetZ);
            if (!directOffset.equals(alignedOffset)) {
                CoordinateOffsetCore.get().getLogger().warning("Provider \"" + config.getUserDefinedProviderName() +
                    "\": Constant offset " + directOffset + " contains a component which is not a multiple of " +
                    CoordinateOffsetCore.get().getConfig().getOffsetsAreMultiplesOfBlocks() +
                    " blocks; the offset will be rounded to " + alignedOffset + " to match the configured " +
                    "offsetsAreMultiplesOfBlocks setting.");
            }
            return new ConstantOffsetProvider(config.getUserDefinedProviderName(), alignedOffset);
        }
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

package com.jtprince.coordinateoffset.config;

import com.jtprince.coordinateoffset.CoordinateOffsetCore;
import de.exlll.configlib.Configuration;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

@NullMarked
@Configuration
public class OffsetMultipleConfig {
    public static final OffsetMultipleConfig AUTO = new OffsetMultipleConfig(null);

    private final @Nullable Integer valueConfigured; // null for "auto"

    private OffsetMultipleConfig(@Nullable Integer valueConfigured) {
        this.valueConfigured = valueConfigured;
    }

    public int getMultiple() {
        if (valueConfigured == null) {
            // "auto" - use server minimum
            return CoordinateOffsetCore.get().getAdapter().getMinimumOffsetMultiple();
        } else {
            return valueConfigured;
        }
    }

    public static class Serializer implements de.exlll.configlib.Serializer<OffsetMultipleConfig, Object> {
        @Override
        public Object serialize(OffsetMultipleConfig config) {
            if (config.valueConfigured == null) return "auto";
            return config.valueConfigured.longValue();
        }

        @Override
        public OffsetMultipleConfig deserialize(Object serialized) {
            if (serialized.toString().equals("auto")) return new OffsetMultipleConfig(null);

            if (serialized instanceof Number n) {
                int i = n.intValue();

                if (i <= 0) {
                    throw new IllegalArgumentException(i + " is not a positive integer");
                }

                if ((i & (i - 1)) != 0) {
                    throw new IllegalArgumentException(i + " is not a power of 2 (16, 32, 64, etc.)");
                }

                if (i < 16) {
                    throw new IllegalArgumentException(i + " is too small for a chunk size of 16x16 blocks");
                }

                return new OffsetMultipleConfig(n.intValue());
            }

            throw new IllegalArgumentException("Could not parse value " + serialized + " as an integer or \"auto\".");
        }
    }

    public String getMetricsString() {
        if (valueConfigured == null) {
            return "auto (" + CoordinateOffsetCore.get().getAdapter().getMinimumOffsetMultiple() + ")";
        } else {
            return valueConfigured.toString();
        }
    }
}

package com.jtprince.coordinateoffset.provider;

import com.jtprince.coordinateoffset.Offset;
import com.jtprince.coordinateoffset.provider.util.RegenerateConfig;
import com.jtprince.coordinateoffset.provider.util.WorldAlignmentConfig;
import org.jspecify.annotations.NullMarked;

import java.util.LinkedHashMap;
import java.util.SequencedMap;

@NullMarked
public class DefaultOffsetProviders {
    static final ConstantOffsetProvider CONSTANT_1024 = new ConstantOffsetProvider(
        "constant",
        new Offset(1024, 1024)
    );
    static final ConstantOffsetProvider CONSTANT_DISABLED = new ConstantOffsetProvider(
        "disabled",
        Offset.ZERO
    );

    static final RandomOffsetProvider RANDOM = new RandomOffsetProvider(
        "random",
        100_000,
        new RegenerateConfig(false, false, false, RegenerateConfig.DEFAULT_MINIMUM_TELEPORT_DISTANCE),
        false,
        RandomOffsetProvider.DEFAULT_PERSISTENCE_KEY,
        WorldAlignmentConfig.DEFAULT
    );

    static final ZeroAtLocationOffsetProvider ZERO_LOC = new ZeroAtLocationOffsetProvider(
        "zeroAtLocation",
        new RegenerateConfig(false, false, false, RegenerateConfig.DEFAULT_MINIMUM_TELEPORT_DISTANCE),
        WorldAlignmentConfig.DEFAULT
    );

    public static SequencedMap<String, OffsetProvider> PROVIDERS = new LinkedHashMap<>();
    static {
        PROVIDERS.put(CONSTANT_1024.name, CONSTANT_1024);
        PROVIDERS.put(CONSTANT_DISABLED.name, CONSTANT_DISABLED);
        PROVIDERS.put(RANDOM.name, RANDOM);
        PROVIDERS.put(RANDOM.name, RANDOM);
        PROVIDERS.put(ZERO_LOC.name, ZERO_LOC);
    }
}

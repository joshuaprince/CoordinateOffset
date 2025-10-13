package com.jtprince.coordinateoffset.provider;

import com.jtprince.coordinateoffset.Offset;
import com.jtprince.coordinateoffset.OffsetProvider;
import com.jtprince.coordinateoffset.provider.util.ResetConfig;
import com.jtprince.coordinateoffset.provider.util.WorldAlignmentConfig;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.SequencedMap;

public class DefaultOffsetProviders {
    static final ConstantOffsetProvider CONSTANT_1024 = new ConstantOffsetProvider(
        "constant",
        new Offset(1024, 1024),
        Map.of("world_nether", 0.125)
    );
    static final ConstantOffsetProvider CONSTANT_DISABLED = new ConstantOffsetProvider(
        "disabled",
        Offset.ZERO,
        null
    );

    static final RandomOffsetProvider RANDOM = new RandomOffsetProvider(
        "random",
        100_000,
        null
    );
    static {
        RANDOM.resetConfig = new ResetConfig(false, false, null);
        RANDOM.isPersistentConfig = false;
        RANDOM.persistenceKeyConfig = RandomOffsetProvider.DEFAULT_PERSISTENCE_KEY;
        RANDOM.worldAlignmentConfig = WorldAlignmentConfig.DEFAULT;
    }

    static final ZeroAtLocationOffsetProvider ZERO_LOC = new ZeroAtLocationOffsetProvider("zeroAtLocation");
    static {
        ZERO_LOC.resetConfig = new ResetConfig(false, false, null);
        ZERO_LOC.worldAlignmentConfig = WorldAlignmentConfig.DEFAULT;
    }

    public static SequencedMap<String, OffsetProvider> PROVIDERS = new LinkedHashMap<>();
    static {
        PROVIDERS.put(CONSTANT_1024.name, CONSTANT_1024);
        PROVIDERS.put(CONSTANT_DISABLED.name, CONSTANT_DISABLED);
        PROVIDERS.put(RANDOM.name, RANDOM);
        PROVIDERS.put(RANDOM.name, RANDOM);
        PROVIDERS.put(ZERO_LOC.name, ZERO_LOC);
    }
}

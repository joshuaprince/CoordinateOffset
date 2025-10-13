package com.jtprince.coordinateoffset.provider;

import com.jtprince.coordinateoffset.Offset;
import com.jtprince.coordinateoffset.OffsetProvider;

import java.util.Map;

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

    // TODO random, zeroAtLocation
    public static Map<String, OffsetProvider> PROVIDERS = Map.of(
        "constant", CONSTANT_1024,
        "disabled", CONSTANT_DISABLED
    );
}

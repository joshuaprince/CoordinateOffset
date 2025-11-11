package com.jtprince.coordinateoffset.provider;

import com.jtprince.coordinateoffset.Offset;
import com.jtprince.coordinateoffset.config.OffsetProviderOverrideConfigImpl;
import com.jtprince.coordinateoffset.provider.util.RegenerateConfig;
import org.jspecify.annotations.NullMarked;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.SequencedMap;

@NullMarked
public class DefaultOffsetProviders {
    static final ConstantOffsetProvider CONSTANT_DISABLED = new ConstantOffsetProvider(
        "disabled",
        null
    );
    static final ConstantOffsetProvider CONSTANT_1024 = new ConstantOffsetProvider(
        "constant",
        Offset.scalable(1024, 1024)
    );

    static final RandomOffsetProvider RANDOM = new RandomOffsetProvider(
        "random",
        100_000,
        new RegenerateConfig(false, false, false, false, RegenerateConfig.DEFAULT_MINIMUM_TELEPORT_DISTANCE, null)
    );

    static final ZeroAtLocationOffsetProvider ZERO_LOC = new ZeroAtLocationOffsetProvider(
        "zeroAtLocation",
        new RegenerateConfig(false, false, false, false, RegenerateConfig.DEFAULT_MINIMUM_TELEPORT_DISTANCE, null)
    );

    static final PermissionOffsetProvider PERMISSION = new PermissionOffsetProvider(
        "permission",
        "coordinateoffset.offset"
    );

    public static SequencedMap<String, OffsetProvider> PROVIDERS = new LinkedHashMap<>();
    static {
        try {
            PROVIDERS.put(CONSTANT_DISABLED.name, CONSTANT_DISABLED);
            PROVIDERS.put(CONSTANT_1024.name, CONSTANT_1024);
            PROVIDERS.put(RANDOM.name, RANDOM);
            PROVIDERS.put(RANDOM.name, RANDOM);
            PROVIDERS.put(ZERO_LOC.name, ZERO_LOC);
            PROVIDERS.put(PERMISSION.name, PERMISSION);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static final List<OffsetProviderOverrideConfigImpl> DEFAULT_OVERRIDES = List.of(
        new OffsetProviderOverrideConfigImpl(CONSTANT_DISABLED.name, "world_the_end", null, null),
        new OffsetProviderOverrideConfigImpl(ZERO_LOC.name, "world_example",
            "coordinateoffset.provider.my_custom_permission", "ExamplePlayerName")
    );
}

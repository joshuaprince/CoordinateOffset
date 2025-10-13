package com.jtprince.coordinateoffset;

import com.jtprince.coordinateoffset.adapter.CoordinateOffsetAdapter;
import com.jtprince.coordinateoffset.provider.ConstantOffsetProvider;
import org.jspecify.annotations.NullMarked;

@NullMarked
public class CoordinateOffsetCoreImpl implements CoordinateOffsetCore {
    private final CoordinateOffsetAdapter adapter;
    private final OffsetProviderRegistry registry;

    private CoordinateOffsetCoreImpl(CoordinateOffsetAdapter adapter) {
        this.adapter = adapter;
        this.registry = new OffsetProviderRegistryImpl();
    }

    public static void bootstrap(CoordinateOffsetAdapter adapter) {
        CoordinateOffset.set(new CoordinateOffsetCoreImpl(adapter));

        // Register built-in providers
        CoordinateOffset.get().getProviderRegistry().registerProviderClass("ConstantOffsetProvider", new ConstantOffsetProvider.ConfigFactory());
    }

    @Override
    public CoordinateOffsetAdapter getAdapter() {
        return adapter;
    }

    @Override
    public OffsetProviderRegistry getProviderRegistry() {
        return registry;
    }
}

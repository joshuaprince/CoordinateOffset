package com.jtprince.coordinateoffset;

import com.jtprince.coordinateoffset.adapter.CoordinateOffsetAdapter;
import com.jtprince.coordinateoffset.provider.ConstantOffsetProvider;
import com.jtprince.coordinateoffset.provider.RandomOffsetProvider;
import com.jtprince.coordinateoffset.provider.ZeroAtLocationOffsetProvider;
import org.jspecify.annotations.NullMarked;

@NullMarked
public class CoordinateOffsetCoreImpl implements CoordinateOffsetCore {
    private final CoordinateOffsetAdapter adapter;

    private final OffsetProviderRegistry registry;
    private final OffsetCreator offsetCreator;
    private final OffsetHolder offsetHolder;

    private CoordinateOffsetCoreImpl(CoordinateOffsetAdapter adapter) {
        this.adapter = adapter;
        this.registry = new OffsetProviderRegistryImpl();
        this.offsetCreator = new OffsetCreator(this);
        this.offsetHolder = new OffsetHolder(this);
    }

    public static void bootstrap(CoordinateOffsetAdapter adapter) {
        CoordinateOffset.set(new CoordinateOffsetCoreImpl(adapter));

        // Register built-in providers
        CoordinateOffset.get().getProviderRegistry().registerProviderClass("ConstantOffsetProvider", new ConstantOffsetProvider.ConfigFactory());
        CoordinateOffset.get().getProviderRegistry().registerProviderClass("RandomOffsetProvider", new RandomOffsetProvider.ConfigFactory());
        CoordinateOffset.get().getProviderRegistry().registerProviderClass("ZeroAtLocationOffsetProvider", new ZeroAtLocationOffsetProvider.ConfigFactory());
    }

    @Override
    public CoordinateOffsetAdapter getAdapter() {
        return adapter;
    }

    @Override
    public OffsetProviderRegistry getProviderRegistry() {
        return registry;
    }

    OffsetCreator getOffsetCreator() {
        return offsetCreator;
    }

    public OffsetHolder getOffsetHolder() {
        return offsetHolder;
    }
}

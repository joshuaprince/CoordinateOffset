package com.jtprince.coordinateoffset;

import com.jtprince.coordinateoffset.adapter.CoordinateOffsetAdapter;
import com.jtprince.coordinateoffset.api.CoordinateOffsetAPI;
import com.jtprince.coordinateoffset.api.CoordinateOffsetAPIImpl;
import com.jtprince.coordinateoffset.config.ConfigHolder;
import com.jtprince.coordinateoffset.config.CoordinateOffsetConfig;
import com.jtprince.coordinateoffset.config.CoordinateOffsetProviderConfig;
import com.jtprince.coordinateoffset.provider.ConstantOffsetProvider;
import com.jtprince.coordinateoffset.provider.RandomOffsetProvider;
import com.jtprince.coordinateoffset.provider.ZeroAtLocationOffsetProvider;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

import java.util.logging.Logger;

@NullMarked
public class CoordinateOffsetCore {
    private static @Nullable CoordinateOffsetCore singleton = null;

    private final CoordinateOffsetAdapter adapter;

    private final ConfigHolder configHolder;
    private final OffsetProviderClassRegistry registry;
    private final OffsetCreator offsetCreator;
    private final OffsetHolder offsetHolder;

    private boolean completedLoading = false;

    private CoordinateOffsetCore(CoordinateOffsetAdapter adapter) {
        this.adapter = adapter;
        this.configHolder = new ConfigHolder(this);
        this.registry = new OffsetProviderClassRegistry();
        this.offsetCreator = new OffsetCreator(this);
        this.offsetHolder = new OffsetHolder(this);
    }

    public static CoordinateOffsetCore bootstrap(CoordinateOffsetAdapter adapter) {
        if (singleton != null) {
            throw new IllegalStateException("CoordinateOffset core is already initialized.");
        }
        CoordinateOffsetCore core = new CoordinateOffsetCore(adapter);
        singleton = core;

        core.configHolder.loadBaseConfig();

        CoordinateOffsetAPI api = new CoordinateOffsetAPIImpl(core);
        CoordinateOffsetAPIImpl.set(api);

        // Register built-in providers
        core.getProviderRegistry().registerProviderClass("ConstantOffsetProvider", true, ConstantOffsetProvider::deserialize);
        core.getProviderRegistry().registerProviderClass("RandomOffsetProvider", true, RandomOffsetProvider::deserialize);
        core.getProviderRegistry().registerProviderClass("ZeroAtLocationOffsetProvider", true, ZeroAtLocationOffsetProvider::deserialize);

        return core;
    }

    public static CoordinateOffsetCore bootstrapForTests(CoordinateOffsetAdapter adapter) {
        // TODO: This is a hack to allow tests to run without a server environment
        CoordinateOffsetCore core = new CoordinateOffsetCore(adapter);
        CoordinateOffsetAPIImpl.set(new CoordinateOffsetAPIImpl(core));
        return core;
    }

    /**
     * Check if all any offset provider classes added by API consumers have had a chance to load.
     *
     * <p>The configuration module must not deserialize Offset Provider config until all provider classes have been
     * registered, or else deserialization will throw an "unknown provider class" exception.</p>
     *
     * @return true if all provider classes are loaded, false if core has not yet received
     */
    public boolean areAllProvidersLoaded() {
        return this.completedLoading;
    }

    public void signalCompletedLoading() {
        if (this.completedLoading) return;
        this.completedLoading = true;
        if (!this.configHolder.loadFullConfig()) {
            adapter.shutdown();
            return;
        }

        // Validate configuration and shutdown if invalid
        if (!configHolder.getProviderConfig().validateBaseAndFullConfig()) {
            adapter.shutdown();
        }
    }

    public CoordinateOffsetAdapter getAdapter() {
        return adapter;
    }

    public OffsetProviderClassRegistry getProviderRegistry() {
        return registry;
    }

    public CoordinateOffsetConfig getConfig() {
        return configHolder.getConfig();
    }

    public CoordinateOffsetProviderConfig getProviderConfig() {
        return configHolder.getProviderConfig();
    }

    public Logger getLogger() {
        return adapter.getLogger();
    }

    OffsetCreator getOffsetCreator() {
        return offsetCreator;
    }

    public OffsetHolder getOffsetHolder() {
        return offsetHolder;
    }

    public static CoordinateOffsetCore get() {
        if (singleton == null) {
            throw new IllegalStateException("CoordinateOffset core is not yet initialized.");
        }
        return singleton;
    }
}

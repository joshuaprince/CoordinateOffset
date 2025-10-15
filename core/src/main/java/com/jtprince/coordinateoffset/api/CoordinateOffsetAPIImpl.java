package com.jtprince.coordinateoffset.api;

import com.jtprince.coordinateoffset.CoordinateOffsetCore;
import com.jtprince.coordinateoffset.Offset;
import com.jtprince.coordinateoffset.adapter.OffsetPlayer;
import com.jtprince.coordinateoffset.config.CoordinateOffsetConfig;
import com.jtprince.coordinateoffset.config.CoordinateOffsetProviderConfig;
import com.jtprince.coordinateoffset.provider.OffsetProvider;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

import java.util.UUID;

@NullMarked
public class CoordinateOffsetAPIImpl implements CoordinateOffsetAPI {
    private final CoordinateOffsetCore core;
    public CoordinateOffsetAPIImpl(CoordinateOffsetCore core) {
        this.core = core;
    }

    @Override
    public Offset getOffset(OffsetPlayer player) {
        return core.getOffsetHolder().getOffset(player);
    }

    @Override
    public @Nullable OffsetPlayer getPlayer(UUID playerUuid) {
        return core.getAdapter().getPlayer(playerUuid);
    }

    @Override
    public OffsetPlayer adaptPlayer(Object platformPlayerObject) {
        return core.getAdapter().adaptPlayer(platformPlayerObject);
    }

    @Override
    public CoordinateOffsetConfig getConfig() {
        return core.getConfig();
    }

    @Override
    public CoordinateOffsetProviderConfig getProviderConfig() {
        return core.getProviderConfig();
    }

    @Override
    public void registerOffsetProviderClass(String className, OffsetProvider.ConfigurationFactory<? extends OffsetProvider> factory) {
        core.getProviderRegistry().registerProviderClass(className, factory);
    }

    public static void set(CoordinateOffsetAPI api) {
        // Helper function to allow setting the singleton from CoordinateOffsetCore, but not expose it as a public API
        CoordinateOffset.set(api);
    }
}

package com.jtprince.coordinateoffset;

import com.jtprince.coordinateoffset.adapter.CoordinateOffsetAdapter;
import com.jtprince.coordinateoffset.config.CoordinateOffsetConfig;
import org.jspecify.annotations.NullMarked;

import java.util.logging.Logger;

@NullMarked
public interface CoordinateOffsetCore {
    CoordinateOffsetAdapter getAdapter();
    OffsetProviderRegistry getProviderRegistry();

    default CoordinateOffsetConfig getConfig() { return getAdapter().getConfig(); }
    default Logger getLogger() { return getAdapter().getLogger(); }
}

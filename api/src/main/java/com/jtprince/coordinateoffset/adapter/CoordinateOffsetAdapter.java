package com.jtprince.coordinateoffset.adapter;

import com.jtprince.coordinateoffset.config.CoordinateOffsetConfig;
import org.jspecify.annotations.NullMarked;

import java.util.logging.Logger;

@NullMarked
public interface CoordinateOffsetAdapter {
    CoordinateOffsetConfig getConfig();
    Logger getLogger();
}

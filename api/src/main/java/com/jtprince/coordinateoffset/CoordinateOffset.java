package com.jtprince.coordinateoffset;

import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

@NullMarked
public class CoordinateOffset {
    private static @Nullable CoordinateOffsetCore coreInstance = null;

    public static CoordinateOffsetCore get() {
        if (coreInstance == null) {
            throw new IllegalStateException("CoordinateOffset is not yet initialized.");
        }
        return coreInstance;
    }

    static void set(CoordinateOffsetCore core) {
        if (coreInstance != null) {
            throw new IllegalStateException("CoordinateOffset is already initialized.");
        }
        coreInstance = core;
    }
}

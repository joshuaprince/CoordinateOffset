package com.jtprince.coordinateoffset.api;

import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

/**
 * Singleton holder for the CoordinateOffsetAPI instance.
 *
 * <p>To get the API instance as an API consumer, call {@link #api()}.</p>
 */
@NullMarked
public class CoordinateOffset {
    private static @Nullable CoordinateOffsetAPI apiSingleton = null;

    /**
     * Get the singleton instance of the CoordinateOffsetAPI.
     *
     * @throws IllegalStateException if the API has not been initialized yet.
     */
    public static CoordinateOffsetAPI api() {
        if (apiSingleton == null) {
            throw new IllegalStateException("CoordinateOffset API is not yet initialized.");
        }
        return apiSingleton;
    }

    static void set(CoordinateOffsetAPI api) {
        if (apiSingleton != null) {
            throw new IllegalStateException("CoordinateOffset API is already initialized.");
        }
        apiSingleton = api;
    }
}

package com.jtprince.coordinateoffset.adapter;

import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

import java.nio.file.Path;
import java.util.UUID;
import java.util.logging.Logger;

@NullMarked
public interface CoordinateOffsetAdapter {
    Path getConfigDir();

    Logger getLogger();

    @Nullable OffsetPlayer getPlayer(UUID playerUuid);
    OffsetPlayer adaptPlayer(Object platformPlayerObject) throws ClassCastException;
    OffsetLocation adaptLocation(Object platformLocationObject) throws ClassCastException;

    /**
     * Get a platform interface into storing persistent offset data on Players.
     */
    OffsetPersistenceAdapter getPersistenceAdapter();

    /**
     * Get a platform interface into applying immediate offset changes.
     */
    OffsetSwapper getOffsetSwapper();

    /**
     * Assert that the current thread is the main server thread, or throw an {@link IllegalStateException} if it is not.
     *
     * @param methodName Name of the method being called. Printed in the exception message for help tracking.
     */
    void assertMainThread(String methodName) throws IllegalStateException;

    /**
     * Initiate internal shutdown of CoordinateOffset due to an error. Should not be called as part of server shutdown.
     */
    void shutdown();
}

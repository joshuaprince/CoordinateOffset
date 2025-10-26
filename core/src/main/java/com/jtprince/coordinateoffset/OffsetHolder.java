package com.jtprince.coordinateoffset;

import com.jtprince.coordinateoffset.adapter.OffsetPlayer;
import com.jtprince.coordinateoffset.provider.OffsetProviderContext;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeoutException;

@NullMarked
public class OffsetHolder {
    private final CoordinateOffsetCore core;
    OffsetHolder(CoordinateOffsetCore core) {
        this.core = core;
    }

    /**
     * Immutable container for player offset data.
     *
     * @param savedWorldOffsets The most recent offset the player has had in each world.
     * @param previousOffset Offset the player had before the most recent offset was applied. This may be the same as
     *                       the current offset.
     * @param currentOffset Offset the player has now and most packets will use.
     * @param nextOffset Offset that the player will have as soon as the server sends the next "position" packet.
     *                   Offset creation logic on the main thread writes to this field, then the Netty thread swaps it
     *                   into current.
     */
    private record PlayerOffsetData(
        Map<String, Offset> savedWorldOffsets,

        Offset previousOffset,
        Offset currentOffset,
        @Nullable Offset nextOffset
    ) {}
    private final ConcurrentHashMap<UUID, PlayerOffsetData> playerOffsetData = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<UUID, Object> pendingDataLocks = new ConcurrentHashMap<>();

    /**
     * Get the current offset applied to a Player.
     *
     * <p>If the player's offset is about to change or changing, this will reflect the *previous* offset until the
     * server sends a "position" packet.</p>
     *
     * This method is safe to call on any thread.
     *
     * @param player Player to query.
     * @return The player's current offset in the world they are in.
     * @throws NoSuchElementException If the player has no offset data or has not yet received a POSITION packet.
     */
    public Offset getOffset(OffsetPlayer player) {
        PlayerOffsetData data = playerOffsetData.get(player.getUuid());
        if (data == null) {
            throw new NoSuchElementException("Player " + player.getName() + " has no offset data!");
        }
        return data.currentOffset;
    }

    /**
     * Look ahead at the next generated offset, which a Player will have after a "position" packet is sent.
     *
     * <p>This is useful for packets like RESPAWN, which refer to coordinates in the world a player is going to before
     * sending a "position" packet to move the player to that world.</p>
     *
     * <p>This should only be called on a Netty thread, but is safe to call on any thread.</p>
     *
     * @param player Player to query.
     * @return The player's next offset in the world they will soon be in, or the current offset if the player has no
     *         next offset.
     */
    public Offset getNextOffset(OffsetPlayer player) {
        PlayerOffsetData data = playerOffsetData.get(player.getUuid());
        if (data == null) {
            throw new NoSuchElementException("Player " + player.getName() + " has no offset data!");
        }
        return (data.nextOffset == null) ? data.currentOffset : data.nextOffset;
    }

    /**
     * Get the most recent offset a Player had in a specific world.
     *
     * <p>This method is safe to call on any thread.</p>
     *
     * @param player Player to query.
     * @param worldName World to query for the offset.
     * @return The player's current offset in the specified world, or null if the player has no known offset data for
     *         that world.
     */
    public @Nullable Offset getSavedWorldOffset(OffsetPlayer player, String worldName) {
        PlayerOffsetData data = playerOffsetData.get(player.getUuid());
        if (data == null) return null;
        return data.savedWorldOffsets.get(worldName);
    }

    /**
     * Block the current thread until the player's offset has been generated, then get that offset.
     *
     * <p>This is useful for JOIN_GAME packets. These packets contain coordinates which must be offset, but the packets
     * are sent concurrently with offset generation occurring on the main thread. See OffsetChangeSequencePaper.md
     * in the project's <code>docs</code> directory for more information.</p>
     *
     * <p>This must only be called on a Netty thread. Blocking the main thread is not acceptable.</p>
     *
     * @param playerUuid Player to query.
     * @param timeoutMillis Maximum time to block the thread if the player's offset has not yet been generated.
     * @return The player's offset in the world they will soon be in.
     * @throws TimeoutException If the player's offset has not yet been generated and the timeout has been reached.
     */
    public Offset waitForJoiningOffset(UUID playerUuid, int timeoutMillis) throws TimeoutException {
        PlayerOffsetData data = playerOffsetData.get(playerUuid);
        if (data == null && timeoutMillis > 0) {
            /*
             * Concurrency hack:
             * Paper *concurrently* (a) calls PlayerJoinEvent and (b) sends a JOIN_GAME packet.
             * The JOIN_GAME packet needs to be offsetted. But the offset isn't generated until PlayerJoinEvent in
             * 1.21.9 (PlayerSpawnLocationEvent is deprecated).
             * This hack is to block the Netty thread until the joining player gets an offset.
             */
            Object pendingOffsetDataLock = pendingDataLocks.computeIfAbsent(playerUuid, uuid -> new Object());
            long now = System.currentTimeMillis();
            long deadline = now + timeoutMillis;
            synchronized (pendingOffsetDataLock) {
                while (data == null && (now = System.currentTimeMillis()) < deadline) {
                    try {
                        pendingOffsetDataLock.wait( deadline - now);
                    } catch (InterruptedException e) {
                        throw new RuntimeException(e);
                    }
                    data = playerOffsetData.get(playerUuid);
                }
            }
        }
        if (data == null) {
            throw new TimeoutException("Player " + playerUuid + " has no offset data!");
        }
        return data.currentOffset;
    }

    /**
     * Generate or regenerate the offset a player will have next based on the context the player will be in.
     * Store that offset in this holder.
     *
     * <p>This must only be called on the main server thread.</p>
     *
     * <p>Note that this does not immediately change the player's offset. Offset changes themselves happen in response
     * to certain "position" packets. However, generating a <code>nextOffset</code> will set up an offset change for
     * when the "position" packet occurs.</p>
     *
     * <p>If is very important that this only be called at specific times, namely when the player is <b>about to</b>
     * join, respawn, or teleport.</p>
     *
     * @param context Offset generation context, containing the player and world that should have an offset regenerated.
     * @return true if the player's offset changed, false if the player's offset was unchanged.
     */
    public boolean generateNextOffset(OffsetProviderContext context) {
        Offset newOffset = core.getOffsetCreator().createOffset(context);
        if (newOffset == null) {
            return false;
        }

        PlayerOffsetData d = playerOffsetData.compute(context.player().getUuid(), (uuid, existingOffsetData) -> {
            if (existingOffsetData == null) {
                debugLog("Generate first: " +
                    newOffset + ", " +
                    newOffset + ", " +
                    null);
                return new PlayerOffsetData(
                    Map.of(context.worldName(), newOffset),
                    newOffset,
                    newOffset,
                    null
                );
            }
            Map<String, Offset> offsetPerWorld = new HashMap<>(existingOffsetData.savedWorldOffsets());
            offsetPerWorld.put(context.worldName(), newOffset);
            debugLog("Generate next: " +
                existingOffsetData.previousOffset + ", " +
                existingOffsetData.currentOffset + ", " +
                newOffset);
            return new PlayerOffsetData(
                Map.copyOf(offsetPerWorld),
                existingOffsetData.previousOffset, // Keep previous the same
                existingOffsetData.currentOffset,  // Keep current the same
                newOffset // Set next
            );
        });

        Object pendingOffsetDataLock = pendingDataLocks.computeIfAbsent(context.player().getUuid(), uuid -> new Object());
        synchronized (pendingOffsetDataLock) {
            pendingOffsetDataLock.notifyAll();
        }

        return (d.nextOffset != null && !d.nextOffset.equals(d.currentOffset));
    }

    /**
     * Shift the player's <code>nextOffset</code> into <code>currentOffset</code>, and <code>currentOffset</code> into
     * <code>previousOffset</code>. Following calls to {@link #getOffset(OffsetPlayer)} will return the new current
     * offset.
     *
     * <p>This should only be called on a Netty thread, but is safe to call on any thread.</p>
     *
     * @param player Player to update.
     */
    public void swapInNextOffset(OffsetPlayer player) {
        playerOffsetData.computeIfPresent(player.getUuid(), (uuid, existingOffsetData) -> {
            if (existingOffsetData.nextOffset == null) {
                // No next offset, so don't swap in anything.
                return existingOffsetData;
            }
            debugLog("Swap in next: " +
                existingOffsetData.currentOffset + ", " +
                existingOffsetData.nextOffset + ", null");
            return new PlayerOffsetData(
                existingOffsetData.savedWorldOffsets,
                existingOffsetData.currentOffset, // Swap current into previous
                existingOffsetData.nextOffset, // Swap next into current
                null // Clear next
            );
        });
    }

    /**
     * Drop all data about a player from this holder. This should be called when a player disconnects from the server.
     *
     * <p>This method is safe to call on any thread.</p>
     *
     * @param uuid The UUID of the player to drop, presumably who is disconnecting from the server.
     */
    public void remove(UUID uuid) {
        playerOffsetData.remove(uuid);
        pendingDataLocks.remove(uuid);
    }

    private void debugLog(String message) {
        if (core.isDebugEnabled()) {
            core.getLogger().info("[Debug] " + message);
        }
    }
}

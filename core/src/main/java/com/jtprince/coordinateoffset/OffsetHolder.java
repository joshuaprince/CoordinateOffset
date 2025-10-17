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
     * @param offsetPerWorld Map of offsets for each world the player is in.
     * @param packetWorld World the player is currently in from packets' perspective. Offsets are applied to packets
     *                    based on this world only. This is updated only when we see a POSITION packet.
     * @param lookaheadWorld World the player has initiated a world change to and will be in soon. Updated whenever
     *                       a server event indicates that a world change is about to happen.
     */
    private record PlayerOffsetData(
        Map<String, Offset> offsetPerWorld,
        @Nullable String packetWorld,
        String lookaheadWorld
    ) {}
    private final ConcurrentHashMap<UUID, PlayerOffsetData> playerOffsetData = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<UUID, Object> pendingDataLocks = new ConcurrentHashMap<>();

    /**
     * Get the current offset applied to a Player.
     *
     * <p>This returns the offset in the world the player is receiving packets for. If the player is currently
     * changing worlds, this will reflect the *previous* world until the server sends a POSITION packet.</p>
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
        if (data.packetWorld == null) {
            throw new NoSuchElementException("Player " + player.getName() + " has not been positioned in a world!");
        }
        return data.offsetPerWorld.get(data.packetWorld);
    }

    /**
     * Get the current offset for a Player in a specific world.
     *
     * @param player Player to query.
     * @param worldName World to query for the offset.
     * @return The player's current offset in the specified world, or null if the player has no offset data for that
     *         world.
     */
    public @Nullable Offset getOffset(OffsetPlayer player, String worldName) {
        PlayerOffsetData data = playerOffsetData.get(player.getUuid());
        if (data == null) return null;
        return data.offsetPerWorld.get(worldName);
    }

    /**
     * Get an offset for a player in the "lookahead world" of that player. The lookahead world is set as soon as a
     * player has a fixed spawn point on join or initiates a world change, but before the player is actually in that
     * world.
     *
     * <p>This is useful for JOIN and RESPAWN packets, which contain coordinates in the world the player is about to
     * enter.</p>
     *
     * @param playerUuid Player to query.
     * @param timeoutMillis Maximum time to block the thread if the player's offset has not yet been generated.
     * @return The player's offset in the world they will soon be in.
     * @throws TimeoutException If the player's offset has not yet been generated and the timeout has been reached.
     */
    public Offset waitForOffsetLookahead(UUID playerUuid, int timeoutMillis) throws TimeoutException {
        PlayerOffsetData data = playerOffsetData.get(playerUuid);
        if (data == null && timeoutMillis > 0) {
            /*
             * Hack below...
             * In 1.21.9+, Paper *concurrently* (a) calls PlayerJoinEvent and (b) sends a JOIN_GAME packet.
             * The JOIN_GAME packet needs to be offsetted. But the offset isn't generated until PlayerJoinEvent.
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
        return data.offsetPerWorld.get(data.lookaheadWorld);
    }

    /**
     * Generate or regenerate the offset for one player in one world and store that offset in this holder.
     *
     * <p>Note that this does not change which world the player is considered to be in for calls to
     * {@link #getOffset(OffsetPlayer)} - it only updates their "lookahead" world until a call to
     * {@link #setPositionedWorld} is made.</p>
     *
     * <p>If is very important that this only be called at specific times, namely when the player is <b>about to</b>
     * join, respawn, or teleport.</p>
     * @param context Offset generation context, containing the player and world that should have an offset regenerated.
     */
    public void regenerateOffset(OffsetProviderContext context) {
        Offset newOffset = core.getOffsetCreator().createOffset(context);

        playerOffsetData.compute(context.player().getUuid(), (uuid, existingOffsetData) -> {
            if (existingOffsetData == null) {
                return new PlayerOffsetData(Map.of(
                    context.worldName(), newOffset),
                    null, // New player, no positioned world yet
                    context.worldName() // Set lookahead world to the world we're generating offsets for
                );
            }
            Map<String, Offset> offsetPerWorld = new HashMap<>(existingOffsetData.offsetPerWorld());
            offsetPerWorld.put(context.worldName(), newOffset);
            return new PlayerOffsetData(
                Map.copyOf(offsetPerWorld),
                existingOffsetData.packetWorld(), // Keep positioned world the same
                context.worldName() // Set lookahead world to the world we're generating offsets for
            );
        });

        Object pendingOffsetDataLock = pendingDataLocks.computeIfAbsent(context.player().getUuid(), uuid -> new Object());
        synchronized (pendingOffsetDataLock) {
            pendingOffsetDataLock.notifyAll();
        }
    }

    /**
     * Update which world a player is receiving packets for. Later calls to {@link #getOffset(OffsetPlayer)}
     * will use this world.
     *
     * @param player Player to update.
     * @param worldName World that the player is now in.
     */
    public void setPositionedWorld(OffsetPlayer player, String worldName) {
        playerOffsetData.compute(player.getUuid(), (uuid, existingOffsetData) -> {
            if (existingOffsetData == null) {
                throw new IllegalStateException("Player " + player.getUuid() + " has no offset data!");
            }
            return new PlayerOffsetData(
                existingOffsetData.offsetPerWorld(),
                worldName,
                existingOffsetData.lookaheadWorld()
            );
        });
    }

    /**
     * Drop all data about a player from this holder. This should be called when a player disconnects from the server.
     *
     * @param uuid The UUID of the player to drop, presumably who is disconnecting from the server.
     */
    public void remove(UUID uuid) {
        playerOffsetData.remove(uuid);
        pendingDataLocks.remove(uuid);
    }
}

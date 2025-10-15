package com.jtprince.coordinateoffset;

import com.jtprince.coordinateoffset.adapter.OffsetPlayer;
import com.jtprince.coordinateoffset.provider.OffsetProvider;
import com.jtprince.coordinateoffset.provider.OffsetProviderContext;
import org.jetbrains.annotations.NotNull;
import org.jspecify.annotations.NullMarked;

import java.util.HashMap;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.UUID;

@NullMarked
public class OffsetHolder {
    private final CoordinateOffsetCore core;
    private final Map<UUID /* player */, Map<String /* world */, Offset>> playerOffsets = new HashMap<>();

    /* There are two worlds cached because of world-change timing issues - see docs/OffsetChangeHandling.md */
    /** World the player is currently in for all intents and purposes (updated by a POSITION packet only) */
    private final Map<UUID /* player */, String /* world */> playerPositionedWorld = new HashMap<>();
    /** World the player has initiated a world change to and will be in soon (updated by Bukkit events) */
    private final Map<UUID /* player */, String /* world */> playerLookaheadWorld = new HashMap<>();

    OffsetHolder(CoordinateOffsetCore core) {
        this.core = core;
    }

    /**
     * Get the current offset for a Player in the world they are known to be positioned in. If the player is currently
     * changing world, this will reflect the previous world - see {@link #getOffsetLookahead}.
     * @param player Player to query.
     * @return The player's current offset in the world they are in.
     */
    public synchronized Offset getOffset(OffsetPlayer player) {
        Map<String, Offset> offsetPerWorldCache = getPerWorldCacheFor(player.getUuid(), player.getName());

        String positionedWorld = playerPositionedWorld.get(player.getUuid());
        if (positionedWorld == null) {
            throw new NoSuchElementException("Can't determine which world player is in: " + player.getName());
        }

        return offsetPerWorldCache.get(positionedWorld);
    }

    /**
     * Get the current offset for a Player in a specific, known world.
     * @param player Player to query.
     * @param worldName World to query for the offset.
     * @return The player's current offset in the specified world.
     */
    public synchronized Offset getOffset(OffsetPlayer player, String worldName) {
        Map<String, Offset> offsetPerWorldCache = getPerWorldCacheFor(player.getUuid(), player.getName());
        return offsetPerWorldCache.get(worldName);
    }

    /**
     * Get an offset for a player in the "lookahead world" of that player. The lookahead world is set as soon as a
     * player has a fixed spawn point on join or initiates a world change, but before the world change completes and
     * the player is actually in that world.
     * This is useful for JOIN and RESPAWN packets. See docs/OffsetChangeHandling.md for details.
     * @param playerUuid Player to query.
     * @return The player's offset in the world they will soon be in.
     */
    public synchronized Offset getOffsetLookahead(UUID playerUuid) {
        Map<String, Offset> offsetPerWorldCache = getPerWorldCacheFor(playerUuid, playerUuid.toString());
        String respawningWorld = playerLookaheadWorld.get(playerUuid);
        if (respawningWorld == null) {
            throw new NoSuchElementException("Can't determine which world player is in: " + playerUuid);
        }

        return offsetPerWorldCache.get(respawningWorld);
    }

    private Map<String, Offset> getPerWorldCacheFor(UUID player, String logName) {
        Map<String, Offset> offsetPerWorldCache = playerOffsets.get(player);
        if (offsetPerWorldCache == null) {
            throw new NoSuchElementException("Unknown player for Offset lookup: " + logName);
        }
        return offsetPerWorldCache;
    }

    /**
     * Generate or regenerate the offset for one player in one world, and store it in the offset manager cache.
     * Note that this does not change which world the player is considered to be in for calls to
     * {@link #getOffset(OffsetPlayer)} - it only updates their "lookahead" world until a call to
     * {@link #setPositionedWorld} is made.
     *
     * <p>If is very important that this only be called at specific times, namely when the player is <b>about to</b>
     * join, respawn, or teleport.</p>
     * @param context Offset generation context, containing the player and world that should have an offset regenerated.
     */
    public synchronized void regenerateOffset(OffsetProviderContext context) {
        Offset newOffset = core.getOffsetCreator().createOffset(context);

        Map<String, Offset> offsetPerWorldCache =
            playerOffsets.computeIfAbsent(context.player().getUuid(), k -> new HashMap<>());
        offsetPerWorldCache.put(context.worldName(), newOffset);

        playerLookaheadWorld.put(context.player().getUuid(), context.worldName());
    }

    /**
     * Update which world a player is considered to be in. Subsequent calls to
     * {@link #getOffset(OffsetPlayer)} will use this world.
     * @param player Player to update.
     * @param worldName World that the player is now in.
     */
    public synchronized void setPositionedWorld(OffsetPlayer player, String worldName) {
        playerPositionedWorld.put(player.getUuid(), worldName);
    }

    /**
     * Drop a player from all caching in this offset manager.
     * @param uuid The UUID of the player to drop, presumably who is disconnecting from the server.
     */
    public synchronized void remove(UUID uuid) {
        playerOffsets.remove(uuid);
        playerLookaheadWorld.remove(uuid);
        playerPositionedWorld.remove(uuid);
    }

    public void quitPlayer(@NotNull OffsetPlayer player) {
        for (OffsetProvider provider : core.getProviderConfig().getAllOffsetProviderConfigs().values()) {
            provider.onPlayerQuit(player);
        }
    }

    public void disconnectPlayer(@NotNull UUID playerUuid) {
        for (OffsetProvider provider : core.getProviderConfig().getAllOffsetProviderConfigs().values()) {
            provider.onPlayerDisconnect(playerUuid);
        }
    }
}

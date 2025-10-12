package com.jtprince.coordinateoffset;

import org.bukkit.World;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.UUID;

/**
 * Container for the offsets all players currently have.
 */
class PlayerOffsetsManager {
    private final CoordinateOffset plugin;

    private final Map<UUID, Map<UUID, Offset>> playerOffsets = new HashMap<>();

    /* There are two worlds cached because of world-change timing issues - see docs/OffsetChangeHandling.md */
    /** World the player is currently in for all intents and purposes (updated by a POSITION packet only) */
    private final Map<UUID, UUID> playerPositionedWorld = new HashMap<>();
    /** World the player has initiated a world change to and will be in soon (updated by Bukkit events) */
    private final Map<UUID, UUID> playerLookaheadWorld = new HashMap<>();

    PlayerOffsetsManager(CoordinateOffset plugin) {
        this.plugin = plugin;
    }

    /**
     * Get the current offset for a Player in the world they are known to be positioned in. If the player is currently
     * changing world, this will reflect the previous world - see {@link PlayerOffsetsManager#getOffsetLookahead}.
     * @param player Player to query.
     * @return The player's current offset in the world they are in.
     */
    synchronized @NotNull Offset getOffset(@NotNull Player player) {
        Map<UUID, Offset> offsetPerWorldCache = getPerWorldCacheFor(player.getUniqueId(), player.getName());

        UUID positionedWorld = playerPositionedWorld.get(player.getUniqueId());
        if (positionedWorld == null) {
            throw new NoSuchElementException("Can't determine which world player is in: " + player.getName());
        }

        return offsetPerWorldCache.get(positionedWorld);
    }

    /**
     * Get the current offset for a Player in a specific, known world.
     * @param player Player to query.
     * @param world World to query for the offset.
     * @return The player's current offset in the specified world.
     */
    synchronized @NotNull Offset getOffset(@NotNull Player player, @NotNull World world) {
        Map<UUID, Offset> offsetPerWorldCache = getPerWorldCacheFor(player.getUniqueId(), player.getName());
        return offsetPerWorldCache.get(world.getUID());
    }

    /**
     * Get an offset for a player in the "lookahead world" of that player. The lookahead world is set as soon as a
     * player has a fixed spawn point on join or initiates a world change, but before the world change completes and
     * the player is actually in that world.
     * This is useful for JOIN and RESPAWN packets. See docs/OffsetChangeHandling.md for details.
     * @param playerUuid Player to query.
     * @return The player's offset in the world they will soon be in.
     */
    synchronized @NotNull Offset getOffsetLookahead(@NotNull UUID playerUuid) {
        Map<UUID, Offset> offsetPerWorldCache = getPerWorldCacheFor(playerUuid, playerUuid.toString());
        UUID respawningWorld = playerLookaheadWorld.get(playerUuid);
        if (respawningWorld == null) {
            throw new NoSuchElementException("Can't determine which world player is in: " + playerUuid);
        }

        return offsetPerWorldCache.get(respawningWorld);
    }

    private @NotNull Map<UUID, Offset> getPerWorldCacheFor(UUID player, String logName) {
        Map<UUID, Offset> offsetPerWorldCache = playerOffsets.get(player);
        if (offsetPerWorldCache == null) {
            throw new NoSuchElementException("Unknown player for Offset lookup: " + logName);
        }
        return offsetPerWorldCache;
    }

    /**
     * Generate or regenerate the offset for one player in one world, and store it in the offset manager cache.
     * Note that this does not change which world the player is considered to be in for calls to
     * {@link PlayerOffsetsManager#getOffset(Player)} - it only updates their "lookahead" world until a call to
     * {@link PlayerOffsetsManager#setPositionedWorld} is made.
     *
     * <p>If is very important that this only be called at specific times, namely when the player is <b>about to</b>
     * join, respawn, or teleport.</p>
     * @param context Offset generation context, containing the player and world that should have an offset regenerated.
     */
    synchronized void regenerateOffset(OffsetProviderContext context) {
        Offset newOffset = plugin.getOffsetProviderManager().provideOffset(context);

        Map<UUID, Offset> offsetPerWorldCache = playerOffsets.computeIfAbsent(context.player().getUniqueId(), k -> new HashMap<>());
        offsetPerWorldCache.put(context.world().getUID(), newOffset);

        playerLookaheadWorld.put(context.player().getUniqueId(), context.world().getUID());
    }

    /**
     * Update which world a player is considered to be in. Subsequent calls to
     * {@link PlayerOffsetsManager#getOffset(Player)} will use this world.
     * @param player Player to update.
     * @param world World that the player is now in.
     */
    synchronized void setPositionedWorld(Player player, World world) {
        playerPositionedWorld.put(player.getUniqueId(), world.getUID());
    }

    /**
     * Drop a player from all caching in this offset manager.
     * @param uuid The UUID of the player to drop, presumably who is disconnecting from the server.
     */
    synchronized void remove(@NotNull UUID uuid) {
        playerOffsets.remove(uuid);
        playerLookaheadWorld.remove(uuid);
        playerPositionedWorld.remove(uuid);
    }
}

package com.jtprince.coordinateoffset.paper.adapter;

import com.destroystokyo.paper.event.server.ServerTickEndEvent;
import com.github.retrooper.packetevents.PacketEvents;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerUnloadChunk;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerUpdateViewPosition;
import com.jtprince.coordinateoffset.CoordinateOffsetCore;
import com.jtprince.coordinateoffset.adapter.OffsetPlayer;
import com.jtprince.coordinateoffset.adapter.OffsetSwapper;
import com.jtprince.coordinateoffset.paper.CoordinateOffsetPaperPlugin;
import io.papermc.paper.FeatureHooks;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.chunk.status.ChunkStatus;
import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.craftbukkit.CraftChunk;
import org.bukkit.craftbukkit.entity.CraftPlayer;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.jspecify.annotations.NullMarked;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Logic to immediately swap a player's offset and simulate a teleport.
 */
@NullMarked
public class PaperOffsetSwapper implements OffsetSwapper, Listener {
    private final CoordinateOffsetPaperPlugin plugin;
    public PaperOffsetSwapper(CoordinateOffsetPaperPlugin plugin) {
        this.plugin = plugin;
    }
    public void initialize() {
        Bukkit.getPluginManager().registerEvents(this, plugin);
    }

    @Override
    public void forceOffsetSwap(OffsetPlayer offsetPlayer) {
        Player player = (Player) offsetPlayer.getPlatformPlayerObject();
        /* Timing of these packets is important. See OffsetChangeSequencePaper.md */

        List<Chunk> chunksClosestFirst = sendUnloadAllSentChunksPackets(player);

        /*
         * This call automatically sends a few useful packets:
         *  - RESPAWN: Needed to update player's death location (recovery compasses), not otherwise necessary (but
         *    makes the "Loading terrain" screen appear)
         *  - PLAYER_POSITION_AND_LOOK: Takes player out of loading screen and puts them at the new coordinates
         *  - SPAWN_POSITION: Sets the player's compass spawn location
         * It doesn't send UPDATE_VIEW_POSITION, so we do that ourselves.
         */
        player.setPlayerProfile(player.getPlayerProfile());

        PacketEvents.getAPI().getPlayerManager().sendPacket(player,
            new WrapperPlayServerUpdateViewPosition(player.getLocation().getChunk().getX(), player.getLocation().getChunk().getZ()));

        refreshChunksAndEntities(player, chunksClosestFirst);
    }

    /**
     * Get a list of chunks that the player has been sent, sorted by ascending distance from the player's current chunk.
     *
     * <p>This must only be called on the main server thread.</p>
     *
     * @param player Player to query.
     * @return List of chunks sorted by distance from the player's current chunk.
     */
    public List<Chunk> getSentChunksClosestFirst(Player player) {
        int cx = player.getChunk().getX();
        int cz = player.getChunk().getZ();
        //noinspection UnstableApiUsage
        return player.getSentChunks().stream()
            .sorted(Comparator.comparing(c ->
                ((c.getX() - cx) * (c.getX() - cx) + (c.getZ() - cz) * (c.getZ() - cz))))
            .toList();
    }

    /**
     * Send packets to tell the client to unload all chunks that the player has been sent.
     *
     * <p>This must only be called on the main server thread.</p>
     *
     * @param player Player to send packets to.
     * @return List of chunks that were unloaded, in ascending distance from the player's current chunk.
     */
    public List<Chunk> sendUnloadAllSentChunksPackets(Player player) {
        List<Chunk> chunksClosestFirst = getSentChunksClosestFirst(player);
        for (Chunk chunk : chunksClosestFirst.reversed()) { // Unload furthest chunks first
            PacketEvents.getAPI().getPlayerManager().sendPacket(player,
                new WrapperPlayServerUnloadChunk(chunk.getX(), chunk.getZ()));
        }
        return chunksClosestFirst;
    }

    /**
     * Forcibly resend all loaded chunks and entities to a player. This function returns immediately, and the refresh
     * sequence may take place over the course of multiple ticks.
     *
     * <p>This must only be called on the main server thread.</p>
     *
     * @param player Player to resend chunks and entities to.
     * @param chunks Chunks to resend.
     */
    public void refreshChunksAndEntities(Player player, List<Chunk> chunks) {
        // Replace any existing task for this player - no harm letting GC get the old one
        chunkRefreshTasks.put(player.getUniqueId(), new ChunkRefreshTask(
            Bukkit.getCurrentTick(),
            player.getWorld().getUID(),
            new ArrayDeque<>(chunks),
            player.getWorld().getEntities().stream()
                .filter(e -> e.getTrackedBy().contains(player))
                .map(Entity::getUniqueId)
                .collect(Collectors.toSet())
        ));
    }

    /*
     * The following code is for spreading chunk refreshes out over multiple ticks and only refreshing chunks when
     * there is extra time between ticks. This prevents chunk refreshes from slowing down the TPS.
     */
    private static final long HEADROOM_NS = 2_000_000L; // save 2ms out of 50ms tick target
    private record ChunkRefreshTask(int startTick, UUID world, Queue<Chunk> chunksLeft, Set<UUID> entitiesLeft) {}
    private final Map<UUID /* player */, ChunkRefreshTask> chunkRefreshTasks = new HashMap<>();
    @EventHandler
    public void onTickEnd(ServerTickEndEvent event) {
        Queue<UUID> toRemove = new ArrayDeque<>();
        // do/while to guarantee 1 chunk refresh per tick (forward progress)
        do {
            // One chunk per player per outer while loop iteration - ensure we check the tick time frequently
            for (UUID playerUuid : chunkRefreshTasks.keySet()) {
                Player player = Bukkit.getPlayer(playerUuid);
                ChunkRefreshTask task = chunkRefreshTasks.get(playerUuid);
                if (player == null) {
                    toRemove.add(playerUuid);
                    continue;
                }
                Chunk chunk = task.chunksLeft().poll();
                if (chunk == null) {
                    // If we run out of chunks, be sure any entities that weren't refreshed yet are refreshed.
                    // Not sure this is possible, but just to be safe.
                    for (UUID entityId : task.entitiesLeft()) {
                        Entity entity = Bukkit.getEntity(entityId);
                        if (entity == null) continue;
                        player.hideEntity(plugin, entity);
                        player.showEntity(plugin, entity);
                    }
                    toRemove.add(playerUuid);
                    continue;
                }

                if (player.isChunkSent(chunk)) {
                    refreshChunkForPlayer(player, chunk);
                }

                for (Entity entity : chunk.getEntities()) {
                    if (task.entitiesLeft.contains(entity.getUniqueId()) && entity.getTrackedBy().contains(player)) {
                        player.hideEntity(plugin, entity);
                        player.showEntity(plugin, entity);
                    }
                    task.entitiesLeft.remove(entity.getUniqueId());
                }
            }

            while (!toRemove.isEmpty()) {
                UUID playerUuid = toRemove.remove();
                if (CoordinateOffsetCore.get().isDebugEnabled()) {
                    CoordinateOffsetCore.get().getLogger().info("Chunks refreshed in " +
                        (Bukkit.getCurrentTick() - chunkRefreshTasks.get(playerUuid).startTick) +
                        " ticks for " + playerUuid);
                }
                chunkRefreshTasks.remove(playerUuid);
            }
        } while (event.getTimeRemaining() > HEADROOM_NS && !chunkRefreshTasks.isEmpty());
    }

    private int lastTickExceptionPrinted = 0;
    private void refreshChunkForPlayer(Player player, Chunk chunk) {
        try {
            // NMS - only way to refresh a chunk for a single player
            FeatureHooks.sendChunkRefreshPackets(
                List.of(((CraftPlayer) player).getHandle()),
                (LevelChunk) ((CraftChunk) chunk).getHandle(ChunkStatus.FULL)
            );
        } catch (Exception e) {
            // Fall back on API method if NMS fails, API method doesn't take a player filter
            if (Bukkit.getCurrentTick() - lastTickExceptionPrinted > 10) { // rate-limit printing exceptions
                new RuntimeException("Failed to refresh " + chunk + " for " + player.getName() +
                    "; falling back on refreshing chunk for all players", e).printStackTrace();
                lastTickExceptionPrinted = Bukkit.getCurrentTick();
            }
            chunk.getWorld().refreshChunk(chunk.getX(), chunk.getZ());
        }
    }
}

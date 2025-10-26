package com.jtprince.coordinateoffset.paper;

import com.github.retrooper.packetevents.PacketEvents;
import com.github.retrooper.packetevents.util.Vector3d;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerPlayerPositionAndLook;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerUnloadChunk;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerUpdateViewPosition;
import com.jtprince.coordinateoffset.OffsetHolder;
import com.jtprince.coordinateoffset.provider.OffsetProviderContext;
import org.bukkit.Chunk;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.jspecify.annotations.NullMarked;

import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@NullMarked
public class OffsetSwapHelpers {
    /**
     * Forcibly swap the player's offset.
     * 
     * <p>This may be called after {@link OffsetHolder#generateNextOffset(OffsetProviderContext)} to apply an offset
     * change immediately.</p>
     *
     * <p>This must only be called on the main server thread.</p>
     *
     * @param player Player to swap the offset for.
     */
    public static void forceOffsetSwap(Player player) {
        List<Chunk> chunksClosestFirst =
            sendUnloadAllSentChunksPackets(player);

        /* Timing of these packets is important. See OffsetChangeSequencePaper.md */
        var l = player.getLocation();
        PacketEvents.getAPI().getPlayerManager().sendPacket(player,
            new WrapperPlayServerPlayerPositionAndLook(0,
                new Vector3d(l.x(), l.y(), l.z()),
                new Vector3d(player.getVelocity().getX(), player.getVelocity().getY(), player.getVelocity().getZ()),
                l.getYaw(), l.getPitch(), (byte) 0));
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
    public static List<Chunk> getSentChunksClosestFirst(Player player) {
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
    public static List<Chunk> sendUnloadAllSentChunksPackets(Player player) {
        List<Chunk> chunksClosestFirst = getSentChunksClosestFirst(player);
        for (Chunk chunk : chunksClosestFirst.reversed()) { // Unload furthest chunks first
            PacketEvents.getAPI().getPlayerManager().sendPacket(player,
                new WrapperPlayServerUnloadChunk(chunk.getX(), chunk.getZ()));
        }
        return chunksClosestFirst;
    }

    /**
     * Forcibly resend all loaded chunks and entities to a player.
     *
     * <p>This must only be called on the main server thread.</p>
     *
     * @param player Player to resend chunks and entities to.
     * @param chunks Chunks to resend.
     */
    public static void refreshChunksAndEntities(Player player, List<Chunk> chunks) {
        assert CoordinateOffsetPaperPlugin.getInstance() != null;
        Set<Entity> alreadyReloadedEntities = new HashSet<>();
        for (Chunk chunk : chunks) {
            /*
             * TODO: Paper doesn't have a way to refresh a chunk for only a specific player.
             *  If added, this can be optimized to not refresh the chunk for everyone.
             */
            player.getWorld().refreshChunk(chunk.getX(), chunk.getZ());
            for (Entity entity : chunk.getEntities()) {
                if (entity.getTrackedBy().contains(player)) {
                    player.hideEntity(CoordinateOffsetPaperPlugin.getInstance(), entity);
                    player.showEntity(CoordinateOffsetPaperPlugin.getInstance(), entity);
                    alreadyReloadedEntities.add(entity);
                }
            }
        }
        for (Entity entity : player.getWorld().getEntities()) {
            if (entity.getTrackedBy().contains(player) && !alreadyReloadedEntities.contains(entity)) {
                player.hideEntity(CoordinateOffsetPaperPlugin.getInstance(), entity);
                player.showEntity(CoordinateOffsetPaperPlugin.getInstance(), entity);
            }
        }
    }
}

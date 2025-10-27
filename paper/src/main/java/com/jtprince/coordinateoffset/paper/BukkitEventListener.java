package com.jtprince.coordinateoffset.paper;

import com.github.retrooper.packetevents.PacketEvents;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerUpdateViewPosition;
import com.jtprince.coordinateoffset.CoordinateOffsetCore;
import com.jtprince.coordinateoffset.adapter.OffsetPlayer;
import com.jtprince.coordinateoffset.paper.adapter.PaperLocation;
import com.jtprince.coordinateoffset.paper.adapter.PaperOffsetPlayer;
import com.jtprince.coordinateoffset.provider.OffsetProvider;
import com.jtprince.coordinateoffset.provider.OffsetProviderContext;
import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.*;
import org.bukkit.event.server.ServerLoadEvent;
import org.jspecify.annotations.NullMarked;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@NullMarked
class BukkitEventListener implements Listener {
    private final CoordinateOffsetPaperPlugin plugin;
    private final CoordinateOffsetCore core;
    private final WorldBorderObfuscator worldBorderObfuscator;

    BukkitEventListener(CoordinateOffsetPaperPlugin plugin, CoordinateOffsetCore core, WorldBorderObfuscator worldBorderObfuscator) {
        this.plugin = plugin;
        this.core = core;
        this.worldBorderObfuscator = worldBorderObfuscator;
    }

    public void registerListeners() {
        Bukkit.getPluginManager().registerEvents(this, plugin);
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onServerLoad(ServerLoadEvent event) {
        plugin.onAllPluginsEnabled();
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onPlayerJoin(PlayerJoinEvent event) {
        PaperOffsetPlayer player = new PaperOffsetPlayer(event.getPlayer());
        core.getOffsetHolder().generateNextOffset(new OffsetProviderContext(
            player,
            null,
            new PaperLocation(event.getPlayer().getLocation()),
            null,
            OffsetProviderContext.ProvideReason.JOIN
        ));
    }

    private final Map<UUID, Location> lastDeathLocation = new HashMap<>();
    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onPlayerDeath(PlayerDeathEvent event) {
        lastDeathLocation.put(event.getEntity().getUniqueId(), event.getEntity().getLocation());
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onPlayerRespawn(PlayerRespawnEvent event) {
        /*
         * The Respawn event is fired after using an End exit portal, but users probably expect that portal to trigger
         * a world change, not a death-based respawn.
         */
        OffsetProviderContext.ProvideReason reason;
        if (event.getRespawnReason() == PlayerRespawnEvent.RespawnReason.END_PORTAL) {
            reason = OffsetProviderContext.ProvideReason.WORLD_CHANGE;
        } else {
            reason = OffsetProviderContext.ProvideReason.DEATH_RESPAWN;
        }

        Location lastDeathLocation = this.lastDeathLocation.get(event.getPlayer().getUniqueId());
        OffsetPlayer player = new PaperOffsetPlayer(event.getPlayer());
        core.getOffsetHolder().generateNextOffset(new OffsetProviderContext(
            player,
            lastDeathLocation == null ? null : new PaperLocation(lastDeathLocation),
            new PaperLocation(event.getRespawnLocation()),
            core.getOffsetHolder().getOffset(player).offset(),
            reason
        ));
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onPlayerTeleport(PlayerTeleportEvent event) {
        PaperOffsetPlayer offsetPlayer = new PaperOffsetPlayer(event.getPlayer());
        OffsetProviderContext.ProvideReason reason;
        if (!event.getFrom().getWorld().equals(event.getTo().getWorld())) {
            reason = OffsetProviderContext.ProvideReason.WORLD_CHANGE;
        } else {
            reason = OffsetProviderContext.ProvideReason.TELEPORT;
        }

        boolean changed = core.getOffsetHolder().generateNextOffset(new OffsetProviderContext(
            offsetPlayer,
            new PaperLocation(event.getFrom()),
            new PaperLocation(event.getTo()),
            core.getOffsetHolder().getOffset(offsetPlayer).offset(),
            reason
        ));

        if (changed && reason == OffsetProviderContext.ProvideReason.TELEPORT) {
            /*
             * Nearby teleportation workaround:
             * A player teleporting a short distance does not trigger chunk unloads and reloads.
             * But if their offset changed, the client sees a much longer teleport distance.
             * Work around this by forcibly resending all chunks that overlap before and after the teleport.
             */
            int viewDistanceChunks = Math.max(
                event.getPlayer().getViewDistance(),
                event.getPlayer().getSendViewDistance()
            ) + 2; // extra buffer to be safe
            double viewDistanceBlocks = (double) viewDistanceChunks * 16;
            double tpDistanceSq = event.getFrom().distanceSquared(event.getTo());
            if (tpDistanceSq < viewDistanceBlocks * viewDistanceBlocks) {
                List<Chunk> chunksClosestFirst =
                    OffsetSwapHelpers.sendUnloadAllSentChunksPackets(event.getPlayer());

                UUID playerId = event.getPlayer().getUniqueId();
                Bukkit.getScheduler().runTaskLater(plugin, () -> { // on the next tick (post teleport)
                    Player player = Bukkit.getPlayer(playerId);
                    if (player == null) return;

                    // View position packet only seems necessary when teleporting within a chunk; otherwise the
                    // teleport itself sends a correct view position packet. Just always send one for now (no harm).
                    PacketEvents.getAPI().getPlayerManager().sendPacket(player,
                        new WrapperPlayServerUpdateViewPosition(player.getLocation().getChunk().getX(), player.getLocation().getChunk().getZ()));

                    OffsetSwapHelpers.refreshChunksAndEntities(player, chunksClosestFirst);
                }, 1L);
            }
        }

        worldBorderObfuscator.tryUpdatePlayerBorders(event.getPlayer(), event.getTo());
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onPlayerMove(PlayerMoveEvent event) {
        worldBorderObfuscator.tryUpdatePlayerBorders(event.getPlayer(), event.getTo());
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerQuit(PlayerQuitEvent event) {
        for (OffsetProvider provider : core.getProviderConfig().getAllOffsetProviderConfigs().values()) {
            try {
                provider.onPlayerQuit(new PaperOffsetPlayer(event.getPlayer()));
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}

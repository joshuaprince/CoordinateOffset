package com.jtprince.coordinateoffset.paper;

import com.github.retrooper.packetevents.PacketEvents;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerUpdateViewPosition;
import com.jtprince.coordinateoffset.CoordinateOffsetCore;
import com.jtprince.coordinateoffset.OffsetChange;
import com.jtprince.coordinateoffset.adapter.OffsetPlayer;
import com.jtprince.coordinateoffset.paper.adapter.PaperLocation;
import com.jtprince.coordinateoffset.paper.adapter.PaperOffsetPlayer;
import com.jtprince.coordinateoffset.paper.adapter.PaperOffsetSwapper;
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

import java.util.*;

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
        worldBorderObfuscator.tryUpdatePlayerBorders(event.getPlayer(), event.getPlayer().getLocation());

        PaperOffsetPlayer player = new PaperOffsetPlayer(event.getPlayer());
        core.getOffsetHolder().generateNextOffset(
            player,
            null,
            new PaperLocation(event.getPlayer().getLocation()),
            OffsetProviderContext.ProvideReason.JOIN
        );
    }

    private final Map<UUID, Location> lastDeathLocation = new HashMap<>();
    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onPlayerDeath(PlayerDeathEvent event) {
        lastDeathLocation.put(event.getEntity().getUniqueId(), event.getEntity().getLocation());
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onPlayerRespawn(PlayerRespawnEvent event) {
        /*
         * Paper fires a Respawn event after using an End exit portal.
         * (Also a Teleport event with UNKNOWN reason, but only when the player has already seen the end credits.)
         * Users probably expect this to be consistently a world change, not a death-respawn.
         */
        OffsetProviderContext.ProvideReason reason;
        if (event.getRespawnReason() == PlayerRespawnEvent.RespawnReason.END_PORTAL) {
            reason = OffsetProviderContext.ProvideReason.WORLD_CHANGE;
        } else {
            reason = OffsetProviderContext.ProvideReason.DEATH_RESPAWN;
        }

        Location lastDeathLocation = this.lastDeathLocation.get(event.getPlayer().getUniqueId());
        OffsetPlayer player = new PaperOffsetPlayer(event.getPlayer());
        core.getOffsetHolder().generateNextOffset(
            player,
            lastDeathLocation == null ? null : new PaperLocation(lastDeathLocation),
            new PaperLocation(event.getRespawnLocation()),
            reason
        );
    }

    private static final Set<String> IGNORED_TELEPORT_CAUSES = Set.of(
        // These don't really feel like teleports and would be surprising if they regenerated offsets.
        // Defined as strings instead of the enum because values get added and removed between versions.
        "DISMOUNT",
        "EXIT_BED",
        "UNKNOWN" // Also fired when entering the end portal for some reason, despite END_PORTAL being available
    );

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onPlayerTeleport(PlayerTeleportEvent event) {
        PaperOffsetPlayer offsetPlayer = new PaperOffsetPlayer(event.getPlayer());
        OffsetProviderContext.ProvideReason reason;

        if (IGNORED_TELEPORT_CAUSES.contains(event.getCause().name())) {
            if (core.isDebugEnabled()) {
                core.getLogger().info("Ignoring teleport event for " + event.getPlayer().getName() +
                    " due to ignored cause: " + event.getCause().name());
            }
            return;
        }

        if (!event.getFrom().getWorld().equals(event.getTo().getWorld())) {
            reason = OffsetProviderContext.ProvideReason.WORLD_CHANGE;
        } else {
            reason = OffsetProviderContext.ProvideReason.TELEPORT;
        }

        OffsetChange result = core.getOffsetHolder().generateNextOffset(
            offsetPlayer, new PaperLocation(event.getFrom()), new PaperLocation(event.getTo()), reason);

        if (result.offsetChanged() && reason == OffsetProviderContext.ProvideReason.TELEPORT) {
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
                    ((PaperOffsetSwapper) core.getAdapter().getOffsetSwapper())
                        .sendUnloadAllSentChunksPackets(event.getPlayer());

                UUID playerId = event.getPlayer().getUniqueId();
                Bukkit.getScheduler().runTaskLater(plugin, () -> { // on the next tick (post teleport)
                    Player player = Bukkit.getPlayer(playerId);
                    if (player == null) return;

                    // View position packet only seems necessary when teleporting within a chunk; otherwise the
                    // teleport itself sends a correct view position packet. Just always send one for now (no harm).
                    PacketEvents.getAPI().getPlayerManager().sendPacket(player,
                        new WrapperPlayServerUpdateViewPosition(player.getLocation().getChunk().getX(), player.getLocation().getChunk().getZ()));

                    ((PaperOffsetSwapper) core.getAdapter().getOffsetSwapper())
                        .refreshChunksAndEntities(player, chunksClosestFirst);
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

package com.jtprince.coordinateoffset.paper;

import com.github.retrooper.packetevents.PacketEvents;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerUpdateViewPosition;
import com.jtprince.coordinateoffset.CoordinateOffsetCore;
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
import org.spigotmc.event.player.PlayerSpawnLocationEvent;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@NullMarked
class BukkitEventListener implements Listener {
    private final CoordinateOffsetPaperPlugin plugin;
    private final CoordinateOffsetCore core;
    private final WorldBorderObfuscator worldBorderObfuscator;
    private boolean isJoinEventFiredBeforeFirstPlayPacket;

    BukkitEventListener(CoordinateOffsetPaperPlugin plugin, CoordinateOffsetCore core, WorldBorderObfuscator worldBorderObfuscator) {
        this.plugin = plugin;
        this.core = core;
        this.worldBorderObfuscator = worldBorderObfuscator;
    }

    public void registerListeners() {
        /*
         * In 1.21.9 Paper deprecated PlayerSpawnLocationEvent in favor of AsyncPlayerSpawnLocationEvent.
         * Offsets must be generated before the first PLAY packet. The strategy for generating offsets on join is:
         *  - 1.21.8 and below: use PlayerSpawnLocationEvent (which always fires before the first PLAY packet)
         *  - 1.21.9 or above: use PlayerJoinEvent; block Netty thread in OffsetHolder until an offset is generated
         *  - Unparseable versions: warn and behave as though Minecraft version is 1.21.9 or above
         */
        isJoinEventFiredBeforeFirstPlayPacket = is1_21_9OrGreater();

        Bukkit.getPluginManager().registerEvents(this, plugin);
        if (!isJoinEventFiredBeforeFirstPlayPacket) {
            // Use a separate listener class so 1.21.9+ doesn't listen for PlayerSpawnLocationEvent and show a warning
            Bukkit.getPluginManager().registerEvents(new OldSpawnLocationListener(), plugin);
        }
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onServerLoad(ServerLoadEvent event) {
        plugin.onAllPluginsEnabled();
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onPlayerJoin(PlayerJoinEvent event) {
        // 1.21.9+ only; use PlayerSpawnLocationEvent instead in 1.21.8 and below
        if (!isJoinEventFiredBeforeFirstPlayPacket) return;

        PaperOffsetPlayer player = new PaperOffsetPlayer(event.getPlayer());
        core.getOffsetHolder().generateNextOffset(new OffsetProviderContext(
            player,
            event.getPlayer().getWorld().getName(),
            null,
            new PaperLocation(event.getPlayer().getLocation()),
            OffsetProviderContext.ProvideReason.JOIN
        ));
    }

    private class OldSpawnLocationListener implements Listener {
        // Only registered in 1.21.8 and below; 1.21.9+ uses PlayerJoinEvent instead
        @EventHandler(priority = EventPriority.MONITOR)
        public void onSpawnLocation(PlayerSpawnLocationEvent event) {
            PaperOffsetPlayer player = new PaperOffsetPlayer(event.getPlayer());
            core.getOffsetHolder().generateNextOffset(new OffsetProviderContext(
                player,
                event.getSpawnLocation().getWorld().getName(),
                null,
                new PaperLocation(event.getSpawnLocation()),
                OffsetProviderContext.ProvideReason.JOIN
            ));
        }
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
        OffsetProviderContext.ProvideReason reason = OffsetProviderContext.ProvideReason.DEATH_RESPAWN;
        try {
            if (event.getRespawnReason() != PlayerRespawnEvent.RespawnReason.DEATH) {
                reason = OffsetProviderContext.ProvideReason.WORLD_CHANGE;
            }
        } catch (NoClassDefFoundError | NoSuchMethodError e) {
            try {
                if (event.getRespawnFlags().contains(PlayerRespawnEvent.RespawnFlag.END_PORTAL)) {
                    reason = OffsetProviderContext.ProvideReason.WORLD_CHANGE;
                }
            } catch (NoClassDefFoundError | NoSuchMethodError e2) {
                plugin.getLogger().fine("No supported method for determining respawn reason.");
            }
        }

        Location lastDeathLocation = this.lastDeathLocation.get(event.getPlayer().getUniqueId());
        core.getOffsetHolder().generateNextOffset(new OffsetProviderContext(
            new PaperOffsetPlayer(event.getPlayer()),
            event.getRespawnLocation().getWorld().getName(),
            lastDeathLocation == null ? null : new PaperLocation(lastDeathLocation),
            new PaperLocation(event.getRespawnLocation()),
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
            event.getTo().getWorld().getName(),
            new PaperLocation(event.getFrom()),
            new PaperLocation(event.getTo()),
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
            provider.onPlayerQuit(new PaperOffsetPlayer(event.getPlayer()));
        }
    }

    private boolean is1_21_9OrGreater() {
        String mcVersion = Bukkit.getMinecraftVersion(); // e.g. "1.21.9", could be "1.21.9 Pre-Release 4
        String warningMessage = "Could not parse Minecraft version \"" + mcVersion +
            "\". Behaving as though Minecraft version is 1.21.9 or above. If you see bugs, please mention this" +
            " message to the plugin author.";

        Pattern pattern = Pattern.compile("^(\\d+)\\.(\\d+)\\.(\\d+).*");
        Matcher m = pattern.matcher(mcVersion);
        if (!m.matches()) {
            core.getLogger().warning(warningMessage);
            return true;
        }
        try {
            int major = Integer.parseInt(m.group(1));
            int minor = Integer.parseInt(m.group(2));
            int patch = Integer.parseInt(m.group(3));
            // true for 1.21.9+, false for 1.21.8 or below
            return major > 1 || (major == 1 && minor > 21) || (major == 1 && minor == 21 && patch >= 9);
        } catch (NumberFormatException e) {
            core.getLogger().warning(warningMessage);
            return true;
        }
    }
}

package com.jtprince.coordinateoffset.paper;

import com.jtprince.coordinateoffset.CoordinateOffsetCore;
import com.jtprince.coordinateoffset.paper.adapter.PaperLocation;
import com.jtprince.coordinateoffset.paper.adapter.PaperOffsetPlayer;
import com.jtprince.coordinateoffset.provider.OffsetProvider;
import com.jtprince.coordinateoffset.provider.OffsetProviderContext;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.*;
import org.bukkit.event.server.ServerLoadEvent;
import org.jspecify.annotations.NullMarked;
import org.spigotmc.event.player.PlayerSpawnLocationEvent;

import java.util.Objects;
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
         * In 1.21.9 Paper and/or PacketEvents made the following changes to the player join sequence:
         *  - Deprecated PlayerSpawnLocationEvent in favor of AsyncPlayerSpawnLocationEvent
         *  - Made PlayerJoinEvent fire *concurrently* with sending the first PLAY packet (prior to 1.21.9, a
         *    JOIN_GAME packet was always sent strictly *before* PlayerJoinEvent)
         * Offsets must be generated before the first PLAY packet. The strategy for generating offsets on join is:
         *  - 1.21.8 and below: use PlayerSpawnLocationEvent (which fires before the first PLAY packet)
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
            new PaperLocation(event.getPlayer().getLocation()),
            OffsetProviderContext.ProvideReason.JOIN
        ));
    }

    private class OldSpawnLocationListener implements Listener {
        // Only registered in 1.21.8 and below
        @EventHandler(priority = EventPriority.MONITOR)
        public void onSpawnLocation(PlayerSpawnLocationEvent event) {
            PaperOffsetPlayer player = new PaperOffsetPlayer(event.getPlayer());
            core.getOffsetHolder().generateNextOffset(new OffsetProviderContext(
                player,
                event.getSpawnLocation().getWorld().getName(),
                new PaperLocation(event.getSpawnLocation()),
                OffsetProviderContext.ProvideReason.JOIN
            ));
        }
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

        core.getOffsetHolder().generateNextOffset(new OffsetProviderContext(
            new PaperOffsetPlayer(event.getPlayer()),
            event.getRespawnLocation().getWorld().getName(),
            new PaperLocation(event.getRespawnLocation()),
            reason
        ));
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onPlayerTeleport(PlayerTeleportEvent event) {
        PaperOffsetPlayer player = new PaperOffsetPlayer(event.getPlayer());
        OffsetProviderContext.ProvideReason reason = null;
        if (event.getFrom().getWorld() != Objects.requireNonNull(event.getTo()).getWorld()) {
            reason = OffsetProviderContext.ProvideReason.WORLD_CHANGE;
        } else if (event.getFrom().distanceSquared(event.getTo()) > getMinimumTeleportDistanceSquared(event.getTo().getWorld())) {
            /*
             * DISTANT_TELEPORT activation requires opt-in
             * https://github.com/joshuaprince/CoordinateOffset/wiki/resetOnDistantTeleport
             */
            if (core.getConfig().getUnsafeResetOnDistantTeleport()) {
                reason = OffsetProviderContext.ProvideReason.DISTANT_TELEPORT;
            }
        }

        if (reason == null) return;

        core.getOffsetHolder().generateNextOffset(new OffsetProviderContext(
            player,
            event.getTo().getWorld().getName(),
            new PaperLocation(event.getTo()),
            reason
        ));

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

    private int getMinimumTeleportDistanceSquared(World world) {
        int viewDistance = world.getViewDistance();

        /*
         * Problem: If the player's offset changes when they teleport a short distance, the server won't re-send the
         * chunks that the server thinks the player already has. That means that the player will just never get some
         * chunks in their "new" location.
         * Easy Solution: Only allow an offset change when the player teleports if there are no overlapping chunks in
         * view distance before and after the teleport.
         * Future Solution: Find a way to resend all visible chunks on demand. Paper's Player#setSendViewDistance or
         * World#refreshChunk might be promising.
         */
        int minimumBlocks = ((viewDistance + 1) * 2) * 16;

        // TODO: Reinstate this config option, but document it better.
//        if (plugin.getConfig().isInt("distantTeleportMinimumDistance")) {
//            // TODO: Not documented for now. Need to either fix the problem described above or document around it.
//            minimumBlocks = plugin.getConfig().getInt("distantTeleportMinimumDistance");
//        }

        return minimumBlocks * minimumBlocks;
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

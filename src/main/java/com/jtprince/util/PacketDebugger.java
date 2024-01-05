package com.jtprince.util;

import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerLoginEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.plugin.java.JavaPlugin;
import org.spigotmc.event.player.PlayerSpawnLocationEvent;

/**
 * Class exclusively used for debugging packets. Should not be used in released versions of the plugin.
 */
public class PacketDebugger implements Listener {
    private final JavaPlugin plugin;

    public PacketDebugger(JavaPlugin plugin) {
        this.plugin = plugin;
    }

    public void registerAdapters() {
        Bukkit.getPluginManager().registerEvents(this, plugin);
    }

    // TBD: Add PacketEvents hook here to inspect packets

    @EventHandler(priority = EventPriority.MONITOR)
    public void onRespawn(PlayerRespawnEvent event) {
        log("EVENT: PlayerRespawnEvent");
    }
    
    @EventHandler(priority = EventPriority.MONITOR)
    public void onLogin(PlayerLoginEvent event) {
        log("EVENT: PlayerLoginEvent");
    }
    
    @EventHandler(priority = EventPriority.MONITOR)
    public void onJoin(PlayerJoinEvent event) {
        log("EVENT: PlayerJoinEvent");
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onSpawnLocation(PlayerSpawnLocationEvent event) {
        log("EVENT: PlayerSpawnLocationEvent");
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onTeleport(PlayerTeleportEvent event) {
        log("EVENT: PlayerTeleportEvent");
    }

    private void log(String message) {
        plugin.getLogger().fine(message);
    }
}

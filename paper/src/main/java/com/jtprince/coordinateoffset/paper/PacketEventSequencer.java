package com.jtprince.coordinateoffset.paper;

import com.destroystokyo.paper.event.player.PlayerPostRespawnEvent;
import com.github.retrooper.packetevents.PacketEvents;
import com.github.retrooper.packetevents.event.PacketListenerAbstract;
import com.github.retrooper.packetevents.event.PacketListenerPriority;
import com.github.retrooper.packetevents.event.PacketSendEvent;
import com.github.retrooper.packetevents.protocol.packettype.PacketType;
import org.bukkit.Bukkit;
import org.bukkit.event.Event;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerChangedWorldEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.plugin.java.JavaPlugin;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;
import org.spigotmc.event.player.PlayerSpawnLocationEvent;

import java.util.Random;
import java.util.Set;

@NullMarked
public class PacketEventSequencer implements Listener {
    private static final int DELAY_MS_MAX = 0; // use 100-500 to slow things down and observe concurrency

    private static final Set<Class<? extends Event>> SEQUENCED_EVENTS = Set.of(
        PlayerJoinEvent.class,
        PlayerSpawnLocationEvent.class,
        PlayerRespawnEvent.class,
        PlayerTeleportEvent.class,
        PlayerChangedWorldEvent.class,
        PlayerPostRespawnEvent.class
    );

    private static final Set<PacketType.Play.Server> SEQUENCED_PACKETS = Set.of(
        PacketType.Play.Server.JOIN_GAME,
        PacketType.Play.Server.RESPAWN,
        PacketType.Play.Server.SPAWN_POSITION,
        PacketType.Play.Server.INITIALIZE_WORLD_BORDER,
        PacketType.Play.Server.PLAYER_POSITION_AND_LOOK,
        PacketType.Play.Server.UPDATE_VIEW_POSITION,
        PacketType.Play.Server.MAP_DATA,
        PacketType.Play.Server.MAP_CHUNK_BULK,
        PacketType.Play.Server.UNLOAD_CHUNK
    );

    private final JavaPlugin plugin;
    private final Random random = new Random();
    private @Nullable PacketListener packetListener;
    public PacketEventSequencer(JavaPlugin plugin) {
        this.plugin = plugin;
    }

    public void install() {
        for (Class<? extends Event> clazz : SEQUENCED_EVENTS) {
            Bukkit.getPluginManager().registerEvent(clazz, this, EventPriority.NORMAL,
                (listener, event) -> onEvent(event), plugin);
        }
        packetListener = new PacketListener();
        PacketEvents.getAPI().getEventManager().registerListener(packetListener);
    }

    private void onEvent(Event event) {
        delayAndLog("EVT", event.getEventName());
    }

    private class PacketListener extends PacketListenerAbstract {
        PacketListener() {
            super(PacketListenerPriority.MONITOR);
        }

        @Override
        public void onPacketSend(PacketSendEvent event) {
            //noinspection SuspiciousMethodCalls
            if (SEQUENCED_PACKETS.contains(event.getPacketType())) {
                delayAndLog("PKT", event.getPacketType().getName());
            }
        }
    }

    private void delayAndLog(String type, String message) {
        if (DELAY_MS_MAX > 0 && !message.contains("UNLOAD_CHUNK")) { // no delays for unload chunk packets, too many
            try {
                Thread.sleep(random.nextLong(DELAY_MS_MAX));
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        plugin.getLogger().info(String.format("[%s] [Thread %03d] %s", type, Thread.currentThread().threadId(), message));
    }
}

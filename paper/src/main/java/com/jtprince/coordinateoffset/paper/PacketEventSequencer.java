package com.jtprince.coordinateoffset.paper;

import com.destroystokyo.paper.event.player.PlayerPostRespawnEvent;
import com.github.retrooper.packetevents.PacketEvents;
import com.github.retrooper.packetevents.event.*;
import com.github.retrooper.packetevents.protocol.packettype.PacketType;
import com.github.retrooper.packetevents.protocol.packettype.PacketTypeCommon;
import com.github.retrooper.packetevents.wrapper.play.client.WrapperPlayClientTeleportConfirm;
import com.github.retrooper.packetevents.wrapper.play.server.*;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
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

    private @Nullable String isPacketLogged(PacketTypeCommon packetType, ProtocolPacketEvent event) {
        return switch (packetType) {
            case
                PacketType.Play.Server.JOIN_GAME,
                PacketType.Play.Server.MAP_DATA,
                PacketType.Play.Server.MAP_CHUNK_BULK,
                PacketType.Play.Server.CHUNK_BATCH_BEGIN,
                PacketType.Play.Server.CHUNK_BATCH_END,
                PacketType.Play.Server.CHUNK_BIOMES,
                PacketType.Play.Server.INITIALIZE_WORLD_BORDER,
                PacketType.Play.Server.WORLD_BORDER_CENTER,
                PacketType.Play.Server.WORLD_BORDER,
                PacketType.Play.Server.WORLD_BORDER_LERP_SIZE,
                PacketType.Play.Server.WORLD_BORDER_SIZE,
                PacketType.Play.Server.WORLD_BORDER_WARNING_REACH,
                PacketType.Play.Server.WORLD_BORDER_WARNING_DELAY -> ""; // Log but no special message

            case PacketType.Play.Server.RESPAWN -> {
                var w = new WrapperPlayServerRespawn((PacketSendEvent) event);
                String deathPos = "null";
                if (w.getLastDeathPosition() != null) {
                    deathPos = "[" + w.getLastDeathPosition().getWorld().toString() + "," +
                        w.getLastDeathPosition().getBlockPosition().x + "," +
                        w.getLastDeathPosition().getBlockPosition().y + "," +
                        w.getLastDeathPosition().getBlockPosition().z + "]";
                }
                yield "dimt=" + w.getDimensionType().getName() + ", dim=" + w.getWorldName().orElse("null") +
                    ", gm=" + w.getGameMode() + ", pgm=" + w.getPreviousGameMode() +
                    ", death=" + deathPos + ", keep=" + w.getKeptData();
            }
            case PacketType.Play.Server.SPAWN_POSITION -> {
                var w = new WrapperPlayServerSpawnPosition((PacketSendEvent) event);
                yield "dim=" + w.getDimension() + ", x=" + w.getPosition().x + ", y=" + w.getPosition().y + ", z=" + w.getPosition().z;
            }
            case PacketType.Play.Server.PLAYER_POSITION_AND_LOOK -> {
                var w = new WrapperPlayServerPlayerPositionAndLook((PacketSendEvent) event);
                yield "tpid=" + w.getTeleportId() + ", x=" + w.getX() + ", y=" + w.getY() + ", z=" + w.getZ() + ", flags=" + w.getRelativeFlags().getFullMask();
            }
            case PacketType.Play.Server.UPDATE_VIEW_POSITION -> {
                var w = new WrapperPlayServerUpdateViewPosition((PacketSendEvent) event);
                yield "x=" + w.getChunkX() + ", z=" + w.getChunkZ();
            }
            case PacketType.Play.Server.UNLOAD_CHUNK -> {
                var w = new WrapperPlayServerUnloadChunk((PacketSendEvent) event);
                yield "x=" + w.getChunkX() + ", z=" + w.getChunkZ();
            }
            case PacketType.Play.Server.CHUNK_DATA -> {
                var w = new WrapperPlayServerChunkData((PacketSendEvent) event);
                yield "x=" + w.getColumn().getX() + ", z=" + w.getColumn().getZ();
            }

            case PacketType.Play.Client.TELEPORT_CONFIRM -> {
                var w = new WrapperPlayClientTeleportConfirm((PacketReceiveEvent) event);
                yield "tpid=" + w.getTeleportId();
            }

            default -> null;
        };
    }

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
        delayAndLog("EVNT", event.getEventName(), "tick " + Bukkit.getCurrentTick());
    }

    private class PacketListener extends PacketListenerAbstract {
        PacketListener() {
            super(PacketListenerPriority.MONITOR);
        }
        @Nullable private ProtocolPacketEvent lastPacket = null;
        private int lastPacketCount = 0;
        private long lastForceLogTime = 0;

        private void onPacket(ProtocolPacketEvent event) {
            String sendOrRecv;
            if (event.getPacketType() instanceof PacketType.Play.Client) {
                sendOrRecv = "RECV";
            } else {
                sendOrRecv = "SEND";
            }
            if (lastPacket != null && lastPacketCount > 0 && lastForceLogTime + 1000 < System.currentTimeMillis()) {
                String extra = isPacketLogged(lastPacket.getPacketType(), lastPacket);
                delayAndLog(sendOrRecv, lastPacket.getPacketType() + " (x" + lastPacketCount + ")", extra);
                lastPacketCount = 0;
                lastForceLogTime = System.currentTimeMillis();
            }
            String extra = isPacketLogged(event.getPacketType(), event);
            if (extra != null) {
                if (lastPacket != null && event.getPacketType() == lastPacket.getPacketType()) {
                    lastPacket = event;
                    lastPacketCount++;
                    return;
                } else if (lastPacket != null && lastPacketCount > 0) {
                    String lastExtra = isPacketLogged(lastPacket.getPacketType(), lastPacket);
                    delayAndLog(sendOrRecv,
                        lastPacket.getPacketType().getName() + " (x" + lastPacketCount + ")",
                        lastExtra);
                    lastPacketCount = 0;
                    lastForceLogTime = System.currentTimeMillis();
                }
                lastPacket = event;
                delayAndLog(sendOrRecv, event.getPacketType().getName(), extra);
            }
        }

        @Override
        public void onPacketSend(PacketSendEvent event) {
            onPacket(event);
        }

        @Override
        public void onPacketReceive(PacketReceiveEvent event) {
            onPacket(event);
        }
    }

    private void delayAndLog(String type, String message, @Nullable String extra) {
        if (DELAY_MS_MAX > 0 && !message.contains("UNLOAD_CHUNK")) { // no delays for unload chunk packets, too many
            try {
                Thread.sleep(random.nextLong(DELAY_MS_MAX));
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        var c = Component.text();
        c.append(Component.text(String.format("[Thread %03d] ", Thread.currentThread().threadId())));
        c.append(Component.text("[" + type + "] ").color(NamedTextColor.BLUE));
        c.append(Component.text(message).color(NamedTextColor.GREEN));
        if (extra != null && !extra.isEmpty()) {
            c.append(Component.text(": "));
            c.append(Component.text(extra).color(NamedTextColor.GOLD));
        }

        plugin.getComponentLogger().info(c.build());
    }
}

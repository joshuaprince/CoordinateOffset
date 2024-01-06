package com.jtprince.coordinateoffset;

import com.github.retrooper.packetevents.PacketEvents;
import com.github.retrooper.packetevents.event.PacketListenerAbstract;
import com.github.retrooper.packetevents.event.PacketListenerPriority;
import com.github.retrooper.packetevents.event.PacketReceiveEvent;
import com.github.retrooper.packetevents.event.PacketSendEvent;
import com.github.retrooper.packetevents.protocol.packettype.PacketType;
import com.jtprince.coordinateoffset.offsetter.OffsetterRegistry;
import org.bukkit.entity.Player;

import java.util.Set;
import java.util.logging.Logger;

class PacketOffsetAdapter {
    private final CoordinateOffset coPlugin;
    private final Logger logger;

    PacketOffsetAdapter(CoordinateOffset plugin) {
        this.coPlugin = plugin;
        this.logger = plugin.getLogger();
    }

    void registerAdapters() {
        PacketEvents.getAPI().getEventManager().registerListener(new Listener());
        PacketEvents.getAPI().init();
    }

    private class Listener extends PacketListenerAbstract {
        Listener() {
            super(PacketListenerPriority.HIGH);
        }

        private static final Set<PacketType.Play.Server> PACKETS_WORLD_BORDER = Set.of(
                // These packets are translated in WorldBorderObfuscator, not this file.
                PacketType.Play.Server.INITIALIZE_WORLD_BORDER,
                PacketType.Play.Server.WORLD_BORDER_CENTER,
                PacketType.Play.Server.WORLD_BORDER_LERP_SIZE,
                PacketType.Play.Server.WORLD_BORDER_SIZE,
                PacketType.Play.Server.WORLD_BORDER_WARNING_DELAY,
                PacketType.Play.Server.WORLD_BORDER_WARNING_REACH
        );

        @Override
        public void onPacketSend(PacketSendEvent event) {
            if (event.getPacketType() == PacketType.Play.Server.PLAYER_POSITION_AND_LOOK
                    || event.getPacketType() == PacketType.Play.Server.UPDATE_VIEW_POSITION) {
                Player player = (Player) event.getPlayer();
                if (player != null) {
                    coPlugin.getPlayerManager().setPositionedWorld(player, player.getWorld());
                }
            }

            Offset offset;
            if (event.getPacketType() == PacketType.Play.Server.JOIN_GAME
                    || event.getPacketType() == PacketType.Play.Server.RESPAWN) {
                /*
                 * Join packets happen before the Player object exists.
                 * Respawn packets need to apply a new world's offsets ahead of actually moving the player to that new
                 * world.
                 * See `docs/OffsetChangeHandling.md`
                 */
                offset = coPlugin.getPlayerManager().getOffsetLookahead(event.getUser().getUUID());
            } else {
                if (event.getPlayer() == null) return;

                offset = coPlugin.getPlayerManager().getOffset((Player) event.getPlayer());
            }

            // Short-circuit when no offset is applied
            if (offset.equals(Offset.ZERO)) return;

            // World border packets must only be manipulated by the World Border Obfuscator
            if (PACKETS_WORLD_BORDER.contains(event.getPacketType())) {
                coPlugin.getWorldBorderObfuscator().translate(event, (Player) event.getPlayer());
                return;
            }

            OffsetterRegistry.attemptToOffset(event, offset);
        }

        @Override
        public void onPacketReceive(PacketReceiveEvent event) {
            Player player = (Player) event.getPlayer();
            if (player == null) return;

            Offset offset = coPlugin.getPlayerManager().getOffset(player, player.getWorld());
            if (offset.equals(Offset.ZERO)) return;

            OffsetterRegistry.attemptToUnOffset(event, offset);
        }
    }
}

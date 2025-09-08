package com.jtprince.coordinateoffset;

import com.github.retrooper.packetevents.PacketEvents;
import com.github.retrooper.packetevents.event.*;
import com.github.retrooper.packetevents.protocol.packettype.PacketType;
import com.jtprince.coordinateoffset.offsetter.OffsetterRegistry;
import com.jtprince.util.PartialStacktraceLogger;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.Set;
import java.util.UUID;
import java.util.logging.Logger;

class PacketOffsetAdapter {
    private final CoordinateOffset coPlugin;
    private final Logger logger;
    private final PacketDebugger packetHistory;
    private final PartialStacktraceLogger partialStacktraceLogger;

    private final long stacktraceRateLimitMs = 2500; // 2.5 seconds

    PacketOffsetAdapter(CoordinateOffset plugin) {
        this.coPlugin = plugin;
        this.logger = plugin.getLogger();
        this.packetHistory = new PacketDebugger(plugin);

        this.partialStacktraceLogger = new PartialStacktraceLogger(logger);
        Bukkit.getServer().getScheduler().runTaskTimer(coPlugin, () -> {
            this.partialStacktraceLogger.flushRateLimits(stacktraceRateLimitMs);
        }, stacktraceRateLimitMs / 50, stacktraceRateLimitMs / 50);
    }

    void registerAdapters() {
        PacketEvents.getAPI().getEventManager().registerListener(new Listener());
        PacketEvents.getAPI().init();
    }

    void onDisable() {
        PacketEvents.getAPI().terminate();
        this.partialStacktraceLogger.flushRateLimits(0);
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
            if (coPlugin.isDebugEnabled()) {
                packetHistory.logPacket(event.getUser(), event.getPacketType());
            }

            try {
                if (event.getPacketType() == PacketType.Play.Server.PLAYER_POSITION_AND_LOOK
                        || event.getPacketType() == PacketType.Play.Server.UPDATE_VIEW_POSITION) {
                    Player player = event.getPlayer();
                    if (player != null) {
                        coPlugin.getPlayerManager().setPositionedWorld(player, player.getWorld());
                    }
                }

                Offset offset;
                if (event.getPacketType() == PacketType.Play.Server.JOIN_GAME
                        || event.getPacketType() == PacketType.Play.Server.RESPAWN) {
                    /*
                     * Join packets happen before the Player object exists.
                     * Respawn packets need to apply a new world's offsets ahead of actually moving the player to that
                     * new world.
                     * See `docs/OffsetChangeHandling.md`
                     */
                    offset = coPlugin.getPlayerManager().getOffsetLookahead(event.getUser().getUUID());
                } else {
                    if (event.getPlayer() == null) return;

                    offset = coPlugin.getPlayerManager().getOffset(event.getPlayer());
                }

                // Short-circuit when no offset is applied
                if (offset.equals(Offset.ZERO)) return;

                // World border packets must only be manipulated by the World Border Obfuscator
                //noinspection SuspiciousMethodCalls
                if (PACKETS_WORLD_BORDER.contains(event.getPacketType())) {
                    coPlugin.getWorldBorderObfuscator().translate(event, event.getPlayer());
                    return;
                }

                OffsetterRegistry.attemptToOffset(event, offset);
            } catch (Exception e) {
                boolean logged = partialStacktraceLogger.logStacktraceRateLimited(logger,
                    "Failed to apply offset for outgoing packet " +
                        event.getPacketType().getName() + " to " + event.getUser().getName(),
                    e, stacktraceRateLimitMs, event.getUser().getName());
                if (logged && coPlugin.isDebugEnabled()) {
                    logger.warning("Packet history for above stacktrace: " + packetHistory.getHistory(event.getUser()));
                }
            }
        }

        @Override
        public void onPacketReceive(PacketReceiveEvent event) {
            if (coPlugin.isDebugEnabled()) {
                packetHistory.logPacket(event.getUser(), event.getPacketType());
            }

            try {
                Player player = event.getPlayer();
                if (player == null) return;

                Offset offset = coPlugin.getPlayerManager().getOffset(player, player.getWorld());
                if (offset.equals(Offset.ZERO)) return;

                OffsetterRegistry.attemptToUnOffset(event, offset);
            } catch (Exception e) {
                boolean logged = partialStacktraceLogger.logStacktraceRateLimited(logger,
                    "Failed to reverse offset for incoming packet " +
                        event.getPacketType().getName() + " from " + event.getUser().getName(),
                    e, stacktraceRateLimitMs, event.getUser().getName());
                if (logged && coPlugin.isDebugEnabled()) {
                    logger.warning("Packet history for above stacktrace: " + packetHistory.getHistory(event.getUser()));
                }
            }
        }

        @Override
        public void onUserDisconnect(UserDisconnectEvent event) {
            UUID playerUuid = event.getUser().getUUID();
            if (playerUuid == null) return;

            Player onlinePlayer = Bukkit.getPlayer(playerUuid);
            if (onlinePlayer != null
                    && PacketEvents.getAPI().getPlayerManager().getUser(onlinePlayer) != event.getUser()) {
                /*
                 * Special handling for attempting to "reconnect from another location":
                 * When a player reconnects from a second instance or location, the original TCP connection is closed.
                 * This eventually fires UserDisconnectEvent on the Netty thread.
                 *
                 * It is possible in certain previously-observed circumstances for UserDisconnectEvent to fire AFTER
                 * the new connection's Player is fully in-game. The result is that the Player's data is erased from
                 * the cache despite the second connection still being in-game, so we get "Unknown player for Offset
                 * lookup" ad infinitum.
                 *
                 * Solution: If the User (connection) object PacketEvents knows about for this Player is not the same
                 * connection that is closing, then the player must still be online in a new User. Therefore, do not
                 * wipe their data.
                 */
                return;
            }

            coPlugin.getPlayerManager().remove(playerUuid);
            coPlugin.getOffsetProviderManager().disconnectPlayer(playerUuid);
            coPlugin.getWorldBorderObfuscator().onPlayerDisconnect(playerUuid);

            if (coPlugin.isDebugEnabled()) {
                packetHistory.forget(event.getUser());
            }
        }
    }
}

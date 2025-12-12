package com.jtprince.coordinateoffset.paper;

import com.github.retrooper.packetevents.PacketEvents;
import com.github.retrooper.packetevents.event.*;
import com.github.retrooper.packetevents.protocol.packettype.PacketType;
import com.jtprince.coordinateoffset.CoordinateOffsetCore;
import com.jtprince.coordinateoffset.FixedOffset;
import com.jtprince.coordinateoffset.offsetter.OffsetterRegistry;
import com.jtprince.coordinateoffset.paper.adapter.PaperOffsetPlayer;
import com.jtprince.coordinateoffset.provider.OffsetProvider;
import com.jtprince.coordinateoffset.util.PartialStacktraceLogger;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.jspecify.annotations.Nullable;

import java.util.Set;
import java.util.UUID;
import java.util.concurrent.TimeoutException;
import java.util.logging.Logger;

class PacketOffsetAdapter {
    private final CoordinateOffsetCore core;
    private final CoordinateOffsetPaperPlugin coPlugin;
    private final Logger logger;
    private final PartialStacktraceLogger partialStacktraceLogger;
    @Nullable private Listener listener;

    private final long stacktraceRateLimitMs = 2500; // 2.5 seconds

    PacketOffsetAdapter(CoordinateOffsetPaperPlugin plugin) {
        this.core = CoordinateOffsetCore.get();
        this.coPlugin = plugin;
        this.logger = plugin.getLogger();

        this.partialStacktraceLogger = new PartialStacktraceLogger(logger);
        Bukkit.getServer().getScheduler().runTaskTimer(coPlugin, () -> {
            this.partialStacktraceLogger.flushRateLimits(stacktraceRateLimitMs);
        }, stacktraceRateLimitMs / 50, stacktraceRateLimitMs / 50);
    }

    void registerAdapters() {
        listener = new Listener();
        PacketEvents.getAPI().getEventManager().registerListener(listener);
    }

    void onDisable() {
        PacketEvents.getAPI().getEventManager().unregisterListener(listener);
        listener = null;
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
            /*
             * Ignore LOGIN and CONFIGURATION packets; these are sent before the players spawn (and therefore before
             * an offset is generated). Only PLAY packets contain coordinates that need to be offset.
             */
            if (event.getPlayer() == null || !(event.getPacketType() instanceof PacketType.Play.Server)) return;

            try {
                if (event.getPacketType() == PacketType.Play.Server.PLAYER_POSITION_AND_LOOK) {
                    core.getOffsetHolder().swapInNextOffset(new PaperOffsetPlayer(event.getPlayer()));
                }

                FixedOffset offset;
                if (event.getPacketType() == PacketType.Play.Server.JOIN_GAME) {
                    /*
                     * Join packets contain coordinates, but happen concurrently with offset generation (1.21.9+ only).
                     * Block the Netty thread until the first offset is generated.
                     */
                    try {
                        offset = core.getOffsetHolder().waitForJoiningOffset(event.getUser().getUUID(), 5000);
                    } catch (TimeoutException e) {
                        logger.severe("Timed out waiting for an offset to generate for " + event.getUser().getName() + ".");
                        logger.severe("This is a bug in CoordinateOffset. Please report it.");
                        e.printStackTrace();
                        event.setCancelled(true); // Causes the player to disconnect with a network error
                        return;
                    }
                } else if (event.getPacketType() == PacketType.Play.Server.RESPAWN) {
                    offset = core.getOffsetHolder().getNextOffset(new PaperOffsetPlayer(event.getPlayer())).offset();
                } else {
                    offset = core.getOffsetHolder().getOffset(new PaperOffsetPlayer(event.getPlayer())).offset();
                }

                // Short-circuit when no offset is applied
//                if (offset.isZero()) return; // TODO

                // Debug packets are hard to offset. Obfuscate them for anyone with a nonzero offset.
                if (core.getConfig().getObfuscateDebugPropertySubscriptions()) {
                    if (event.getPacketType().getName().startsWith("DEBUG")) {
                        event.setCancelled(true);
                        return;
                    }
                }

                // World border packets must only be manipulated by the World Border Obfuscator
                //noinspection SuspiciousMethodCalls
                if (PACKETS_WORLD_BORDER.contains(event.getPacketType()) && coPlugin.getWorldBorderObfuscator() != null) {
                    coPlugin.getWorldBorderObfuscator().translate(event, event.getPlayer());
                    return;
                }

                OffsetterRegistry.attemptToOffset(event, offset);
            } catch (Exception e) {
                boolean logged = partialStacktraceLogger.logStacktraceRateLimited(logger,
                    "Failed to apply offset for outgoing packet " +
                        event.getPacketType().getName() + " to " + event.getUser().getName(),
                    e, stacktraceRateLimitMs, event.getUser().getName());
            }
        }

        @Override
        public void onPacketReceive(PacketReceiveEvent event) {
            /*
             * Ignore LOGIN and CONFIGURATION packets; these are sent before the players spawn (and therefore before
             * an offset is generated). Only PLAY packets contain coordinates that need to be offset.
             */
            if (event.getPlayer() == null || !(event.getPacketType() instanceof PacketType.Play.Client)) return;

            try {
                FixedOffset offset = core.getOffsetHolder().getOffset(new PaperOffsetPlayer(event.getPlayer())).offset();
//                if (offset.isZero()) return; // TODO

                OffsetterRegistry.attemptToUnOffset(event, offset);
            } catch (Exception e) {
                boolean logged = partialStacktraceLogger.logStacktraceRateLimited(logger,
                    "Failed to reverse offset for incoming packet " +
                        event.getPacketType().getName() + " from " + event.getUser().getName(),
                    e, stacktraceRateLimitMs, event.getUser().getName());
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

            core.getOffsetHolder().remove(playerUuid);
            for (OffsetProvider provider : core.getProviderConfig().getAllOffsetProviderConfigs().values()) {
                try {
                    provider.onPlayerDisconnect(playerUuid);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            if (coPlugin.getWorldBorderObfuscator() != null) {
                coPlugin.getWorldBorderObfuscator().onPlayerDisconnect(playerUuid);
            }
            OffsetterRegistry.onUserDisconnect(event.getUser());
        }
    }
}

package com.jtprince.coordinateoffset;

import com.github.retrooper.packetevents.event.PacketSendEvent;
import com.github.retrooper.packetevents.protocol.packettype.PacketType;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerInitializeWorldBorder;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerWorldBorderCenter;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerWorldBorderSize;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayWorldBorderLerpSize;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.WorldBorder;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.*;

/**
 * World border packets require special handling, since applying a plain offset would run into two problems:
 * <ul>
 *     <li>The player can intercept the packet and use it to derive their offset.</li>
 *     <li>World border coordinates apply world scaling for some reason (the only packets that seem to...)</li>
 * </ul>
 * <a href="https://github.com/joshuaprince/CoordinateOffset/wiki/Implications-and-Limitations#world-border">Wiki</a>
 */
class WorldBorderObfuscator {
    private static final double BASELINE_SIZE = 60_000_000;

    private final CoordinateOffset plugin;
    private final Map<UUID, EnumSet<Wall>> knownSeenWalls = new HashMap<>();

    WorldBorderObfuscator(CoordinateOffset plugin) {
        this.plugin = plugin;
    }

    void tryUpdatePlayerBorders(Player player, Location movingTo) {
        EnumSet<Wall> currentlyVisible = visibleBorders(movingTo);
        if (!currentlyVisible.equals(knownSeenWalls.get(player.getUniqueId()))) {
            plugin.getLogger().fine("Seen walls update for " + player.getName() + ": " + currentlyVisible);
            knownSeenWalls.put(player.getUniqueId(), currentlyVisible);

            // Force-send the player border packets that we will then translate.
            // Online check is necessary so that we don't send PLAY-phase packets during player login. (GitHub issue #5)
            if (player.isOnline() && enableObfuscation()) {
                try {
                    player.setWorldBorder(player.getWorldBorder());
                } catch (NoSuchMethodError e) {

                }
            }
        }
    }

    void onPlayerQuit(Player player) {
        knownSeenWalls.remove(player.getUniqueId());
    }

    boolean enableObfuscation() {
        return plugin.getConfig().getBoolean("obfuscateWorldBorder");
    }

    private EnumSet<Wall> visibleBorders(Location location) {
        double viewDistanceBlocks = Objects.requireNonNull(location.getWorld()).getViewDistance() * 16;

        WorldBorder realBorder = location.getWorld().getWorldBorder();
        double xMax = realBorder.getCenter().getX() + realBorder.getSize() / 2;
        double xMin = realBorder.getCenter().getX() - realBorder.getSize() / 2;
        double zMax = realBorder.getCenter().getZ() + realBorder.getSize() / 2;
        double zMin = realBorder.getCenter().getZ() - realBorder.getSize() / 2;

        EnumSet<Wall> seen = EnumSet.noneOf(Wall.class);
        if (xMax - location.getX() < viewDistanceBlocks) {
            seen.add(Wall.X_POSITIVE);
        }
        if (location.getX() - xMin < viewDistanceBlocks) {
            seen.add(Wall.X_NEGATIVE);
        }
        if (zMax - location.getZ() < viewDistanceBlocks) {
            seen.add(Wall.Z_POSITIVE);
        }
        if (location.getZ() - zMin < viewDistanceBlocks) {
            seen.add(Wall.Z_NEGATIVE);
        }

        return seen;
    }

    void translate(@NotNull PacketSendEvent packet, @NotNull Player player) {
        Offset offset = plugin.getPlayerManager().getOffset(player);

        /*
         * For reasons I cannot fathom, the Minecraft protocol applies the world's coordinate scaling to the world
         * border center location. (e.g. if I wanted to center a border at Nether coordinates (100,100), I would need to
         * send a packet containing (800, 800) as the center.)
         *
         * This could cause problems if the server is running a custom world with a different coordinateScale (which is
         * only accessible through NMS as DimensionType::coordinateScale). For now, just checking environment should be
         * enough.
         */
        double scaleFactor;
        if (player.getWorld().getEnvironment() == World.Environment.NETHER) {
            scaleFactor = 8.0;
        } else {
            scaleFactor = 1.0;
        }

        EnumSet<Wall> seenWalls = knownSeenWalls.getOrDefault(player.getUniqueId(), EnumSet.noneOf(Wall.class));
        if (!enableObfuscation() ||
                (seenWalls.contains(Wall.X_POSITIVE) && seenWalls.contains(Wall.X_NEGATIVE)) ||
                (seenWalls.contains(Wall.Z_POSITIVE) && seenWalls.contains(Wall.Z_NEGATIVE))) {
            // If the player can see opposing walls, or obfuscation is disabled, we should just send the complete
            // offsetted border. No diameter change.
            if (packet.getPacketType().equals(PacketType.Play.Server.INITIALIZE_WORLD_BORDER)) {
                WrapperPlayServerInitializeWorldBorder wrapper = new WrapperPlayServerInitializeWorldBorder(packet);
                wrapper.setX(wrapper.getX() - (offset.x() * scaleFactor));
                wrapper.setZ(wrapper.getZ() - (offset.z() * scaleFactor));
            } else if (packet.getPacketType().equals(PacketType.Play.Server.WORLD_BORDER_CENTER)) {
                WrapperPlayServerWorldBorderCenter wrapper = new WrapperPlayServerWorldBorderCenter(packet);
                wrapper.setX(wrapper.getX() - (offset.x() * scaleFactor));
                wrapper.setZ(wrapper.getZ() - (offset.z() * scaleFactor));
            }
            return;
        }

        WorldBorder border;
        try {
            border = player.getWorldBorder();
        } catch (NoSuchMethodError e) {
            /*
             * Spigot API added per-player world border interface in 1.18. Previous versions will not support proper
             * obfuscation, and instead we obfuscate by blocking all world border packets for players on those versions.
             */
            packet.setCancelled(true);
            return;
        }

        // Player may not have a world border override, fall back on global world border
        if (border == null) border = player.getWorld().getWorldBorder();

        double centerX = 0.0, centerZ = 0.0;
        final double diameter = BASELINE_SIZE;

        if (seenWalls.size() >= 1) {
            // The player can see one wall, or two walls that are on different axes, adjust the border such that the
            // walls they can't see are a constant and large distance away.
            if (seenWalls.contains(Wall.X_POSITIVE)) {
                double realXMax = border.getCenter().getX() + border.getSize() / 2;
                centerX = realXMax - (BASELINE_SIZE / 2);
                centerX -= offset.x();
            }
            if (seenWalls.contains(Wall.X_NEGATIVE)) {
                double realXMin = border.getCenter().getX() - border.getSize() / 2;
                centerX = realXMin + (BASELINE_SIZE / 2);
                centerX -= offset.x();
            }
            if (seenWalls.contains(Wall.Z_POSITIVE)) {
                double realZMax = border.getCenter().getZ() + border.getSize() / 2;
                centerZ = realZMax - (BASELINE_SIZE / 2);
                centerZ -= offset.z();
            }
            if (seenWalls.contains(Wall.Z_NEGATIVE)) {
                double realZMin = border.getCenter().getZ() - border.getSize() / 2;
                centerZ = realZMin + (BASELINE_SIZE / 2);
                centerZ -= offset.z();
            }
        } else {
            // The player cannot see any walls. Fully obfuscate the worldborder.
            centerX = centerZ = 0.0;
        }

        if (packet.getPacketType().equals(PacketType.Play.Server.INITIALIZE_WORLD_BORDER)) {
            var wrapper = new WrapperPlayServerInitializeWorldBorder(packet);
            wrapper.setX(centerX * scaleFactor);
            wrapper.setZ(centerZ * scaleFactor);
            wrapper.setOldDiameter(diameter);
            wrapper.setNewDiameter(diameter);
        } else if (packet.getPacketType().equals(PacketType.Play.Server.WORLD_BORDER_CENTER)) {
            var wrapper = new WrapperPlayServerWorldBorderCenter(packet);
            wrapper.setX(centerX * scaleFactor);
            wrapper.setZ(centerZ * scaleFactor);
        } else if (packet.getPacketType().equals(PacketType.Play.Server.WORLD_BORDER_LERP_SIZE)) {
            var wrapper = new WrapperPlayWorldBorderLerpSize(packet);
            wrapper.setOldDiameter(diameter);
            wrapper.setNewDiameter(diameter);
        } else if (packet.getPacketType().equals(PacketType.Play.Server.WORLD_BORDER_SIZE)) {
            var wrapper = new WrapperPlayServerWorldBorderSize(packet);
            wrapper.setDiameter(diameter);
        }
    }

    enum Wall {
        X_POSITIVE,
        X_NEGATIVE,
        Z_POSITIVE,
        Z_NEGATIVE
    }
}

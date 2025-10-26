package com.jtprince.coordinateoffset.paper;

import com.github.retrooper.packetevents.PacketEvents;
import com.github.retrooper.packetevents.util.Vector3d;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerPlayerPositionAndLook;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerUpdateViewPosition;
import com.jtprince.coordinateoffset.CoordinateOffsetCore;
import com.jtprince.coordinateoffset.CoordinateOffsetPermission;
import com.jtprince.coordinateoffset.paper.adapter.PaperAdapter;
import com.jtprince.coordinateoffset.paper.adapter.PaperLocation;
import com.jtprince.coordinateoffset.paper.adapter.PaperOffsetPlayer;
import com.jtprince.coordinateoffset.paper.lib.org.geysermc.hurricane.CollisionFix;
import com.jtprince.coordinateoffset.provider.OffsetProviderContext;
import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.entity.Player;
import org.bukkit.permissions.Permission;
import org.bukkit.permissions.PermissionDefault;
import org.bukkit.plugin.java.JavaPlugin;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@NullMarked
public final class CoordinateOffsetPaperPlugin extends JavaPlugin {
    private static @Nullable CoordinateOffsetPaperPlugin instance;
    private @Nullable PaperAdapter adapter;

    private @Nullable CoordinateOffsetCore core;
    private @Nullable WorldBorderObfuscator worldBorderObfuscator;
    private @Nullable PacketOffsetAdapter packetOffsetAdapter;
    private @Nullable CollisionFix collisionFix;

    @Override
    public void onEnable() {
        instance = this;

        adapter = new PaperAdapter(this);
        core = CoordinateOffsetCore.bootstrap(adapter);

        worldBorderObfuscator = new WorldBorderObfuscator(this);

        new BukkitEventListener(this, core, worldBorderObfuscator).registerListeners();

        new OffsetCommand(this).registerCommands();

        packetOffsetAdapter = new PacketOffsetAdapter(this);
        packetOffsetAdapter.registerAdapters();

        if (core.isDebugEnabled()) {
            new PacketEventSequencer(this).install();
        }

        for (CoordinateOffsetPermission p : CoordinateOffsetPermission.values()) {
            Map<String, Boolean> children = new HashMap<>();
            if (p == CoordinateOffsetPermission.QUERY_OTHERS) {
                children.put(CoordinateOffsetPermission.QUERY_SELF.node, true);
            }
            Bukkit.getPluginManager().addPermission(new Permission(p.node, p.description, PermissionDefault.OP, children));
        }

        if (core.getConfig().getFixCollisionBamboo() || core.getConfig().getFixCollisionDripstone()) {
            try {
                collisionFix = new CollisionFix(this, core.getConfig().getFixCollisionBamboo(), core.getConfig().getFixCollisionDripstone());
            } catch (Exception e) {
                getLogger().severe("Failed to enable bamboo/dripstone collision fix: " + e.getMessage());
                if (core.getConfig().getVerbose()) {
                    //noinspection CallToPrintStackTrace
                    e.printStackTrace();
                }
            }
        }
    }

    void onAllPluginsEnabled() {
        CoordinateOffsetCore.get().signalCompletedLoading();

        // bStats Metrics
        if (this.isEnabled()) { // signalCompletedLoading may have disabled the plugin
            MetricsWrapper.reportMetrics(this);
        }
    }

    @Override
    public void onDisable() {
        if (packetOffsetAdapter != null) {
            packetOffsetAdapter.onDisable();
            packetOffsetAdapter = null;
        }
    }

    /**
     * Get the loaded instance of the CoordinateOffset plugin on the server.
     * @return The instance of the plugin, or <code>null</code> if the plugin is not loaded.
     */
    @SuppressWarnings("unused")
    public static @Nullable CoordinateOffsetPaperPlugin getInstance() {
        return instance;
    }

    @Nullable WorldBorderObfuscator getWorldBorderObfuscator() {
        return worldBorderObfuscator;
    }

    public void regenerateOffsetImmediately(Player player, OffsetProviderContext.ProvideReason reason) {
        PaperLocation location = new PaperLocation(player.getLocation());
        boolean changed = core.getOffsetHolder().generateNextOffset(new OffsetProviderContext(
            new PaperOffsetPlayer(player),
            player.getWorld().getName(),
            location,
            location,
            reason
        ));

        if (!changed) return;

        List<Chunk> chunksClosestFirst =
            TeleportHelpers.sendUnloadAllSentChunksPackets(player);

        /* Timing of these packets is important. See OffsetChangeSequencePaper.md */
        var l = player.getLocation();
        PacketEvents.getAPI().getPlayerManager().sendPacket(player,
            new WrapperPlayServerPlayerPositionAndLook(0,
                new Vector3d(l.x(), l.y(), l.z()),
                new Vector3d(player.getVelocity().getX(), player.getVelocity().getY(), player.getVelocity().getZ()),
                l.getYaw(), l.getPitch(), (byte) 0));
        PacketEvents.getAPI().getPlayerManager().sendPacket(player,
            new WrapperPlayServerUpdateViewPosition(player.getLocation().getChunk().getX(), player.getLocation().getChunk().getZ()));

        TeleportHelpers.refreshChunksAndEntities(player, chunksClosestFirst);
    }
}

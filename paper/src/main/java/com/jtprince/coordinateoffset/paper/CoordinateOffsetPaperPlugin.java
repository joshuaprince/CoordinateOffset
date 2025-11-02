package com.jtprince.coordinateoffset.paper;

import com.jtprince.coordinateoffset.CoordinateOffsetCore;
import com.jtprince.coordinateoffset.CoordinateOffsetPermission;
import com.jtprince.coordinateoffset.paper.adapter.PaperAdapter;
import com.jtprince.coordinateoffset.paper.lib.org.geysermc.hurricane.CollisionFix;
import org.bukkit.Bukkit;
import org.bukkit.permissions.Permission;
import org.bukkit.permissions.PermissionDefault;
import org.bukkit.plugin.java.JavaPlugin;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

import java.util.stream.Collectors;

@NullMarked
public final class CoordinateOffsetPaperPlugin extends JavaPlugin {
    private static @Nullable CoordinateOffsetPaperPlugin instance;
    private @Nullable PaperAdapter adapter;

    private @Nullable CoordinateOffsetCore core;
    private @Nullable WorldBorderObfuscator worldBorderObfuscator;
    private @Nullable PacketOffsetAdapter packetOffsetAdapter;
    private @Nullable CollisionFix collisionFix;
    private @Nullable OffsetSwapper offsetSwapper;

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

        offsetSwapper = new OffsetSwapper(this);
        offsetSwapper.initialize();

        if (core.isDebugEnabled()) {
            new PacketEventSequencer(this).install();
        }

        for (CoordinateOffsetPermission p : CoordinateOffsetPermission.values()) {
            Bukkit.getPluginManager().addPermission(new Permission(p.node, p.description, PermissionDefault.OP,
                p.getChildren().stream().collect(Collectors.toMap(p1 -> p1.node, p1 -> true))));
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

    OffsetSwapper getOffsetSwapper() {
        assert offsetSwapper != null;
        return offsetSwapper;
    }
}

package com.jtprince.coordinateoffset.paper;

import com.jtprince.coordinateoffset.CoordinateOffsetCore;
import com.jtprince.coordinateoffset.CoordinateOffsetPermission;
import com.jtprince.coordinateoffset.paper.adapter.PaperAdapter;
import com.jtprince.coordinateoffset.paper.lib.org.geysermc.hurricane.CollisionFix;
import org.bukkit.Bukkit;
import org.bukkit.permissions.Permission;
import org.bukkit.permissions.PermissionDefault;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;

public final class CoordinateOffsetPaperPlugin extends JavaPlugin {
    private static CoordinateOffsetPaperPlugin instance;
    private PaperAdapter adapter;

    private WorldBorderObfuscator worldBorderObfuscator;
    private PacketOffsetAdapter packetOffsetAdapter;
    private @Nullable CollisionFix collisionFix;

    @Override
    public void onEnable() {
        instance = this;

        adapter = new PaperAdapter(this);
        CoordinateOffsetCore core = CoordinateOffsetCore.bootstrap(adapter);

        worldBorderObfuscator = new WorldBorderObfuscator(this);
        Bukkit.getPluginManager().registerEvents(new BukkitEventListener(this, core, worldBorderObfuscator), this);

        new OffsetCommand().registerCommands(this);

        packetOffsetAdapter = new PacketOffsetAdapter(this);
        packetOffsetAdapter.registerAdapters();

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
        packetOffsetAdapter.onDisable();
    }

    /**
     * Get the loaded instance of the CoordinateOffset plugin on the server.
     * @return The instance of the plugin, or <code>null</code> if the plugin is not loaded.
     */
    @SuppressWarnings("unused")
    public static @Nullable CoordinateOffsetPaperPlugin getInstance() {
        return instance;
    }

    WorldBorderObfuscator getWorldBorderObfuscator() {
        return worldBorderObfuscator;
    }
}

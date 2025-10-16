package com.jtprince.coordinateoffset.paper;

import com.jtprince.coordinateoffset.CoordinateOffsetCore;
import com.jtprince.coordinateoffset.paper.adapter.PaperAdapter;
import com.jtprince.coordinateoffset.paper.lib.org.geysermc.hurricane.CollisionFix;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.Nullable;

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

        CoordinateOffsetCommands commands = new CoordinateOffsetCommands(this);
        //  TODO! FIXME: Re-enable commands when updated for Paper API
//        Objects.requireNonNull(this.getCommand("offset")).setExecutor(commands.new OffsetCommand());
//        Objects.requireNonNull(this.getCommand("offsetreload")).setExecutor(commands.new OffsetReloadCommand());

        packetOffsetAdapter = new PacketOffsetAdapter(this);
        packetOffsetAdapter.registerAdapters();

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

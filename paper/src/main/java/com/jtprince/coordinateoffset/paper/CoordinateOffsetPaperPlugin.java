package com.jtprince.coordinateoffset.paper;

import com.jtprince.coordinateoffset.CoordinateOffset;
import com.jtprince.coordinateoffset.CoordinateOffsetCoreImpl;
import com.jtprince.coordinateoffset.CoordinateOffsetInternalsAdapter;
import com.jtprince.coordinateoffset.config.CoordinateOffsetConfig;
import com.jtprince.coordinateoffset.paper.adapter.PaperPlayerOffsetPersistence;
import com.jtprince.coordinateoffset.paper.lib.org.geysermc.hurricane.CollisionFix;
import com.jtprince.coordinateoffset.provider.util.PlayerOffsetPersistence;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.Nullable;
import org.jspecify.annotations.NullMarked;

import java.util.logging.Logger;

public final class CoordinateOffsetPaperPlugin extends JavaPlugin {
    private static CoordinateOffsetPaperPlugin instance;
    private CoordinateOffsetPaperAdapter adapter;

    private PaperConfigProvider configProvider;
    private WorldBorderObfuscator worldBorderObfuscator;
    private PacketOffsetAdapter packetOffsetAdapter;
    private @Nullable CollisionFix collisionFix;

    @NullMarked
    class CoordinateOffsetPaperAdapter implements CoordinateOffsetInternalsAdapter {
        private final PaperPlayerOffsetPersistence offsetPersistence =
            new PaperPlayerOffsetPersistence(CoordinateOffsetPaperPlugin.this);

        @Override
        public CoordinateOffsetConfig getConfig() {
            return configProvider.get();
        }

        @Override
        public Logger getLogger() {
            return CoordinateOffsetPaperPlugin.this.getLogger();
        }

        @Override
        public PlayerOffsetPersistence getPlayerOffsetPersistence() {
            return offsetPersistence;
        }
    }

    @Override
    public void onEnable() {
        instance = this;

        adapter = new CoordinateOffsetPaperAdapter();
        CoordinateOffsetCoreImpl.bootstrap(adapter);

        configProvider = new PaperConfigProvider(this);

        worldBorderObfuscator = new WorldBorderObfuscator(this);
        Bukkit.getPluginManager().registerEvents(new BukkitEventListener(
            this, (CoordinateOffsetCoreImpl) CoordinateOffset.get(), worldBorderObfuscator), this);

        CoordinateOffsetCommands commands = new CoordinateOffsetCommands(this);
        //  TODO! FIXME: Re-enable commands when updated for Paper API
//        Objects.requireNonNull(this.getCommand("offset")).setExecutor(commands.new OffsetCommand());
//        Objects.requireNonNull(this.getCommand("offsetreload")).setExecutor(commands.new OffsetReloadCommand());

        packetOffsetAdapter = new PacketOffsetAdapter(this);
        packetOffsetAdapter.registerAdapters();

        if (configProvider.get().getFixCollisionBamboo() || configProvider.get().getFixCollisionDripstone()) {
            try {
                collisionFix = new CollisionFix(this, configProvider.get().getFixCollisionBamboo(), configProvider.get().getFixCollisionDripstone());
            } catch (Exception e) {
                getLogger().severe("Failed to enable bamboo/dripstone collision fix: " + e.getMessage());
                if (configProvider.get().getVerbose()) {
                    //noinspection CallToPrintStackTrace
                    e.printStackTrace();
                }
            }
        }
    }

    void onAllPluginsEnabled() {
        // Wait to load providers until all plugins are loaded in case other plugins register their own providers.
        // TODO
        // providerManager.loadProvidersFromConfig(getConfig());

        // bStats Metrics
        MetricsWrapper.reportMetrics(this);
    }

    @Override
    public void onDisable() {
        packetOffsetAdapter.onDisable();
    }

    /**
     * Reload the CoordinateOffset configuration defined in <code>config.yml</code>.
     */
    public void reload() {
        // TODO
//        providerManager.loadProvidersFromConfig(getConfig());
        getLogger().info("Config reloaded.");
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

package com.jtprince.coordinateoffset.paper;

import com.jtprince.coordinateoffset.CoordinateOffsetCore;
import com.jtprince.coordinateoffset.adapter.CoordinateOffsetAdapter;
import com.jtprince.coordinateoffset.adapter.OffsetPlayer;
import com.jtprince.coordinateoffset.paper.adapter.PaperOffsetPlayer;
import com.jtprince.coordinateoffset.paper.adapter.PaperPlayerOffsetPersistence;
import com.jtprince.coordinateoffset.paper.lib.org.geysermc.hurricane.CollisionFix;
import com.jtprince.coordinateoffset.provider.util.PlayerOffsetPersistence;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.Nullable;
import org.jspecify.annotations.NullMarked;

import java.nio.file.Path;
import java.util.UUID;
import java.util.logging.Logger;

public final class CoordinateOffsetPaperPlugin extends JavaPlugin {
    private static CoordinateOffsetPaperPlugin instance;
    private CoordinateOffsetPaperAdapter adapter;

    private WorldBorderObfuscator worldBorderObfuscator;
    private PacketOffsetAdapter packetOffsetAdapter;
    private @Nullable CollisionFix collisionFix;

    @NullMarked
    class CoordinateOffsetPaperAdapter implements CoordinateOffsetAdapter {
        private final PaperPlayerOffsetPersistence offsetPersistence =
            new PaperPlayerOffsetPersistence(CoordinateOffsetPaperPlugin.this);


        @Override
        public Path getConfigPath() {
            return getDataFolder().toPath().resolve("config.yml");
        }

        @Override
        public Logger getLogger() {
            return CoordinateOffsetPaperPlugin.this.getLogger();
        }

        @Override
        public @Nullable OffsetPlayer getPlayer(UUID playerUuid) {
            Player bukkitPlayer = Bukkit.getPlayer(playerUuid);
            if (bukkitPlayer == null) {
                return null;
            }
            return new PaperOffsetPlayer(bukkitPlayer);
        }

        @Override
        public OffsetPlayer adaptPlayer(Object platformPlayerObject) {
            if (!(platformPlayerObject instanceof Player bukkitPlayer)) {
                throw new IllegalArgumentException("Object \"" + platformPlayerObject + "\" of class " +
                    platformPlayerObject.getClass().getName() + " is not a valid Bukkit Player.");
            }
            return new PaperOffsetPlayer(bukkitPlayer);
        }

        @Override
        public PlayerOffsetPersistence getPlayerOffsetPersistence() {
            return offsetPersistence;
        }

        @Override
        public void shutdown() {
            Bukkit.getPluginManager().disablePlugin(CoordinateOffsetPaperPlugin.this);
        }
    }

    @Override
    public void onEnable() {
        instance = this;

        adapter = new CoordinateOffsetPaperAdapter();
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

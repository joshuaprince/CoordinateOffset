package com.jtprince.coordinateoffset.paper;

import com.jtprince.coordinateoffset.CoordinateOffsetCoreImpl;
import com.jtprince.coordinateoffset.CoordinateOffsetInternalsAdapter;
import com.jtprince.coordinateoffset.Offset;
import com.jtprince.coordinateoffset.config.CoordinateOffsetConfig;
import com.jtprince.coordinateoffset.paper.lib.org.geysermc.hurricane.CollisionFix;
import com.jtprince.coordinateoffset.paper.provider.ConstantOffsetProvider;
import com.jtprince.coordinateoffset.paper.provider.RandomOffsetProvider;
import com.jtprince.coordinateoffset.paper.provider.ZeroAtLocationOffsetProvider;
import com.jtprince.coordinateoffset.provider.util.PlayerOffsetPersistence;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jspecify.annotations.NullMarked;

import java.util.logging.Logger;

public final class CoordinateOffsetPaperPlugin extends JavaPlugin {
    private static CoordinateOffsetPaperPlugin instance;
    private CoordinateOffsetPaperAdapter adapter;

    private PaperConfigProvider configProvider;
    private PlayerOffsetsManager playerOffsetsManager;
    private OffsetProviderManager providerManager;
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

        saveDefaultConfig();
        configProvider = new PaperConfigProvider(this);

        playerOffsetsManager = new PlayerOffsetsManager(this);
        providerManager = new OffsetProviderManager(this);
        worldBorderObfuscator = new WorldBorderObfuscator(this);
        Bukkit.getPluginManager().registerEvents(new BukkitEventListener(this, playerOffsetsManager, worldBorderObfuscator), this);

        CoordinateOffsetCommands commands = new CoordinateOffsetCommands(this);
        //  TODO! FIXME: Re-enable commands when updated for Paper API
//        Objects.requireNonNull(this.getCommand("offset")).setExecutor(commands.new OffsetCommand());
//        Objects.requireNonNull(this.getCommand("offsetreload")).setExecutor(commands.new OffsetReloadCommand());

        providerManager.registerConfigurationFactory("ConstantOffsetProvider", new ConstantOffsetProvider.ConfigFactory());
        providerManager.registerConfigurationFactory("RandomOffsetProvider", new RandomOffsetProvider.ConfigFactory());
        providerManager.registerConfigurationFactory("ZeroAtLocationOffsetProvider", new ZeroAtLocationOffsetProvider.ConfigFactory());

        packetOffsetAdapter = new PacketOffsetAdapter(this);
        packetOffsetAdapter.registerAdapters();

        boolean collisionFixBambooEnabled = getConfig().getBoolean("fixCollision.bamboo", true);
        boolean collisionFixDripstoneEnabled = getConfig().getBoolean("fixCollision.dripstone", true);
        if (collisionFixBambooEnabled || collisionFixDripstoneEnabled) {
            try {
                collisionFix = new CollisionFix(this, collisionFixBambooEnabled, collisionFixDripstoneEnabled);
            } catch (Exception e) {
                getLogger().severe("Failed to enable bamboo/dripstone collision fix: " + e.getMessage());
                if (isVerboseLoggingEnabled()) {
                    e.printStackTrace();
                }
            }
        }
    }

    void onAllPluginsEnabled() {
        // Wait to load providers until all plugins are loaded in case other plugins register their own providers.
        providerManager.loadProvidersFromConfig(getConfig());

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
        reloadConfig();
        providerManager.loadProvidersFromConfig(getConfig());
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

    public boolean isVerboseLoggingEnabled() {
        return getConfig().getBoolean("verbose");
    }

    public boolean isDebugEnabled() {
        return getConfig().getBoolean("debug.enable");
    }

    boolean isUnsafeResetOnTeleportEnabled() {
        return getConfig().getBoolean("allowUnsafeResetOnDistantTeleport");
    }

    /**
     * Get the currently active coordinate {@link Offset} for a player.
     *
     * <p>For example, a player might have a coordinate Offset of <code>(128, 128)</code>. This would mean that the
     * player's "F3" menu reports that they are standing at <code>(0, 0)</code> when they are really standing at
     * <code>(128, 128)</code>.</p>
     *
     * <p>This Offset is subject to change, for example if the Player changes worlds.</p>
     *
     * @param player A player currently logged in to the server.
     * @return The coordinate Offset this player sees, or <code>Offset.ZERO</code> if the player has no offset.
     */
    @SuppressWarnings("unused")
    public @NotNull Offset getOffset(@NotNull Player player) {
        return playerOffsetsManager.getOffset(player);
    }

    /**
     * Register a new Offset Provider class, allowing an extending plugin to generate offsets in a custom way.
     * <br><br>
     * Since this only defines a new Provider <i>class</i>, server administrators will need to activate your new
     * behavior by defining a Provider in the CoordinateOffset config.yml that uses the same {@code class} key.
     *
     * @param className A unique name to match on the {@code class} key in config.yml.
     * @param providerConfigFactory Your custom Provider factory, which will accept a
     *                              {@link org.bukkit.configuration.ConfigurationSection} the user has configured and
     *                              should return your custom {@link OffsetProvider} with those specifications.
     */
    @SuppressWarnings("unused")
    public void registerCustomProviderClass(@NotNull String className, @NotNull OffsetProvider.ConfigurationFactory<?> providerConfigFactory) {
        providerManager.registerConfigurationFactory(className, providerConfigFactory);
    }

    PlayerOffsetsManager getPlayerManager() {
        return playerOffsetsManager;
    }

    OffsetProviderManager getOffsetProviderManager() {
        return providerManager;
    }

    WorldBorderObfuscator getWorldBorderObfuscator() {
        return worldBorderObfuscator;
    }
}

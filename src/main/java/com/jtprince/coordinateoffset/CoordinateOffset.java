package com.jtprince.coordinateoffset;

import com.github.retrooper.packetevents.PacketEvents;
import com.jtprince.coordinateoffset.provider.ConstantOffsetProvider;
import com.jtprince.coordinateoffset.provider.RandomOffsetProvider;
import com.jtprince.coordinateoffset.provider.ZeroAtLocationOffsetProvider;
import com.jtprince.util.PacketDebugger;
import dev.jorel.commandapi.CommandAPI;
import dev.jorel.commandapi.CommandAPIBukkitConfig;
import io.github.retrooper.packetevents.factory.spigot.SpigotPacketEventsBuilder;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public final class CoordinateOffset extends JavaPlugin {
    private static CoordinateOffset instance;
    private PlayerOffsetsManager playerOffsetsManager;
    private OffsetProviderManager providerManager;
    private WorldBorderObfuscator worldBorderObfuscator;

    @Override
    public void onLoad() {
        PacketEvents.setAPI(SpigotPacketEventsBuilder.build(this));
        PacketEvents.getAPI().load();
    }

    @Override
    public void onEnable() {
        CommandAPI.onLoad(new CommandAPIBukkitConfig(this).silentLogs(true).verboseOutput(false));
        CommandAPI.onEnable();

        instance = this;
        saveDefaultConfig();

        playerOffsetsManager = new PlayerOffsetsManager(this);
        providerManager = new OffsetProviderManager(this);
        new CoordinateOffsetCommands(this).registerCommands();
        worldBorderObfuscator = new WorldBorderObfuscator(this);
        Bukkit.getPluginManager().registerEvents(new BukkitEventListener(this, playerOffsetsManager, worldBorderObfuscator), this);

        providerManager.registerConfigurationFactory("ConstantOffsetProvider", new ConstantOffsetProvider.ConfigFactory());
        providerManager.registerConfigurationFactory("RandomOffsetProvider", new RandomOffsetProvider.ConfigFactory());
        providerManager.registerConfigurationFactory("ZeroAtLocationOffsetProvider", new ZeroAtLocationOffsetProvider.ConfigFactory());

        new PacketOffsetAdapter(this).registerAdapters();

        if (isDebugEnabled()) {
            new PacketDebugger(this).registerAdapters();
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
        // Plugin shutdown logic
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
    public static @Nullable CoordinateOffset getInstance() {
        return instance;
    }

    public boolean isVerboseLoggingEnabled() {
        return getConfig().getBoolean("verbose");
    }

    public boolean isDebugEnabled() {
        return getConfig().getBoolean("debug");
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

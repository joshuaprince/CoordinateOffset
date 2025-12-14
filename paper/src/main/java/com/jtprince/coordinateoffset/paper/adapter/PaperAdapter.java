package com.jtprince.coordinateoffset.paper.adapter;

import com.jtprince.coordinateoffset.adapter.CoordinateOffsetAdapter;
import com.jtprince.coordinateoffset.paper.CoordinateOffsetPaperPlugin;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

import java.nio.file.Path;
import java.util.UUID;
import java.util.logging.Logger;

@NullMarked
public class PaperAdapter implements CoordinateOffsetAdapter {
    private final CoordinateOffsetPaperPlugin plugin;
    private final PaperPlayerOffsetPersistence offsetPersistence;
    private final PaperOffsetSwapper offsetSwapper;

    public PaperAdapter(CoordinateOffsetPaperPlugin plugin) {
        this.plugin = plugin;
        offsetPersistence = new PaperPlayerOffsetPersistence(plugin);
        offsetSwapper = new PaperOffsetSwapper(plugin);
        offsetSwapper.initialize();
    }

    @Override
    public Path getConfigDir() {
        return plugin.getDataFolder().toPath();
    }

    @Override
    public Logger getLogger() {
        return plugin.getLogger();
    }

    @Override
    public @Nullable PaperOffsetPlayer getPlayer(UUID playerUuid) {
        Player bukkitPlayer = Bukkit.getPlayer(playerUuid);
        if (bukkitPlayer == null) {
            return null;
        }
        return new PaperOffsetPlayer(bukkitPlayer);
    }

    @Override
    public PaperOffsetPlayer adaptPlayer(Object platformPlayerObject) throws ClassCastException {
        if (!(platformPlayerObject instanceof Player bukkitPlayer)) {
            throw new ClassCastException("Object \"" + platformPlayerObject + "\" of class " +
                platformPlayerObject.getClass().getName() + " is not a valid Bukkit Player.");
        }
        return new PaperOffsetPlayer(bukkitPlayer);
    }

    @Override
    public PaperLocation adaptLocation(Object platformLocationObject) throws ClassCastException {
        if (!(platformLocationObject instanceof Location bukkitLocation)) {
            throw new ClassCastException("Object \"" + platformLocationObject + "\" of class " +
                platformLocationObject.getClass().getName() + " is not a valid Bukkit Location.");
        }
        return new PaperLocation(bukkitLocation);
    }

    @Override
    public PaperPlayerOffsetPersistence getPersistenceAdapter() {
        return offsetPersistence;
    }

    @Override
    public PaperOffsetSwapper getOffsetSwapper() {
        return offsetSwapper;
    }

    private static boolean printedDHSupportWarning = false;
    @Override
    public int getMinimumOffsetMultiple() {
        if (Bukkit.getPluginManager().getPlugin("DHSupport") != null) {
            if (!printedDHSupportWarning) {
                getLogger().info("DHSupport plugin is detected. Offset X and Z values must be divisible by 64 " +
                    "blocks for offsets to be compatible with Distant Horizons LODs.");
                printedDHSupportWarning = true;
            }
            return 64;
        }

        return 16;
    }

    @Override
    public void assertMainThread(String methodName) throws IllegalStateException {
        if (!Bukkit.isPrimaryThread()) {
            throw new IllegalStateException("Method \"" + methodName + "\" must be called on the main server thread.");
        }
    }

    @Override
    public void shutdown() {
        Bukkit.getPluginManager().disablePlugin(plugin);
    }
}

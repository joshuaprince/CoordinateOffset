package com.jtprince.coordinateoffset.paper.adapter;

import com.jtprince.coordinateoffset.adapter.CoordinateOffsetAdapter;
import com.jtprince.coordinateoffset.adapter.OffsetLocation;
import com.jtprince.coordinateoffset.adapter.OffsetPersistenceAdapter;
import com.jtprince.coordinateoffset.adapter.OffsetPlayer;
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

    public PaperAdapter(CoordinateOffsetPaperPlugin plugin) {
        this.plugin = plugin;
        offsetPersistence = new PaperPlayerOffsetPersistence(plugin);
    }

    @Override
    public Path getConfigPath() {
        return plugin.getDataFolder().toPath().resolve("config.yml");
    }

    @Override
    public Logger getLogger() {
        return plugin.getLogger();
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
    public OffsetPlayer adaptPlayer(Object platformPlayerObject) throws ClassCastException {
        if (!(platformPlayerObject instanceof Player bukkitPlayer)) {
            throw new ClassCastException("Object \"" + platformPlayerObject + "\" of class " +
                platformPlayerObject.getClass().getName() + " is not a valid Bukkit Player.");
        }
        return new PaperOffsetPlayer(bukkitPlayer);
    }

    @Override
    public OffsetLocation adaptLocation(Object platformLocationObject) throws ClassCastException {
        if (!(platformLocationObject instanceof Location bukkitLocation)) {
            throw new ClassCastException("Object \"" + platformLocationObject + "\" of class " +
                platformLocationObject.getClass().getName() + " is not a valid Bukkit Location.");
        }
        return new PaperLocation(bukkitLocation);
    }

    @Override
    public OffsetPersistenceAdapter getPersistenceAdapter() {
        return offsetPersistence;
    }

    @Override
    public void shutdown() {
        Bukkit.getPluginManager().disablePlugin(plugin);
    }
}

package com.jtprince.coordinateoffset;

import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class CoordinateOffsetPlaceholderExpansion extends PlaceholderExpansion {
    private final CoordinateOffset plugin;

    CoordinateOffsetPlaceholderExpansion(CoordinateOffset plugin) {
        this.plugin = plugin;
    }

    @Override
    public @NotNull String getIdentifier() {
        return "coordinateoffset";
    }

    @Override
    public @NotNull String getAuthor() {
        return "joshuaprince";
    }

    @Override
    public @NotNull String getVersion() {
        try {
            // Paper-only API
            //noinspection UnstableApiUsage
            return plugin.getPluginMeta().getVersion();
        } catch (NoSuchMethodError e) {
            //noinspection deprecation
            return plugin.getDescription().getVersion();
        }
    }

    @Override
    public @Nullable String onRequest(OfflinePlayer offlinePlayer, @NotNull String params) {
        // For now, only online players have an offset, so offline players return an invalid placeholder
        Player player = offlinePlayer.getPlayer();
        if (player == null) return null;

        Offset offset = plugin.getOffset(player);

        return switch (params) {
            case "" -> offset.x() + "," + offset.z();
            case "x" -> String.valueOf(offset.x());
            case "z" -> String.valueOf(offset.z());
            // No match for any cases above, so invalid placeholder
            default -> null;
        };
    }
}

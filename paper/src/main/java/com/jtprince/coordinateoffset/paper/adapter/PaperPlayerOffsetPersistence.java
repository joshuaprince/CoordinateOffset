package com.jtprince.coordinateoffset.paper.adapter;

import com.jeff_media.morepersistentdatatypes.DataType;
import com.jtprince.coordinateoffset.Offset;
import com.jtprince.coordinateoffset.adapter.OffsetPlayer;
import com.jtprince.coordinateoffset.paper.CoordinateOffsetPaperPlugin;
import com.jtprince.coordinateoffset.provider.util.PlayerOffsetPersistence;
import org.bukkit.Bukkit;
import org.bukkit.NamespacedKey;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@NullMarked
public class PaperPlayerOffsetPersistence implements PlayerOffsetPersistence {
    private final PersistentDataType<PersistentDataContainer, Map<String, Offset>> PDT_TYPE =
        DataType.asMap(DataType.STRING, PaperOffset.PDT_TYPE);

    private final CoordinateOffsetPaperPlugin plugin;
    public PaperPlayerOffsetPersistence(CoordinateOffsetPaperPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public void storePlayerOffset(OffsetPlayer player, PlayerOffsetPersistence.Key persistenceKey, String worldName, Offset offset) {
        if (!(player instanceof PaperOffsetPlayer paperPlayer)) {
            throw new IllegalArgumentException("Player must be an instance of PaperOffsetPlayer");
        }

        Map<String, Offset> map = paperPlayer.getPlayer().getPersistentDataContainer().get(persistenceKeyToBukkitKey(persistenceKey), PDT_TYPE);
        if (map == null) {
            map = new HashMap<>();
        }
        map.put(worldName, offset);
        paperPlayer.getPlayer().getPersistentDataContainer().set(persistenceKeyToBukkitKey(persistenceKey), PDT_TYPE, map);
    }

    @Override
    public void clearPlayerOffsets(UUID playerUuid, Key persistenceKey) {
        Player player = Bukkit.getPlayer(playerUuid);
        if (player == null) {
            OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(playerUuid);
            plugin.getLogger().warning("Failed to clear persistent offset for " + offlinePlayer.getName() + " (" + playerUuid + ")");
            return;
        }

        player.getPersistentDataContainer().remove(persistenceKeyToBukkitKey(persistenceKey));
    }

    @Override
    public @Nullable Offset getPlayerOffset(OffsetPlayer player, PlayerOffsetPersistence.Key persistenceKey, String worldName) {
        if (!(player instanceof PaperOffsetPlayer paperPlayer)) {
            throw new IllegalArgumentException("Player must be an instance of PaperOffsetPlayer");
        }

        Map<String, Offset> map = paperPlayer.getPlayer().getPersistentDataContainer().get(persistenceKeyToBukkitKey(persistenceKey), PDT_TYPE);
        if (map == null) {
            return null;
        }
        return map.get(worldName);
    }

    private NamespacedKey persistenceKeyToBukkitKey(PlayerOffsetPersistence.Key persistenceKey) {
        // e.g. "coordinateoffset:random-persistence.foo"
        return new NamespacedKey(plugin, persistenceKey.toString());
    }
}

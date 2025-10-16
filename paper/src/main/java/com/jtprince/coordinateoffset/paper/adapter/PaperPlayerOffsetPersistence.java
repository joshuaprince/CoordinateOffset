package com.jtprince.coordinateoffset.paper.adapter;

import com.jeff_media.morepersistentdatatypes.DataType;
import com.jeff_media.morepersistentdatatypes.datatypes.GenericDataType;
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
    private static final PersistentDataType<int[], Offset> PDT_OFFSET =
        new GenericDataType<>(DataType.INTEGER_ARRAY.getPrimitiveType(), Offset.class,
            PaperPlayerOffsetPersistence::fromPdt, PaperPlayerOffsetPersistence::toPdt);

    private static final PersistentDataType<PersistentDataContainer, Map<String, Offset>> PDT_WORLD_OFFSET_CONTAINER =
        DataType.asMap(DataType.STRING, PDT_OFFSET);

    private final CoordinateOffsetPaperPlugin plugin;
    public PaperPlayerOffsetPersistence(CoordinateOffsetPaperPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public void storePlayerOffset(OffsetPlayer player, PlayerOffsetPersistence.Key persistenceKey, String worldName, Offset offset) {
        if (!(player instanceof PaperOffsetPlayer paperPlayer)) {
            throw new IllegalArgumentException("Player must be an instance of PaperOffsetPlayer");
        }

        Map<String, Offset> map = paperPlayer.getPlayer().getPersistentDataContainer().get(persistenceKeyToBukkitKey(persistenceKey), PDT_WORLD_OFFSET_CONTAINER);
        if (map == null) {
            map = new HashMap<>();
        }
        map.put(worldName, offset);
        paperPlayer.getPlayer().getPersistentDataContainer().set(persistenceKeyToBukkitKey(persistenceKey), PDT_WORLD_OFFSET_CONTAINER, map);
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

        Map<String, Offset> map = paperPlayer.getPlayer().getPersistentDataContainer().get(persistenceKeyToBukkitKey(persistenceKey), PDT_WORLD_OFFSET_CONTAINER);
        if (map == null) {
            return null;
        }
        return map.get(worldName);
    }

    private static Offset fromPdt(int[] arr) {
        return new Offset(arr[0], arr[1]);
    }

    private static int[] toPdt(Offset offset) {
        return new int[] { offset.x(), offset.z() };
    }

    private NamespacedKey persistenceKeyToBukkitKey(PlayerOffsetPersistence.Key persistenceKey) {
        // e.g. "coordinateoffset:random-persistence.foo"
        return new NamespacedKey(plugin, persistenceKey.toString());
    }
}

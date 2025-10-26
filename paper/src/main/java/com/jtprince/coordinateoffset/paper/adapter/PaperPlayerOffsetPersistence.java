package com.jtprince.coordinateoffset.paper.adapter;

import com.jeff_media.morepersistentdatatypes.DataType;
import com.jeff_media.morepersistentdatatypes.datatypes.GenericDataType;
import com.jtprince.coordinateoffset.CoordinateOffsetCore;
import com.jtprince.coordinateoffset.Offset;
import com.jtprince.coordinateoffset.adapter.OffsetPlayer;
import com.jtprince.coordinateoffset.paper.CoordinateOffsetPaperPlugin;
import com.jtprince.coordinateoffset.provider.util.PlayerOffsetPersistence;
import org.bukkit.Bukkit;
import org.bukkit.NamespacedKey;
import org.bukkit.OfflinePlayer;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

import java.util.Map;
import java.util.UUID;

@NullMarked
public class PaperPlayerOffsetPersistence implements PlayerOffsetPersistence {
    private static final PersistentDataType<int[], Offset> PDT_OFFSET =
        new GenericDataType<>(DataType.INTEGER_ARRAY.getPrimitiveType(), Offset.class,
            PaperPlayerOffsetPersistence::fromPdt, PaperPlayerOffsetPersistence::toPdt);

    private final CoordinateOffsetPaperPlugin plugin;
    public PaperPlayerOffsetPersistence(CoordinateOffsetPaperPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public void storeOffset(OffsetPlayer player, PlayerOffsetPersistence.Key persistenceKey, Offset offset) {
        if (!(player instanceof PaperOffsetPlayer paperPlayer)) {
            throw new IllegalArgumentException("Player must be an instance of PaperOffsetPlayer");
        }

        paperPlayer.getPlayer().getPersistentDataContainer().set(persistenceKeyToBukkitKey(persistenceKey), PDT_OFFSET, offset);
    }

    @Override
    public void clearOffset(UUID playerUuid, Key persistenceKey) {
        Player player = Bukkit.getPlayer(playerUuid);
        if (player == null) {
            OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(playerUuid);
            plugin.getLogger().warning("Failed to clear persistent offset for offline player " + offlinePlayer.getName() + " (" + playerUuid + ")");
            return;
        }

        player.getPersistentDataContainer().remove(persistenceKeyToBukkitKey(persistenceKey));
        preV6.clearOldPersistence(player, persistenceKey.userKey());
    }

    @Override
    public @Nullable Offset getOffset(OffsetPlayer player, PlayerOffsetPersistence.Key persistenceKey) {
        if (!(player instanceof PaperOffsetPlayer paperPlayer)) {
            throw new IllegalArgumentException("Player must be an instance of PaperOffsetPlayer");
        }

        PersistentDataContainer pdc = paperPlayer.getPlayer().getPersistentDataContainer();
        if (pdc.has(persistenceKeyToBukkitKey(persistenceKey), PDT_OFFSET)) {
            return pdc.get(persistenceKeyToBukkitKey(persistenceKey), PDT_OFFSET);
        } else {
            // Check for old offset data to migrate
            Offset recovered = preV6.recover(paperPlayer.getPlayer(), persistenceKey.userKey());
            if (recovered != null) {
                storeOffset(paperPlayer, persistenceKey, recovered);
            }
            return recovered;
        }
    }

    private static Offset fromPdt(int[] arr) {
        return new Offset(arr[0], arr[1]);
    }

    private static int[] toPdt(Offset offset) {
        return new int[] { offset.x(), offset.z() };
    }

    private NamespacedKey persistenceKeyToBukkitKey(PlayerOffsetPersistence.Key persistenceKey) {
        // e.g. "coordinateoffset:provider.persistence.default"
        return new NamespacedKey(plugin, persistenceKey.toString());
    }

    private final PreV6 preV6 = new PreV6();
    private class PreV6 {
        private static final PersistentDataType<PersistentDataContainer, Map<String, Offset>> PDT_WORLD_OFFSET_CONTAINER =
            DataType.asMap(DataType.STRING, PDT_OFFSET);
        private static final String OLD_KEY_PREFIX = "random-persistence.";
        private static final String MIGRATED_KEY_SUFFIX = ".old-migrated-v6";

        @Nullable Offset recover(Player player, String persistenceKeyUserSet) {
            try {
                String key = OLD_KEY_PREFIX + persistenceKeyUserSet; // e.g. "random-persistence.default"
                NamespacedKey fullKey = new NamespacedKey(plugin, key); // e.g. "coordinateoffset:random-persistence.default"
                Map<String /* world name */, Offset> map =
                    player.getPersistentDataContainer().get(fullKey, PDT_WORLD_OFFSET_CONTAINER);
                if (map == null) return null;

                Offset offset = find(player, map);
                if (offset != null) {
                    // Found a valid offset, archive the old data for visibility that it's old
                    NamespacedKey oldKey = new NamespacedKey(plugin, key + MIGRATED_KEY_SUFFIX);
                    player.getPersistentDataContainer().set(oldKey, PDT_WORLD_OFFSET_CONTAINER, map);
                    player.getPersistentDataContainer().remove(fullKey);
                }
                return offset;
            } catch (Exception e) {
                plugin.getLogger().warning("Failed to recover old persistent offset for " + player.getName());
                plugin.getLogger().warning("This is a bug, please report it to the developer.");
                e.printStackTrace();
                return null;
            }
        }

        private @Nullable Offset find(Player player, Map<String /* world name */, Offset> map) {
            /* Attempt 1: Find the currently loaded world with scaling of 1 that's in this data */
            for (World w : Bukkit.getWorlds()) {
                if (w.getCoordinateScale() == 1 && map.containsKey(w.getName())) {
                    return map.get(w.getName());
                }
            }

            /* Attempt 2: Find any currently loaded world that's in this data, then manually scale it */
            for (World w : Bukkit.getWorlds()) {
                if (map.containsKey(w.getName())) {
                    // Need to scale UP here. Example: the player only has an old offset of 160 in the nether; they
                    //  should get a new overworld offset of (160 * 8)
                    Offset o = map.get(w.getName()).scaleDownBy(1 / w.getCoordinateScale());
                    CoordinateOffsetCore.get().getLogger().warning("Migrating old format of a persistent random offset for "
                        + player.getName() + " from world " + w.getName() + " - this may not be accurate (scaling it by "
                        + w.getCoordinateScale() + " to make " + o + "). Manually change this with /offset set if this is "
                        + "not what you want.");
                    return o;
                }
            }

            /* Attempt 3: Just take any offset, not good... */
            for (Map.Entry<String, Offset> entry : map.entrySet()) {
                CoordinateOffsetCore.get().getLogger().warning("Migrating old format of a persistent random offset for "
                    + player.getName() + " from world " + entry.getKey() + " - this is almost certainly inaccurate. "
                    + "Taking " + entry.getValue() + ". Manually change this with /offset set if this is not what you want.");
                return entry.getValue();
            }

            return null;
        }

        private void clearOldPersistence(Player player, String persistenceKeyUserSet) {
            String key = OLD_KEY_PREFIX + persistenceKeyUserSet; // e.g. "random-persistence.default"
            NamespacedKey fullKey = new NamespacedKey(plugin, key); // e.g. "coordinateoffset:random-persistence.default"
            player.getPersistentDataContainer().remove(fullKey);
        }
    }
}

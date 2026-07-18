package com.jtprince.coordinateoffset.paper.adapter;

import com.jeff_media.morepersistentdatatypes.DataType;
import com.jeff_media.morepersistentdatatypes.datatypes.GenericDataType;
import com.jtprince.coordinateoffset.CoordinateOffsetCore;
import com.jtprince.coordinateoffset.Offset;
import com.jtprince.coordinateoffset.ScalableOffset;
import com.jtprince.coordinateoffset.adapter.OffsetPersistenceAdapter;
import com.jtprince.coordinateoffset.adapter.OffsetPlayer;
import com.jtprince.coordinateoffset.paper.CoordinateOffsetPaperPlugin;
import com.jtprince.coordinateoffset.provider.util.RegenerateConfig;
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
public class PaperPlayerOffsetPersistence implements OffsetPersistenceAdapter {
    private static final PersistentDataType<int[], ScalableOffset> PDT_OFFSET =
        new GenericDataType<>(DataType.INTEGER_ARRAY.getPrimitiveType(), ScalableOffset.class,
            PaperPlayerOffsetPersistence::fromPdt, PaperPlayerOffsetPersistence::toPdt);

    private final CoordinateOffsetPaperPlugin plugin;
    public PaperPlayerOffsetPersistence(CoordinateOffsetPaperPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public @Nullable ScalableOffset get(OffsetPlayer player, Key persistenceKey) {
        if (!(player instanceof PaperOffsetPlayer paperPlayer)) {
            throw new IllegalArgumentException("Player must be an instance of PaperOffsetPlayer");
        }

        PersistentDataContainer pdc = paperPlayer.getPlayer().getPersistentDataContainer();
        if (pdc.has(persistenceKeyToBukkitKey(persistenceKey), PDT_OFFSET)) {
            return pdc.get(persistenceKeyToBukkitKey(persistenceKey), PDT_OFFSET);
        } else {
            // Check for old offset data to migrate
            ScalableOffset recovered = preV6.recover(paperPlayer.getPlayer(), persistenceKey);
            if (recovered != null) {
                put(paperPlayer, persistenceKey, recovered);
            }
            return recovered;
        }
    }

    @Override
    public void put(OffsetPlayer player, Key persistenceKey, ScalableOffset offset) {
        if (!(player instanceof PaperOffsetPlayer paperPlayer)) {
            throw new IllegalArgumentException("Player must be an instance of PaperOffsetPlayer");
        }

        paperPlayer.getPlayer().getPersistentDataContainer().set(persistenceKeyToBukkitKey(persistenceKey), PDT_OFFSET, offset);
    }

    @Override
    public void clear(UUID playerUuid, Key persistenceKey) {
        Player player = Bukkit.getPlayer(playerUuid);
        if (player == null) {
            OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(playerUuid);
            plugin.getLogger().warning("Failed to clear persistent offset for offline player " + offlinePlayer.getName() + " (" + playerUuid + ")");
            return;
        }

        player.getPersistentDataContainer().remove(persistenceKeyToBukkitKey(persistenceKey));
        preV6.clearOldPersistence(player, persistenceKey);
    }

    private static ScalableOffset fromPdt(int[] arr) {
        if (arr.length >= 3) {
            return Offset.scalable(arr[0], arr[1], arr[2]);
        } else {
            return Offset.scalable(arr[0], 0, arr[1]);
        }
    }

    private static int[] toPdt(ScalableOffset offset) {
        return new int[] { offset.x(), offset.y(), offset.z() };
    }

    private NamespacedKey persistenceKeyToBukkitKey(Key persistenceKey) {
        // e.g. "coordinateoffset:provider.random"
        return new NamespacedKey(plugin, persistenceKey.getPersistenceKey());
    }

    private final PreV6 preV6 = new PreV6();
    private class PreV6 {
        private static final PersistentDataType<PersistentDataContainer, Map<String, ScalableOffset>> PDT_WORLD_OFFSET_CONTAINER =
            DataType.asMap(DataType.STRING, PDT_OFFSET);
        private static final String OLD_KEY_PREFIX = "random-persistence.";
        private static final String MIGRATED_KEY_SUFFIX = ".old-migrated-v6";

        @Nullable ScalableOffset recover(Player player, Key persistenceKey) {
            try {
                String key = null;
                NamespacedKey fullKey = null;
                Map<String /* world name */, ScalableOffset> foundData = null;

                // 1: look for old persistence from user override key (if override is set)
                if (persistenceKey.persistenceKeyOverride() != null) {
                    key = OLD_KEY_PREFIX + persistenceKey.persistenceKeyOverride(); // e.g. "random-persistence.foo"
                    fullKey = new NamespacedKey(plugin, key); // e.g. "coordinateoffset:random-persistence.foo"
                    foundData = player.getPersistentDataContainer().get(fullKey, PDT_WORLD_OFFSET_CONTAINER);
                }

                // 2: look for old persistence from old "default" key if override is not set
                //     (config migration drops "persistenceKey=default", so this is required to handle configs where
                //      the user didn't set a custom override)
                if (foundData == null) {
                    key = OLD_KEY_PREFIX + RegenerateConfig.LEGACY_DEFAULT_PERSISTENCE_KEY; // "random-persistence.default"
                    fullKey = new NamespacedKey(plugin, key); // "coordinateoffset:random-persistence.default"
                    foundData = player.getPersistentDataContainer().get(fullKey, PDT_WORLD_OFFSET_CONTAINER);
                }

                if (foundData == null) return null; // No data to recover.

                ScalableOffset offset = find(player, foundData);
                if (offset != null) {
                    // Found a valid offset, archive the old data for visibility that it's old
                    NamespacedKey oldKey = new NamespacedKey(plugin, key + MIGRATED_KEY_SUFFIX);
                    player.getPersistentDataContainer().set(oldKey, PDT_WORLD_OFFSET_CONTAINER, foundData);
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

        private @Nullable ScalableOffset find(Player player, Map<String /* world name */, ScalableOffset> map) {
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
                    ScalableOffset saved = map.get(w.getName());
                    ScalableOffset scaledUp = Offset.scalable(
                        (int) (saved.x() * w.getCoordinateScale()),
                        (int) (saved.z() * w.getCoordinateScale())
                    );
                    CoordinateOffsetCore.get().getLogger().warning("Migrating old format of a persistent random offset for "
                        + player.getName() + " from world " + w.getName() + " - this may not be accurate (scaling it by "
                        + w.getCoordinateScale() + " to make " + scaledUp + "). Manually change this with /offset set if this is "
                        + "not what you want.");
                    return scaledUp;
                }
            }

            /* Attempt 3: Just take any offset, not good... */
            for (Map.Entry<String, ScalableOffset> entry : map.entrySet()) {
                CoordinateOffsetCore.get().getLogger().warning("Migrating old format of a persistent random offset for "
                    + player.getName() + " from world " + entry.getKey() + " - this is almost certainly inaccurate. "
                    + "Taking " + entry.getValue() + ". Manually change this with /offset set if this is not what you want.");
                return entry.getValue();
            }

            return null;
        }

        private void clearOldPersistence(Player player, Key persistenceKey) {
            if (persistenceKey.persistenceKeyOverride() != null) {
                String key = OLD_KEY_PREFIX + persistenceKey.persistenceKeyOverride(); // e.g. "random-persistence.foo"
                NamespacedKey fullKey = new NamespacedKey(plugin, key); // e.g. "coordinateoffset:random-persistence.foo"
                player.getPersistentDataContainer().remove(fullKey);
            }

            String key = OLD_KEY_PREFIX + RegenerateConfig.LEGACY_DEFAULT_PERSISTENCE_KEY; // "random-persistence.default"
            NamespacedKey fullKey = new NamespacedKey(plugin, key); // "coordinateoffset:random-persistence.default"
            player.getPersistentDataContainer().remove(fullKey);
        }
    }
}

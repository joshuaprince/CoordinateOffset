package com.jtprince.coordinateoffset.provider.util;

import com.jeff_media.morepersistentdatatypes.DataType;
import com.jtprince.coordinateoffset.CoordinateOffset;
import com.jtprince.coordinateoffset.Offset;
import org.bukkit.Bukkit;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public interface PerWorldOffsetStore {
    default @Nullable Offset get(Player player, String worldName) { return getAll(player).get(worldName); }
    @NotNull Map<String, Offset> getAll(Player player);
    void put(Player player, String worldName, Offset offset);
    void reset(Player player);
    void reset(UUID playerUuid);

    class Cached implements PerWorldOffsetStore {
        private final Map<UUID, Map<String, Offset>> playerCache = new HashMap<>();

        @Override
        public @NotNull Map<String, Offset> getAll(Player player) {
            Map<String, Offset> map = playerCache.get(player.getUniqueId());
            if (map == null) return Collections.emptyMap(); else return map;
        }

        @Override
        public void put(Player player, String worldName, Offset offset) {
            if (!playerCache.containsKey(player.getUniqueId())) {
                playerCache.put(player.getUniqueId(), new HashMap<>());
            }
            playerCache.get(player.getUniqueId()).put(worldName, offset);
        }

        @Override
        public void reset(Player player) {
            playerCache.remove(player.getUniqueId());
        }

        @Override
        public void reset(UUID uuid) {
            playerCache.remove(uuid);
        }
    }

    class Persistent implements PerWorldOffsetStore {
        private final NamespacedKey key;
        private final PersistentDataType<PersistentDataContainer, Map<String, Offset>> PDT_TYPE = DataType.asMap(DataType.STRING, Offset.PDT_TYPE);
        public Persistent(@NotNull NamespacedKey key) {
            this.key = key;
        }

        @Override
        public @NotNull Map<String, Offset> getAll(Player player) {
            Map<String, Offset> map = player.getPersistentDataContainer().get(key, PDT_TYPE);
            if (map == null) return Collections.emptyMap(); else return map;
        }

        @Override
        public void put(Player player, String worldName, Offset offset) {
            Map<String, Offset> map = player.getPersistentDataContainer().get(key, PDT_TYPE);
            if (map == null) {
                map = new HashMap<>();
            }
            map.put(worldName, offset);
            player.getPersistentDataContainer().set(key, PDT_TYPE, map);
        }

        @Override
        public void reset(Player player) {
            player.getPersistentDataContainer().remove(key);
        }

        @Override
        public void reset(UUID playerUuid) {
            Player player = Bukkit.getPlayer(playerUuid);
            if (player != null) {
                player.getPersistentDataContainer().remove(key);
            } else {
                CoordinateOffset.getInstance().getLogger().warning("Failed to reset a persistent offset! " +
                        "Is \"persistent\" enabled at the same time as a \"resetOn\" option? (Player UUID: " +
                        playerUuid + ")");
            }
        }
    }
}

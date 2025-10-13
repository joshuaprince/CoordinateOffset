package com.jtprince.coordinateoffset.provider.util;

import com.jtprince.coordinateoffset.CoordinateOffset;
import com.jtprince.coordinateoffset.CoordinateOffsetInternalsAdapter;
import com.jtprince.coordinateoffset.Offset;
import com.jtprince.coordinateoffset.adapter.OffsetPlayer;
import org.jetbrains.annotations.Nullable;
import org.jspecify.annotations.NullMarked;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@NullMarked
public sealed interface PerWorldOffsetStore {
    @Nullable Offset get(OffsetPlayer player, String worldName);
    void put(OffsetPlayer player, String worldName, Offset offset);
    void reset(OffsetPlayer player);
    void reset(UUID playerUuid);

    final class Cached implements PerWorldOffsetStore {
        private final Map<UUID, Map<String, Offset>> playerCache = new HashMap<>();

        @Override
        public @Nullable Offset get(OffsetPlayer player, String worldName) {
            Map<String, Offset> map = playerCache.get(player.getUuid());
            if (map == null) return null;
            return map.get(worldName);
        }

        @Override
        public void put(OffsetPlayer player, String worldName, Offset offset) {
            if (!playerCache.containsKey(player.getUuid())) {
                playerCache.put(player.getUuid(), new java.util.HashMap<>());
            }
            playerCache.get(player.getUuid()).put(worldName, offset);
        }

        @Override
        public void reset(OffsetPlayer player) {
            playerCache.remove(player.getUuid());
        }

        @Override
        public void reset(UUID uuid) {
            playerCache.remove(uuid);
        }
    }

    final class Persistent implements PerWorldOffsetStore {
        private final PlayerOffsetPersistence.Key persistenceKey;
        public Persistent(PlayerOffsetPersistence.Key persistenceKey) {
            /* Prepended with "coordinateoffset:" in platform adapter */
            this.persistenceKey = persistenceKey;
        }

        @Override
        public @Nullable Offset get(OffsetPlayer player, String worldName) {
            return ((CoordinateOffsetInternalsAdapter) CoordinateOffset.get().getAdapter())
                .getPlayerOffsetPersistence()
                .getPlayerOffset(player, persistenceKey, worldName);
        }

        @Override
        public void put(OffsetPlayer player, String worldName, Offset offset) {
            ((CoordinateOffsetInternalsAdapter) CoordinateOffset.get().getAdapter())
                .getPlayerOffsetPersistence()
                .storePlayerOffset(player, persistenceKey, worldName, offset);
        }

        @Override
        public void reset(OffsetPlayer player) {
            ((CoordinateOffsetInternalsAdapter) CoordinateOffset.get().getAdapter())
                .getPlayerOffsetPersistence()
                .clearPlayerOffsets(player.getUuid(), persistenceKey);
        }

        @Override
        public void reset(UUID playerUuid) {
            throw new UnsupportedOperationException("Not yet implemented"); // TODO!
        }
    }
}

package com.jtprince.coordinateoffset.provider.util;

import com.jtprince.coordinateoffset.Offset;
import com.jtprince.coordinateoffset.adapter.OffsetPlayer;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@NullMarked
public sealed interface ProviderOffsetStore {
    @Nullable Offset get(OffsetPlayer player);
    void put(OffsetPlayer player, Offset offset);
    void clear(OffsetPlayer player);
    void clear(UUID playerUuid);

    final class Cached implements ProviderOffsetStore {
        private final ConcurrentHashMap<UUID, Offset> playerCache = new ConcurrentHashMap<>();

        @Override
        public @Nullable Offset get(OffsetPlayer player) {
            return playerCache.get(player.getUuid());
        }

        @Override
        public void put(OffsetPlayer player, Offset offset) {
            playerCache.put(player.getUuid(), offset);
        }

        @Override
        public void clear(OffsetPlayer player) {
            playerCache.remove(player.getUuid());
        }

        @Override
        public void clear(UUID uuid) {
            playerCache.remove(uuid);
        }
    }

    final class Persistent implements ProviderOffsetStore {
        private final PlayerOffsetPersistence persistence;
        private final PlayerOffsetPersistence.Key persistenceKey;
        public Persistent(PlayerOffsetPersistence persistence, PlayerOffsetPersistence.Key persistenceKey) {
            this.persistence = persistence;
            /* Prepended with "coordinateoffset:" in platform adapter */
            this.persistenceKey = persistenceKey;
        }

        @Override
        public @Nullable Offset get(OffsetPlayer player) {
            return persistence.getOffset(player, persistenceKey);
        }

        @Override
        public void put(OffsetPlayer player, Offset offset) {
            persistence.storeOffset(player, persistenceKey, offset);
        }

        @Override
        public void clear(OffsetPlayer player) {
            persistence.clearOffset(player.getUuid(), persistenceKey);
        }

        @Override
        public void clear(UUID playerUuid) {
            persistence.clearOffset(playerUuid, persistenceKey);
        }
    }
}

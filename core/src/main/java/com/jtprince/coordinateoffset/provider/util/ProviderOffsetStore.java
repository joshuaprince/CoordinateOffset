package com.jtprince.coordinateoffset.provider.util;

import com.jtprince.coordinateoffset.Offset;
import com.jtprince.coordinateoffset.adapter.OffsetPersistenceAdapter;
import com.jtprince.coordinateoffset.adapter.OffsetPlayer;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

import java.util.UUID;

@NullMarked
public class ProviderOffsetStore {
    private final OffsetPersistenceAdapter adapter;
    private final OffsetPersistenceAdapter.Key persistenceKey;

    public ProviderOffsetStore(
        OffsetPersistenceAdapter adapter,
        String providerName,
        @Nullable String persistenceKeyOverride
    ) {
        this.adapter = adapter;
        this.persistenceKey = new OffsetPersistenceAdapter.Key(providerName, persistenceKeyOverride);
    }

    public @Nullable Offset get(OffsetPlayer player) {
        return adapter.get(player, persistenceKey);
    }

    public void put(OffsetPlayer player, Offset offset) {
        adapter.put(player, persistenceKey, offset);
    }

    public void clear(UUID playerUuid) {
        adapter.clear(playerUuid, persistenceKey);
    }
}

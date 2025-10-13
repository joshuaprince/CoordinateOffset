package com.jtprince.coordinateoffset.provider.util;

import com.jtprince.coordinateoffset.Offset;
import com.jtprince.coordinateoffset.adapter.OffsetPlayer;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

import java.util.UUID;

@NullMarked
public interface PlayerOffsetPersistence {
    void storePlayerOffset(OffsetPlayer player, Key persistenceKey, String worldName, Offset offset);
    void clearPlayerOffsets(UUID playerUuid, Key persistenceKey);
    @Nullable Offset getPlayerOffset(OffsetPlayer player, Key persistenceKey, String worldName);

    record Key(String providerClassKey, String userKey) {
        @Override
        public String toString() {
            // Appended with "coordinateoffset." in platform implementation
            return providerClassKey + "." + userKey;
        }
    }
}

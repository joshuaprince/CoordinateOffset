package com.jtprince.coordinateoffset.provider.util;

import com.jtprince.coordinateoffset.Offset;
import com.jtprince.coordinateoffset.adapter.OffsetPlayer;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

import java.util.UUID;

@NullMarked
public interface PlayerOffsetPersistence {
    void storeOffset(OffsetPlayer player, Key persistenceKey, Offset offset);
    void clearOffset(UUID playerUuid, Key persistenceKey);
    @Nullable Offset getOffset(OffsetPlayer player, Key persistenceKey);

    /**
     * A key to index persistent player data, for example <code>provider.persistence.default</code>
     *
     * @param providerClassKey A built-in key to identify the use of this data, for example
     *                         <code>provider.persistence</code>
     * @param userKey A key set by the user, for example <code>default</code>
     */
    record Key(String providerClassKey, String userKey) {
        @Override
        public String toString() {
            // Appended with "coordinateoffset:" in platform implementation
            return providerClassKey + "." + userKey;
        }
    }
}
